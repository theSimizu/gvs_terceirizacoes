package com.h_group.gvs_terceirizacoes_desktop.api;

/**
 * Falha comunicável à interface, com status HTTP e indicação de validação detalhada.
 * O status zero identifica falhas sem uma resposta HTTP, como indisponibilidade.
 */
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

	/**
	 * Indica que a mensagem contém detalhes de campos que os serviços devem preservar.
	 * @return true quando há uma mensagem de validação detalhada
	 */
	public boolean hasValidationDetails() {
		return validationDetails;
	}

}
