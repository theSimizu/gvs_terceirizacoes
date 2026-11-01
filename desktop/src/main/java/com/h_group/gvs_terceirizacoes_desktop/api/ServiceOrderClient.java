package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateServiceOrderRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "serviceOrderClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ServiceOrderClient {

	@GetMapping("/ordens-servico")
	List<ServiceOrderDTO> findAll();

	@PostMapping("/ordens-servico")
	ServiceOrderDTO create(@RequestBody CreateServiceOrderRequest request);

	@PutMapping("/ordens-servico/{id}")
	ServiceOrderDTO update(@PathVariable("id") UUID id, @RequestBody UpdateServiceOrderRequest request);

	@DeleteMapping("/ordens-servico/{id}")
	void delete(@PathVariable("id") UUID id);

}
