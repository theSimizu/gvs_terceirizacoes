package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.concurrent.Task;

public final class FxAsync {

	private FxAsync() {
	}

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
