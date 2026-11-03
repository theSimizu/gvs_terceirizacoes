package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateProviderRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ProviderResponse;
import com.h_group.gvs_terceirizacoes.entity.Provider;
import com.h_group.gvs_terceirizacoes.exception.ApiException;
import com.h_group.gvs_terceirizacoes.repository.ProviderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Mantém prestadores e verifica conflitos do documento informado.
 * Disponibilidade representa disponível/ocupado; não é um indicador de inativação.
 * A implementação atual usa cadastro genérico, sem subtipos de prestador.
 */
@Service
public class ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    /**
     * Lista prestadores e converte as entidades dentro da transação de leitura.
     * @return registros disponíveis na base
     */
    @Transactional(readOnly = true)
    public List<ProviderResponse> findAll() {
        return providerRepository.findAll()
                .stream()
                .map(ProviderResponse::from)
                .toList();
    }

    /**
     * Consulta um registro pelo identificador.
     * @param id identificador persistido
     * @return representação pública do registro
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se não existir
     */
    @Transactional(readOnly = true)
    public ProviderResponse findById(UUID id) {
        return ProviderResponse.from(getEntity(id));
    }

    /**
     * Cria um registro usando a transação do serviço.
     * @param request dados validados na entrada HTTP
     * @return registro criado
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se houver conflito ou vínculo inexistente
     */
    @Transactional
    public ProviderResponse create(CreateProviderRequest request) {
        String taxId = request.taxId().trim();

        if (providerRepository.existsByTaxId(taxId)) {
            throw new ApiException("Tax ID already registered", HttpStatus.CONFLICT);
        }

        Provider provider = new Provider();
        provider.setName(request.name().trim());
        provider.setTaxId(taxId);
        provider.setSpecialty(request.specialty().trim());
        provider.setEmail(request.email().trim());
        provider.setPhone(request.phone().trim());
        provider.setAvailable(request.available() == null || request.available());

        return ProviderResponse.from(providerRepository.save(provider));
    }

    /**
     * Atualiza os campos não nulos enviados; campos ausentes preservam o valor atual.
     * @param id identificador persistido
     * @param request alterações recebidas
     * @return representação atualizada
     */
    @Transactional
    public ProviderResponse update(UUID id, UpdateProviderRequest request) {
        Provider provider = getEntity(id);

        if (request.name() != null) {
            provider.setName(request.name().trim());
        }
        if (request.taxId() != null) {
            String taxId = request.taxId().trim();
            if (!taxId.equals(provider.getTaxId())
                    && providerRepository.existsByTaxIdAndIdNot(taxId, id)) {
                throw new ApiException(
                        "Tax ID already belongs to another provider",
                        HttpStatus.CONFLICT
                );
            }
            provider.setTaxId(taxId);
        }
        if (request.specialty() != null) {
            provider.setSpecialty(request.specialty().trim());
        }
        if (request.email() != null) {
            provider.setEmail(request.email().trim());
        }
        if (request.phone() != null) {
            provider.setPhone(request.phone().trim());
        }
        if (request.available() != null) {
            provider.setAvailable(request.available());
        }

        return ProviderResponse.from(providerRepository.save(provider));
    }

    /**
     * Solicita exclusão física do registro; relacionamentos podem impedir a operação.
     * @param id identificador persistido
     */
    @Transactional
    public void delete(UUID id) {
        Provider provider = getEntity(id);
        providerRepository.delete(provider);
    }

    private Provider getEntity(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ApiException("Provider not found", HttpStatus.NOT_FOUND));
    }
}
