package com.h_group.gvs_terceirizacoes.controller;

import com.h_group.gvs_terceirizacoes.dto.request.CreateContractRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateContractRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ContractResponse;
import com.h_group.gvs_terceirizacoes.service.ContractService;
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
@RequestMapping("/api/contratos")
@Tag(name = "Contracts")
@SecurityRequirement(name = "bearerAuth")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<ContractResponse> findAll() {
        return contractService.findAll();
    }

    @GetMapping("/{id}")
    public ContractResponse findById(@PathVariable UUID id) {
        return contractService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractResponse create(@Valid @RequestBody CreateContractRequest request) {
        return contractService.create(request);
    }

    @PutMapping("/{id}")
    public ContractResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContractRequest request
    ) {
        return contractService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        contractService.delete(id);
    }
}
