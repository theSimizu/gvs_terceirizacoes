package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateHistoryRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.HistoryDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "historyClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface HistoryClient {

	@GetMapping("/historico")
	List<HistoryDTO> findAll();

	@PostMapping("/historico")
	HistoryDTO create(@RequestBody CreateHistoryRequest request);

}
