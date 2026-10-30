package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.CreateContractRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateContractRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ContractResponse;
import com.h_group.gvs_terceirizacoes.entity.Client;
import com.h_group.gvs_terceirizacoes.entity.Contract;
import com.h_group.gvs_terceirizacoes.entity.ContractStatus;
import com.h_group.gvs_terceirizacoes.entity.Provider;
import com.h_group.gvs_terceirizacoes.exception.ApiException;
import com.h_group.gvs_terceirizacoes.repository.ClientRepository;
import com.h_group.gvs_terceirizacoes.repository.ContractRepository;
import com.h_group.gvs_terceirizacoes.repository.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;

    public ContractService(
            ContractRepository contractRepository,
            ClientRepository clientRepository,
            ProviderRepository providerRepository
    ) {
        this.contractRepository = contractRepository;
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> findAll() {
        return contractRepository.findAll()
                .stream()
                .map(ContractResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContractResponse findById(UUID id) {
        return ContractResponse.from(getEntity(id));
    }

    @Transactional
    public ContractResponse create(CreateContractRequest request) {
        Client client = getClient(request.clientId());
        Provider provider = getProvider(request.providerId());

        Contract contract = new Contract();
        contract.setClient(client);
        contract.setProvider(provider);
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setAgreedValue(request.agreedValue());
        contract.setStatus(request.status() != null ? request.status() : ContractStatus.ACTIVE);

        return ContractResponse.from(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse update(UUID id, UpdateContractRequest request) {
        Contract contract = getEntity(id);

        if (request.clientId() != null) {
            contract.setClient(getClient(request.clientId()));
        }
        if (request.providerId() != null) {
            contract.setProvider(getProvider(request.providerId()));
        }
        if (request.startDate() != null) {
            contract.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            contract.setEndDate(request.endDate());
        }
        if (request.agreedValue() != null) {
            contract.setAgreedValue(request.agreedValue());
        }
        if (request.status() != null) {
            contract.setStatus(request.status());
        }

        return ContractResponse.from(contractRepository.save(contract));
    }

    @Transactional
    public void delete(UUID id) {
        Contract contract = getEntity(id);
        contractRepository.delete(contract);
    }

    private Contract getEntity(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ApiException("Contract not found", HttpStatus.NOT_FOUND));
    }

    private Client getClient(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ApiException("Client not found", HttpStatus.NOT_FOUND));
    }

    private Provider getProvider(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Provider not found", HttpStatus.NOT_FOUND));
    }
}
