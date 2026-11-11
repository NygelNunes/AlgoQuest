# 🎮 AlgoQuest — Guia Prático

> Windows + terminal **PowerShell do VS Code** (`Ctrl + '` abre o terminal; o botão **+** abre outro).
> Todos os comandos abaixo já são para esse terminal.

## Índice

1. [Visão geral](#1-visão-geral)
2. [Pré-requisitos](#2-pré-requisitos)
3. [Subir o projeto (passo a passo)](#3-subir-o-projeto-passo-a-passo)
4. [Usuários e papéis (Master, Admin, Usuário)](#4-usuários-e-papéis-master-admin-usuário)
5. [Acessar o banco pelo terminal](#5-acessar-o-banco-pelo-terminal)
6. [Acessar a API pelo terminal](#6-acessar-a-api-pelo-terminal)
7. [Resetar o banco](#7-resetar-o-banco)
8. [Testes](#8-testes)
9. [Problemas comuns](#9-problemas-comuns)
10. [Cola rápida](#10-cola-rápida)

---

## 1. Visão geral

| Peça | O que é | Endereço | Como sobe |
|---|---|---|---|
| ☕ Backend | Java + Spring Boot | `http://localhost:8080` | `mvn spring-boot:run` |
| 🗄️ Banco | H2 (padrão) ou PostgreSQL | pasta `data\` ou porta `5432` | automático (H2) ou Docker (Postgres) |
| 🖥️ Frontend | HTML/CSS/JS | `http://127.0.0.1:5500` | Live Server |

**Papéis de usuário** (coluna `papel` da tabela `usuario`):

| Papel | O que pode |
|---|---|
| `USUARIO` | Jogar (módulos, exercícios, laboratório). É o padrão de quem cria conta. |
| `ADMIN` | Tudo do usuário + painel de **módulos e questões** (`admin.html`). |
| `MASTER` | Tudo do admin + **gerenciar usuários e papéis**. Conta protegida. |

**Ordem de execução:**

```
1. (Postgres) subir o banco
2. Definir as variáveis de ambiente do banco
3. mvn spring-boot:run
4. Abrir o frontend no Live Server
5. Criar a conta e definir como Master (seção 4)
```

> 💡 Use **2 terminais**: o 1º fica ocupado com o backend; o 2º serve para os demais comandos.

---

## 2. Pré-requisitos

```powershell
java -version      # 17 ou maior
mvn -version       # Maven no PATH (o projeto não tem mvnw)
```

| Ferramenta | Necessária? | Observação |
|---|---|---|
| JDK 17+ | ✅ | Ex.: Temurin 17 ou 21 |
| Maven | ✅ | Precisa estar no PATH |
| Docker Desktop | Só com Postgres via Docker | Jeito mais fácil de ter o banco |
| Node.js | Opcional | Só para os testes do Grimório |

Depois de instalar, **feche e reabra o VS Code** para o PATH atualizar.

**Extensões do VS Code:**

| Extensão | Para quê | |
|---|---|---|
| **Live Server** (Ritwick Dey) | Serve o frontend na porta 5500 | ⭐ Essencial |
| **Extension Pack for Java** (Microsoft) | Java, debug, Maven | ⭐ Essencial |
| **Spring Boot Extension Pack** (VMware) | Painel do Spring | Recomendada |
| **SQLTools** + **SQLTools PostgreSQL Driver** | Consultar o banco no VS Code | Com Postgres |
| **Thunder Client** | Testar a API com interface | Recomendada |
| **Docker** (Microsoft) | Gerenciar contêineres | Com Docker |

> ❓ **Por que Live Server e não "Open in Browser"?** O "Open in Browser" abre como `file:///C:/...` e a API bloqueia (CORS). Só são aceitas as origens `localhost`/`127.0.0.1` nas portas **5500** e **3000**.

---

## 3. Subir o projeto (passo a passo)

### Passo 1 — Abrir a pasta certa

`File > Open Folder` → pasta **`AlgoQuest`** (a que tem o `pom.xml`). Confirme no terminal:

```powershell
dir
# deve aparecer: pom.xml  README.md  frontend  src  database ...
```

### Passo 2 — Subir o banco

#### 🅰️ H2 (padrão) — não precisa fazer nada

Banco em arquivo (`data\algoquest`), criado sozinho. Pule para o Passo 4.

#### 🅱️ PostgreSQL via Docker

Cria o banco `algoquest` na porta 5432, com o usuário e a senha definidos em `ALGOQUEST_DB_USUARIO` (padrão `postgres`) e `ALGOQUEST_DB_SENHA` (obrigatória). Defina as variáveis **antes** (veja o Passo 3) ou coloque-as em um `.env` ao lado do `docker-compose.yml`. O volume guarda as credenciais da primeira criação.

```powershell
docker compose up -d        # sobe em segundo plano
docker compose ps           # deve mostrar "running"
```

Parar: `docker compose down` (mantém os dados) · `docker compose down -v` (apaga os dados).

#### 🅲 PostgreSQL instalado no Windows

1. Defina `$env:ALGOQUEST_DB_SENHA` com a senha que o usuário `algoquest` terá (veja o Passo 3).
2. Rode:

```powershell
psql -U postgres -v senha="$env:ALGOQUEST_DB_SENHA" -f database\00_criar_banco_postgresql.sql
```

> Se o `psql` não for reconhecido, adicione a pasta `bin` do PostgreSQL ao PATH.

### Passo 3 — Variáveis de ambiente do banco

Só para Postgres. **Sem `ALGOQUEST_DB_URL` o backend usa o H2.**

**Docker** (use os mesmos valores com que o contêiner foi criado):

```powershell
$env:ALGOQUEST_DB_URL     = "jdbc:postgresql://localhost:5432/algoquest"
$env:ALGOQUEST_DB_USUARIO = "postgres"
$env:ALGOQUEST_DB_SENHA   = "SUA_SENHA_DO_BANCO"
```

**Postgres instalado** (use o usuário e a senha que você definiu):

```powershell
$env:ALGOQUEST_DB_URL     = "jdbc:postgresql://localhost:5432/algoquest"
$env:ALGOQUEST_DB_USUARIO = "algoquest"
$env:ALGOQUEST_DB_SENHA   = "SUA_SENHA_DO_BANCO"
```

| Variável | O que é |
|---|---|
| `ALGOQUEST_DB_URL` | Endereço do banco (`jdbc:postgresql://host:porta/banco`) |
| `ALGOQUEST_DB_USUARIO` | Usuário do Postgres |
| `ALGOQUEST_DB_SENHA` | Senha do Postgres (a mesma do banco, senão dá `password authentication failed`) |

> ⚠️ As variáveis valem **só para o terminal em que foram definidas**. Se abrir outro, defina de novo.
> ⚠️ Se a senha tiver `$`, use **aspas simples**: `'minha$senha'`.
> ⚠️ Comentário no PowerShell é `#` (não `//`).

### Passo 4 — Subir o backend

No **mesmo terminal** das variáveis:

```powershell
mvn spring-boot:run
```

Deu certo quando aparecer:

```
Started AlgoQuestApplication in X seconds
```

Na primeira vez, o **Flyway** cria as tabelas e os **50 exercícios** (5 módulos × 10). Você não roda nenhum SQL.

Parar o backend: `Ctrl + C`. Os dados continuam salvos.

### Passo 5 — Abrir o frontend

Com o backend rodando:

1. Abra `frontend\index.html` no VS Code.
2. Clique com o botão direito → **Open with Live Server** (ou **Go Live** na barra inferior).
3. Abre em `http://127.0.0.1:5500/frontend/index.html`.

Sem a extensão (use um **segundo** terminal, para não derrubar o backend):

```powershell
cd frontend
python -m http.server 5500      # abra http://localhost:5500
```

| Página | O que é |
|---|---|
| `index.html` | Login e criação de conta |
| `modulos.html` | Módulos e progresso |
| `exercicio.html` | Tela de exercício |
| `laboratorio.html` | 🔮 Grimório Vivo (Bubble Sort, Busca Binária, Hash, BST, AVL) |
| `admin.html` | Painel administrativo (só Admin e Master) |

> O frontend chama a API na porta **8080** do mesmo host (`frontend\js\api.js`).

### Passo 6 — Criar o usuário e definir o Master

Vá para a [seção 4](#4-usuários-e-papéis-master-admin-usuário).

---

## 4. Usuários e papéis (Master, Admin, Usuário)

Fluxo: **1) criar a conta → 2) promover a Master direto no banco → 3) recarregar o frontend.**

### 4.1 Criar a conta

Toda conta nova nasce como `USUARIO`. Regras: nome de **3 a 50** caracteres (letras, números, `.`, `-`, `_`) e senha de **8 a 128**.

**Pela tela:** `index.html` → **Criar conta**.

**Pelo terminal** (2º terminal, backend rodando):

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/registrar `
  -ContentType "application/json" `
  -Body '{"nomeUsuario":"meu_usuario","senha":"MinhaSenhaForte123"}'
```

### 4.2 Definir como Master (pelo terminal)

A mudança vale na hora: o papel é lido do banco a cada requisição, **sem reiniciar o backend**. Basta recarregar a página (`F5`) para o menu **Administração** aparecer.

#### 🐘 PostgreSQL via Docker

```powershell
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest -c "UPDATE usuario SET papel = 'MASTER' WHERE nome_usuario = 'meu_usuario';"
```

#### 🐘 PostgreSQL instalado

```powershell
$env:PGPASSWORD = 'SUA_SENHA_DO_BANCO'      # evita o pedido de senha
psql -U algoquest -d algoquest -c "UPDATE usuario SET papel = 'MASTER' WHERE nome_usuario = 'meu_usuario';"
```

> Se você conecta como superusuário, troque `algoquest` por `postgres` (e use a senha dele).

#### 🗃️ H2 (padrão)

O H2 em arquivo **trava enquanto o backend roda**. Pare o backend (`Ctrl + C`) e, **dentro da pasta `AlgoQuest`**:

```powershell
$h2 = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
java -cp $h2 org.h2.tools.Shell -url "jdbc:h2:file:./data/algoquest" -user sa -password '""' `
  -sql "UPDATE usuario SET papel = 'MASTER' WHERE nome_usuario = 'meu_usuario';"
```

Depois suba o backend de novo.

#### ✅ Conferir

```powershell
# Docker:
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest -c "SELECT id, nome_usuario, papel FROM usuario;"
# Postgres instalado:
psql -U algoquest -d algoquest -c "SELECT id, nome_usuario, papel FROM usuario;"
```

> ⚠️ O valor precisa ser **maiúsculo** e exato: `'USUARIO'`, `'ADMIN'` ou `'MASTER'`. Qualquer outro é recusado pelo banco.

#### ⚡ Atalho: criar já como Master ao subir o backend

Em vez de criar a conta e promover, defina as duas variáveis **antes** do `mvn spring-boot:run`:

```powershell
$env:ALGOQUEST_USUARIO_INICIAL = "meu_usuario"
$env:ALGOQUEST_SENHA_INICIAL   = "MinhaSenhaForte123"
mvn spring-boot:run
```

- Usuário **não existe** → é criado como `MASTER`.
- Usuário **já existe** → só é promovido se a senha informada **conferir** com a da conta. Se não conferir, aparece um aviso no log e nada muda.
- Senha com menos de 8 caracteres → ignorado (aviso no log).

### 4.3 Promover ou rebaixar Admin

**Pelo painel (jeito normal):** entre como Master → `admin.html` → aba de usuários.

**Pelo terminal:**

```powershell
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest -c "UPDATE usuario SET papel = 'ADMIN' WHERE nome_usuario = 'outro_usuario';"
```

Para voltar a usuário comum, use `'USUARIO'`. (Postgres instalado e H2: mesmo `UPDATE`, com o comando da seção 4.2.)

> 🛡️ Pelo painel/API o Master **não** pode promover alguém a Master, alterar a própria conta nem a de outro Master. Por isso Master só se define por SQL ou pelo atalho acima.

### 4.4 Listar, deletar e outras operações

Rode o SQL com o mesmo comando da seção 4.2 (troque só a parte `-c "..."` ou `-sql "..."`):

| Ação | SQL |
|---|---|
| Listar usuários | `SELECT id, nome_usuario, papel, nivel, xp, pontuacao, criado_em FROM usuario;` |
| Deletar usuário (apaga progresso e sessões dele) | `DELETE FROM usuario WHERE nome_usuario = 'outro_usuario';` |
| Forçar logout de todos | `DELETE FROM sessao;` |

Exemplo (Docker):

```powershell
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest -c "DELETE FROM usuario WHERE nome_usuario = 'outro_usuario';"
```

- **Trocar senha:** não há endpoint. Delete o usuário e crie de novo (progresso e XP são perdidos).
- **`INSERT` manual em `usuario` não funciona:** `senha_hash` exige um hash que só o backend gera. Crie a conta pelo cadastro e depois ajuste o papel.
- **Listar pela API:** `GET /api/admin/usuarios` (só Master).

---

## 5. Acessar o banco pelo terminal

### Shell interativo do Postgres

```powershell
# Docker:
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest
# Instalado:
psql -U algoquest -d algoquest
```

```sql
\dt                                                        -- lista as tabelas
SELECT id, nome_usuario, papel, nivel, xp FROM usuario;
\q                                                         -- sair
```

> Com a extensão **SQLTools**: conexão PostgreSQL (host `localhost`, porta `5432`, banco `algoquest`) e rode consultas direto em um arquivo `.sql`.

### Shell interativo do H2

Com o backend parado e dentro da pasta `AlgoQuest`:

```powershell
$h2 = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
java -cp $h2 org.h2.tools.Shell -url "jdbc:h2:file:./data/algoquest" -user sa -password '""'
```

No prompt `sql>`, rode a consulta terminando com `;` e saia com `quit`.

> 🔎 "Arquivo não encontrado": veja a versão existente com `dir "$env:USERPROFILE\.m2\repository\com\h2database\h2"` e ajuste o número.
> 🔎 Fora da pasta `AlgoQuest`, o `./data/algoquest` aponta para outro lugar e cria um banco vazio.

### Arquivo `.sql` da pasta `database\`

**Normalmente você não roda nenhum.** O Flyway executa sozinho as migrações `V1` a `V5` de `src\main\resources\db\migration\`, uma única vez por banco. O único script avulso é `00_criar_banco_postgresql.sql`, usado só no Postgres instalado (Passo 2).

> 🚫 Use **só o Flyway** para criar tabelas e dados. Scripts manuais no mesmo banco causam erro ou dados duplicados.

---

## 6. Acessar a API pelo terminal

**Como funciona:** o login devolve um **token** (vale 12 h); nas demais chamadas ele vai no cabeçalho `Authorization: Bearer SEU_TOKEN`.

### Endpoints

| Método | Caminho | Quem | O que faz |
|---|---|---|---|
| `POST` | `/api/auth/registrar` | público | Cria conta (`USUARIO`) e já loga |
| `POST` | `/api/auth/login` | público | Login → dados + `token` |
| `POST` | `/api/auth/logout` | opcional | Encerra a sessão |
| `GET` | `/api/auth/me` | logado | Seus dados (papel, nível, XP, pontos) |
| `POST` | `/api/auth/me/avatar` | logado | Escolhe avatar do catálogo |
| `POST` | `/api/auth/me/avatar/personalizado` | logado | Envia avatar próprio |
| `GET` | `/api/modulos` | logado | Módulos + % de progresso |
| `GET` | `/api/modulos/{id}` | logado | Módulo com exercícios |
| `POST` | `/api/modulos/exercicios/{id}/resposta` | logado | Responde um exercício |
| `*` | `/api/admin/modulos/**` e `/api/admin/questoes/**` | Admin, Master | CRUD de módulos, questões e importação JSON |
| `GET` | `/api/admin/usuarios` | Master | Lista usuários |
| `PUT` | `/api/admin/usuarios/{id}/papel` | Master | Troca entre `ADMIN` e `USUARIO` |

### Comandos

> No PowerShell, `curl` é apelido do `Invoke-WebRequest`. Use `Invoke-RestMethod`.

```powershell
# 1) Login e guardar o token
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"nomeUsuario":"meu_usuario","senha":"MinhaSenhaForte123"}'
$token = $login.token
$h = @{ Authorization = "Bearer $token" }

# 2) Quem sou eu? (mostra o papel)
Invoke-RestMethod -Uri http://localhost:8080/api/auth/me -Headers $h

# 3) Módulos
Invoke-RestMethod -Uri http://localhost:8080/api/modulos -Headers $h

# 4) Módulo 1 com os IDs dos exercícios
Invoke-RestMethod -Uri http://localhost:8080/api/modulos/1 -Headers $h | ConvertTo-Json -Depth 6

# 5) Responder o exercício 1 com a opção de índice 0
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/modulos/exercicios/1/resposta `
  -Headers $h -ContentType "application/json" -Body '{"resposta":0}'

# 6) (Master) listar usuários
Invoke-RestMethod -Uri http://localhost:8080/api/admin/usuarios -Headers $h

# 7) (Master) tornar o usuário de id 2 Admin
Invoke-RestMethod -Method Put -Uri http://localhost:8080/api/admin/usuarios/2/papel `
  -Headers $h -ContentType "application/json" -Body '{"papel":"ADMIN"}'

# 8) Logout
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/logout -Headers $h
```

### Thunder Client (VS Code)

1. Ícone ⚡ → **New Request**.
2. `POST` · `http://localhost:8080/api/auth/login` · **Body > JSON**:
   ```json
   { "nomeUsuario": "meu_usuario", "senha": "MinhaSenhaForte123" }
   ```
3. Copie o `token` da resposta e, nas outras requisições, use **Auth > Bearer**.

### Códigos de resposta

| Código | Significa |
|---|---|
| `200` / `201` / `204` | Deu certo (`201` = conta criada, `204` = logout) |
| `400` | Campo faltando ou inválido |
| `401` | Login errado, ou token ausente/expirado → faça login de novo |
| `403` | Seu papel não permite essa ação |
| `404` | Módulo, questão ou exercício não existe |
| `409` | Nome de usuário já em uso |

**Pontos:** cada exercício rende pontos **uma vez** por usuário (Fácil +10, Médio +20, Difícil +30). Responder de novo devolve `pontosGanhos: 0`.

---

## 7. Resetar o banco

| Situação | O que fazer |
|---|---|
| **H2**: zerar tudo | Pare o backend e rode `Remove-Item -Recurse -Force data` |
| **Postgres/Docker**: zerar tudo | `docker compose down -v` e depois `docker compose up -d` |
| Só zerar o progresso | `DELETE FROM progresso_usuario;` e `UPDATE usuario SET pontuacao=0, xp=0, nivel=1;` |

> ⚠️ Zerar tudo apaga também os usuários: recrie a conta e defina o Master de novo (seção 4).
> ⚠️ Nunca edite as migrações `V1` a `V5`. Mudança no banco vira uma **nova** migração (`V6__descricao.sql`) em `src\main\resources\db\migration\`.

---

## 8. Testes

```powershell
mvn test                                   # testes Java
node dev-tools\testes\lab-sims.test.js     # simuladores (precisa do Node)
```

---

## 9. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `'mvn' não é reconhecido` | Maven fora do PATH | Instale o Maven e reabra o VS Code |
| `release version 17 not supported` | Java anterior ao 17 | Instale o JDK 17+ |
| `Port 8080 was already in use` | Backend já rodando em outro terminal | `Ctrl + C` no outro |
| Login não funciona / erro de rede | Frontend aberto com "Open in Browser" ou porta ≠ 5500 | Use o **Live Server** |
| "Usuário ou senha incorretos" | Conta não existe | Crie a conta (seção 4.1) |
| Volta para a tela inicial após o login | Token expirou (12 h) ou foi derrubado | Faça login de novo |
| `password authentication failed` | Senha diferente de `ALGOQUEST_DB_SENHA` | Use a mesma senha do banco |
| `Connection refused` na porta 5432 | Postgres não está rodando | `docker compose up -d` |
| Erro do Flyway / tabelas duplicadas | Misturou scripts manuais com Flyway | Resete o banco (seção 7) |
| Menu **Administração** não aparece / erro `403` | Papel ainda é `USUARIO` | Confira com o `SELECT` da seção 4.2 e recarregue (`F5`) |
| `violates check constraint "ck_usuario_papel"` | Papel digitado errado | Use `'USUARIO'`, `'ADMIN'` ou `'MASTER'` em maiúsculas |
| Log: "Usuario inicial ignorado" | Senha com menos de 8 caracteres ou nome maior que 50 | Corrija as variáveis e suba de novo |
| Log: "senha informada nao confere" | Usuário já existe com outra senha | Use a senha real da conta, ou promova por SQL (seção 4.2) |
| Mudei uma variável e nada mudou | Backend ainda com os valores antigos | `Ctrl + C` e suba de novo no mesmo terminal |

---

## 10. Cola rápida

### ▶️ Do zero com H2

```powershell
cd caminho\para\AlgoQuest
mvn spring-boot:run
# Live Server em frontend\index.html → Criar conta
# Pare o backend (Ctrl + C) e promova a Master (seção 4.2, H2)
```

### ▶️ Do zero com Postgres (Docker)

```powershell
$env:ALGOQUEST_DB_USUARIO = "postgres"
$env:ALGOQUEST_DB_SENHA   = "SUA_SENHA_DO_BANCO"
docker compose up -d
$env:ALGOQUEST_DB_URL     = "jdbc:postgresql://localhost:5432/algoquest"
mvn spring-boot:run
```

Em **outro terminal**, depois de criar a conta:

```powershell
docker compose exec db psql -U $env:ALGOQUEST_DB_USUARIO -d algoquest -c "UPDATE usuario SET papel = 'MASTER' WHERE nome_usuario = 'meu_usuario';"
```

### 🔑 Todas as variáveis de ambiente

| Variável | Padrão | Função |
|---|---|---|
| `ALGOQUEST_DB_URL` / `_USUARIO` / `_SENHA` | H2 local | Conexão com o Postgres |
| `ALGOQUEST_USUARIO_INICIAL` / `ALGOQUEST_SENHA_INICIAL` | vazio | Cria/promove o Master ao subir |
| `ALGOQUEST_PORTA` | `8080` | Porta da API |
| `ALGOQUEST_CORS_ORIGENS` | `localhost` e `127.0.0.1` nas portas 5500 e 3000 | Quem pode chamar a API |
| `ALGOQUEST_SESSAO_HORAS` | `12` | Validade do login |
| `ALGOQUEST_SENHA_ITERACOES` | `600000` | Força do hash de senha |

### 🧱 Tabelas

`usuario` · `modulo` · `exercicio` · `exercicio_multipla_escolha` · `exercicio_opcao` · `progresso_usuario` · `sessao`

### 📚 Mais documentação no projeto

- `README.md` — visão geral, estrutura de pastas e arquitetura
- `docs\ARQUITETURA.md` — índice dos diagramas e convenções
- `docs\exemplo-importacao.json` — modelo para importar questões em lote
