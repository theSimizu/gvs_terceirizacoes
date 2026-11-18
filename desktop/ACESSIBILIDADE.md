# Aparência e acessibilidade visual

Os controles ficam no topo da janela, no login, no cadastro e na área de trabalho. As mudanças são imediatas: trocar a aparência não recarrega a página nem substitui o formulário em edição.

| Controle | Comportamento |
|---|---|
| Tema | Claro ou escuro |
| A− e A+ | Texto entre 80% e 200%, em passos de 10% |
| Acessibilidade → Alto contraste | Reforça texto, contornos e foco |
| Paleta alternativa | Usa azul, laranja e roxo; os status continuam escritos por extenso |
| Reduzir animações | Desativa a animação dos gráficos e alguns efeitos decorativos |
| Restaurar padrão | Tema claro, texto em 100% e opções adicionais desmarcadas |

Use Tab e Shift+Tab para percorrer os controles; Enter ou Espaço os acionam conforme o tipo do controle. O foco tem contorno visível. Os atalhos Ctrl+mais/Ctrl+menos ajustam a fonte; Ctrl+0 restaura somente o tamanho do texto, e Ctrl+vírgula abre as opções de acessibilidade.

As preferências ficam em `%USERPROFILE%/.gvs-terceirizacoes/appearance.properties`, separadas da configuração do servidor e da sessão. O arquivo contém apenas tema, porcentagem da fonte e as três opções visuais. Vale para esse computador, inclusive antes de entrar; não é uma configuração da conta nem do banco. Se não for possível gravar o arquivo, a escolha continua ativa na sessão e uma mensagem informa o problema. Arquivo ausente ou inválido usa o padrão sem impedir a abertura.

A partir de 150%, o acesso usa uma disposição compacta e o painel empilha indicadores e gráficos. Menu lateral, área principal e formulários têm rolagem para alcançar conteúdo ampliado. As escolhas também são aplicadas aos modais e alertas. A barra nativa da janela continua seguindo o Windows.

## Como foi implementado

- `UiSettings`: valores e limites das escolhas.
- `UiPreferences`: carregamento, gravação e atualização das telas abertas. Uma gravação temporária seguida de substituição preserva o arquivo anterior se a preparação falhar.
- `AppearanceControls`: barra de controles, atalhos e painel de opções.
- `preferences.css`: paletas e indicadores de foco. Os outros estilos usam cores semânticas e tamanhos de texto relativos.
- `AuthLayout`, `AppShell` e `DialogDesign`: adaptação dos layouts e integração das preferências.
- `DashboardPage`: gráficos com categorias/quantidades em texto e controle da animação.

O PNG original da marca foi preservado. Sua superfície de apresentação mantém contraste nos dois temas; o restante da interface muda de paleta.

## Referências e limites

A suíte passou com **147 testes**, sem falhas, erros ou testes ignorados. Os testes novos verificam persistência e restauração das preferências, arquivo inválido, falha de gravação, limites da fonte, atalhos, preservação de texto digitado e ampliação efetiva do texto até 200%. Amostras de orientação, botão e status tiveram contraste mínimo de 4,5:1 conferido nas oito combinações de tema, alto contraste e paleta alternativa; isso não cobre todos os elementos da aplicação.

Em prévia isolada, foram conferidos a tela de entrada e o painel de acessibilidade com tema escuro, alto contraste, paleta alternativa e fonte em 200%. O restante das combinações e formulários ainda precisa de revisão visual completa.

A orientação de identificar informação também por texto vem de [Uso de cor — W3C](https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html). A ampliação até 200% e o foco visível usam como referências [Redimensionamento de texto](https://www.w3.org/WAI/WCAG22/Understanding/resize-text.html) e [Foco visível](https://www.w3.org/WAI/WCAG22/Understanding/focus-visible.html). Os recursos de estilos seguem a [referência CSS do JavaFX 25](https://openjfx.io/javadoc/25/javafx.graphics/javafx/scene/doc-files/cssref.html).

Essas referências orientam as decisões; não representam certificação WCAG deste aplicativo desktop. A paleta alternativa não simula nem corrige todos os tipos de daltonismo. Ainda são necessários testes com pessoas com diferentes necessidades, tecnologias assistivas e monitores. Em particular, o uso com leitor de tela e a ausência de cortes em todos os formulários/combinações de escala precisam de validação própria.
