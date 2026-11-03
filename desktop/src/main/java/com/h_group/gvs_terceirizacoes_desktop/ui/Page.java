package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.scene.Node;

/**
 * Contrato de uma página do desktop: título, conteúdo, ações e ciclo de exibição.
 * A navegação depende deste contrato e pode apresentar páginas diferentes de forma uniforme.
 */
public interface Page {

	/**
	 * Fornece o título apresentado na navegação.
	 * @return título da página
	 */
	String title();

	/**
	 * Constrói os controles da página na thread JavaFX.
	 * @return conteúdo visual
	 */
	Node build();

	/**
	 * Atualiza a página quando ela é selecionada; a implementação padrão não faz trabalho.
	 */
	default void onShow() {
	}

	/**
	 * Fornece os controles de ação do cabeçalho.
	 * @return controles da página ou null quando não existem
	 */
	default Node action() {
		return null;
	}

}
