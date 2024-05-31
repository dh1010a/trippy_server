package com.example.server.domain.country.controller;

import com.example.server.domain.country.service.CountryService;
import com.example.server.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/country")
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ApiResponse<?> getCountry(@RequestParam("isoApl2") String isoApl2) {
        return ApiResponse.onSuccess(countryService.getCountryByIsoAlp2(isoApl2));
    }
}
