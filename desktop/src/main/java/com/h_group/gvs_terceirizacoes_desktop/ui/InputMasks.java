package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;

public final class InputMasks {

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
