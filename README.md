# GVS Terceirizações — aplicação Java desktop

Sistema acadêmico para manter clientes, prestadores, contratos e ordens de serviço. O desktop JavaFX consulta uma API Java/Spring Boot, que persiste os dados em MySQL.

## Funcionalidades

- Login, cadastro e perfis administrador/usuário, com permissões conferidas pela API.
- Consulta e manutenção de clientes, prestadores, contratos e ordens de serviço, conforme o perfil.
- Painel de indicadores, acompanhamento de status e histórico auxiliar das operações.
- Iniciador para Windows com configuração do servidor e verificação do JDK 25.
- Temas claro/escuro, fonte ajustável e opções de acessibilidade visual.

## Organização

| Diretório | Responsabilidade |
|---|---|
| `backend/` | API REST, autenticação, autorização, regras de aplicação e JPA/MySQL |
| `desktop/` | Interface JavaFX, clientes Feign, sessão local, formulários e indicadores |
| `docs/` | Arquitetura, instalação, rede, testes, Javadoc e conceitos de POO |

Os módulos têm POMs e Maven Wrappers próprios. Os comandos Maven devem ser executados dentro do módulo escolhido.

## Requisitos

| Uso | O que preparar |
|---|---|
| Servidor com Docker | Docker com containers Linux e Docker Compose v2; API e MySQL 8 executam nos containers |
| Desktop no Windows | JDK 25 completo, com `java` e `javac`, ambiente gráfico e acesso à API |
| Desenvolvimento e testes | JDK 25 completo; cada módulo usa seu próprio Maven Wrapper |
| Geração de Javadoc | JDK 25 com `javadoc` |

A primeira preparação baixa ferramentas e dependências e precisa de internet. O projeto usa dependências Spring `SNAPSHOT`. Não é necessário instalar Maven separadamente. O build do servidor por Docker usa seu próprio Java 25; o JDK local é necessário para executar o desktop ou desenvolver fora dos containers.

O desktop ainda depende do projeto e do Maven Wrapper; não é um instalador independente. Mantenha a pasta `desktop` completa e extraia o ZIP antes de executar.

## Começar

### 1. Preparar o servidor

Siga o [guia do servidor com Docker Compose](backend/README_SERVIDOR.md). Na primeira instalação, copie `backend/.env.example` para `backend/.env` e preencha as senhas, o segredo JWT e, se necessário, a configuração do primeiro administrador. **Preserve um `.env` já existente.** Não há senha administrativa padrão no código.

Depois de configurar, execute a partir da raiz do projeto:

