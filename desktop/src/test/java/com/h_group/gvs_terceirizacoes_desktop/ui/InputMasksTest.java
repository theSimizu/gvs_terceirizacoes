package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
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

	@Test
	void formatsCurrency() {
		assertEquals("R$ 1.234,56", InputMasks.formatCurrency(new BigDecimal("1234.56")));
	}

	@Test
	void formatsWholeCurrency() {
		assertEquals("R$ 1.000,00", InputMasks.formatCurrency(new BigDecimal("1000")));
	}

	@Test
	void dropsLeadingZerosFromTypedCents() {
		assertEquals("R$ 0,15", InputMasks.formatCents("0015"));
		assertEquals("R$ 1,23", InputMasks.formatCents("000123"));
		assertEquals("R$ 0,00", InputMasks.formatCents("000"));
	}

	@Test
	void parsesCurrencyFromMaskedText() {
		assertEquals(new BigDecimal("1234.56"),
				InputMasks.parseCurrency("R$ 1.234,56"));
	}

	@Test
	void parsesEmptyCurrencyAsNull() {
		assertNull(InputMasks.parseCurrency(""));
		assertNull(InputMasks.parseCurrency(null));
	}

	@Test
	void placesCaretAfterThirdCnpjDigit() {
		assertEquals(4, InputMasks.caretFor("123", 3, "12.3"));
	}

	@Test
	void placesCaretAfterThirdPhoneDigit() {
		assertEquals(6, InputMasks.caretFor("(119", 4, "(11) 9"));
	}

	@Test
	void placesCaretAtEndOfCompleteCpf() {
		assertEquals(14, InputMasks.caretFor("12345678901", 11, "123.456.789-01"));
	}

	@Test
	void keepsCaretAtStartWhenNoDigitsPrecedeIt() {
		assertEquals(0, InputMasks.caretFor("", 0, ""));
		assertEquals(0, InputMasks.caretFor("123", 0, "12.3"));
	}

	@Test
	void placesCaretAfterInsertedMiddleDigit() {
		assertEquals(4, InputMasks.caretFor("12.93", 4, "12.93"));
	}

}
