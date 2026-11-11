# 🎮 AlgoQuest

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![POO](https://img.shields.io/badge/POO-Arquitetura_Limpa-0284c7?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Em_Desenvolvimento-15803d?style=for-the-badge)

> Transformando o aprendizado de Algoritmos e Estruturas de Dados em uma experiência gamificada e interativa.

## 📖 Sobre o Projeto

O **AlgoQuest** é um software educacional desenvolvido como projeto acadêmico. Conteúdos de algoritmos (notação Big O, árvores, tabelas hash) costumam ser apresentados de forma abstrata; aqui o usuário resolve desafios contextuais, recebe feedback educacional e ganha pontos, XP e nível. A API é Spring Boot (JDBC puro) e o frontend é HTML/CSS/JavaScript puro, sem build.

---

---

## 🖼️ Visão geral da interface

Abaixo estão algumas telas principais do AlgoQuest. As imagens ficam na pasta [`screenshots/`](screenshots/), localizada na raiz do repositório.

<table>
  <tr>
    <td width="50%" align="center">
      <img src="screenshots/Login.png" alt="Tela de Login" width="100%">
      <br>
      <strong>🔐 Login</strong>
    </td>
    <td width="50%" align="center">
      <img src="screenshots/Home.png" alt="Tela inicial do AlgoQuest" width="100%">
      <br>
      <strong>🏠 Home</strong>
    </td>
  </tr>
  <tr>
    <td width="50%" align="center">
      <img src="screenshots/Avatar.png" alt="Tela de seleção de avatar" width="100%">
      <br>
      <strong>🧙 Avatar</strong>
    </td>
    <td width="50%" align="center">
      <img src="screenshots/Modulo_Exercisc.png" alt="Tela de módulo e exercícios" width="100%">
      <br>
      <strong>📚 Módulo e Exercícios</strong>
    </td>
  </tr>
  <tr>
    <td width="50%" align="center">
      <img src="screenshots/Grimorio_Vivo.png" alt="Tela do Grimório Vivo" width="100%">
      <br>
      <strong>🔮 Grimório Vivo</strong>
    </td>
    <td width="50%" align="center">
      <img src="screenshots/Administracao_Mestre.png" alt="Painel de administração Master" width="100%">
      <br>
      <strong>⚙️ Administração Mestre</strong>
    </td>
  </tr>
</table>

---

## 🔮 Grimório Vivo (simuladores visuais)

Página `frontend/laboratorio.html` com simulações **passo a passo, em português**: Bubble Sort, Busca Binária, Tabela Hash (encadeamento), Árvore Binária de Busca e Árvore AVL (fatores de balanceamento e rotações animadas). Recursos: reproduzir/pausar/voltar/linha do tempo, dados editáveis, **Modo Desafio** (preveja o próximo passo para ganhar maestria — Noviço, Adepto, Mestre) e **ordenar à mão** (Bubble Sort). Nos exercícios, o botão **Ver na prática** abre o simulador correspondente. Frontend puro (sem build); a maestria fica no `localStorage`. Testes: `node dev-tools/testes/lab-sims.test.js`.

---

## 🧠 Estrutura Educacional (Módulos)

O conteúdo é organizado em trilhas de aprendizagem. Os módulos iniciais (5 módulos × 10 exercícios no banco) são os abaixo; administradores podem criar outros pelo painel:

*   📊 **Análise de Algoritmos:** Notação Big O (O(1), O(n), O(log n), etc).
*   🔎 **Algoritmos de Busca:** Busca sequencial vs. Busca binária.
*   🔃 **Algoritmos de Ordenação:** Bubble, Selection, Insertion, Merge e Quick Sort.
*   #️⃣ **Tabelas Hash:** Funções, colisões e endereçamento.
*   🌳 **Estruturas de Dados:** Árvores binárias, de busca e balanceamento.

---

## 🎮 A Experiência do Usuário (Gamificação)

A gamificação existe para incentivar, sem tornar o sistema um jogo complexo. O fluxo padrão consiste em:

1. **Escolha do Módulo:** O usuário acessa seu painel e visualiza seu progresso (ex: *Busca: 90% concluído*).
2. **O Desafio:** O sistema apresenta um cenário (ex: *"Você tem 1.000.000 de elementos ordenados. Como achar um valor rapidamente?"*).
3. **Resolução e Feedback:** Após a resposta, o sistema não apenas diz se está certo ou errado, mas fornece uma **explicação educacional**.
4. **Recompensa:** Acertos geram pontos baseados na dificuldade (Fácil: +10, Médio: +20, Difícil: +30) e aumentam o XP/Nível do perfil.

---

## 📁 Estrutura do projeto

```
AlgoQuest/
├── screenshots/           imagens das principais telas do sistema
├── pom.xml · docker-compose.yml · .env.example
├── database/00_criar_banco_postgresql.sql   cria banco e usuário no PostgreSQL instalado
├── src/main/java/algoquest/
│   ├── controller/   HTTP: recebe JSON, delega ao serviço (sem regra, sem SQL)
│   ├── service/      regras, transações e permissões
│   ├── repository/   SQL parametrizado (JdbcTemplate)
│   ├── model/        domínio e regras puras (Usuario, Modulo, Exercicio, Papel, validações)
│   ├── dto/          contrato JSON de entrada e saída
│   ├── config/       CORS, autenticação por token, Master inicial
│   ├── exception/    ErroApi e formato único de erro
│   └── util/         HashSenha (PBKDF2, tokens)
├── src/main/resources/   application.properties e db/migration (Flyway V1…V5)
├── src/test/java/        testes JUnit (domínio, regras, hash e API sobre H2 em memória)
├── frontend/             View: páginas, css/, js/ e assets/
├── docs/                 ARQUITETURA.md, diagramas/ (.mmd, .svg, .png) e exemplo-importacao.json
├── tools/gerar_avatares.py   gera retratos, catálogo JS e AvatarIds.java
└── dev-tools/            mock da API e testes de interface (Playwright), teste dos simuladores (Node)
```

No MVC do projeto, **View** é o `frontend/`, **Controller** é o pacote `controller` (com `service` como camada de aplicação) e **Model** reúne `model`, `repository` e o banco.

---

## 🏗️ Arquitetura e Modelagem

Esta seção é o **documento de referência do sistema**: explica como as peças se organizam, por que cada regra existe e onde ela mora no código. Os diagramas são escritos em Mermaid (o GitHub os desenha sozinho); a fonte de cada um está em [`docs/diagramas/*.mmd`](docs/diagramas), com `.svg` e `.png` renderizados na mesma pasta (veja [Mantendo os diagramas](#mantendo-os-diagramas)).

O AlgoQuest é uma API REST em **Java + Spring Boot** (JDBC puro, sem JPA) com frontend em **HTML/CSS/JavaScript puro**, organizada em camadas: *Controller → Service → Repository → Banco*, com o **Domínio** (`model`) no centro e uma camada de **Infraestrutura** transversal.

### 1. Visão geral por camadas

```mermaid
flowchart LR
    FE["<b>Frontend</b><br/>HTML · CSS · JS puro<br/>pasta frontend/<br/><br/>login.js · modulos.js<br/>exercicio.js · lab.js<br/>admin.js<br/><br/>api.js (Api) · ui.js<br/>tema.js · arte.js"]
    WEB["<b>Controllers</b><br/>pacote controller<br/><br/>LoginController<br/>/api/auth<br/>ModuloController<br/>/api/modulos<br/>AdminController<br/>/api/admin"]
    SVC["<b>Serviços</b><br/>pacote service<br/><br/>ServicoAutenticacao<br/>ServicoAvatar<br/>ServicoModulo<br/>ServicoResposta<br/>ServicoGamificacao<br/>ServicoAdmin"]
    REP["<b>Repositórios</b><br/>pacote repository<br/>JDBC puro<br/><br/>UsuarioRepositorio<br/>ModuloRepositorio<br/>ProgressoRepositorio<br/>SessaoRepositorio<br/>ConteudoRepositorio"]
    DB[("<b>Banco</b><br/>H2 ou PostgreSQL<br/>esquema Flyway<br/>V1 … V5")]
    DOM["<b>Domínio</b><br/>pacote model<br/><br/>Usuario · Modulo<br/>Exercicio · Papel<br/>ProgressoUsuario<br/>Avatar · Visual<br/>ValidadorConteudo"]
    INF["<b>Infraestrutura</b><br/>config · exception · util<br/><br/>ConfigWeb<br/>ResolvedorUsuarioAutenticado<br/>TratadorErros · ErroApi<br/>HashSenha · UsuarioInicial"]

    FE -->|"HTTP + JSON<br/>Bearer token"| WEB
    WEB -->|"DTOs<br/>(pacote dto)"| SVC
    SVC --> REP
    REP --> DB
    SVC -->|"entidades<br/>e regras"| DOM
    INF -.->|"autentica,<br/>valida e<br/>traduz erros"| WEB

    style FE fill:#fef3c7,stroke:#b45309,color:#000
    style WEB fill:#e0f2fe,stroke:#0284c7,color:#000
    style SVC fill:#dcfce7,stroke:#15803d,color:#000
    style REP fill:#ffedd5,stroke:#c2410c,color:#000
    style DB fill:#fce7f3,stroke:#be185d,color:#000
    style DOM fill:#ede9fe,stroke:#6d28d9,color:#000
    style INF fill:#f1f5f9,stroke:#475569,color:#000
```

| Camada | Pacote | Responsabilidade | O que **não** faz |
|---|---|---|---|
| Frontend | `frontend/` | Telas, chamadas à API (`api.js`), tema visual e simuladores | Não decide permissões: só adapta a tela (o servidor autoriza) |
| Controller | `controller` | Recebe HTTP, converte JSON ⇄ DTO, delega ao serviço | Sem regra de negócio, sem SQL |
| Serviço | `service` | Orquestra regras, transações e permissões | Não monta SQL nem conhece HTTP |
| Domínio | `model` | Entidades e regras puras (nível, verificação de resposta, validações, papéis) | Não depende de Spring nem de banco |
| Repositório | `repository` | SQL parametrizado com `JdbcTemplate` | Não decide regra de negócio |
| DTO | `dto` | Contrato JSON de entrada e saída (`record`s imutáveis) | Nunca expõe entidade nem gabarito indevido |
| Infraestrutura | `config`, `exception`, `util` | Autenticação por token, CORS, tratamento de erros, hash de senha, criação do Master | — |

### 2. Domínio (`model`)

```mermaid
classDiagram
    direction TB

    class Usuario {
        +int XP_POR_NIVEL = 100
        -int id
        -String nomeUsuario
        -String senha «hash PBKDF2»
        -int pontuacao
        -int xp
        -int nivel
        -Papel papel
        -String avatar «id do catálogo ou "personalizado"»
        -String avatarImagem «carregado só sob demanda»
        +adicionarPontos(int pontos)
        +adicionarXp(int xp) «sobe de nível enquanto xp ≥ nivel × 100»
        +subirNivel()
        +getXpProximoNivel() int
    }

    class Papel {
        <<enumeration>>
        USUARIO
        ADMIN
        MASTER
        +podeAdministrarConteudo() boolean
        +podeGerenciarUsuarios() boolean
        +de(String texto)$ Papel
        +validarAlteracao(ator, alvo, novo)$ String
    }

    class ProgressoUsuario {
        -int idUsuario
        -List~Integer~ exerciciosConcluidos
        +marcarExercicioConcluido(int idExercicio)
        +isConcluido(int idExercicio) boolean
    }

    class Modulo {
        -int id
        -String titulo
        -String descricao
        -String tema «chave da ilustração SVG»
        -List~Exercicio~ exercicios
        +adicionarExercicio(Exercicio e)
        +getQuantidadeExercicios() int
        +calcularPorcentagem(ProgressoUsuario p) double
    }

    class Exercicio {
        <<abstract>>
        -int id
        -String descricao
        -Dificuldade dificuldade
        -int pontos
        -String textoFeedback
        -String visual «imagem ou SVG validado»
        +verificarResposta(Object resposta)* boolean
    }

    class ExercicioMultiplaEscolha {
        -List~String~ opcoes
        -int indiceCorreto
        +verificarResposta(Object resposta) boolean
        -converterIndice(Object resposta) int
    }

    class Dificuldade {
        <<enumeration>>
        FACIL
        MEDIO
        DIFICIL
    }

    Usuario "1" --> "1" Papel : tem
    ProgressoUsuario "0..1" .. "1" Usuario : ligado por idUsuario
    Modulo "1" o-- "0..*" Exercicio : agrega
    Exercicio <|-- ExercicioMultiplaEscolha : estende
    Exercicio "*" --> "1" Dificuldade
    Modulo ..> ProgressoUsuario : calcula % concluída

    note for Modulo "Raiz do agregado. ordem e ativo existem só<br/>no banco<br/>(ConteudoRepositorio), não nesta classe.<br/>ModuloRepositorio carrega o módulo com seus<br/>exercícios."
    note for ProgressoUsuario "Guarda só o id do usuário,<br/>nunca o objeto Usuario."
    note for ExercicioMultiplaEscolha "Única subclasse hoje. Novo tipo de exercício<br/>=<br/>nova subclasse com sua verificarResposta."

    style Usuario fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style ProgressoUsuario fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Modulo fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Exercicio fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style ExercicioMultiplaEscolha fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Papel fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style Dificuldade fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
```

- **`Usuario`** guarda pontos, XP e nível. O nível sobe **enquanto `xp ≥ nivel × 100`** (`adicionarXp`), então um ganho grande pode subir vários níveis de uma vez. Carrega também o `papel` e o `avatar`.
- **`Modulo`** é a **raiz do agregado**: contém seus `Exercicio`s e calcula a porcentagem concluída (`concluídos ÷ total`). `ordem` e `ativo` existem só no banco (são geridos por `ConteudoRepositorio`); `tema` é a chave da ilustração SVG.
- **`Exercicio`** é abstrato; cada tipo implementa `verificarResposta`. Hoje só existe `ExercicioMultiplaEscolha` (compara com `indiceCorreto`). O campo `visual` guarda uma imagem ou um SVG validado.
- **`ProgressoUsuario`** guarda só o **id** do usuário e os ids dos exercícios concluídos — nunca o objeto `Usuario`.
- **`Papel`** é um enum **com comportamento**: `podeAdministrarConteudo()`, `podeGerenciarUsuarios()` e a regra de alteração de papéis (`validarAlteracao`).

#### Regras puras (sem Spring, testáveis só com o JDK)

```mermaid
classDiagram
    direction TB

    class Avatar {
        <<utility · final>>
        +String PERSONALIZADO = "personalizado"
        +int MAX_BYTES_IMAGEM = 150 KB
        +idDoCatalogo(String id)$ boolean
        +padraoPara(String nomeUsuario, RandomGenerator r)$ String
        +validarImagem(String dataUrl)$ String
        +validarImagem(String dataUrl, int maxBytes)$ String
    }

    class AvatarIds {
        <<gerado · package-private>>
        ~String[] TODOS «48 ids»
        ~String[] FEMININOS
        ~String[] MASCULINOS
    }

    class Visual {
        <<utility · final>>
        +int MAX_BYTES_IMAGEM = 300 KB
        +int MAX_CARACTERES_SVG = 30000
        +validar(String bruto)$ String
        -validarSvg(String svg)$ String
        -conferir(Element e)$ void
    }

    class ValidadorConteudo {
        <<utility · final>>
        +Set~String~ TEMAS
        +int MAX_QUESTOES_IMPORTACAO = 500
        +validarModulo(titulo, descricao, tema)$ String
        +validarQuestao(QuestaoRequisicao q)$ QuestaoRequisicao
        +dificuldade(String texto)$ Dificuldade
        +pontosPadrao(Dificuldade d)$ int
        +chave(String descricao)$ String
    }

    class Papel {
        <<enumeration>>
        +validarAlteracao(...)$ String
    }

    class Dificuldade {
        <<enumeration>>
    }

    class QuestaoRequisicao {
        <<record · dto>>
    }

    Avatar ..> AvatarIds : lê a lista de ids
    Visual ..> Avatar : validarImagem(dataUrl, max)
    ValidadorConteudo ..> Visual : valida o recurso visual
    ValidadorConteudo ..> QuestaoRequisicao : valida e normaliza
    ValidadorConteudo ..> Dificuldade

    note for Visual "SVG por lista de permissão: sem script,<br/>foreignObject,<br/>eventos on*, href externo, DOCTYPE ou<br/>entidades."
    note for ValidadorConteudo "Mesma validação para edição manual e<br/>importação em lote."
    note for AvatarIds "Gerado por tools/gerar_avatares.py. Não<br/>editar à mão."

    style Avatar fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Visual fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style ValidadorConteudo fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Papel fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style AvatarIds fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style QuestaoRequisicao fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
```

`Avatar`, `Visual` e `ValidadorConteudo` são classes utilitárias `final`, sem estado. Por isso a **mesma validação** vale para edição manual e importação em lote, e dá para testá-las sem subir o servidor (`dev-tools/testes/java/RegrasAdmin.java`).

### 3. Controllers e serviços

Três fatias por funcionalidade (os serviços compartilham repositórios, então um diagrama único ficaria ilegível).

#### 3.1 Autenticação e avatar

```mermaid
classDiagram
    direction TB

    class LoginController {
        <<RestController /api/auth>>
        +login(LoginRequisicao) UsuarioResposta
        +registrar(RegistroRequisicao) UsuarioResposta
        +logout()
        +eu(Usuario) UsuarioResposta
        +escolherAvatar(Usuario, AvatarRequisicao) UsuarioResposta
        +enviarAvatar(Usuario, AvatarRequisicao) UsuarioResposta
    }
    class ServicoAutenticacao {
        <<Service>>
        +login(nome, senha) UsuarioResposta
        +registrar(nome, senha) UsuarioResposta
        +autenticar(cabecalho) Usuario
        +logout(cabecalho)
    }
    class ServicoAvatar {
        <<Service>>
        +sortearPadrao(nome) String
        +garantirAvatar(Usuario)
        +escolher(Usuario, id) Usuario
        +enviarPersonalizado(Usuario, dataUrl) Usuario
        +comImagem(Usuario) Usuario
    }
    class UsuarioRepositorio
    class SessaoRepositorio
    class HashSenha
    class Avatar {
        <<regras puras>>
    }

    LoginController --> ServicoAutenticacao
    LoginController --> ServicoAvatar
    ServicoAutenticacao --> ServicoAvatar : garantirAvatar no login
    ServicoAutenticacao --> UsuarioRepositorio
    ServicoAutenticacao --> SessaoRepositorio
    ServicoAutenticacao --> HashSenha
    ServicoAvatar --> UsuarioRepositorio
    ServicoAvatar ..> Avatar : sorteio e validação

    note for ServicoAutenticacao "autenticar() é chamado por<br/>ResolvedorUsuarioAutenticado<br/>em toda rota protegida: token → SHA-256 →<br/>sessão → Usuario."

    style LoginController fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ServicoAutenticacao fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ServicoAvatar fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style UsuarioRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style SessaoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style HashSenha fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style Avatar fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
```

#### 3.2 Aprendizado (módulos, exercícios, gamificação)

```mermaid
classDiagram
    direction TB

    class ModuloController {
        <<RestController /api/modulos>>
        +listar(Usuario) List~ModuloResumoResposta~
        +abrir(Usuario, id) ModuloDetalheResposta
        +responder(Usuario, id, RespostaRequisicao) ResultadoResposta
    }
    class ServicoModulo {
        <<Service>>
        +listar(Usuario) List~ModuloResumoResposta~
        +abrir(int idModulo) ModuloDetalheResposta
    }
    class ServicoResposta {
        <<Service>>
        +responder(Usuario, idExercicio, resposta) ResultadoResposta
    }
    class ServicoGamificacao {
        <<Service>>
        +processarRespostaCorreta(Usuario, Exercicio)
    }
    class ModuloRepositorio
    class ProgressoRepositorio
    class UsuarioRepositorio

    ModuloController --> ServicoModulo
    ModuloController --> ServicoResposta
    ServicoModulo --> ModuloRepositorio : só módulos ativos
    ServicoModulo --> ProgressoRepositorio : % concluída
    ServicoResposta --> ModuloRepositorio : buscarExercicioPorId
    ServicoResposta --> ProgressoRepositorio
    ServicoResposta --> UsuarioRepositorio : FOR UPDATE
    ServicoResposta --> ServicoGamificacao

    note for ServicoResposta "Chama Exercicio.verificarResposta<br/>(polimorfismo) fora da<br/>transação. Só abre TransactionTemplate se a<br/>resposta<br/>está correta: cada exercício premia 1 vez<br/>por usuário."
    note for ServicoGamificacao "Concede pontos e o mesmo valor em XP.<br/>A subida de nível é regra de Usuario."

    style ModuloController fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ServicoModulo fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ServicoResposta fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ServicoGamificacao fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ModuloRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style ProgressoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style UsuarioRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
```

#### 3.3 Administração

```mermaid
classDiagram
    direction TB

    class AdminController {
        <<RestController /api/admin>>
        +modulos() · criarModulo() · editarModulo() · excluirModulo() · moverModulo()
        +questoes() · criarQuestao() · editarQuestao() · excluirQuestao() · moverQuestao()
        +importar(id, ImportacaoRequisicao) ImportacaoResposta
        +usuarios() · papel(id, PapelRequisicao)
    }
    class ServicoAdmin {
        <<Service>>
        -exigirConteudo(ator) «ADMIN ou MASTER»
        -exigirMaster(ator) «somente MASTER»
        +listarModulos / criarModulo / editarModulo / excluirModulo / moverModulo
        +listarQuestoes / criarQuestao / editarQuestao / excluirQuestao / moverQuestao
        +importar(ator, moduloId, ImportacaoRequisicao) ImportacaoResposta
        +listarUsuarios(ator) List~AdminUsuarioResposta~
        +alterarPapel(ator, idAlvo, papel) AdminUsuarioResposta
    }
    class ConteudoRepositorio
    class UsuarioRepositorio
    class TransactionTemplate {
        <<Spring>>
    }
    class ValidadorConteudo {
        <<regras puras>>
    }
    class Papel {
        <<enumeration>>
        +validarAlteracao(...)$ String
    }

    AdminController --> ServicoAdmin
    ServicoAdmin --> ConteudoRepositorio : módulos, questões, usuários
    ServicoAdmin --> UsuarioRepositorio : atualizarPapel
    ServicoAdmin --> TransactionTemplate : ordem, edição e importação
    ServicoAdmin ..> ValidadorConteudo : valida módulo e questão
    ServicoAdmin ..> Papel : regras de promoção

    note for ServicoAdmin "Toda operação recebe o ator (Usuario<br/>autenticado, papel lido do banco)<br/>e começa exigindo o papel. Falha =<br/>ErroApi.proibido (403).<br/>O Master é protegido: ninguém o altera nem<br/>promove outro a Master."

    style AdminController fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ServicoAdmin fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ConteudoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style UsuarioRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style TransactionTemplate fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style ValidadorConteudo fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
    style Papel fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
```

#### API HTTP

| Rota | Quem acessa | O que faz |
|---|---|---|
| `POST /api/auth/login` · `POST /api/auth/registrar` | público | Entra / cria conta (devolve token; cadastro sempre cria `USUARIO`) |
| `POST /api/auth/logout` · `GET /api/auth/me` | logado | Encerra a sessão / dados do usuário (inclui `papel` e `avatar`) |
| `POST /api/auth/me/avatar` · `POST /api/auth/me/avatar/personalizado` | logado | Escolhe retrato do catálogo / envia imagem própria |
| `GET /api/modulos` · `GET /api/modulos/{id}` | logado | Lista com % de progresso / abre um módulo (só módulos ativos; sem gabarito) |
| `POST /api/modulos/exercicios/{id}/resposta` | logado | Responde; devolve acerto, explicação, pontos e usuário atualizado |
| `/api/admin/modulos…` · `/api/admin/questoes…` | `ADMIN`, `MASTER` | CRUD, ordem e importação de módulos e questões |
| `GET /api/admin/usuarios` · `PUT /api/admin/usuarios/{id}/papel` | **só `MASTER`** | Lista usuários / promove ou rebaixa entre `USUARIO` e `ADMIN` |

Erros seguem sempre `{"erro": "mensagem"}` com `400` (dado inválido), `401` (sem sessão), `403` (sem permissão), `404`, `409` (conflito, ex.: nome já usado) ou `500`.

### 4. Persistência

```mermaid
classDiagram
    direction TB

    class UsuarioRepositorio {
        <<Repository>>
        +salvar(Usuario u)
        +buscarPorId(int id) Usuario
        +buscarPorNome(String nome) Usuario
        +buscarPorIdParaAtualizar(int id) Usuario
        +atualizarPapel(int id, Papel papel)
        +atualizarAvatar(int id, avatar, imagem)
        +definirAvatarSeVazio(int id, avatar) boolean
        +buscarAvatarImagem(int id) String
    }
    class ModuloRepositorio {
        <<Repository>>
        +buscarModulosAtivos() List~Modulo~
        +buscarAtivoPorId(int id) Modulo
        +buscarExercicioPorId(int id) Exercicio
    }

    note for UsuarioRepositorio "buscarPorIdParaAtualizar = SELECT ... FOR<br/>UPDATE<br/>(trava a linha na transação de resposta).<br/>definirAvatarSeVazio = UPDATE ... WHERE<br/>avatar IS NULL<br/>(atômico: nunca troca um avatar já<br/>escolhido).<br/>salvar() grava só nome, senha, pontos, xp e<br/>nível:<br/>não sobrescreve papel nem avatar."
    note for ModuloRepositorio "Agregado: carrega Modulo + Exercicios +<br/>alternativas.<br/>Só enxerga módulos com ativo = TRUE."

    style UsuarioRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style ModuloRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
```

```mermaid
classDiagram
    direction TB

    class ProgressoRepositorio {
        <<Repository>>
        +buscarPorUsuario(int idUsuario) ProgressoUsuario
        +registrarConclusao(int idUsuario, int idExercicio)
    }
    class SessaoRepositorio {
        <<Repository>>
        +criar(tokenHash, idUsuario, expiraEm)
        +buscarIdUsuarioValido(tokenHash, agora) Integer
        +remover(String tokenHash)
        +removerExpiradas(Instant agora)
    }
    class ConteudoRepositorio {
        <<Repository · administração>>
        +listarModulos() · buscarModulo(id)
        +inserirModulo · atualizarModulo · excluirModulo
        +listarQuestoes(moduloId) · buscarQuestao(id)
        +inserirQuestao · atualizarQuestao · excluirQuestao
        +gravarOrdemModulos · gravarOrdemQuestoes
        +enunciados(moduloId) · listarUsuarios()
    }
    class JdbcTemplate {
        <<Spring JDBC>>
    }

    ProgressoRepositorio ..> JdbcTemplate
    SessaoRepositorio ..> JdbcTemplate
    ConteudoRepositorio ..> JdbcTemplate

    note for ProgressoRepositorio "Contrato próprio, sem CRUD genérico:<br/>não implementa Repositorio&lt;T&gt;."
    note for SessaoRepositorio "Guarda só o SHA-256 do token,<br/>nunca o token em si."
    note for ConteudoRepositorio "Lado de escrita do painel admin.<br/>Devolve DTOs, não o agregado Modulo.<br/>As transações ficam em ServicoAdmin."

    style ProgressoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style SessaoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style ConteudoRepositorio fill:#ffedd5,stroke:#c2410c,stroke-width:2px,color:#000
    style JdbcTemplate fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
```

Cada repositório tem contrato próprio, sem interface genérica de CRUD: o que cada um expõe é só o que os serviços usam.

#### Modelo do banco

```mermaid
erDiagram
    usuario ||--o{ sessao : "abre (CASCADE)"
    usuario ||--o{ progresso_usuario : "conclui (CASCADE)"
    modulo ||--o{ exercicio : "contém (CASCADE)"
    exercicio ||--o{ progresso_usuario : "é concluído em (CASCADE)"
    exercicio ||--o| exercicio_multipla_escolha : "1 por exercício (CASCADE)"
    exercicio ||--|{ exercicio_opcao : "alternativas (CASCADE)"

    usuario {
        int id PK
        varchar nome_usuario UK "até 50"
        varchar senha_hash "PBKDF2-SHA256"
        int pontuacao
        int xp
        int nivel
        varchar papel "USUARIO ADMIN MASTER (V5)"
        varchar avatar "id do catálogo ou personalizado (V4)"
        text avatar_imagem "data URL, só se personalizado (V4)"
        timestamp criado_em
    }
    sessao {
        varchar token_hash PK "SHA-256 do token"
        int usuario_id FK
        timestamp criada_em
        timestamp expira_em
    }
    modulo {
        int id PK
        varchar titulo
        varchar descricao
        int ordem
        boolean ativo
        varchar tema "chave da ilustração, NULL = deduzida do título (V5)"
    }
    exercicio {
        int id PK
        int modulo_id FK
        int ordem
        varchar tipo "MULTIPLA_ESCOLHA"
        varchar descricao
        varchar dificuldade "FACIL MEDIO DIFICIL"
        int pontos
        varchar texto_feedback
        text visual "imagem ou SVG validado (V5)"
    }
    exercicio_multipla_escolha {
        int exercicio_id PK, FK
        int indice_correto
    }
    exercicio_opcao {
        int exercicio_id PK, FK
        int indice PK
        varchar texto
    }
    progresso_usuario {
        int usuario_id PK, FK
        int exercicio_id PK, FK
        timestamp concluido_em
    }
```

O esquema é criado pelo Flyway (`V1`…`V5`): `V1` esquema, `V2`/`V3` conteúdo inicial (5 módulos × 10 exercícios), `V4` avatar, `V5` papéis, tema do módulo e visual da questão. Todas as chaves estrangeiras são `ON DELETE CASCADE`: **excluir um módulo ou questão apaga também alternativas e o progresso dos jogadores nela** (pontos já ganhos não são revertidos).

### 5. Contrato da API (DTOs)

```mermaid
classDiagram
    direction TB

    class ModuloResumoResposta {
        <<record>>
        id · titulo · descricao · tema
        porcentagem · quantidadeExercicios
        +de(Modulo, porcentagem)$
    }
    class ModuloDetalheResposta {
        <<record>>
        id · titulo · descricao · tema
        List~ExercicioResposta~ exercicios
        +de(Modulo m)$
    }
    class ResultadoResposta {
        <<record>>
        correta · feedback
        pontosGanhos · usuario
    }
    class ExercicioResposta {
        <<record · sem gabarito>>
        id · descricao · opcoes
        dificuldade · pontos · visual
    }
    class UsuarioResposta {
        <<record>>
        id · nomeUsuario · pontuacao
        xp · nivel · xpProximoNivel
        avatar · avatarImagem · papel · token
        +de(Usuario)$
        +comToken(Usuario, token)$
    }

    ModuloDetalheResposta *-- ExercicioResposta
    ResultadoResposta o-- UsuarioResposta

    style ModuloResumoResposta fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ModuloDetalheResposta fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ResultadoResposta fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style ExercicioResposta fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
    style UsuarioResposta fill:#e0f2fe,stroke:#0284c7,stroke-width:2px,color:#000
```

```mermaid
classDiagram
    direction TB

    class RequisicoesDeContaEJogo {
        <<records>>
        LoginRequisicao(nomeUsuario, senha)
        RegistroRequisicao(nomeUsuario, senha)
        RespostaRequisicao(resposta)
        AvatarRequisicao(avatar, imagem)
    }
    class QuestaoRequisicao {
        <<record>>
        descricao · dificuldade · pontos
        opcoes · indiceCorreto
        explicacao · visual
    }
    class AdminDtos {
        <<container de records>>
        ModuloRequisicao · MoverRequisicao · PapelRequisicao
        ImportacaoRequisicao · ImportacaoResposta · ErroImportacao
        AdminModuloResposta · AdminQuestaoResposta · AdminUsuarioResposta
    }
    class ValidadorConteudo {
        <<regras puras>>
        +validarQuestao(QuestaoRequisicao)$
    }

    AdminDtos ..> QuestaoRequisicao : ImportacaoRequisicao contém lista
    ValidadorConteudo ..> QuestaoRequisicao : valida e normaliza

    note for RequisicoesDeContaEJogo "Entrada do cliente. Os campos são validados<br/>no serviço,<br/>nunca confiados: tamanho, formato e papel do<br/>ator."
    note for AdminDtos "AdminQuestaoResposta expõe o gabarito<br/>(indiceCorreto e explicacao): só nas rotas<br/>/api/admin."

    style RequisicoesDeContaEJogo fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style QuestaoRequisicao fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style AdminDtos fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style ValidadorConteudo fill:#ede9fe,stroke:#6d28d9,stroke-width:2px,color:#000
```

Dois cuidados de segurança vivem aqui: `ExercicioResposta` **não tem** `indiceCorreto` nem explicação (o gabarito só volta em `ResultadoResposta`, depois de responder), e `UsuarioResposta` só leva `token` no login/cadastro e `avatarImagem` só quando o avatar é personalizado. Apenas as rotas `/api/admin` devolvem o gabarito (`AdminQuestaoResposta`).

### 6. Infraestrutura: autenticação, erros e segurança

```mermaid
classDiagram
    direction TB

    class ConfigWeb {
        <<Component>>
        +addCorsMappings(CorsRegistry)
        +addArgumentResolvers(List)
    }
    class WebMvcConfigurer {
        <<Spring · interface>>
    }
    class ResolvedorUsuarioAutenticado {
        <<Component>>
        +supportsParameter(MethodParameter) boolean
        +resolveArgument(...) Usuario
    }
    class HandlerMethodArgumentResolver {
        <<Spring · interface>>
    }
    class UsuarioAutenticado {
        <<annotation>>
    }
    class ServicoAutenticacao {
        <<Service>>
        +autenticar(cabecalho) Usuario
    }

    ConfigWeb ..|> WebMvcConfigurer
    ResolvedorUsuarioAutenticado ..|> HandlerMethodArgumentResolver
    ConfigWeb --> ResolvedorUsuarioAutenticado : registra
    ResolvedorUsuarioAutenticado ..> UsuarioAutenticado : procura a anotação
    ResolvedorUsuarioAutenticado --> ServicoAutenticacao : token → Usuario

    note for UsuarioAutenticado "Marca o parâmetro Usuario de um método de<br/>controller.<br/>Sem ela o parâmetro não é resolvido: o<br/>controller<br/>nunca lê o papel nem o id de dados da<br/>requisição."

    style ConfigWeb fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style ResolvedorUsuarioAutenticado fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style UsuarioAutenticado fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style ServicoAutenticacao fill:#dcfce7,stroke:#15803d,stroke-width:2px,color:#000
    style WebMvcConfigurer fill:#fef3c7,stroke:#b45309,stroke-width:1px,color:#000
    style HandlerMethodArgumentResolver fill:#fef3c7,stroke:#b45309,stroke-width:1px,color:#000
```

```mermaid
classDiagram
    direction TB

    class ErroApi {
        <<extends RuntimeException>>
        -HttpStatus status
        +requisicaoInvalida(msg)$ 400
        +naoAutenticado(msg)$ 401
        +proibido(msg)$ 403
        +naoEncontrado(msg)$ 404
        +conflito(msg)$ 409
    }
    class TratadorErros {
        <<RestControllerAdvice>>
        +erroApi(ErroApi) status do ErroApi
        +corpoInvalido(...) 400
        +metodoNaoSuportado(...) 405
        +tipoNaoSuportado(...) 415
        +inesperado(Exception) 500
    }
    class HashSenha {
        <<Component>>
        +gerar(senha) String
        +verificar(senha, armazenado) boolean
        +verificarFalso(senha)
        +gerarToken()$ String
        +hashDeToken(token)$ String
    }
    class UsuarioInicial {
        <<Component · SmartInitializingSingleton>>
        +afterSingletonsInstantiated()
    }

    TratadorErros ..> ErroApi : converte em resposta HTTP
    UsuarioInicial --> HashSenha

    note for HashSenha "PBKDF2-HMAC-SHA256 com salt.<br/>verificarFalso gasta o mesmo custo se o<br/>usuário não existe."
    note for UsuarioInicial "Cria o Master na subida. Conta existente<br/>só é promovida se a senha conferir."

    style ErroApi fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style TratadorErros fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style HashSenha fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
    style UsuarioInicial fill:#f1f5f9,stroke:#475569,stroke-width:2px,color:#000
```

- **Autenticação por token:** o login gera um token aleatório; o banco guarda **só o SHA-256** dele (`sessao.token_hash`), com validade de 12 h (`ALGOQUEST_SESSAO_HORAS`). Em cada rota protegida, `ResolvedorUsuarioAutenticado` transforma o cabeçalho `Authorization: Bearer …` em um `Usuario` carregado **do banco**.
- **Senhas:** PBKDF2-HMAC-SHA256 com salt aleatório (600 000 iterações por padrão), comparação em tempo constante. Se o usuário não existe, `verificarFalso` gasta o mesmo custo, para o tempo de resposta não revelar quais nomes existem.
- **Master:** `UsuarioInicial` cria o Master na subida a partir de `ALGOQUEST_USUARIO_INICIAL`/`..._SENHA_INICIAL`. Se a conta já existe, só é promovida quando a senha informada confere (evita promover quem cadastrou o mesmo nome antes). Não há outro caminho para criar um Master.

### 7. Fluxos (como as camadas colaboram)

#### 7.1 Autenticação e permissões por papel

```mermaid
%%{init: {'sequence': {'width': 130, 'actorMargin': 24, 'messageMargin': 26, 'noteMargin': 6, 'boxMargin': 6, 'wrap': true, 'mirrorActors': false}}}%%
sequenceDiagram
    autonumber
    actor C as Cliente
    participant R as Resolvedor<br/>UsuarioAutenticado
    participant SA as Servico<br/>Autenticacao
    participant RP as Repositorios<br/>Sessao e Usuario
    participant AC as Admin<br/>Controller
    participant AD as Servico<br/>Admin

    C->>R: GET /api/admin/usuarios<br/>Authorization: Bearer token
    R->>SA: autenticar(cabeçalho)
    SA->>RP: sessão válida para SHA-256(token)?
    RP-->>SA: idUsuario ou nulo
    alt token ausente, inválido ou expirado
        SA-->>C: 401 (ErroApi.naoAutenticado)
    else sessão válida
        SA->>RP: buscarPorId(idUsuario)
        RP-->>SA: Usuario com papel lido do BANCO
        SA-->>R: Usuario
        R->>AC: usuarios(Usuario)
        AC->>AD: listarUsuarios(ator)
        alt papel não é MASTER
            AD-->>C: 403 (ErroApi.proibido)
        else papel MASTER
            AD-->>C: 200 lista de usuários
        end
    end
    Note over R,AD: O papel nunca vem da requisição: é relido do banco a cada chamada.<br/>Rebaixar um administrador vale na hora, mesmo com o mesmo token.
```

O papel **nunca** vem da requisição: é relido do banco a cada chamada. Por isso rebaixar um administrador vale imediatamente, mesmo que ele continue com o mesmo token. O frontend só esconde botões; o servidor responde `403` a qualquer chamada sem o papel exigido.

#### 7.2 Cadastro e avatar padrão

```mermaid
%%{init: {'sequence': {'width': 130, 'actorMargin': 24, 'messageMargin': 26, 'noteMargin': 6, 'boxMargin': 6, 'wrap': true, 'mirrorActors': false}}}%%
sequenceDiagram
    autonumber
    actor C as Cliente
    participant SA as Servico<br/>Autenticacao
    participant H as HashSenha
    participant AV as Servico<br/>Avatar
    participant UR as Usuario<br/>Repositorio
    participant SR as Sessao<br/>Repositorio

    C->>SA: POST /api/auth/registrar<br/>{nomeUsuario, senha}
    SA->>SA: valida nome (3 a 50, letras, números, . _ -)<br/>e senha (8 a 128)
    SA->>UR: buscarPorNome(nome)
    alt nome já existe
        SA-->>C: 409 nome em uso
    else nome livre
        SA->>H: gerar(senha) → hash PBKDF2
        SA->>AV: sortearPadrao(nome)
        Note right of AV: Avatar.padraoPara: nome termina em "a" → grupo FEMININOS, senão MASCULINOS. Monstros nunca entram.
        AV-->>SA: id do retrato
        SA->>UR: salvar(Usuario, papel USUARIO, avatar)
        Note right of UR: UNIQUE(nome_usuario) fecha a corrida: DuplicateKeyException vira 409
        SA->>SR: criar(SHA-256(token), idUsuario, agora + 12 h)
        SA-->>C: 201 {id, avatar, papel, token}
    end
    Note over C,SR: Login de conta antiga sem avatar: garantirAvatar faz UPDATE ... WHERE avatar IS NULL (atômico). Quem já tem avatar nunca é sorteado de novo.
```

#### 7.3 Responder um exercício

```mermaid
%%{init: {'sequence': {'width': 130, 'actorMargin': 24, 'messageMargin': 26, 'noteMargin': 6, 'boxMargin': 6, 'wrap': true, 'mirrorActors': false}}}%%
sequenceDiagram
    autonumber
    actor C as Cliente
    participant SR as Servico<br/>Resposta
    participant MR as Modulo<br/>Repositorio
    participant EX as Exercicio
    participant UR as Usuario<br/>Repositorio
    participant PR as Progresso<br/>Repositorio
    participant SG as Servico<br/>Gamificacao

    C->>SR: POST /api/modulos/exercicios/{id}/resposta
    SR->>MR: buscarExercicioPorId(id)
    MR-->>SR: Exercicio (só de módulo ativo) ou nulo
    SR->>EX: verificarResposta(resposta)
    Note right of EX: Polimorfismo: ExercicioMultiplaEscolha compara com indiceCorreto. Resposta malformada → 400.
    alt resposta errada
        SR-->>C: 200 {correta: false, pontosGanhos: 0}
    else resposta correta
        rect rgb(224, 242, 254)
            Note over SR,SG: Transação (TransactionTemplate)
            SR->>UR: buscarPorIdParaAtualizar(id) = SELECT ... FOR UPDATE
            SR->>PR: buscarPorUsuario(id)
            alt ainda não concluído por este usuário
                SR->>PR: registrarConclusao(usuário, exercício)
                SR->>SG: processarRespostaCorreta(usuário, exercício)
                Note right of SG: +pontos e +XP. Usuario.adicionarXp sobe de nível enquanto xp ≥ nivel × 100
                SR->>UR: salvar(usuario)
            else já concluído
                Note right of SR: não premia de novo
            end
        end
        SR-->>C: 200 {correta: true, feedback, pontosGanhos, usuario}
    end
```

A resposta é verificada **fora** da transação; a transação só abre quando está correta. Dentro dela, `SELECT … FOR UPDATE` trava a linha do usuário e o registro de conclusão garante que **cada exercício premia uma única vez**, mesmo com dois cliques simultâneos.

#### 7.4 Importação de questões em lote

```mermaid
%%{init: {'sequence': {'width': 130, 'actorMargin': 24, 'messageMargin': 26, 'noteMargin': 6, 'boxMargin': 6, 'wrap': true, 'mirrorActors': false}}}%%
sequenceDiagram
    autonumber
    actor C as Painel admin
    participant AD as Servico<br/>Admin
    participant V as Validador<br/>Conteudo
    participant CR as Conteudo<br/>Repositorio

    C->>AD: POST /api/admin/modulos/{id}/importacao<br/>{modulo?, questoes, confirmar, ignorarDuplicadas}
    AD->>AD: exigirConteudo(ator) → 403 se não for ADMIN ou MASTER
    AD->>CR: buscarModulo(id) → 404 se não existir
    AD->>AD: máx. 500 questões e "modulo" do arquivo deve bater com o destino
    AD->>CR: enunciados(id)
    loop cada questão do arquivo
        AD->>V: validarQuestao(q)
        V-->>AD: questão normalizada ou erro
        AD->>AD: enunciado repetido (módulo ou arquivo)? ignora ou erro
    end
    alt confirmar = false (validar e pré-visualizar)
        AD-->>C: {total, validas, duplicadas, erros, previa, importado: false}
        Note right of C: Nada foi gravado.
    else confirmar = true e sem erros
        rect rgb(224, 242, 254)
            Note over AD,CR: Transação única
            loop cada questão aceita
                AD->>CR: inserirQuestao(id, q)
            end
        end
        AD-->>C: {importado: true, importadas: N}
        Note right of C: Qualquer exceção desfaz tudo.
    else confirmar = true com erros
        AD-->>C: {importado: false, erros} e nada é gravado
    end
```

### 8. Regras de negócio em um só lugar

| Tema | Regra | Onde está |
|---|---|---|
| Pontos | Fácil 10 · Médio 20 · Difícil 30 (padrão; o admin pode definir 1–1000) | `ValidadorConteudo.pontosPadrao` |
| Recompensa | Resposta correta dá **pontos e o mesmo valor em XP**, uma vez por exercício | `ServicoGamificacao`, `ServicoResposta` |
| Nível | Sobe enquanto `xp ≥ nivel × 100` | `Usuario.adicionarXp` |
| Progresso | `concluídos ÷ total` do módulo | `Modulo.calcularPorcentagem` |
| Módulo inativo | Some da Central e não aceita respostas | `ModuloRepositorio` (`ativo = TRUE`) |
| Papéis | `USUARIO` joga · `ADMIN` + gere módulos/questões · `MASTER` + gere papéis. Só o Master altera papéis, só entre `USUARIO` e `ADMIN`; não altera a si mesmo nem o Master | `Papel`, `ServicoAdmin` |
| Avatar padrão | Nome terminando em "a" → retrato feminino sorteado; demais → masculino; **monstros nunca** entram no sorteio. Só na criação da conta ou se ainda não houver avatar | `Avatar.padraoPara`, `ServicoAvatar.garantirAvatar` |
| Imagem própria | JPEG/PNG/WebP até 150 KB, validada no servidor | `Avatar.validarImagem` |
| Tema visual (frontend) | A **categoria** do retrato (Heróis, Aldeões, Monstros) define a paleta de todo o app; imagem própria mantém o tema neutro | `frontend/js/tema.js`, `frontend/css/tema.css` |
| Questão | 2–6 alternativas, 1 correta, enunciado 5–1000 caracteres, visual opcional (imagem ≤ 300 KB ou SVG por lista de permissão) | `ValidadorConteudo`, `Visual` |
| Importação | Um módulo por vez, até 500 questões, tudo-ou-nada, duplicatas ignoradas | `ServicoAdmin.importar` |

### 9. Padrões de projeto (verificados no código)

> Esta seção foi revisada contra os fontes. Só aparece aqui o que **existe de fato**; o que não existe está listado logo abaixo, para ninguém procurar.

**Padrões presentes**

| Padrão | Onde | Para quê |
|---|---|---|
| **Herança + polimorfismo** | `Exercicio` (abstrata, `verificarResposta`) → `ExercicioMultiplaEscolha` | `ServicoResposta` só conhece `Exercicio`; um novo tipo de exercício é uma nova subclasse, sem mexer no serviço |
| **Repository** | `UsuarioRepositorio`, `ModuloRepositorio`, `ProgressoRepositorio`, `SessaoRepositorio`, `ConteudoRepositorio` | Isola o SQL; a regra de negócio não sabe de tabelas |
| **Service Layer** | `Servico*` | Orquestra regras, transações e permissões; controllers ficam finos |
| **DTO** | `record`s do pacote `dto` | Contrato JSON imutável; esconde entidades e gabarito |
| **Static Factory Method** (*Effective Java*, item 1) | `UsuarioResposta.de/comToken`, `ModuloResumoResposta.de`, `ModuloDetalheResposta.de`, `ErroApi.proibido/conflito/…`, `Papel.de` | Nomes que explicam a criação; centraliza a montagem |
| **Agregado** (DDD) | Raiz `Modulo` contém `Exercicio`s; `ModuloRepositorio` carrega o conjunto | Exercícios só são acessados pelo módulo |
| **Injeção de dependência** | Construtores de `@Service`, `@Repository`, `@RestController`, `@Component` | Spring monta o grafo; testes montam à mão (`AmbienteTeste`) |
| **Bloqueio pessimista** | `UsuarioRepositorio.buscarPorIdParaAtualizar` (`SELECT … FOR UPDATE`) | Evita premiar duas vezes em respostas simultâneas |
| **Atualização condicional atômica** | `definirAvatarSeVazio` (`UPDATE … WHERE avatar IS NULL`) | Nunca troca um avatar já escolhido |
| **Enum com comportamento** | `Papel` | As regras de permissão moram junto dos papéis |
| **Classes utilitárias de regras puras** | `Avatar`, `Visual`, `ValidadorConteudo` | Regras sem estado, compartilhadas e testáveis sem Spring |

**Pontos de extensão e padrões do framework que o código usa**

| O quê | Onde | Observação |
|---|---|---|
| **Argument Resolver** (a *Strategy* do Spring MVC para parâmetros) | `ResolvedorUsuarioAutenticado` + `@UsuarioAutenticado` | O controller recebe o `Usuario` já autenticado, sem ler papel ou id da requisição |
| **Controller Advice** | `TratadorErros` | Converte `ErroApi` e erros comuns em JSON com o status certo |
| **Template Method + callback** (do Spring) | `JdbcTemplate` + `RowMapper`; `TransactionTemplate` em `ServicoResposta` e `ServicoAdmin` | O código usa os templates; não implementa o padrão por conta própria |
| **Hook de inicialização** | `UsuarioInicial` (`SmartInitializingSingleton`) | Cria/promove o Master na subida |
| **Singleton do contêiner** | Beans do Spring | É escopo gerenciado pelo Spring, **não** o Singleton do GoF |

**Padrões que NÃO existem no código** (e por isso não aparecem nos diagramas): Singleton (GoF), Strategy (GoF), Facade, Proxy, Chain of Responsibility, Command, Observer, Builder, Decorator, Adapter, State, Abstract Factory e o Factory Method do GoF. Em particular, `Papel` é um enum com regras, **não** o padrão State.

### 10. Frontend (mapa rápido)

| Arquivo | Papel | Organização |
|---|---|---|
| `js/api.js` | Cliente HTTP: injeta o token e traduz erros (`Api.login`, `Api.admModulos`, …) | Objeto único (*gateway* da API) |
| `js/ui.js` · `ui-base.js` | Sessão (`Sessao`), menu, perfil (`Perfil`). `ui-base.js` é a versão enxuta da tela de login | Objetos globais por arquivo |
| `js/tema.js` + `css/tema.css` | Aplica `data-tema="herois\|aldeoes\|monstros"` no `<html>`; paletas em variáveis CSS `--theme-*` | Única lógica de tema; sem `data-tema` = visual neutro |
| `js/arte.js` | Ilustrações SVG dos módulos (`TEMAS`, `arteSvg`), `temaDoModulo`, `visualSrc` | Tabela de temas por chave |
| `js/avatares.js` + `avatares-catalogo.js` | Galeria de retratos e recorte da imagem própria | Módulo por IIFE; catálogo gerado por `tools/gerar_avatares.py` |
| `js/modulos.js` · `exercicio.js` | Central de desafios e tela de exercício | Funções por página |
| `js/lab-sims.js` + `lab.js` | Grimório Vivo: simuladores passo a passo | `lab-sims.js` é lógica pura em `AQSims.SIMS` (tabela `id → simulador`, cada um gera uma lista de passos); `lab.js` só desenha |
| `js/admin.js` | Painel de administração | IIFE; o servidor autoriza cada chamada |

### Como estender

- **Novo tipo de exercício:** subclasse de `Exercicio` com `verificarResposta`, tabela própria como `exercicio_multipla_escolha` e um ramo em `ModuloRepositorio`.
- **Novo tema de módulo:** adicione a chave em `TEMAS`/`CORPOS` (`arte.js`) e em `ValidadorConteudo.TEMAS`.
- **Mudança no banco:** nova migração `V6__….sql` (nunca edite `V1`–`V5`: o Flyway recusa migrações alteradas) e atualize `docs/diagramas/06-modelo-banco.mmd`.
- **Novo retrato:** edite `CATALOGO` em `tools/gerar_avatares.py` e rode de novo (gera assets, catálogo JS e `AvatarIds.java`).

### Mantendo os diagramas

Edite o `.mmd` correspondente em `docs/diagramas`, copie o conteúdo para o bloco ```` ```mermaid ```` deste README e gere as imagens:

```bash
npx @mermaid-js/mermaid-cli -i docs/diagramas/NOME.mmd -o docs/diagramas/NOME.svg
npx @mermaid-js/mermaid-cli -i docs/diagramas/NOME.mmd -o docs/diagramas/NOME.png -s 2 -b white
```

---

## 🛠️ Executando

Pré-requisitos: JDK 17+ e Maven. O passo a passo completo para Windows/PowerShell está em [`docs/GUIA_ALGOQUEST.md`](docs/GUIA_ALGOQUEST.md).

```bash
export ALGOQUEST_USUARIO_INICIAL=seu_usuario
export ALGOQUEST_SENHA_INICIAL='uma-senha-forte-com-8+-caracteres'
mvn spring-boot:run
cd frontend && python3 -m http.server 5500
mvn test
node dev-tools/testes/lab-sims.test.js
```

A API sobe em `http://localhost:8080`; o frontend deve ser servido em uma origem permitida por `ALGOQUEST_CORS_ORIGENS` (padrão: `localhost` e `127.0.0.1` nas portas 5500 e 3000). O usuário inicial é criado como **Master** (único que gerencia papéis); qualquer pessoa também pode criar conta pela tela "Criar conta" (papel `USUARIO`).

### Variáveis de ambiente

Copie `.env.example` para `.env` (nunca versione o `.env` real) e exporte as variáveis antes de iniciar. Nenhuma credencial fica no código.

| Variável | Padrão | Função |
|---|---|---|
| `ALGOQUEST_DB_URL` · `ALGOQUEST_DB_USUARIO` · `ALGOQUEST_DB_SENHA` | H2 em `./data/algoquest` | Conexão com o PostgreSQL. Se `ALGOQUEST_DB_URL` estiver ausente (não defina vazia), usa H2 |
| `ALGOQUEST_USUARIO_INICIAL` · `ALGOQUEST_SENHA_INICIAL` | vazio | Cria (ou promove, se a senha conferir) o Master ao subir |
| `ALGOQUEST_PORTA` | `8080` | Porta da API |
| `ALGOQUEST_CORS_ORIGENS` | `localhost`/`127.0.0.1`, portas 5500 e 3000 | Origens do frontend |
| `ALGOQUEST_SESSAO_HORAS` | `12` | Validade do token |
| `ALGOQUEST_SENHA_ITERACOES` | `600000` | Iterações do PBKDF2 |

### Banco de dados

O esquema e o conteúdo inicial (5 módulos × 10 exercícios) são criados **uma única vez** pelo Flyway (`V1`…`V5` em `src/main/resources/db/migration`). Em um banco que já existe, nada é recriado nem repopulado; o Master inicial só é criado se ainda não existir.

- **H2 (padrão):** não exige configuração.
- **PostgreSQL via Docker:** defina `ALGOQUEST_DB_USUARIO` (padrão `postgres`) e `ALGOQUEST_DB_SENHA` no ambiente ou no `.env` e rode `docker compose up -d`; use os mesmos valores, mais `ALGOQUEST_DB_URL=jdbc:postgresql://localhost:5432/algoquest`, ao iniciar o backend. O volume guarda as credenciais da primeira criação.
- **PostgreSQL instalado:** `psql -U postgres -v senha="$ALGOQUEST_DB_SENHA" -f database/00_criar_banco_postgresql.sql` cria o banco e o usuário `algoquest`.

Para criar ou editar módulos e questões sem mexer no código, use o painel *Administração* (`admin.html`, com importação JSON; modelo em `docs/exemplo-importacao.json`).

## Avatares (retratos RPG + imagem própria)
- 48 retratos (Heróis, Aldeões, Monstros) em `frontend/assets/avatares/{p,g}/*.webp`; o catálogo (`frontend/js/avatares-catalogo.js`) e `AvatarIds.java` são gerados por `tools/gerar_avatares.py` (os PNG originais não vão no projeto por restrição de licença).
- Cadastro: nome terminado em "a" → retrato feminino sorteado; demais → masculino; monstros nunca entram no sorteio. Contas antigas sem avatar recebem um padrão uma única vez no login.
- Troca: `POST /api/auth/me/avatar {avatar}` e `POST /api/auth/me/avatar/personalizado {imagem}` (data URL JPEG/PNG/WebP ≤ 150 KB, validada no servidor).

## Administração (papéis, módulos, questões, importação)
O usuário definido em `ALGOQUEST_USUARIO_INICIAL` é o **Master**. Administradores e Master acessam `admin.html` para criar, editar, excluir e ordenar módulos e questões e importar bancos em JSON (um módulo por vez). Só o Master gerencia papéis.

---

👨‍💻 **Equipe:** Nygel & Antônio
