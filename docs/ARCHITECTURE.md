# Architecture technique — Banking Microservices

**Projet d'évaluation : Fatimaezzahra Zahnoune**

Ce dossier décrit l'application pédagogique structurée autour de Spring Boot, Spring Cloud, Spring AI, MCP et Angular. Il accompagne les tutoriels utilisés en cours :

- [Partie 1](https://www.youtube.com/watch?v=cdFz3VemT1Q)
- [Partie 2](https://www.youtube.com/watch?v=JHvxSZTWSsY)

## 1. Vue d'ensemble

Le dossier `banking-platform/` contient les modules suivants :

| Module (dossier) | Service Spring / rôle | Port |
| --- | --- | ---: |
| `customer-service` | `customer-service` : clients et serveur MCP | 8081 |
| `account-service` | `ebank-service` : comptes, transactions, serveur MCP | 8082 |
| `ai-assistant-service` | `ebank-chatbot-service` : agent Spring AI et client MCP | 8083 |
| `api-gateway` | `gateway-service` : point d'entrée API | 8888 |
| `banking-dashboard` | Interface Angular | 4200 |
| `docker-compose.yml` | Registre Consul | 8500 |

**Important :** les noms des dossiers sont distincts des noms `spring.application.name` enregistrés dans Consul. Les routes, les ports et les identifiants Spring restent ceux configurés dans `application.yml`.

### Diagramme de composants

```mermaid
flowchart TB
  user["Utilisateur"] --> ui["Dashboard Angular :4200"]
  user --> html["Interface chatbot HTML"]
  ui --> gw["API Gateway :8888"]
  html --> chat["Spring AI Chatbot :8083"]
  gw --> customer["Customer Service :8081"]
  gw --> bank["Account Service :8082"]
  gw --> chat
  bank -->|"OpenFeign"| customer
  chat -->|"MCP HTTP"| customer
  chat -->|"MCP HTTP"| bank
  chat --> openai["API OpenAI"]
  customer --> cdb[("H2 clients")]
  bank --> adb[("H2 comptes + transactions")]
  consul["Consul :8500"] -.-> customer
  consul -.-> bank
  consul -.-> chat
  consul -.-> gw
```

### Principes appliqués

- **Découverte des services** : les services Spring s'enregistrent auprès de Consul.
- **Gateway** : les appels `/api/customers/**`, `/api/accounts/**` et `/api/chat/**` sont redirigés vers les services correspondants.
- **OpenFeign** : le service bancaire consulte les informations d'un client pour enrichir la réponse d'un compte.
- **Persistance** : JPA et H2 en mémoire, une base par service métier.
- **MCP** : les modules clients et bancaires exposent des outils consultables par l'assistant IA.
- **Spring AI** : le client MCP fournit ces outils à un `ChatClient` OpenAI.
- **Angular** : consultation des listes de comptes et de clients via la Gateway.


## Cas d'utilisation

Les acteurs principaux sont l'utilisateur du dashboard, le client API et l'utilisateur de l'assistant conversationnel. Les cas ci-dessous représentent uniquement les opérations présentes dans le code.

```mermaid
flowchart LR
    U["Utilisateur dashboard"] --> A["Consulter les comptes"]
    U --> B["Consulter les clients"]
    API["Client API REST"] --> C["Créer et gérer les clients"]
    API --> D["Créer un compte"]
    API --> E["Effectuer un dépôt ou retrait"]
    API --> F["Consulter les transactions"]
    API --> G["Consulter compte et client associés"]
    CHAT["Utilisateur assistant"] --> H["Poser une question en langage naturel"]
    H --> I["Interroger outils MCP clients"]
    H --> J["Interroger outils MCP bancaires"]
```

## 2. Modèle métier — diagramme de classes

Les deux bases H2 sont indépendantes : `Account.customerId` est un identifiant métier, pas une clé étrangère JPA vers la base des clients.

```mermaid
classDiagram
  direction LR
  class Customer {
    +Long id
    +String name
    +String email
    +String phone
  }
  class Account {
    +Long id
    +String rib
    +double balance
    +Long customerId
  }
  class Transaction {
    +Long id
    +double amount
    +Date date
    +TransactionType type
  }
  class TransactionType {
    <<enumeration>>
    DEPOT
    RETRAIT
  }
  class CustomerController {
    +getAllCustomers()
    +getCustomerById(id)
    +createCustomer(customer)
    +updateCustomer(id,customer)
    +deleteCustomer(id)
  }
  class AccountController {
    +getAllAccounts()
    +getAccountById(id)
    +getAccountWithCustomer(id)
    +getTransactions(id)
    +deposit(id,amount)
    +withdraw(id,amount)
  }
  class CustomerTools {
    +getCustomerById(customerId)
    +getAllCustomers()
  }
  class AccountTools {
    +getAccountById(accountId)
    +getAccountsByCustomerId(customerId)
    +getTransactionsByAccountId(accountId)
    +deposit(accountId,amount)
    +withdraw(accountId,amount)
  }
  Customer .. Account : reference par customerId
  Account "1" --> "0..*" Transaction : transactions
  Transaction --> TransactionType
  CustomerController ..> Customer
  AccountController ..> Account
  AccountController ..> Transaction
  CustomerTools ..> Customer
  AccountTools ..> Account
```

## 3. Séquences des scénarios essentiels

### 3.1 Consulter un compte enrichi avec son client

```mermaid
sequenceDiagram
    actor User as Utilisateur
    participant UI as Client HTTP / Angular
    participant GW as Gateway :8888
    participant BANK as Service bancaire :8082
    participant DB as H2 comptes
    participant FEIGN as OpenFeign
    participant CONSUL as Consul :8500
    participant CUSTOMER as Service clients :8081
    User->>UI: Consulter compte 1
    UI->>GW: GET /api/accounts/1/details
    GW->>CONSUL: Résolution ebank-service
    GW->>BANK: GET /accounts/1/details
    BANK->>DB: findById(1)
    DB-->>BANK: Compte (customerId)
    BANK->>FEIGN: getCustomerById(customerId)
    FEIGN->>CONSUL: Résolution customer-service
    FEIGN->>CUSTOMER: GET /customers/{id}
    CUSTOMER-->>FEIGN: Client
    FEIGN-->>BANK: CustomerDTO
    BANK-->>GW: AccountWithCustomerDTO
    GW-->>UI: JSON
    UI-->>User: Informations du compte et du client
```

### 3.2 Question bancaire en langage naturel

```mermaid
sequenceDiagram
    actor User as Utilisateur
    participant Web as Interface chatbot
    participant AI as Agent Spring AI :8083
    participant LLM as OpenAI
    participant MCP as Serveur MCP bancaire :8082
    participant DB as H2 comptes
    User->>Web: Quel est le solde du compte 1 ?
    Web->>AI: POST /chat/ask
    AI->>LLM: Demande + définitions des outils
    LLM-->>AI: Appeler getAccountById(1)
    AI->>MCP: Appel outil MCP
    MCP->>DB: findById(1)
    DB-->>MCP: Compte
    MCP-->>AI: Résultat de l'outil
    AI->>LLM: Contexte métier
    LLM-->>AI: Réponse en français
    AI-->>Web: Réponse JSON
    Web-->>User: Solde du compte
```

### 3.3 Dépôt ou retrait via l'API REST

```mermaid
sequenceDiagram
    actor User as Client API
    participant BANK as AccountController
    participant A as AccountRepository
    participant T as TransactionRepository
    User->>BANK: POST /accounts/1/depot?amount=100
    BANK->>BANK: Vérifier montant positif et fini
    alt Montant invalide
        BANK-->>User: HTTP 400
    else Montant valide
        BANK->>A: findById(1)
        A-->>BANK: Account
        BANK->>A: save(balance + 100)
        BANK->>T: save(Transaction)
        T-->>BANK: Transaction
        BANK-->>User: JSON de la transaction
    end
```

**Limite métier :** la sauvegarde du solde et de la transaction devrait être regroupée dans une transaction applicative atomique pour un usage réel. Les exemples restent pédagogiques.

## 4. Interfaces et API

La Gateway expose :
- `/api/customers/**` pour le service clients ;
- `/api/accounts/**` pour les comptes et transactions ;
- `/api/chat/**` pour le chatbot.

Les services exposent aussi leurs endpoints natifs aux ports 8081, 8082 et 8083. Leurs configurations MCP utilisent `/mcp` sur les services 8081 et 8082. Le module Angular appelle la Gateway sur `http://localhost:8888` et `chatbot.html` appelle directement `http://localhost:8083/chat/ask`.

Les routes existantes ont été préservées pour maintenir la compatibilité entre le frontend, Gateway, Consul et Spring AI.

## 5. Exploitation et sécurité

La clé OpenAI n'est pas stockée dans le code : `application.yml` contient `${OPENAI_API_KEY}`. Elle doit être définie localement dans l'environnement d'exécution.

Le dépôt n'a pas besoin de fichier `.env` suivi par Git. Le fichier `.gitignore` exclut les fichiers d'environnement, certificats et clés privées usuelles.

Cette application n'est **pas** un système bancaire de production : elle n'inclut pas l'authentification et l'autorisation nécessaires aux opérations financières. Les outils MCP de dépôt et retrait ne doivent pas être accessibles publiquement sans contrôle d'accès.
