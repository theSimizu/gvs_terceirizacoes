package com.h_group.gvs_terceirizacoes_desktop.api;

public class ApiException extends RuntimeException {

	private final int status;
	private final boolean validationDetails;

	public ApiException(String message, int status) {
		this(message, status, false);
	}

	public ApiException(String message, int status, boolean validationDetails) {
		super(message);
		this.status = status;
		this.validationDetails = validationDetails;
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
		this.status = 0;
		this.validationDetails = false;
	}

	public int getStatus() {
		return status;
	}

	public boolean hasValidationDetails() {
		return validationDetails;
	}

}
