package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "contractClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ContractClient {

	@GetMapping("/contratos")
	List<ContractDTO> findAll();

}
