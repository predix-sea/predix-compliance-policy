package com.predix.compliance.controller;

import com.predix.compliance.dto.CountryProfilePatchRequest;
import com.predix.compliance.dto.CountryProfileResponse;
import com.predix.compliance.service.CountryProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {

    private final CountryProfileService countryProfileService;

    public CountryController(CountryProfileService countryProfileService) {
        this.countryProfileService = countryProfileService;
    }

    @GetMapping
    public List<CountryProfileResponse> list() {
        return countryProfileService.listAll();
    }

    @PatchMapping("/{countryCode}")
    public CountryProfileResponse patch(@PathVariable String countryCode,
                                        @Valid @RequestBody CountryProfilePatchRequest request,
                                        Authentication auth) {
        return countryProfileService.patch(countryCode, request, auth != null ? auth.getName() : "system");
    }
}
