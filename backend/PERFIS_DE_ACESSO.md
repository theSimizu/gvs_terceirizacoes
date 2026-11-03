# Perfis de acesso

A API persiste o perfil de cada conta em `users.role`. No banco os valores são `ADM` e `USER`; nas respostas JSON são `adm` e `user`. O cadastro público sempre cria `USER`.

| Operação | Usuário | Administrador |
|---|---|---|
| Consultar painel, clientes, prestadores, contratos, ordens e histórico | Sim | Sim |
| Criar/editar clientes, prestadores e contratos | Não | Sim |
| Criar/editar ordens e atualizar seu status | Sim | Sim |
| Registrar eventos no histórico auxiliar | Sim | Sim |
| Excluir clientes, prestadores, contratos ou ordens | Não | Sim |

A validação é feita pelo filtro da API. O perfil atual é consultado no banco em cada requisição protegida; o campo de perfil no JWT não é a autoridade final. Usuário inexistente ou token inválido resulta em 401. Perfil insuficiente resulta em 403.

`GET /api/auth/me` exige token e retorna a identidade e o perfil atuais, sem senha. Login também inclui `user.role`. O desktop consulta `/me` ao restaurar a sessão, exibe o perfil e omite ações indisponíveis. Após uma mudança de perfil, entre novamente para atualizar a interface; a API aplica a mudança imediatamente.

## Primeiro administrador

Na máquina que executa o backend, configure:

- `GVS_ADMIN_EMAIL`: e-mail de uma nova conta administrativa.
- `GVS_ADMIN_PASSWORD`: senha, sem gravá-la no código ou em arquivos versionados.
- `GVS_ADMIN_NAME`: opcional; padrão `Administrador GVS`.

Se as duas primeiras variáveis estiverem vazias, nenhuma conta especial será criada. Se estiverem configuradas, a inicialização valida os dados com as mesmas restrições do cadastro e cria um administrador com senha BCrypt **somente se ainda não houver nenhum ADM**. Configuração parcial/inválida interrompe a inicialização. Se o e-mail já pertencer a uma conta comum e não houver ADM, a aplicação informa o conflito: ela não promove essa conta nem troca sua senha automaticamente.

Depois de criar e verificar a conta inicial, remova as variáveis de bootstrap do ambiente de execução. Elas não são necessárias para os próximos logins. Não existe senha administrativa padrão nem escolha de perfil no cadastro público.

Alterar perfis de contas existentes exige uma operação administrativa controlada no banco nesta etapa. Ainda não há tela de gestão de contas/perfis. Não compartilhar a conta ADM entre os integrantes como substituto das contas individuais.

## Banco existente

O projeto mantém `ddl-auto=update`; o mapeamento de `role` define padrão `USER` para a coluna nova. Todos os usuários existentes devem iniciar como USER.

Para uma migração SQL explícita, faça backup e execute **uma única vez, somente se a coluna ainda não existir**, antes de iniciar a nova versão:

```sql
ALTER TABLE users ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'USER';
```

O mesmo comando está em `src/main/resources/db/manual/V001__user_roles.sql`. Esse arquivo é uma migração manual, não é executado automaticamente pelo Spring e não instala uma ferramenta de migrações. Não execute o ALTER depois que `ddl-auto=update` já tiver criado a coluna. Valide o procedimento em uma cópia da base antes de atualizar o banco do grupo.

Atualize backend e desktop juntos. O endpoint de cadastro e os nomes dos campos operacionais existentes foram preservados. Sessões locais de versões antigas podem não conter perfil; o desktop obtém esse dado do servidor ao restaurar a sessão.

## Verificação

Com JDK 25 e Maven preparados, execute `mvnw.cmd test` dentro de `backend` e depois dentro de `desktop`. Os testes de integração usam H2; não comprovam execução no MySQL ou nas três máquinas físicas. O teste `ProfilePermissionsTest` cria controles JavaFX e requer ambiente gráfico com as bibliotecas nativas JavaFX da plataforma.

Para o ensaio manual, entre com uma conta user, consulte os cadastros, crie/atualize uma ordem e confira que os comandos administrativos não aparecem. Tente a mesma gravação administrativa pela API com o token user e confira 403. Entre com ADM e repita a operação autorizada. Verifique também reinício, restauração de sessão e conta excluída.

O histórico continua auxiliar: sua gravação é solicitada pelo desktop e não é uma trilha de auditoria inviolável. Esta alteração não implementa gestão de contas, recuperação de senha, inativação de usuário ou revogação de todos os tokens no logout.
