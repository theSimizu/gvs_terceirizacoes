package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ServiceOrderResponse;
import com.h_group.gvs_terceirizacoes.entity.Contract;
import com.h_group.gvs_terceirizacoes.entity.ServiceOrder;
import com.h_group.gvs_terceirizacoes.entity.ServiceOrderStatus;
import com.h_group.gvs_terceirizacoes.exception.ApiException;
import com.h_group.gvs_terceirizacoes.repository.ContractRepository;
import com.h_group.gvs_terceirizacoes.repository.ServiceOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ServiceOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ContractRepository contractRepository;

    public ServiceOrderService(
            ServiceOrderRepository serviceOrderRepository,
            ContractRepository contractRepository
    ) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderResponse> findAll() {
        return serviceOrderRepository.findAll()
                .stream()
                .map(ServiceOrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceOrderResponse findById(UUID id) {
        return ServiceOrderResponse.from(getEntity(id));
    }

    @Transactional
    public ServiceOrderResponse create(CreateServiceOrderRequest request) {
        Contract contract = getContract(request.contractId());

        ServiceOrder serviceOrder = new ServiceOrder();
        serviceOrder.setContract(contract);
        serviceOrder.setDescription(request.description().trim());
        serviceOrder.setExecutionDate(request.executionDate());
        serviceOrder.setStatus(request.status() != null ? request.status() : ServiceOrderStatus.PENDING);

        return ServiceOrderResponse.from(serviceOrderRepository.save(serviceOrder));
    }

    @Transactional
    public ServiceOrderResponse update(UUID id, UpdateServiceOrderRequest request) {
        ServiceOrder serviceOrder = getEntity(id);

        if (request.contractId() != null) {
            serviceOrder.setContract(getContract(request.contractId()));
        }
        if (request.description() != null) {
            serviceOrder.setDescription(request.description().trim());
        }
        if (request.executionDate() != null) {
            serviceOrder.setExecutionDate(request.executionDate());
        }
        if (request.status() != null) {
            serviceOrder.setStatus(request.status());
        }

        return ServiceOrderResponse.from(serviceOrderRepository.save(serviceOrder));
    }

    @Transactional
    public void delete(UUID id) {
        ServiceOrder serviceOrder = getEntity(id);
        serviceOrderRepository.delete(serviceOrder);
    }

    private ServiceOrder getEntity(UUID id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ApiException("Service order not found", HttpStatus.NOT_FOUND));
    }

    private Contract getContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ApiException("Contract not found", HttpStatus.NOT_FOUND));
    }
}
