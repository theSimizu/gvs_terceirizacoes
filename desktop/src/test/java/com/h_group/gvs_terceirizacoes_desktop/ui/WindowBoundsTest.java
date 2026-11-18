package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import com.h_group.gvs_terceirizacoes_desktop.validation.AuthValidator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WindowBoundsTest {

	@BeforeAll
	static void toolkit() throws Exception {
		CountDownLatch ready = new CountDownLatch(1);
		try {
			Platform.startup(ready::countDown);
		} catch (IllegalStateException alreadyStarted) {
			ready.countDown();
		}
		assertTrue(ready.await(15, TimeUnit.SECONDS));
	}

	@Test
	void mainWindowFitsTheWorkAreaAndKeepsNativeControls() throws Exception {
		FutureTask<Void> task = new FutureTask<>(() -> {
			Stage stage = new Stage();
			Navigator navigator = new Navigator(mock(AuthService.class), new AuthSession(),
					new AuthValidator(), mock(PageDependencies.class));
			navigator.initialize(stage);
			Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
			assertEquals(StageStyle.DECORATED, stage.getStyle());
			assertTrue(stage.isResizable());
			assertTrue(stage.getWidth() <= bounds.getWidth());
			assertTrue(stage.getHeight() <= bounds.getHeight());
			assertTrue(stage.getMinWidth() <= stage.getWidth());
			assertTrue(stage.getMinHeight() <= stage.getHeight());
			return null;
		});
		Platform.runLater(task);
		task.get(20, TimeUnit.SECONDS);
	}
}
