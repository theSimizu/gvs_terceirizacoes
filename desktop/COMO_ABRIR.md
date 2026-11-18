# Abrir o GVS no Windows

Com o servidor já ligado, abra a pasta `desktop` e dê dois cliques em **Iniciar-GVS.cmd**. Uma janela de acompanhamento será aberta; mantenha-a aberta enquanto usa o sistema.

## Controles da janela

Para escolher tema claro/escuro, ajustar o tamanho da fonte e abrir alto contraste, paleta alternativa ou redução de animações, use os controles no topo da aplicação. Veja [aparência e acessibilidade visual](ACESSIBILIDADE.md).

A barra superior do Windows contém os botões **Minimizar**, **Maximizar/Restaurar** e **Fechar**. A janela abre centralizada, com tamanho limitado à área útil da tela para manter esses controles visíveis. Também é possível redimensionar pelas bordas.

Depois de minimizar, clique no GVS na barra de tarefas para voltar. Fechar encerra o aplicativo desktop; a API e o MySQL no Docker continuam ligados para os outros computadores.

## O que informar

O iniciador pergunta o endereço do servidor:

| Onde você está abrindo | O que digitar |
|---|---|
| No mesmo computador da API | `localhost` |
| Em outro computador | O IPv4 do servidor, por exemplo `192.168.1.20` |
| Servidor com porta diferente de 8080 | IP e porta, por exemplo `192.168.1.20:9090` |

Sem porta informada, um IP/nome simples usa a porta 8080. Também é possível informar a URL completa, como `http://192.168.1.20:9090/api`; URLs completas preservam a porta informada ou a padrão do protocolo. O iniciador acrescenta `/api` quando necessário.

Na próxima abertura, pressione **Enter** para manter o endereço salvo, ou digite um endereço novo se o servidor mudou de IP. No terminal do servidor, `ipconfig` mostra o IPv4 usado na rede local. Não coloque senha, usuário ou comandos nesse campo.

## Java e primeira abertura

É necessário um **JDK 25 completo**, com `java` e `javac`. O iniciador procura a pasta salva, `JAVA_HOME`, o Java no PATH e instalações Java 25 nos diretórios usuais de Java, Eclipse Adoptium e Microsoft em `Program Files`.

Se não encontrar, ele pede a pasta do JDK 25, sem o `bin`, por exemplo `C:\Program Files\Java\jdk-25`. Colar um caminho entre aspas também é aceito. Pressionar Enter nessa pergunta cancela a abertura, com orientação para preparar o JDK. O iniciador não instala Java nem modifica a configuração global do computador.

O iniciador confere se o endereço responde como uma API GVS. Depois usa o Maven Wrapper do próprio desktop para compilar e preparar as dependências, e abre o JavaFX com o JDK escolhido. A primeira abertura pode baixar Maven, JavaFX e outras dependências; é necessário acesso à internet. A janela JavaFX aparece após essa preparação.

Se ocorrer erro, a janela de acompanhamento permanece aberta para você ler a orientação. Se o servidor não estiver acessível, confira API ligada, IP, porta e firewall. O iniciador não sobe o banco nem a API; esse procedimento está no [guia do servidor](../backend/README_SERVIDOR.md).

## O que fica salvo

O endereço e a pasta do JDK são guardados somente neste computador, em:

```text
%USERPROFILE%\.gvs-terceirizacoes\launcher.json
```

Esse arquivo não contém credenciais. A sessão de login continua sendo administrada pela aplicação em arquivo separado, `session.json`. O iniciador só guarda uma configuração depois de validar JDK e servidor; erro de conexão não substitui o endereço anterior.

Esse é um atalho para executar o projeto com Maven. Ainda não é um instalador independente nem permite distribuir somente o arquivo `.cmd`: mantenha a pasta `desktop` completa. O ensaio nos três computadores continua necessário, conforme [instalação e rede](../docs/INSTALACAO_E_REDE.md).

Se a execução manual de `mvnw.cmd javafx:run` informar `Could not find or load main class`, mesmo com o projeto compilado, use `Iniciar-GVS.cmd`. O iniciador abre as classes pelo caminho relativo `target\classes`, evitando a falha de carregamento observada com o caminho absoluto desta pasta no Windows. Não é necessário mover o projeto ou alterar o banco.

## Uso por terminal

Quem preferir pode manter os comandos Maven existentes. Para passar o endereço diretamente ao iniciador, na pasta `desktop`:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-GvsDesktop.ps1 -Server 192.168.1.20
```

O parâmetro opcional `-JavaHome` informa a pasta do JDK. `-CheckOnly` confere JDK/endereço/conexão sem abrir o JavaFX nem gravar configuração. `-ConfigFile` permite usar um arquivo de configuração separado em uma verificação. O `.cmd` aplica a política de execução somente ao processo que executa o roteiro local; políticas administrativas da instituição continuam sendo respeitadas.

## Verificação desta versão

O desktop usa tema claro com azul petróleo, marca integrada ao fundo e telas de entrada/cadastro com altura ajustada ao conteúdo. Os formulários de cliente, prestador, contrato e ordem compartilham cabeçalho, organização em colunas e rolagem quando necessário. O PNG original da marca foi preservado.

A suíte mais recente passou com **139 testes**, sem falhas, erros ou testes ignorados. Foram conferidos visualmente o login, o cadastro de conta e o modal de cliente em prévia isolada, sem gravação no banco. A revisão visual dos outros três formulários continua pendente, assim como o ensaio nas três máquinas.

Em 18/11/2026, a correção do caminho de abertura passou pelos 138 testes do desktop, sem falhas ou erros. O iniciador foi executado na pasta real do projeto, com JDK 25 e API local: a tela de acesso abriu com sessão de teste separada e encerrou normalmente. O login administrativo na instalação local também foi observado em uma sessão operada pela usuária. O ensaio físico em três máquinas continua pendente.

Em 10/11/2026, passaram 138 testes do desktop e 36 verificações do iniciador no Windows PowerShell 5.1. Foram conferidos endereços/portas, rejeição de Java 17, configuração local, passagem de parâmetros ao processo filho, restauração do ambiente e tratamento de falhas. O CMD foi executado em pasta com espaços, `&` e acentos, com perfil de teste isolado. A tela real de login JavaFX também abriu pelo iniciador e foi encerrada normalmente, usando uma API simulada apenas para a verificação inicial de conexão. Esse ensaio não substitui login com o servidor do grupo nem o teste físico nos três computadores.
