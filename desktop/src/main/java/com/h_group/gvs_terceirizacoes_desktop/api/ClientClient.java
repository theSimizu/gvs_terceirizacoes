package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateClientRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateClientRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "clientClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ClientClient {

	@GetMapping("/clientes")
	List<ClientDTO> findAll();

	@PostMapping("/clientes")
	ClientDTO create(@RequestBody CreateClientRequest request);

	@PutMapping("/clientes/{id}")
	ClientDTO update(@PathVariable("id") UUID id, @RequestBody UpdateClientRequest request);

	@DeleteMapping("/clientes/{id}")
	void delete(@PathVariable("id") UUID id);

}
