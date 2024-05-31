package com.example.server.domain.country.dto;

import com.example.server.domain.country.domain.Country;

public class CountryDtoConverter {

    public static Country convertDtoToCountry(CountryResponseDto.CountryDto dto) {
        return Country.builder()
                .countryEngNm(dto.getCountryEngNm())
                .countryIsoAlp2(dto.getCountryIsoAlp2())
                .countryNm(dto.getCountryNm())
                .isoAlp3(dto.getIsoAlp3())
                .isoNum(dto.getIsoNum())
                .build();
    }

    public static CountryResponseDto.FindCountryResponseDto convertCountryToDto(Country country) {
        return CountryResponseDto.FindCountryResponseDto.builder()
                .countryEngNm(country.getCountryEngNm())
                .countryIsoAlp2(country.getCountryIsoAlp2())
                .countryNm(country.getCountryNm())
                .isoAlp3(country.getIsoAlp3())
                .isoNum(country.getIsoNum())
                .build();
    }
}
