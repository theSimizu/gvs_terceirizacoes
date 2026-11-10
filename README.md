# GVS Terceirizações — aplicação Java desktop

Sistema acadêmico para manter clientes, prestadores, contratos e ordens de serviço. O desktop JavaFX consulta uma API Java/Spring Boot, que persiste os dados em MySQL.

## Organização

| Diretório | Responsabilidade |
|---|---|
| `backend/` | API REST, autenticação, autorização, regras de aplicação e JPA/MySQL |
| `desktop/` | Interface JavaFX, clientes Feign, sessão local, formulários e indicadores |
| `docs/` | Arquitetura, instalação, rede, testes, Javadoc e conceitos de POO |

Os módulos têm POMs e Maven Wrappers próprios. Os comandos Maven devem ser executados dentro do módulo escolhido.

## Requisitos para desenvolver

- **JDK 25**, incluindo `java`, `javac` e `javadoc`, conforme os POMs.
- MySQL 8 para executar o backend com persistência real.
- Ambiente gráfico para o desktop e para os testes que criam controles JavaFX.
- Acesso à internet na primeira resolução de dependências. O projeto usa versões Spring `SNAPSHOT`.

JavaFX é uma dependência do desktop; não basta reutilizar um JAR de outro protótipo Java. O empacotamento para distribuição sem Maven ainda precisa de validação própria.

## Começar

1. Siga [instalação e configuração](docs/INSTALACAO_E_REDE.md) para preparar MySQL, variáveis do backend e a URL do desktop.
2. Inicie a API em um terminal:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

3. Em outro terminal, na pasta `desktop`, execute:

   ```powershell
   .\mvnw.cmd javafx:run
   ```

Os comandos acima dependem da configuração anterior. O backend exige `PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`. Executar Maven não carrega um arquivo `.env` automaticamente.

Para terminais compatíveis com shell POSIX, use `./mvnw` no lugar de `.\mvnw.cmd`.

## Acesso

O cadastro público cria contas `user`. O primeiro administrador é preparado por configuração do servidor, conforme [perfis de acesso](backend/PERFIS_DE_ACESSO.md).

| Ação | User | Adm |
|---|---|---|
| Consultar painel, cadastros, ordens e histórico | Sim | Sim |
| Criar/editar clientes, prestadores e contratos | Não | Sim |
| Criar/editar ordens e atualizar status | Sim | Sim |
| Excluir registros operacionais | Não | Sim |

O backend confere o perfil atual no banco em cada requisição protegida. Os controles da interface acompanham o perfil recebido da API.

## Documentação

- [Arquitetura, domínio e fluxos](docs/ARQUITETURA.md)
- [Instalação e ensaio com três máquinas](docs/INSTALACAO_E_REDE.md)
- [Testes e geração de Javadoc](docs/TESTES_E_JAVADOC.md)
- [Conceitos de orientação a objetos aplicados](docs/CONCEITOS_POO.md)
- [Perfis, primeiro administrador e migração](backend/PERFIS_DE_ACESSO.md)
- [Iniciar API e MySQL com Docker Compose](backend/README_SERVIDOR.md)
- [Abrir o desktop por duplo clique no Windows](desktop/COMO_ABRIR.md)

## Estado da implementação

Há código para login/cadastro, perfis adm/user, CRUD de clientes/prestadores/contratos/ordens, painel e histórico auxiliar. As suítes usam H2, respostas HTTP simuladas e controles JavaFX; sua aprovação não comprova um ensaio em três computadores com MySQL.

O domínio implementado usa **contratos**. Projetos coordenados por administrador, catálogo de tipos de serviço, prestações, equipes, pagamentos e avaliações do escopo revisado ainda precisam de alinhamento com a implementação. Consulte os limites detalhados na arquitetura.

Para iniciar a API e o MySQL juntos, siga o [guia do servidor com Docker Compose](backend/README_SERVIDOR.md). Ele usa um `.env` privado, exige os segredos antes de subir os containers e publica somente a porta da API. A execução sem Docker, com MySQL já preparado, continua descrita no guia de instalação.
