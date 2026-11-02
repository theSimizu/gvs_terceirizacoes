package com.h_group.gvs_terceirizacoes.config;

import com.h_group.gvs_terceirizacoes.entity.UserRole;

public final class AccessPolicy {
    private AccessPolicy() {
    }

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
