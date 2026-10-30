package com.h_group.gvs_terceirizacoes.repository;

import com.h_group.gvs_terceirizacoes.entity.Contract;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {

    @Override
    @EntityGraph(attributePaths = {"client", "provider"})
    List<Contract> findAll();

    @Override
    @EntityGraph(attributePaths = {"client", "provider"})
    Optional<Contract> findById(UUID id);
}
