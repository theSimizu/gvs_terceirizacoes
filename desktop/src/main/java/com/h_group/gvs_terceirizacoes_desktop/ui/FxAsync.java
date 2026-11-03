package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

/**
 * Executa trabalho em uma Task fora da thread da interface.
 * Os callbacks de sucesso e falha são executados pelo ciclo de eventos JavaFX.
 */
public final class FxAsync {

	private FxAsync() {
	}

	/**
	 * Executa uma operação fora da thread da interface e encaminha seu resultado.
	 * @param <T> tipo do resultado
	 * @param work operação de rede ou processamento
	 * @param onSuccess callback no ciclo JavaFX quando a operação conclui
	 * @param onFailure callback no ciclo JavaFX quando há falha
	 */
	public static <T> void run(Supplier<T> work, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
		Task<T> task = new Task<>() {
			@Override
			protected T call() {
				return work.get();
			}
		};
		task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
		task.setOnFailed(event -> onFailure.accept(task.getException()));

		Thread thread = new Thread(task, "fx-request");
		thread.setDaemon(true);
		thread.start();
	}

}
