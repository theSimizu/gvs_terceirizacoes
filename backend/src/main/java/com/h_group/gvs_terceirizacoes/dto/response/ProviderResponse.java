package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.Provider;

import java.util.UUID;

public record ProviderResponse(
        UUID id,
        String name,
        String taxId,
        String specialty,
        String email,
        String phone,
        boolean available
) {

    public static ProviderResponse from(Provider provider) {
        return new ProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getTaxId(),
                provider.getSpecialty(),
                provider.getEmail(),
                provider.getPhone(),
                provider.isAvailable()
        );
    }
}
