package br.com.petterson.nbassistant.controller;

import br.com.petterson.nbassistant.dto.CategoryInfo;
import br.com.petterson.nbassistant.dto.DashboardResponse;
import br.com.petterson.nbassistant.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse dashboard() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryInfo>> categories() {
        return ResponseEntity.ok(dashboardService.getCategories());
    }
}