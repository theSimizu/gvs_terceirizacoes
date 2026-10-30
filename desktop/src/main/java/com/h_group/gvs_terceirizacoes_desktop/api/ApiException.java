package com.h_group.gvs_terceirizacoes_desktop.api;

public class ApiException extends RuntimeException {

	private final int status;

	public ApiException(String message, int status) {
		super(message);
		this.status = status;
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
		this.status = 0;
	}

	public int getStatus() {
		return status;
	}

}
