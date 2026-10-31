package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "providerClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ProviderClient {

	@GetMapping("/prestadores")
	List<ProviderDTO> findAll();

}
