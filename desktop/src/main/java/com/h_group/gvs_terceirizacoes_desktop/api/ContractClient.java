package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.config.FeignConfig;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateContractRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateContractRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "contractClient", url = "${gvs.api.base-url}", configuration = FeignConfig.class)
public interface ContractClient {

	@GetMapping("/contratos")
	List<ContractDTO> findAll();

	@PostMapping("/contratos")
	ContractDTO create(@RequestBody CreateContractRequest request);

	@PutMapping("/contratos/{id}")
	ContractDTO update(@PathVariable("id") UUID id, @RequestBody UpdateContractRequest request);

	@DeleteMapping("/contratos/{id}")
	void delete(@PathVariable("id") UUID id);

}
