package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ClientClient;
import com.h_group.gvs_terceirizacoes_desktop.api.ContractClient;
import com.h_group.gvs_terceirizacoes_desktop.api.ProviderClient;
import com.h_group.gvs_terceirizacoes_desktop.api.ServiceOrderClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.DashboardSummary;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Calcula os indicadores do painel a partir das listas recebidas da API.
 * As agregações ocorrem no desktop; não representam consultas paginadas no banco
 * nem validação de desempenho com grande volume.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {
	private static final String ACTIVE_STATUS = "active";

	private final ClientClient clientClient;
	private final ProviderClient providerClient;
	private final ContractClient contractClient;
	private final ServiceOrderClient serviceOrderClient;

	/**
	 * Obtém listas da API e calcula contagens e distribuições para o painel.
	 * @return indicadores calculados no desktop
	 */
	public DashboardSummary loadSummary() {
		List<ClientDTO> clients = clientClient.findAll();
		List<ProviderDTO> providers = providerClient.findAll();
		List<ContractDTO> contracts = contractClient.findAll();
		List<ServiceOrderDTO> orders = serviceOrderClient.findAll();

		long availableProvidersCount = providers.stream().filter(ProviderDTO::available).count();
		long activeContractsCount = contracts.stream()
				.filter(contract -> ACTIVE_STATUS.equals(contract.status()))
				.count();

		Map<String, Long> ordersByStatus = orders.stream()
				.filter(order -> order.status() != null && !order.status().isBlank())
				.collect(Collectors.groupingBy(ServiceOrderDTO::status, TreeMap::new, Collectors.counting()));

		Map<String, Long> providersBySpecialty = providers.stream()
				.map(ProviderDTO::specialty)
				.filter(specialty -> specialty != null && !specialty.isBlank())
				.collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));

		return new DashboardSummary(clients.size(), availableProvidersCount, activeContractsCount, ordersByStatus,
				providersBySpecialty);
	}

}
