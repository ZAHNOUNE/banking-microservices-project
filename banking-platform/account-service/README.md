# Account Service — Comptes et transactions

Service REST bancaire, gestion des comptes et historiques de transactions, serveur MCP et client OpenFeign.

## Modèles et persistance
- `Account` : `id`, `rib`, `balance` (`double`), `customerId`, `transactions`.
- `Transaction` : `id`, `amount`, `date`, `type`, `account`.
- `TransactionType` : `DEPOT`, `RETRAIT`.
- `AccountRepository` : CRUD JPA et `findByCustomerId`.
- `TransactionRepository` : CRUD JPA et `findByAccountId`.
- `CustomerDTO` et `AccountWithCustomerDTO` : représentation du client et vue enrichie.
- `CustomerRestClient` : Feign vers `customer-service`.

Les requêtes portant sur un compte inexistant répondent HTTP 404 pour les routes de lecture et de détail.

## Routes REST (port 8082)
| Méthode | Chemin | Action |
|---|---|---|
| GET | `/accounts` | Liste des comptes |
| GET | `/accounts/{id}` | Compte |
| POST | `/accounts` | Création |
| GET | `/accounts/customer/{customerId}` | Comptes d'un client |
| GET | `/accounts/{id}/details` | Compte enrichi par Feign |
| GET | `/accounts/{id}/transactions` | Historique |
| POST | `/accounts/{id}/depot?amount=100` | Dépôt |
| POST | `/accounts/{id}/retrait?amount=50` | Retrait |

À travers la Gateway, préfixer par `http://localhost:8888/api`.

## Service métier transactionnel

`BankingTransactionService` centralise les dépôts et retraits utilisés par les API REST et les outils MCP. Les opérations exécutent la mise à jour du solde et l'enregistrement de la transaction dans une transaction Spring `@Transactional`. Les erreurs comprennent : montant invalide (HTTP 400), compte absent (404) et solde insuffisant (409). Cette implémentation reste pédagogique : montants en `double`, H2 en mémoire, absence d'authentification et de protection métier contre les accès concurrents.

## Outils MCP
`getAccountById`, `getAccountsByCustomerId`, `getTransactionsByAccountId`, `deposit` et `withdraw` ; transport MCP HTTP via `/mcp`. Les montants sont vérifiés comme strictement positifs et finis. Un retrait échoue en cas de solde insuffisant.

## Tests
- `AccountControllerTest` : 14 tests MockMvc/Mockito.
- `AccountToolsTest` : 15 tests JUnit/Mockito.
- Dans le workflow GitHub Actions contrôlé le 19 octobre 2026 : 44 tests exécutés, 0 échec, 0 erreur, 0 test ignoré.
- Commande locale : `mvn test`.

## Configuration et lancement
Spring Boot 3.5.6, Java 21, JPA/H2 mémoire, Consul `ebank-service`, OpenFeign et Spring AI MCP. Depuis ce répertoire : `mvn spring-boot:run` (Consul doit être démarré).

## Précautions
Stockage volatile ; pas de gestion monétaire robuste (`double`) ni d'authentification. Le contrôleur REST et les outils MCP implémentent chacun le mouvement du solde ; la modification du solde et la création de transaction ne forment pas une unité transactionnelle garantie.

Voir [l'architecture](../../docs/ARCHITECTURE.md).
