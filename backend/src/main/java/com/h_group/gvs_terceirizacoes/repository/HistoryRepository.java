package com.h_group.gvs_terceirizacoes.repository;

import com.h_group.gvs_terceirizacoes.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoryRepository extends JpaRepository<History, UUID> {

    List<History> findAllByOrderByDateDesc();
}
