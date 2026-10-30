package com.h_group.gvs_terceirizacoes.dto.response;

import java.util.List;

public record ValidationErrorResponse(String status, String error, List<FieldError> details) {

    public record FieldError(String field, String message) {
    }
}
