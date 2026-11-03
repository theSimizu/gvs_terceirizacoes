# Testes e Javadoc

Os módulos `backend` e `desktop` têm POMs e Maven Wrappers independentes. Use JDK 25 completo e execute cada comando dentro do módulo correspondente.

## Executar os testes

Em um terminal na pasta `backend`, configure uma porta de teste livre e remova o bootstrap administrativo apenas desse ambiente de teste:

```powershell
$env:PORT = '0'
Remove-Item Env:GVS_ADMIN_EMAIL, Env:GVS_ADMIN_PASSWORD, Env:GVS_ADMIN_NAME -ErrorAction SilentlyContinue
.\mvnw.cmd test
```

Os testes de integração possuem configuração H2 própria; não precisam do banco MySQL do grupo. Use um terminal separado daquele que mantém as credenciais e a configuração do servidor de demonstração.

Em outro terminal, na pasta `desktop`:

```powershell
.\mvnw.cmd test
```

O desktop inclui testes de controles JavaFX e precisa de ambiente gráfico com bibliotecas nativas compatíveis. Não pressupõe execução em servidor sem interface gráfica.

Na revisão desta documentação, em 02/11/2026, foram aprovados **33 testes no backend e 138 no desktop, total de 171**, sem falhas, erros ou testes ignorados. O número descreve essa revisão e pode mudar conforme a suíte evoluir.

As suítes cobrem regras, perfis, autorização, sessão, mensagens e fluxos com H2, HTTP simulado/Feign e controles JavaFX. Elas não comprovam funcionamento no MySQL, instalação de pacote distribuível nem apresentação em três máquinas. Use o [roteiro de rede](INSTALACAO_E_REDE.md) para registrar essas validações.

Os relatórios de teste de cada módulo ficam em `target/surefire-reports/`. Se houver falha, leia a causa original no relatório; não considere uma execução aprovada apenas porque o Maven iniciou.

## Gerar a documentação Java

Dentro de cada módulo:

```powershell
.\mvnw.cmd javadoc:javadoc
```

Para conferir testes e geração na mesma execução, use:

```powershell
.\mvnw.cmd test javadoc:javadoc
```

No backend, mantenha a preparação do terminal de testes descrita acima. Uma compilação do zero pode usar `clean test javadoc:javadoc`; `clean` remove a saída anterior. Se o Windows ou o sincronizador estiver bloqueando arquivos em `target`, feche os processos que os usam e resolva o bloqueio antes de tentar novamente.

O Maven Javadoc Plugin **3.12.0**, configurado nos dois POMs, gera a entrada em:

```text
backend/target/reports/apidocs/index.html
desktop/target/reports/apidocs/index.html
```

Na pasta do módulo, abra `target/reports/apidocs/index.html` pelo explorador de arquivos ou navegador. Para compartilhar a saída HTML, preserve toda a pasta `apidocs`, incluindo páginas, índices e estilos. O relatório gerado não deve ser versionado; `target` está ignorado pelo Git.

Opcionalmente, `javadoc:jar` produz o artefato classificado como `javadoc` em `target`, útil para ferramentas de desenvolvimento. Ele contém documentação, não é o executável do sistema. A configuração do plugin não vincula a geração automaticamente a todo `compile` ou `package`. Consulte o [uso oficial do Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html).

## O que foi configurado

| Configuração | Efeito |
|---|---|
| UTF-8 em fontes, relatórios e HTML | Preserva acentos nos comentários e páginas |
| `doclint=all,-missing` | Valida estrutura e referências; permite comentários ausentes |
| `failOnError=true` | Falha a geração quando houver erro |
| `notimestamp=true` | Evita data/hora variável no HTML gerado |
| `detectJavaApiLink=false` | Desativa a descoberta automática de links para a API Java |
| Títulos próprios por módulo | Identifica backend e desktop nas páginas |

Foram documentados pacotes com `package-info.java` e tipos/métodos relevantes para segurança, domínio, serviços, mensagens, sessão e navegação. A documentação é progressiva: não há alegação de cobertura de todos os membros ou dos acessores gerados pelo Lombok. As dependências de execução e as regras do sistema foram preservadas.

## Evidência desta revisão

A execução `clean test javadoc:javadoc` terminou com `BUILD SUCCESS` nos dois módulos. Como o Windows/OneDrive bloqueou a limpeza da saída original, a validação usou cópias temporárias novas dos fontes, recursos, testes, POM e Wrapper. Os arquivos correspondentes foram comparados por SHA-256 com a cópia de trabalho, evitando usar classes antigas de outra revisão. O HTML foi conferido quanto a conteúdo, acentuação e referências locais.

Ao alterar Java ou POM, repita as verificações apropriadas e gere novamente o Javadoc. Alterações apenas nestes guias não exigem repetir toda a suíte quando os fontes/POM continuam iguais aos da execução aprovada.

## Três documentos com finalidades diferentes

| Material | Responde |
|---|---|
| Javadoc | O que faz um tipo/método Java e qual seu contrato? |
| OpenAPI/Swagger | Qual endpoint HTTP chamar e com quais dados? |
| README e guias | Como instalar, operar, entender e validar a aplicação? |

Para conectar os exemplos do código à disciplina, consulte [conceitos de POO](CONCEITOS_POO.md).
