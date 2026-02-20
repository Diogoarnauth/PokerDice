# 🎲 Poker Dice

**Aplicação Web Multiplayer do jogo Poker Dice**

Poker Dice é uma aplicação web multiplayer que permite a criação e gestão de lobbies onde vários jogadores podem participar em partidas do jogo Poker Dice em tempo real.

O projeto foi desenvolvido com foco em:

* Estruturação modular
* Separação clara de responsabilidades
* Pipeline bem definida de processamento de pedidos
* Gestão segura de autenticação e sessões
* Comunicação reativa entre backend e frontend
* Organização transacional consistente da lógica de negócio

---

# 🏗️ Arquitetura Geral

A aplicação encontra-se dividida em múltiplos containers Docker:

* **Nginx** – Reverse proxy
* **Backend API** – Kotlin + Spring
* **Frontend** – React + TypeScript

O **Nginx** atua como ponto de entrada único, fazendo reverse proxy para os containers internos, permitindo:

* Separação clara de responsabilidades
* Isolamento de serviços
* Escalabilidade futura
* Configuração centralizada de routing

---

# 🖥️ Frontend

## ⚛️ React + TypeScript

O frontend foi desenvolvido com:

* **React** para construção da interface baseada em componentes
* **TypeScript** para tipagem estática e maior robustez
* Gestão de estado baseada no estado do jogo no backend

---

## ⚡ Vite

Utilizámos **Vite** como ferramenta de desenvolvimento e bundling.

O Vite foi essencial para:

* Bundling otimizado da aplicação
* Transpiling de TypeScript
* Hot Module Replacement (HMR)
* Build otimizada para produção
* Servidor de desenvolvimento extremamente rápido

Vantagens práticas no projeto:

* Redução significativa no tempo de reload
* Pipeline simplificada de build
* Melhor organização modular do frontend

---

# 🧠 Backend

## 🔹 Kotlin + Spring

O backend foi desenvolvido com:

* **Kotlin**
* **Spring Boot**
* Arquitetura baseada em separação de camadas
* Organização por domínio, repositórios, serviços e controladores

---

# 🔄 Pipeline de Processamento de Pedidos

A pipeline definida para cada pedido HTTP segue a seguinte ordem:

1. Servidor HTTP (container)
2. `HttpServlet`
3. Filtro HTTP global (executado antes do Spring)
4. Interceptors do Spring
5. Controllers
6. Services
7. Repositórios

Esta abordagem permitiu:

* Separar autenticação da lógica de negócio
* Validar campos antes de chegar aos serviços
* Centralizar verificação de permissões
* Garantir consistência no tratamento de erros

---

# 🔐 Autenticação e Gestão de Sessão

## 🍪 Sistema de Cookies

Foi implementado um sistema próprio de autenticação baseado em:

* Cookies configurados manualmente
* Tokens armazenados na base de dados
* Expiração automática após 24 horas
* Sistema de reposição e invalidação de tokens

### Processo:

1. Login → geração de token
2. Token armazenado na base de dados
3. Hash do token guardado
4. Cookie enviado ao browser
5. Interceptor valida token em cada pedido

---

## 🔑 Cookies vs Tokens (Vantagens da abordagem usada)

### Cookies (usados no projeto)

* Envio automático pelo browser
* Maior integração com mecanismos HTTP
* Possibilidade de uso das flags `HttpOnly` e `Secure`
* Melhor controlo em contexto web tradicional

No projeto, a combinação de cookies + tokens persistidos permitiu:

* Controlo total da sessão
* Revogação manual de tokens
* Expiração garantida
* Maior controlo de segurança

---

# 🔐 Encriptação e Segurança

## Hashing com SHA-256

A encriptação de tokens foi feita com SHA-256:

### Estratégia adotada:

* Nunca guardar tokens em claro
* Guardar apenas hash
* Comparar hash do token enviado com o hash armazenado
* Passwords tratadas da mesma forma

Isto garante:

* Redução de impacto em caso de fuga de dados
* Impossibilidade de reconstrução direta do token original

---

# 🗄️ Persistência de Dados

## JDBI + Transaction Manager

A camada de persistência foi implementada com **JDBI**.

### Handle

O `Handle` representa uma ligação ativa à base de dados dentro de uma transação.

Cada repositório recebe o mesmo `Handle`, garantindo:

* Consistência transacional
* Operações atómicas
* Isolamento de contexto

---

## Transaction Manager

```kotlin
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {

    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            val transaction = JdbiTransaction(handle)
            block(transaction)
        }
}
```

### Vantagens:

* Todas as operações dentro do `run {}` executam na mesma transação
* Commit automático se não houver erro
* Rollback automático em caso de exceção
* Serviços mantêm-se independentes da tecnologia de persistência

---

## Organização por Repositórios

Cada entidade possui o seu repositório:

* UsersRepository
* LobbiesRepository
* GamesRepository
* RoundRepository
* TurnsRepository
* InviteRepository
Isto demonstra:

* Separação clara de queries
* Encapsulamento de acesso a dados
* Código SQL centralizado

---

# 💼 Lógica de Negócio Transacional

Exemplo: criação de lobby.

Durante `createLobby`:

* Verificação de existência do host
* Verificação de créditos
* Validação de parâmetros
* Verificação de estado do utilizador
* Criação do lobby
* Atualização do utilizador
* Emissão de evento SSE

Tudo dentro de:

```kotlin
transactionManager.run { ... }
```

Garantindo atomicidade total da operação.

---

# 📡 Comunicação em Tempo Real

## SSE (Server-Sent Events)

Foram utilizados:

* Event Emitters
* SSEEmitters

Para notificar o frontend de alterações no backend.

### Exemplo prático:

Quando um lobby é criado:

```kotlin
eventService.sendToAll(
    PokerEvent.LobbiesListChanges(...)
)
```

O frontend recebe o evento e:

* Atualiza automaticamente a lista de lobbies
* Re-renderiza componentes
* Sincroniza estado visual com estado real

Vantagens:

* Atualização em tempo real
* Redução de polling
* Melhor experiência de utilizador
* UI reativa ao estado da aplicação

---

# 📦 Estrutura de Resposta de Erros

Todas as respostas de erro seguem estrutura consistente:

```json
{
  "status": 400,
  "title": "InvalidSettings",
  "detail": "Lobby configuration is invalid",
  "link": "https://github.com/.../docs/errors#InvalidSettings"
}
```

Isto permite:

* Padronização
* Facilidade de debugging
* Documentação associada a cada erro
* Melhor integração frontend/backend

---

# 🧩 Técnica: Application Problem + JSON

O backend segue uma abordagem baseada em:

* Representação clara de problemas de domínio
* Serialização estruturada para JSON
* Separação entre erros técnicos e erros de negócio
* Mapeamento explícito de resultados (`success` / `failure`)

Isto melhora:

* Legibilidade
* Manutenção
* Testabilidade
* Consistência da API

---

# 🎯 Principais Decisões Técnicas

* Arquitetura modular com containers
* Reverse proxy com Nginx
* Pipeline HTTP estruturada
* Autenticação com cookies + tokens persistidos
* Hashing com SHA-256
* Persistência com JDBI
* Transaction Manager customizado
* SSE para comunicação reativa
* Estrutura padronizada de erros
* Separação rigorosa de lógica de negócio
