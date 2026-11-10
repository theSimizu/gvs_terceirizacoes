# Instalação e ensaio em rede

Este roteiro usa o backend e o desktop deste repositório. Os comandos são para PowerShell, executados na pasta indicada. A execução em três computadores é uma etapa manual ainda pendente; os testes automatizados não substituem esse ensaio.

Para iniciar o servidor com API e MySQL em containers, siga o [guia do Docker Compose](../backend/README_SERVIDOR.md) e depois retome este documento em **Executar o desktop**. As próximas seções descrevem a alternativa de executar o backend por Maven com MySQL já preparado.

## Preparar o ambiente

Instale um JDK 25 completo, MySQL 8 no servidor e mantenha acesso à internet para o Maven Wrapper baixar ferramentas e dependências. Cada computador que executar o desktop precisa de ambiente gráfico e das bibliotecas JavaFX da plataforma, resolvidas pelo Maven.

Confira no terminal:

```powershell
java -version
javac -version
javadoc --version
```

Os três comandos devem usar o JDK 25. Se necessário, ajuste apenas o terminal atual, substituindo o caminho pelo JDK instalado:

```powershell
$env:JAVA_HOME = 'C:\caminho\para\jdk-25'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Os módulos têm wrappers próprios: execute Maven dentro de `backend` ou `desktop`. Não há POM agregador na raiz.

## Preparar o banco e o backend

No MySQL, prepare uma base chamada `gvs` e um usuário próprio da aplicação com acesso a essa base, incluindo permissões de criação/alteração das tabelas exigidas pelo atual `ddl-auto=update`. O procedimento de instalação e criação do usuário depende da instalação de MySQL adotada pelo grupo. Evite usar a conta root como conta da aplicação.

Na pasta `backend`, configure o terminal que executará o servidor:

```powershell
$env:PORT = '8080'
$env:DB_URL = 'jdbc:mysql://localhost:3306/gvs'
$env:DB_USERNAME = 'gvs_app'
$dbPassword = Read-Host 'Senha do usuário MySQL da aplicação' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $dbPassword).Password
$jwtSecret = Read-Host 'Segredo JWT do servidor (ao menos 32 bytes)' -AsSecureString
$env:JWT_SECRET = [System.Net.NetworkCredential]::new('', $jwtSecret).Password
```

Use os nomes e as credenciais realmente preparados no servidor. O segredo JWT deve ser aleatório, forte e guardado fora do Git. Para criar um novo segredo, uma única vez, pode-se gerar 32 bytes aleatórios codificados em Base64:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Guarde esse valor em um mecanismo privado de configuração do servidor para reutilizá-lo. Gerar outro segredo a cada inicialização invalida as sessões anteriores. Não imprima nem inclua as variáveis de senha/segredo em logs, capturas ou commits.

| Variável | Uso |
|---|---|
| `PORT` | Porta HTTP; o exemplo usa 8080 |
| `DB_URL` | URL JDBC da base MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciais de acesso à base |
| `JWT_SECRET` | Chave de assinatura dos tokens HS256 |
| `GVS_ADMIN_EMAIL` / `GVS_ADMIN_PASSWORD` | Bootstrap opcional do primeiro ADM |
| `GVS_ADMIN_NAME` | Nome opcional do primeiro ADM |

Para criar o primeiro administrador, siga [perfis de acesso](../backend/PERFIS_DE_ACESSO.md) antes de iniciar. O cadastro da tela cria somente USER. Não existe senha administrativa padrão. Em base existente, confira também a migração manual de `users.role`: ela só deve ser aplicada se a coluna ainda não existir, após backup e validação em cópia da base.

Inicie o backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Com a porta 8080, confira `http://localhost:8080/`, a descrição HTTP em `http://localhost:8080/api-docs` e a interface Swagger em `http://localhost:8080/swagger-ui.html`. Falha de acesso ao banco ou configuração incompleta deve ser resolvida antes de iniciar os clientes.

As variáveis definidas com `$env:` valem para esse terminal e seus processos filhos. Um `.env` na pasta não é carregado automaticamente pelo comando Maven. O Docker Compose, por sua vez, lê esse arquivo para montar a configuração descrita no guia próprio; não misture as URLs de banco interna do Docker e externa do Maven.

## Executar o desktop

Abra outro terminal na pasta `desktop`. Para executar no mesmo computador da API:

```powershell
$env:GVS_API_BASEURL = 'http://localhost:8080/api'
.\mvnw.cmd javafx:run
```

Em outro computador, troque `localhost` pelo IPv4 do servidor, por exemplo:

