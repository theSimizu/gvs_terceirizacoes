# Servidor com Docker Compose

Este procedimento inicia MySQL e API no computador servidor. Os desktops JavaFX executam nos computadores clientes e apontam para essa API. É necessário Docker com suporte a containers Linux e Docker Compose v2 ou superior. JDK/Maven do computador não são necessários para construir o backend por este procedimento: o Dockerfile usa seu próprio ambiente Java 25.

## Configurar uma instalação nova

Na pasta `backend`, copie o modelo **somente se ainda não existir um `.env`**:

```powershell
Copy-Item .env.example .env
```

Edite o `.env` em um editor local e preencha `MYSQL_ROOT_PASSWORD`, `DB_PASSWORD` e `JWT_SECRET`. Escolha senhas diferentes para root e para `gvs_app`; `DB_USERNAME` não deve ser `root`. Para gerar um segredo JWT aleatório no PowerShell 7, use o comando abaixo e guarde o resultado somente no `.env` privado:

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Não compartilhe a saída em capturas ou mensagens. Valores com `$` ou `#` podem ser delimitados por aspas simples no `.env`, conforme a [sintaxe oficial do Compose](https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/). Variáveis já definidas no terminal têm prioridade sobre o arquivo; use um terminal sem configurações de outra instalação.

O Compose recusa senhas/segredo vazios antes de criar os containers. O `.env.example` é apenas um modelo, não é carregado como configuração de execução. O `.env` fica ignorado pelo Git e fora do contexto de build.

| Variável | Significado no Compose |
|---|---|
| `PORT` | Porta publicada no servidor; padrão 8080. Internamente a API sempre usa 8080 |
| `MYSQL_DATABASE` | Base criada pelo MySQL e usada na URL JDBC; padrão `gvs_terceirizacoes` |
| `DB_USERNAME` / `DB_PASSWORD` | Conta da aplicação, criada na primeira inicialização da base |
| `MYSQL_ROOT_PASSWORD` | Senha administrativa do MySQL, separada da aplicação |
| `JWT_SECRET` | Segredo estável para assinar JWTs |
| `GVS_ADMIN_EMAIL` / `GVS_ADMIN_PASSWORD` / `GVS_ADMIN_NAME` | Bootstrap opcional do primeiro ADM |

Configure o primeiro ADM conforme [perfis de acesso](PERFIS_DE_ACESSO.md), com um e-mail novo e senha compatível com o cadastro. Sem esse bootstrap, o cadastro público continua criando apenas USER. Após confirmar o primeiro ADM, limpe os campos de bootstrap no `.env` e recrie a API com `docker compose up -d backend`.

## Subir e conferir

Na pasta `backend`:

```powershell
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

O primeiro build baixa imagens e dependências. O MySQL só fica saudável quando a conta da aplicação consegue consultar a base por TCP; a API aguarda essa condição antes de iniciar. Depois, a API ainda leva um tempo para carregar o Spring e preparar as tabelas.

Com a porta padrão, confira:

```powershell
Invoke-RestMethod http://localhost:8080/
```

O resultado esperado inclui `GVS Terceirizacoes API is running`. A descrição HTTP está em `http://localhost:8080/api-docs`, e a interface Swagger em `http://localhost:8080/swagger-ui.html`. Se escolher outra `PORT`, use-a também nessas URLs e na configuração dos desktops.

O Compose constrói `DB_URL` com o hostname interno `mysql`, a porta interna 3306 e `MYSQL_DATABASE`. Um `DB_URL` usado com Maven fora dos containers não substitui essa conexão interna. O MySQL não publica porta para a rede do computador; somente a API é publicada. Para manutenção SQL local, use `docker compose exec mysql mysql -u root -p`, informando a senha quando solicitado.

## Conectar os outros computadores

No servidor, consulte o IPv4 com `ipconfig`. Libere a porta da API no firewall apenas para a rede privada do ensaio, quando necessário. No terminal da pasta `desktop` de cada cliente:

```powershell
$env:GVS_API_BASEURL = 'http://192.168.1.20:8080/api'
.\mvnw.cmd javafx:run
```

Substitua o IP e a porta pelos valores do servidor. No próprio servidor, um desktop pode usar `localhost`. O desktop ainda exige JDK 25 e ambiente gráfico; preparar um pacote independente é uma etapa separada. Consulte o [roteiro de três máquinas](../docs/INSTALACAO_E_REDE.md) e registre seus resultados após o ensaio.

## Parar, reiniciar e preservar os dados

```powershell
docker compose stop
docker compose start
```

O volume nomeado `mysql_data` guarda os dados. `docker compose down` remove os containers/rede do projeto, preservando esse volume. **Não use `down -v` em uma instalação com dados que precisam ser mantidos.** Mantenha a mesma pasta/nome de projeto Compose para reutilizar o volume; trocar `-p` pode selecionar outro volume e aparentar uma base vazia.

As variáveis de inicialização do MySQL são aplicadas somente quando o diretório de dados está vazio, conforme a [documentação da imagem oficial](https://hub.docker.com/_/mysql). Editar senhas, usuário ou nome da base no `.env` não migra um volume existente. Em instalação anterior, preserve o volume, faça backup e alinhe a conta/base por manutenção SQL. Não exclua os dados para resolver um erro de credenciais.

Os antigos nomes fixos `gvs_mysql`/`gvs_backend` deixaram de ser usados. Antes de atualizar uma instalação antiga, pare os containers antigos pelo Compose da instalação, usando o mesmo projeto e preservando seus volumes. Não mantenha duas APIs disputando a mesma porta.

Para diagnóstico, comece por `docker compose ps`. Logs podem ser consultados localmente com `docker compose logs --tail 100 backend` e `docker compose logs --tail 100 mysql`; não publique logs ou `docker compose config` completo sem conferir dados privados. `config --quiet` valida sem exibir valores.

## Verificação técnica

Em 10/11/2026, foram aprovados os 33 testes do backend e um ensaio local isolado com o build Java 25 e MySQL 8.0.46. Foram conferidos: recusa de segredos vazios, porta publicada alternativa, base/URL interna, primeiro ADM, cadastro/login USER, identidade da sessão, HTTP 403 para gravação administrativa por USER e OpenAPI. A conta e a sessão permaneceram válidas após reiniciar a API e após recriar o container MySQL reutilizando seu volume. Os recursos descartáveis desse ensaio foram removidos ao terminar.

Esse resultado valida a configuração local dos containers. O ensaio físico dos três computadores e o procedimento de backup/restauração do grupo continuam pendentes.
