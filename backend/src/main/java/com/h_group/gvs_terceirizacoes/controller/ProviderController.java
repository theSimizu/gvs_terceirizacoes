package com.h_group.gvs_terceirizacoes.controller;

import com.h_group.gvs_terceirizacoes.dto.request.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateProviderRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ProviderResponse;
import com.h_group.gvs_terceirizacoes.service.ProviderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prestadores")
@Tag(name = "Providers")
@SecurityRequirement(name = "bearerAuth")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public List<ProviderResponse> findAll() {
        return providerService.findAll();
    }

    @GetMapping("/{id}")
    public ProviderResponse findById(@PathVariable UUID id) {
        return providerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProviderResponse create(@Valid @RequestBody CreateProviderRequest request) {
        return providerService.create(request);
    }

    @PutMapping("/{id}")
    public ProviderResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProviderRequest request
    ) {
        return providerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        providerService.delete(id);
    }
}
