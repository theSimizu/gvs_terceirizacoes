package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.service.ClientService;
import com.h_group.gvs_terceirizacoes_desktop.service.ContractService;
import com.h_group.gvs_terceirizacoes_desktop.service.DashboardService;
import com.h_group.gvs_terceirizacoes_desktop.service.ProviderService;
import com.h_group.gvs_terceirizacoes_desktop.service.ServiceOrderService;
import com.h_group.gvs_terceirizacoes_desktop.validation.ClientValidator;
import com.h_group.gvs_terceirizacoes_desktop.validation.ContractValidator;
import com.h_group.gvs_terceirizacoes_desktop.validation.ProviderValidator;
import com.h_group.gvs_terceirizacoes_desktop.validation.ServiceOrderValidator;
import org.springframework.stereotype.Component;

@Component
public record PageDependencies(
		DashboardService dashboardService,
		ClientService clientService,
		ClientValidator clientValidator,
		ProviderService providerService,
		ProviderValidator providerValidator,
		ContractService contractService,
		ContractValidator contractValidator,
		ServiceOrderService serviceOrderService,
		ServiceOrderValidator serviceOrderValidator
) {
}
