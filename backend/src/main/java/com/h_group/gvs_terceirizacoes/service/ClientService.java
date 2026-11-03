package com.h_group.gvs_terceirizacoes.service;

import com.h_group.gvs_terceirizacoes.dto.request.CreateClientRequest;
import com.h_group.gvs_terceirizacoes.dto.request.UpdateClientRequest;
import com.h_group.gvs_terceirizacoes.dto.response.ClientResponse;
import com.h_group.gvs_terceirizacoes.entity.Client;
import com.h_group.gvs_terceirizacoes.entity.ClientStatus;
import com.h_group.gvs_terceirizacoes.exception.ApiException;
import com.h_group.gvs_terceirizacoes.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Mantém clientes e verifica conflitos do CNPJ informado.
 * Os métodos de escrita são transacionais; a exclusão física continua sujeita
 * às restrições dos relacionamentos persistidos.
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Lista clientes e converte as entidades dentro da transação de leitura.
     * @return registros disponíveis na base
     */
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    /**
     * Consulta um registro pelo identificador.
     * @param id identificador persistido
     * @return representação pública do registro
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se não existir
     */
    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) {
        return ClientResponse.from(getEntity(id));
    }

    /**
     * Cria um registro usando a transação do serviço.
     * @param request dados validados na entrada HTTP
     * @return registro criado
     * @throws com.h_group.gvs_terceirizacoes.exception.ApiException se houver conflito ou vínculo inexistente
     */
    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        String cnpj = request.cnpj().trim();

        if (clientRepository.existsByCnpj(cnpj)) {
            throw new ApiException("CNPJ already registered", HttpStatus.CONFLICT);
        }

        Client client = new Client();
        client.setLegalName(request.legalName().trim());
        client.setCnpj(cnpj);
        client.setEmail(request.email().trim());
        client.setPhone(request.phone().trim());
        client.setAddress(request.address());
        client.setStatus(request.status() != null ? request.status() : ClientStatus.ACTIVE);

        return ClientResponse.from(clientRepository.save(client));
    }

    /**
     * Atualiza os campos não nulos enviados; campos ausentes preservam o valor atual.
     * @param id identificador persistido
     * @param request alterações recebidas
     * @return representação atualizada
     */
    @Transactional
    public ClientResponse update(UUID id, UpdateClientRequest request) {
        Client client = getEntity(id);

        if (request.legalName() != null) {
            client.setLegalName(request.legalName().trim());
        }
        if (request.cnpj() != null) {
            String cnpj = request.cnpj().trim();
            if (!cnpj.equals(client.getCnpj())
                    && clientRepository.existsByCnpjAndIdNot(cnpj, id)) {
                throw new ApiException(
                        "CNPJ already belongs to another client",
                        HttpStatus.CONFLICT
                );
            }
            client.setCnpj(cnpj);
        }
        if (request.email() != null) {
            client.setEmail(request.email().trim());
        }
        if (request.phone() != null) {
            client.setPhone(request.phone().trim());
        }
        if (request.address() != null) {
            client.setAddress(request.address());
        }
        if (request.status() != null) {
            client.setStatus(request.status());
        }

        return ClientResponse.from(clientRepository.save(client));
    }

    /**
     * Solicita exclusão física do registro; relacionamentos podem impedir a operação.
     * @param id identificador persistido
     */
    @Transactional
    public void delete(UUID id) {
        Client client = getEntity(id);
        clientRepository.delete(client);
    }

    private Client getEntity(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ApiException("Client not found", HttpStatus.NOT_FOUND));
    }
}