```powershell
cd backend
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Espere a API terminar de iniciar. Com a porta padrão, [http://localhost:8080/](http://localhost:8080/) deve responder com `GVS Terceirizacoes API is running`.

O MySQL mantém os dados em volume persistente. Não use `docker compose down -v` nessa instalação. Senhas do banco e segredo JWT ficam somente no servidor, fora do Git.

### 2. Abrir o desktop

Com a API ligada, abra `desktop` e dê dois cliques em **[Iniciar-GVS.cmd](desktop/Iniciar-GVS.cmd)**. Informe o servidor:

| Computador | Endereço no iniciador |
|---|---|
| Mesmo computador da API | `localhost` |
| Outro computador na mesma rede | IPv4 do servidor, por exemplo `192.168.1.20` |
| API com porta diferente | IP e porta, por exemplo `192.168.1.20:9090` |

Se o iniciador pedir o JDK, informe a pasta do **JDK 25 sem o `bin`**. Ele confere a conexão, usa o Maven Wrapper para preparar as dependências e abre o JavaFX. Mantenha a janela de acompanhamento aberta durante o uso. A primeira abertura pode demorar por causa dos downloads.

O iniciador usa um caminho relativo para as classes, corrigindo a falha de carregamento observada com `javafx:run` na pasta do projeto no Windows. Veja [como abrir e diagnosticar o desktop](desktop/COMO_ABRIR.md).

### 3. Conectar três máquinas

| Máquina | Executa |
|---|---|
| PC 1 — servidor | MySQL, API e, se desejado, um desktop |
| PC 2 | Desktop conectado à API do PC 1 |
| PC 3 | Desktop conectado à mesma API, com conta própria |

Os computadores devem estar na mesma rede local, com a porta da API acessível. O servidor precisa continuar ligado. Os clientes não precisam instalar MySQL nem receber uma cópia do banco ou suas senhas. `localhost` em outro PC aponta para o próprio PC; use o IP do servidor.

O [roteiro de instalação e rede](docs/INSTALACAO_E_REDE.md) descreve a conexão e os testes. **O ensaio físico nas três máquinas continua pendente.**

### Alternativa de desenvolvimento sem Docker

Também é possível preparar MySQL e executar o backend com `mvnw.cmd spring-boot:run`, dentro de `backend`. Nesse caso, configure `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` conforme o [guia de instalação](docs/INSTALACAO_E_REDE.md): o comando Maven não carrega `.env` automaticamente. Em shell POSIX, use `./mvnw` no lugar de `.\mvnw.cmd`.

## Acesso

O cadastro público cria contas `user`. O primeiro administrador é preparado por configuração do servidor, conforme [perfis de acesso](backend/PERFIS_DE_ACESSO.md).

| Ação | User | Adm |
|---|---|---|
| Consultar painel, cadastros, ordens e histórico | Sim | Sim |
| Criar/editar clientes, prestadores e contratos | Não | Sim |
| Criar/editar ordens e atualizar status | Sim | Sim |
| Excluir registros operacionais | Não | Sim |

O backend confere o perfil atual no banco em cada requisição protegida. Os controles da interface acompanham o perfil recebido da API.

## Aparência e acessibilidade visual

Os controles ficam no topo da janela, inclusive antes do login:

- **Tema:** claro ou escuro.
- **A− / A+:** texto entre 80% e 200%, em passos de 10%.
- **Acessibilidade:** alto contraste, paleta alternativa para distinguir cores e redução das animações dos gráficos.
- Navegação por teclado, foco visível e resultados dos gráficos também apresentados em texto.

As escolhas são aplicadas sem recarregar o formulário e ficam salvas neste computador, separadas da conta e da sessão. Os controles nativos de minimizar, maximizar/restaurar e fechar permanecem disponíveis. Fechar o desktop mantém API e banco ligados.

Consulte [aparência e acessibilidade](desktop/ACESSIBILIDADE.md) para atalhos, arquivos locais, implementação e limites de validação. Esses recursos não representam certificação WCAG nem substituem testes com usuários e tecnologias assistivas.

## Testes e validação

Use JDK 25 e execute `mvnw.cmd test` **dentro do módulo correspondente**. O backend usa H2 nos testes e requer a preparação do terminal descrita no [guia de testes](docs/TESTES_E_JAVADOC.md). O desktop inclui testes JavaFX e precisa de ambiente gráfico.

| Evidência registrada | Resultado |
|---|---|
| Desktop — 18/11/2026 | 147 testes aprovados, sem falhas, erros ou testes ignorados |
| Backend — 10/11/2026 | 33 testes aprovados e ensaio local com MySQL real; detalhes no guia do servidor |
| Revisão visual | Login, cadastro de conta e modal de cliente; entrada e painel de acessibilidade também conferidos em escuro/alto contraste/fonte 200%, em prévia isolada |

As execuções têm datas e escopos diferentes. Não comprovam uma revisão visual completa, o ensaio físico de três computadores ou o procedimento de backup/restauração. Os relatórios automatizados ficam em `target/surefire-reports/` de cada módulo.

## Documentação

- [Arquitetura, domínio e fluxos](docs/ARQUITETURA.md)
- [Instalação e ensaio com três máquinas](docs/INSTALACAO_E_REDE.md)
- [Testes e geração de Javadoc](docs/TESTES_E_JAVADOC.md)
- [Conceitos de orientação a objetos aplicados](docs/CONCEITOS_POO.md)
- [Perfis, primeiro administrador e migração](backend/PERFIS_DE_ACESSO.md)
- [Iniciar API e MySQL com Docker Compose](backend/README_SERVIDOR.md)
- [Abrir o desktop por duplo clique no Windows](desktop/COMO_ABRIR.md)
- [Temas, fonte e acessibilidade visual](desktop/ACESSIBILIDADE.md)

## Estado da implementação

O domínio implementado usa **contratos**. Projetos coordenados por administrador, catálogo de tipos de serviço, prestações, equipes, pagamentos e avaliações do escopo revisado ainda precisam de alinhamento com a implementação. Consulte os limites detalhados na arquitetura.

Também permanecem pendentes:

- Revisão visual completa dos formulários, temas e tamanhos de fonte; testes com usuários e tecnologias assistivas.
- Teste de cadastro/persistência pela interface real e ensaio físico nas três máquinas.
- Procedimento de backup/restauração e distribuição do desktop como pacote independente.
