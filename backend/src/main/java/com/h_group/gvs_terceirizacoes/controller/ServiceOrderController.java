package com.h_group.gvs_terceirizacoes.controller;

import com.h_group.gvs_terceirizacoes.dto.request.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ServiceOrderResponse;
import com.h_group.gvs_terceirizacoes.service.ServiceOrderService;
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
@RequestMapping("/api/ordens-servico")
@Tag(name = "Service Orders")
@SecurityRequirement(name = "bearerAuth")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    public ServiceOrderController(ServiceOrderService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    @GetMapping
    public List<ServiceOrderResponse> findAll() {
        return serviceOrderService.findAll();
    }

    @GetMapping("/{id}")
    public ServiceOrderResponse findById(@PathVariable UUID id) {
        return serviceOrderService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceOrderResponse create(@Valid @RequestBody CreateServiceOrderRequest request) {
        return serviceOrderService.create(request);
    }

    @PutMapping("/{id}")
    public ServiceOrderResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceOrderRequest request
    ) {
        return serviceOrderService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        serviceOrderService.delete(id);
    }
}
