package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateProviderRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "providerClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ProviderClient {

	@GetMapping("/prestadores")
	List<ProviderDTO> findAll();

	@PostMapping("/prestadores")
	ProviderDTO create(@RequestBody CreateProviderRequest request);

	@PutMapping("/prestadores/{id}")
	ProviderDTO update(@PathVariable("id") UUID id, @RequestBody UpdateProviderRequest request);

	@DeleteMapping("/prestadores/{id}")
	void delete(@PathVariable("id") UUID id);

}
