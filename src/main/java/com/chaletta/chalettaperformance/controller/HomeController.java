package com.chaletta.chalettaperformance.controller;

import com.chaletta.chalettaperformance.dto.stats.HomePageDto;
import com.chaletta.chalettaperformance.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomePageService homePageService;

    /**
     * Get data for the home page.
     * @return HomePageDto.
     */
    @GetMapping
    public ResponseEntity<HomePageDto> getHomePage() {
        return ResponseEntity.ok(homePageService.getHomePageData());
    }

}
