package com.h_group.gvs_terceirizacoes.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção de aplicação para falhas esperadas, com status HTTP associado.
 * O tratamento global converte a falha no formato de erro público da API.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
