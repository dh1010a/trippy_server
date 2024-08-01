package com.example.server.domain.country.service;

import com.example.server.domain.country.domain.Country;
import com.example.server.domain.country.dto.CountryDtoConverter;
import com.example.server.domain.country.dto.CountryResponseDto;
import com.example.server.domain.country.dto.CountryResponseDto.CountryApiResponseDto;
import com.example.server.domain.country.dto.CountryResponseDto.CountryDto;
import com.example.server.domain.country.dto.CountryResponseDto.FindCountryResponseDto;
import com.example.server.domain.country.repository.CountryRepository;
import com.example.server.domain.post.dto.OotdReqResDto;
import com.example.server.global.apiPayload.code.status.ErrorStatus;
import com.example.server.global.apiPayload.exception.handler.ErrorHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CountryService {

    @Value("${openApi.serviceKey}")
    private String serviceKey;

    @Value("${openApi.endPoint}")
    private String endPoint;

    @Value("${openApi.dataType}")
    private String dataType;

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;

    public CountryApiResponseDto getCountryList() {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(endPoint);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        WebClient webClient = WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(endPoint)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        String response = webClient.get()
                .uri(uriBuilder -> {
                    return uriBuilder.path("/getCountryCodeList2")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("dataType", dataType)
                            .queryParam("numOfRows", "237")
                            .queryParam("pageNo", "1")
                            .queryParam("returnType", "JSON")
//                            .queryParam("cond[country_nm::EQ]", "가나")
                            .build(true);
                })
                .retrieve()
                .bodyToMono(String.class)
                .block();

        CountryApiResponseDto dto = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            dto = mapper.readValue(response, CountryApiResponseDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dto != null) saveCountryList(dto);

        return dto;

    }

    private void saveCountryList(CountryApiResponseDto dto) {
        List<CountryDto> data = dto.getData();
        for (CountryDto countryDto : data) {
            Country country = CountryDtoConverter.convertDtoToCountry(countryDto);
            countryRepository.save(country);
            log.info("country 저장완료. : {}", country.getCountryNm());
        }
    }

    public FindCountryResponseDto getCountryByIsoAlp2(String countryIsoAlp2) {
        return countryRepository.findByCountryIsoAlp2(countryIsoAlp2)
                .map(CountryDtoConverter::convertCountryToDto)
                .orElse(null);
    }

    public FindCountryResponseDto getCountryByAddress(String address){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://flask-app:5000/api/location")
                .queryParam("location", address);
        String url = builder.toUriString();

        String response = restTemplate.getForObject(url, String.class).replace("\"", "");

        String IsoAlo2;
        if (response.equals("500")) {
            throw new ErrorHandler(ErrorStatus.ERROR_WHILE_GET_WEATHER);
        } else if (response.equals("4001")) {
            throw new ErrorHandler(ErrorStatus.NO_PERMISSION_NATION);
        } else {
            IsoAlo2 = response;

        }

        return getCountryByIsoAlp2(IsoAlo2);
    }


}
