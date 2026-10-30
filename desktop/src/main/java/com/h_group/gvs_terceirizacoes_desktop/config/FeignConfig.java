package com.h_group.gvs_terceirizacoes_desktop.config;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiErrorDecoder;
import com.h_group.gvs_terceirizacoes_desktop.api.AuthTokenInterceptor;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class FeignConfig {

	@Bean
	public Request.Options feignRequestOptions(ApiProperties properties) {
		return new Request.Options(properties.getConnectTimeout(), properties.getReadTimeout(), true);
	}

	@Bean
	public ErrorDecoder apiErrorDecoder(ObjectMapper objectMapper) {
		return new ApiErrorDecoder(objectMapper);
	}

	@Bean
	public RequestInterceptor authTokenInterceptor(AuthSession authSession) {
		return new AuthTokenInterceptor(authSession);
	}

}
