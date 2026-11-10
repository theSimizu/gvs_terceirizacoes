# Abrir o GVS no Windows

Com o servidor já ligado, abra a pasta `desktop` e dê dois cliques em **Iniciar-GVS.cmd**. Uma janela de acompanhamento será aberta; mantenha-a aberta enquanto usa o sistema.

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

O iniciador confere se o endereço responde como uma API GVS. Depois executa o Maven Wrapper do próprio desktop, com a URL e o JDK escolhidos. A primeira abertura pode baixar Maven, JavaFX e outras dependências; é necessário acesso à internet. A janela JavaFX aparece após essa preparação.

Se ocorrer erro, a janela de acompanhamento permanece aberta para você ler a orientação. Se o servidor não estiver acessível, confira API ligada, IP, porta e firewall. O iniciador não sobe o banco nem a API; esse procedimento está no [guia do servidor](../backend/README_SERVIDOR.md).

## O que fica salvo

O endereço e a pasta do JDK são guardados somente neste computador, em:

```text
%USERPROFILE%\.gvs-terceirizacoes\launcher.json
```

Esse arquivo não contém credenciais. A sessão de login continua sendo administrada pela aplicação em arquivo separado, `session.json`. O iniciador só guarda uma configuração depois de validar JDK e servidor; erro de conexão não substitui o endereço anterior.

Esse é um atalho para executar o projeto com Maven. Ainda não é um instalador independente nem permite distribuir somente o arquivo `.cmd`: mantenha a pasta `desktop` completa. O ensaio nos três computadores continua necessário, conforme [instalação e rede](../docs/INSTALACAO_E_REDE.md).

## Uso por terminal

Quem preferir pode manter os comandos Maven existentes. Para passar o endereço diretamente ao iniciador, na pasta `desktop`:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\Start-GvsDesktop.ps1 -Server 192.168.1.20
```

O parâmetro opcional `-JavaHome` informa a pasta do JDK. `-CheckOnly` confere JDK/endereço/conexão sem abrir o JavaFX nem gravar configuração. `-ConfigFile` permite usar um arquivo de configuração separado em uma verificação. O `.cmd` aplica a política de execução somente ao processo que executa o roteiro local; políticas administrativas da instituição continuam sendo respeitadas.

## Verificação desta versão

Em 10/11/2026, passaram 138 testes do desktop e 36 verificações do iniciador no Windows PowerShell 5.1. Foram conferidos endereços/portas, rejeição de Java 17, configuração local, passagem de parâmetros ao processo filho, restauração do ambiente e tratamento de falhas. O CMD foi executado em pasta com espaços, `&` e acentos, com perfil de teste isolado. A tela real de login JavaFX também abriu pelo iniciador e foi encerrada normalmente, usando uma API simulada apenas para a verificação inicial de conexão. Esse ensaio não substitui login com o servidor do grupo nem o teste físico nos três computadores.
