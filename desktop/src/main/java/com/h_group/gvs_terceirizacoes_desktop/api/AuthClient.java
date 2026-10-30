package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginResponse;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "authClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface AuthClient {

	@PostMapping(value = "/auth/login", consumes = MediaType.APPLICATION_JSON_VALUE)
	LoginResponse login(@RequestBody LoginRequest request);

	@PostMapping(value = "/auth/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	RegisterResponse register(@RequestBody RegisterRequest request);

}