```powershell
$env:GVS_API_BASEURL = 'http://192.168.1.20:8080/api'
.\mvnw.cmd javafx:run
```

O endereço é apenas ilustrativo. Preserve o sufixo `/api`. A propriedade é `gvs.api.base-url`; a variável correspondente é `GVS_API_BASEURL`, conforme as [regras de configuração do Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables).

O desktop usa timeout de conexão de 5 segundos e leitura de 15 segundos. A sessão local fica, por padrão, em `${user.home}/.gvs-terceirizacoes/session.json`. O token representa acesso à conta; não compartilhe esse arquivo ao distribuir o projeto. O desktop não recebe a senha do banco nem o segredo JWT.

Após cadastrar uma conta pela tela, entre com e-mail e senha. A tela exige senha com ao menos oito caracteres, maiúscula, minúscula, número e um símbolo entre `!@#$%^&*`; a validação mínima do cadastro na API é de seis caracteres. Essa diferença ainda existe no código.

O procedimento acima usa o projeto e o Maven. A distribuição de um instalador ou pacote desktop independente ainda precisa ser preparada e validada; não basta copiar o JAR de um protótipo anterior.

## Organizar três máquinas

| Máquina | Executa | Configuração |
|---|---|---|
| PC 1 | MySQL, backend e um desktop | Desktop pode usar `localhost` |
| PC 2 | Desktop | URL com o IPv4 do PC 1 |
| PC 3 | Desktop | Mesma API do PC 1, sessão própria |

Conecte as três máquinas à mesma rede local e consulte o IPv4 do PC 1 com `ipconfig`. Mantenha esse endereço estável durante a apresentação. Se necessário, configure uma regra de entrada para a porta da API no firewall do servidor, restrita à rede privada usada no ensaio; não é necessário expor o MySQL aos clientes.

Nos PCs 2 e 3, verifique a conexão, substituindo o IP:

```powershell
Test-NetConnection 192.168.1.20 -Port 8080
```

`TcpTestSucceeded: True` confirma acesso TCP à porta; ainda é preciso testar login e operações do sistema. Se houver isolamento entre clientes do Wi-Fi, solicite uma rede que permita comunicação entre as máquinas. O HTTP do exemplo é para o ensaio local; uso fora dessa rede exige planejamento de transporte seguro.

## Roteiro de validação manual

Registre data, versões/commit, máquinas, operadores e evidências sem senhas ou tokens. Deixe os resultados abaixo em aberto até executar o ensaio.

| Verificação | Resultado/evidência |
|---|---|
| Três desktops conectam à mesma API e autenticam com contas próprias | Pendente |
| ADM cria cliente/prestador/contrato no PC 1; outros PCs veem após atualizar | Pendente |
| USER consulta cadastros e cria/edita ordem vinculada a contrato | Pendente |
| USER não vê ações administrativas; tentativa administrativa pela API retorna 403 | Pendente |
| ADM realiza a mesma operação administrativa com sucesso | Pendente |
| Alterações feitas em um PC aparecem nos demais após nova consulta | Pendente |
| Tentativas simultâneas de cadastro duplicado são tratadas sem registros indevidos | Pendente |
| API reinicia e os dados permanecem no MySQL | Pendente |
| Interrupção da API produz mensagem compreensível; após retorno é possível continuar | Pendente |
| Expiração de sessão exige autenticar novamente; login inválido mantém mensagem apropriada | Pendente |

Não há atualização em tempo real por push; atualize/reabra a consulta para obter dados novos. O comportamento de edições simultâneas precisa ser observado: não há garantia documentada de controle de versão otimista. Teste backup/restauração separadamente em base de ensaio, antes de operar uma base compartilhada.

## Problemas comuns

| Sintoma | Conferir |
|---|---|
| Erro de versão de classe/compilação | JDK efetivamente usado pelo terminal e pelo Maven deve ser 25 |
| API não inicia | Variáveis obrigatórias, MySQL ativo, base/usuário e porta livre |
| Desktop não conecta | Backend ativo, IP/porta, sufixo `/api`, firewall e rede |
| HTTP 401 no login | E-mail e senha; em chamada protegida, token válido e conta existente |
| HTTP 403 | Perfil atual sem permissão; não se resolve repetindo o login com a mesma conta USER |
| Cadastro recusado | Mensagens por campo e conflitos de dados; conferir também o estado no servidor |
| Alteração de perfil não aparece na tela | Entrar novamente; a API já consulta o perfil atual no banco |

Veja também [arquitetura e limites](ARQUITETURA.md) e [testes/Javadoc](TESTES_E_JAVADOC.md).
