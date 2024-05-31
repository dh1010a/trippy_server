package com.example.server.domain.country.service;

import com.example.server.domain.country.domain.Country;
import com.example.server.domain.country.dto.CountryDtoConverter;
import com.example.server.domain.country.dto.CountryResponseDto;
import com.example.server.domain.country.dto.CountryResponseDto.CountryApiResponseDto;
import com.example.server.domain.country.dto.CountryResponseDto.CountryDto;
import com.example.server.domain.country.dto.CountryResponseDto.FindCountryResponseDto;
import com.example.server.domain.country.repository.CountryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

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


}
