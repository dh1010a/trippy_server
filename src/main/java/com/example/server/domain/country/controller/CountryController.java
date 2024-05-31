package com.example.server.domain.country.controller;

import com.example.server.domain.country.service.CountryService;
import com.example.server.global.apiPayload.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/country")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ApiResponse<?> getCountryList() {
        return ApiResponse.onSuccess(countryService.getCountryList());
    }
}
