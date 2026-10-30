package com.h_group.gvs_terceirizacoes.controller;

import com.h_group.gvs_terceirizacoes.dto.request.CreateHistoryRequest;
import com.h_group.gvs_terceirizacoes.dto.response.HistoryResponse;
import com.h_group.gvs_terceirizacoes.service.HistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historico")
@Tag(name = "History")
@SecurityRequirement(name = "bearerAuth")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<HistoryResponse> findAll() {
        return historyService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoryResponse create(@Valid @RequestBody CreateHistoryRequest request) {
        return historyService.create(request);
    }
}
