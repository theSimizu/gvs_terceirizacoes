package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InputMasksTest {

	@Test
	void formatsCompleteCnpj() {
		assertEquals("12.345.678/0001-90", InputMasks.formatCnpj("12345678000190"));
	}

	@Test
	void formatsPartialCnpj() {
		assertEquals("12.345", InputMasks.formatCnpj("12345"));
	}

	@Test
	void formatsElevenDigitPhone() {
		assertEquals("(11) 91234-5678", InputMasks.formatPhone("11912345678"));
	}

	@Test
	void formatsTenDigitPhone() {
		assertEquals("(11) 3456-7890", InputMasks.formatPhone("1134567890"));
	}

	@Test
	void formatsElevenDigitCpf() {
		assertEquals("123.456.789-01", InputMasks.formatCpf("12345678901"));
	}

	@Test
	void formatsCpfCnpjAsCpfUpToElevenDigits() {
		assertEquals("123.456.789-01", InputMasks.formatCpfCnpj("12345678901"));
	}

	@Test
	void formatsCpfCnpjAsCnpjBeyondElevenDigits() {
		assertEquals("12.345.678/0001-90", InputMasks.formatCpfCnpj("12345678000190"));
	}

}
