# 📋 Task Board Manager

Sistema de gerenciamento de tarefas inspirado no conceito de Kanban, permitindo a criação e manipulação de boards customizáveis para acompanhamento do fluxo de trabalho.

---

# 🎯 Objetivo

Desenvolver uma aplicação completa para gerenciamento de tarefas utilizando Java e MySQL, aplicando conceitos de:

- Programação Orientada a Objetos
- Camadas de Arquitetura
- Persistência de Dados
- Migrations
- DAO Pattern
- Service Layer
- DTO Pattern
- Tratamento de Exceções
- Clean Code

---

# 🏗 Arquitetura

O sistema segue uma arquitetura em camadas:

```text
UI
│
├── Service Layer
│
├── DTO Layer
│
├── DAO Layer
│
├── Entity Layer
│
└── Database (MySQL)
```

---

# 🛠 Tecnologias

- Java 17+
- Maven
- JDBC
- MySQL
- Flyway
- Lombok (opcional)
- JUnit 5
- Mockito

---

# 📦 Entidades

## Board

Representa um quadro de trabalho.

### Atributos

```java
id
nome
dataCriacao
```

### Regras

- Deve possuir nome.
- Deve possuir pelo menos 3 colunas.
- Deve possuir exatamente:
  - 1 coluna inicial
  - 1 coluna final
  - 1 coluna de cancelamento
- Pode possuir N colunas pendentes.

---

## BoardColumn

Representa uma coluna do board.

### Atributos

```java
id
nome
ordem
tipo
```

### Tipos

```text
INITIAL
PENDING
FINAL
CANCEL
```

---

## Card

Representa uma tarefa.

### Atributos

```java
id
titulo
descricao
dataCriacao
bloqueado
motivoBloqueio
motivoDesbloqueio
```

---

# 📜 Funcionalidades

## Gerenciamento de Boards

Permitir:

- Criar Board
- Selecionar Board
- Listar Boards
- Excluir Board

---

## Gerenciamento de Colunas

Ao criar um board:

- Criar coluna inicial
- Criar coluna final
- Criar coluna de cancelamento
- Criar colunas pendentes customizadas

---

## Gerenciamento de Cards

Permitir:

- Criar Card
- Consultar Card
- Bloquear Card
- Desbloquear Card
- Cancelar Card
- Mover Card

---

# 🔄 Fluxo de Movimentação

O card deve seguir obrigatoriamente a ordem das colunas.

### Exemplo

```text
Backlog
↓
Desenvolvimento
↓
Teste
↓
Homologação
↓
Concluído
```

### Não é permitido

```text
Backlog
↓
Homologação
```

Pulando etapas do fluxo.

### Exceção

A coluna de cancelamento pode ser acessada a partir de qualquer estágio.

---

# 🔒 Bloqueio de Card

Um card bloqueado:

✅ Permanece visível

✅ Mantém histórico

❌ Não pode ser movido

❌ Não pode avançar de coluna

### Ao bloquear

Obrigatório informar:

```text
Motivo do bloqueio
```

### Ao desbloquear

Obrigatório informar:

```text
Motivo do desbloqueio
```

---

# 📊 Consulta do Board

Ao visualizar um board:

```text
Board
├── Coluna Inicial
│   ├── Card 1
│   └── Card 2
│
├── Em Desenvolvimento
│   └── Card 3
│
├── Teste
│
└── Concluído
```

---

# 🗄 Banco de Dados

## Tabelas

```text
boards
board_columns
cards
card_movements
card_block_history
```

---

## Histórico de Movimentações

Registrar:

```text
id
card_id
coluna_origem
coluna_destino
data_movimentacao
```

---

## Histórico de Bloqueios

Registrar:

```text
id
card_id
motivo
data
tipo
```

Onde:

```text
BLOCK
UNBLOCK
```

---

# ✅ Menu Principal

```text
1 - Criar Board
2 - Selecionar Board
3 - Listar Boards
4 - Excluir Board
5 - Sair
```

---

# ✅ Menu do Board

```text
1 - Criar Card
2 - Mover Card
3 - Bloquear Card
4 - Desbloquear Card
5 - Cancelar Card
6 - Visualizar Board
7 - Consultar Card
8 - Voltar
```

---

# ⚠️ Regras de Negócio

- Não permitir board sem nome.
- Não permitir colunas duplicadas.
- Não permitir card sem título.
- Não permitir movimentação inválida.
- Não permitir mover card bloqueado.
- Não permitir avançar diretamente para coluna final.
- Não permitir criar board sem coluna inicial.
- Não permitir mais de uma coluna inicial.
- Não permitir mais de uma coluna final.
- Não permitir mais de uma coluna de cancelamento.

---

# 🧪 Testes

## Unitários

Implementar testes para:

- Services
- DAOs
- Regras de negócio

## Integração

Implementar testes para:

- Banco de dados
- Persistência
- Consultas

---

# 🚀 Critérios de Conclusão

O projeto será considerado concluído quando:

- Todas as entidades estiverem implementadas.
- Persistência estiver funcional.
- Migrations executarem corretamente.
- Regras de negócio forem respeitadas.
- Interface permitir todas as operações.
- Dados permanecerem salvos no MySQL.
- Testes executarem com sucesso.

---

# 📚 Conceitos Aplicados

- SOLID
- Clean Architecture
- DAO Pattern
- DTO Pattern
- Service Layer Pattern
- Repository Pattern
- Tratamento Global de Erros
- Boas práticas de persistência
- Versionamento de banco com Flyway
- Testes unitários e de integração

---

# 👨‍💻 Desafio

Projeto desenvolvido para praticar modelagem de domínio, persistência de dados, arquitetura em camadas e implementação de um sistema de gerenciamento de tarefas baseado em fluxo Kanban.
