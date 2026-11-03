package com.h_group.gvs_terceirizacoes.config;

import com.h_group.gvs_terceirizacoes.entity.UserRole;

/**
 * Política de autorização por perfil, método HTTP e caminho canônico.
 * O filtro fornece o perfil atual do banco; esta classe não autentica tokens.
 */
public final class AccessPolicy {
    private AccessPolicy() {
    }

    /**
     * Decide se o perfil pode executar a operação informada.
     * @param role perfil atual carregado do banco
     * @param method método HTTP em letras maiúsculas
     * @param path caminho canônico do recurso, incluindo o prefixo da API
     * @return true quando a operação é permitida
     */
    public static boolean permits(UserRole role, String method, String path) {
        if (role == UserRole.ADM) {
            return true;
        }
        if (role != UserRole.USER) {
            return false;
        }
        if (method.equals("GET") || method.equals("HEAD")) {
            return true;
        }
        if (path.equals("/api/historico") && method.equals("POST")) {
            return true;
        }
        return (path.equals("/api/ordens-servico") || path.startsWith("/api/ordens-servico/"))
                && (method.equals("POST") || method.equals("PUT"));
    }
}
