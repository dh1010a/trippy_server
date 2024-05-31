package com.example.server.domain.country.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

public class CountryResponseDto {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryApiResponseDto{

        @JsonProperty("currentCount")
        private String currentCount;

        @JsonProperty("data")
        private List<CountryDto> data;

        @JsonProperty("numOfRows")
        private String numOfRows;

        @JsonProperty("pageNo")
        private String pageNo;

        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;

        @JsonProperty("totalCount")
        private String totalCount;


    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CountryDto{
        @JsonProperty("country_eng_nm")
        String countryEngNm;

        @JsonProperty("country_iso_alp2")
        String countryIsoAlp2;

        @JsonProperty("country_nm")
        String countryNm;

        @JsonProperty("iso_alp3")
        String isoAlp3;

        @JsonProperty("iso_num")
        String isoNum;
    }


}
