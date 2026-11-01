package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public final class InputMasks {

	private static final int MAX_CURRENCY_DIGITS = 12;

	private InputMasks() {
	}

	public static TextFormatter<String> cnpj() {
		return new TextFormatter<>(change -> applyMask(change, 14, InputMasks::formatCnpj));
	}

	public static TextFormatter<String> phone() {
		return new TextFormatter<>(change -> applyMask(change, 11, InputMasks::formatPhone));
	}

	public static TextFormatter<String> cpfCnpj() {
		return new TextFormatter<>(change -> applyMask(change, 14, InputMasks::formatCpfCnpj));
	}

	public static TextFormatter<String> currency() {
		return new TextFormatter<>(change -> {
			if (!change.isContentChange()) {
				return change;
			}
			String digits = change.getControlNewText().replaceAll("\\D", "");
			if (digits.length() > MAX_CURRENCY_DIGITS) {
				digits = digits.substring(0, MAX_CURRENCY_DIGITS);
			}
			String masked = digits.isEmpty() ? "" : formatCents(digits);
			if (masked.equals(change.getControlNewText())) {
				return change;
			}
			change.setRange(0, change.getControlText().length());
			change.setText(masked);
			return change;
		});
	}

	static String formatCurrency(BigDecimal value) {
		if (value == null) {
			return "";
		}
		String digits = value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toPlainString();
		return formatCents(digits);
	}

	static BigDecimal parseCurrency(String text) {
		if (text == null) {
			return null;
		}
		String digits = text.replaceAll("\\D", "");
		if (digits.isEmpty()) {
			return null;
		}
		return new BigDecimal(digits).movePointLeft(2);
	}

	private static String formatCents(String digits) {
		String padded = digits.length() < 3 ? "0".repeat(3 - digits.length()) + digits : digits;
		String cents = padded.substring(padded.length() - 2);
		String units = padded.substring(0, padded.length() - 2);

		StringBuilder grouped = new StringBuilder();
		int count = 0;
		for (int i = units.length() - 1; i >= 0; i--) {
			grouped.insert(0, units.charAt(i));
			count++;
			if (count % 3 == 0 && i > 0) {
				grouped.insert(0, '.');
			}
		}
		return "R$ " + grouped + "," + cents;
	}

	private static Change applyMask(Change change, int maxDigits, UnaryOperator<String> formatter) {
		if (!change.isContentChange()) {
			return change;
		}
		String digits = change.getControlNewText().replaceAll("\\D", "");
		if (digits.length() > maxDigits) {
			digits = digits.substring(0, maxDigits);
		}
		String masked = formatter.apply(digits);
		if (masked.equals(change.getControlNewText())) {
			return change;
		}
		change.setRange(0, change.getControlText().length());
		change.setText(masked);
		return change;
	}

	static String formatCnpj(String digits) {
		StringBuilder formatted = new StringBuilder();
		for (int i = 0; i < digits.length(); i++) {
			if (i == 2 || i == 5) {
				formatted.append('.');
			}
			if (i == 8) {
				formatted.append('/');
			}
			if (i == 12) {
				formatted.append('-');
			}
			formatted.append(digits.charAt(i));
		}
		return formatted.toString();
	}

	static String formatCpfCnpj(String digits) {
		return digits.length() <= 11 ? formatCpf(digits) : formatCnpj(digits);
	}

	static String formatCpf(String digits) {
		StringBuilder formatted = new StringBuilder();
		for (int i = 0; i < digits.length(); i++) {
			if (i == 3 || i == 6) {
				formatted.append('.');
			}
			if (i == 9) {
				formatted.append('-');
			}
			formatted.append(digits.charAt(i));
		}
		return formatted.toString();
	}

	static String formatPhone(String digits) {
		if (digits.isEmpty()) {
			return "";
		}
		int dashIndex = digits.length() == 11 ? 7 : 6;
		StringBuilder formatted = new StringBuilder("(");
		for (int i = 0; i < digits.length(); i++) {
			if (i == 2) {
				formatted.append(") ");
			}
			if (i == dashIndex) {
				formatted.append('-');
			}
			formatted.append(digits.charAt(i));
		}
		return formatted.toString();
	}

}
