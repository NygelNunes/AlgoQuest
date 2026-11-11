# AlgoQuest — Arquitetura

A documentação de arquitetura (camadas, **diagramas de classes**, banco de dados, fluxos, regras de negócio e **padrões de projeto**) fica no **[README.md → seção "Arquitetura e Modelagem"](../README.md)**, para existir em um único lugar e não divergir. Este arquivo é só o índice dos diagramas e das convenções.

## Diagramas

Fonte única: [`docs/diagramas/*.mmd`](diagramas) (Mermaid). Cada um também existe como `.svg` e `.png` na mesma pasta.

| # | Diagrama | Tipo |
|---|---|---|
| 01 | [Visão geral por camadas](diagramas/01-visao-geral-camadas.svg) | fluxo |
| 02 | [Domínio (`model`)](diagramas/02-classes-dominio.svg) | classes |
| 03 | [Regras puras: Avatar, Visual, ValidadorConteudo](diagramas/03-classes-regras-puras.svg) | classes |
| 04a | [Autenticação e avatar](diagramas/04a-classes-aplicacao-autenticacao.svg) | classes |
| 04b | [Aprendizado: módulos, resposta, gamificação](diagramas/04b-classes-aplicacao-aprendizado.svg) | classes |
| 04c | [Administração](diagramas/04c-classes-aplicacao-administracao.svg) | classes |
| 05a | [Repositórios de usuário e módulo](diagramas/05a-classes-persistencia-repositorio.svg) | classes |
| 05b | [Repositórios específicos](diagramas/05b-classes-persistencia-especificos.svg) | classes |
| 06 | [Modelo do banco](diagramas/06-modelo-banco.svg) | entidade-relacionamento |
| 07a | [DTOs de resposta](diagramas/07a-classes-contrato-respostas.svg) | classes |
| 07b | [DTOs de requisição](diagramas/07b-classes-contrato-requisicoes.svg) | classes |
| 08a | [Infraestrutura web (token e CORS)](diagramas/08a-classes-infraestrutura-web.svg) | classes |
| 08b | [Erros, segurança e utilitários](diagramas/08b-classes-infraestrutura-erros-seguranca.svg) | classes |
| 09 | [Autenticação e permissões por papel](diagramas/09-sequencia-autenticacao-papeis.svg) | sequência |
| 10 | [Cadastro e avatar padrão](diagramas/10-sequencia-registro-avatar.svg) | sequência |
| 11 | [Responder um exercício](diagramas/11-sequencia-responder-exercicio.svg) | sequência |
| 12 | [Importação de questões em lote](diagramas/12-sequencia-importacao-lote.svg) | sequência |

Para regenerar as imagens, veja "Mantendo os diagramas" no README.

## Convenções

- **Nomes:** pacotes e `*Controller` em inglês; classes de negócio em português (`Servico*`, `*Repositorio`, `*Resposta`, `*Requisicao`). Consistente dentro de cada camada.
- **SQL:** sempre parametrizado (`?`); transações programáticas com `TransactionTemplate` nos serviços (`ServicoResposta`, `ServicoAdmin`), nunca no repositório.
- **Permissões:** sempre no servidor (`ServicoAdmin.exigirConteudo/exigirMaster`); o papel é lido do banco a cada requisição.
- **Migrações:** `src/main/resources/db/migration` (`V1`…`V5`). Mudanças novas = arquivo novo; nunca edite uma migração já aplicada.
