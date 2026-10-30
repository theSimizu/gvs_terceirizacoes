package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.CreateHistoryRequest;
import com.h_group.gvs_terceirizacoes.dto.response.HistoryResponse;
import com.h_group.gvs_terceirizacoes.entity.History;
import com.h_group.gvs_terceirizacoes.repository.HistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> findAll() {
        return historyRepository.findAllByOrderByDateDesc()
                .stream()
                .map(HistoryResponse::from)
                .toList();
    }

    @Transactional
    public HistoryResponse create(CreateHistoryRequest request) {
        History history = new History();
        history.setAction(request.action().trim());
        history.setEntity(request.entity().trim());
        history.setDocumentId(request.documentId());
        history.setDetails(request.details());
        history.setDate(request.date());

        return HistoryResponse.from(historyRepository.save(history));
    }
}
