package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.*;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import com.h_group.gvs_terceirizacoes_desktop.validation.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilePermissionsTest {

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
	void userSeesRegistriesWithoutMutationControls() throws Exception {
		checkRegistries("user", false);
	}

	@Test
	void adminSeesRegistryMutationControls() throws Exception {
		checkRegistries("adm", true);
	}

	private void checkRegistries(String role, boolean editable) throws Exception {
		FutureTask<Void> task = new FutureTask<>(() -> {
			AuthSession session = new AuthSession();
			session.store("test-token", new UserDTO("id", "Profile test", "profile@example.com", role));
			AppShell shell = new AppShell(mock(Navigator.class), mock(AuthService.class), session, mock(PageDependencies.class));
			assertEquals(editable, shell.canDelete());
			Page[] pages = {
				new ClientsPage(shell, mock(ClientService.class), new ClientValidator(), mock(AuditLog.class)),
				new ProvidersPage(shell, mock(ProviderService.class), new ProviderValidator(), mock(AuditLog.class)),
				new ContractsPage(shell, mock(ContractService.class), new ContractValidator(), mock(ClientService.class), mock(ProviderService.class), mock(AuditLog.class))
			};
			for (Page page : pages) {
				Parent root = (Parent) page.build();
				TableView<?> table = findTable(root);
				assertEquals(editable, table.getColumns().getLast().isVisible(), page.title());
				HBox actions = (HBox) page.action();
				Button create = (Button) actions.getChildren().getLast();
				assertEquals(editable, create.isVisible(), page.title());
				assertEquals(editable, create.isManaged(), page.title());
			}
			return null;
		});
		Platform.runLater(task);
		task.get(20, TimeUnit.SECONDS);
	}

	private TableView<?> findTable(Parent parent) {
		for (var child : parent.getChildrenUnmodifiable()) {
			if (child instanceof TableView<?> table) {
				return table;
			}
			if (child instanceof Parent nested) {
				TableView<?> table = findTable(nested);
				if (table != null) {
					return table;
				}
			}
		}
		return null;
	}
}
