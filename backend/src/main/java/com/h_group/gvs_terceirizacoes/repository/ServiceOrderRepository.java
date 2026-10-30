package com.h_group.gvs_terceirizacoes.repository;

import com.h_group.gvs_terceirizacoes.entity.ServiceOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, UUID> {

    @Override
    @EntityGraph(attributePaths = {"contract", "contract.client", "contract.provider"})
    List<ServiceOrder> findAll();

    @Override
    @EntityGraph(attributePaths = {"contract", "contract.client", "contract.provider"})
    Optional<ServiceOrder> findById(UUID id);
}
