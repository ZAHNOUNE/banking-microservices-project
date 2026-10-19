# Guide technique de maintenance — Banking Microservices

## Objet et périmètre
Application pédagogique bancaire en microservices Java, accompagnée d'un dashboard Angular et d'un assistant conversationnel utilisant Spring AI et MCP. Ce fichier décrit le dépôt et les conventions à respecter pendant les évolutions ; il n'est pas un historique de développement.

## Cartographie actuelle
- `banking-platform/customer-service/` : CRUD des clients, JPA/H2, REST, outils MCP.
- `banking-platform/account-service/` : comptes, opérations de dépôt et retrait, transactions, OpenFeign vers les clients, outils MCP, tests.
- `banking-platform/api-gateway/` : point d'entrée Spring Cloud Gateway et routage via Consul.
- `banking-platform/ai-assistant-service/` : API conversationnelle, Spring AI, OpenAI et client MCP.
- `banking-platform/banking-dashboard/` : UI Angular et services HTTP de consultation.
- `banking-platform/chatbot.html` : client HTML autonome de l'assistant.
- `banking-platform/docker-compose.yml` : Consul en mode développement.
- `docs/ARCHITECTURE.md` : architecture, modèle et diagrammes.
- `README.md` : présentation globale et procédure d'installation.
- `banking-platform/README.md` : référence technique détaillée.

## Flux essentiels
1. Angular -> Gateway `:8888` -> API Clients `:8081` ou Comptes `:8082`.
2. La consultation `/api/accounts/{id}/details` appelle `customer-service` via Feign et Consul.
3. Le client HTML appelle `POST http://localhost:8083/chat/ask` ; Spring AI peut interroger les outils MCP sur `:8081/mcp` et `:8082/mcp`.
4. Les comptes et les clients possèdent chacun une base H2 en mémoire. `Account.customerId` n'est pas une relation JPA interbases.

## Versions et prérequis
- Java 21, Maven 3.9+ ; Spring Boot 3.5.6, Spring Cloud 2025.0.0, Spring AI 1.1.8.
- Angular 22, TypeScript 6, Node.js compatible Angular 22, npm.
- Docker et Docker Compose pour Consul (port 8500).
- `OPENAI_API_KEY` dans l'environnement local pour le service conversationnel.

## Identifiants à ne pas confondre
Les répertoires portent les noms métier mentionnés ci-dessus. Les valeurs `spring.application.name` restent celles inscrites dans les fichiers de configuration : `customer-service`, `ebank-service`, `gateway-service`, `ebank-chatbot-service`. La Gateway et Feign utilisent ces identifiants. Ne pas les modifier sans ajuster toutes les références.

## Lancement local
Depuis `banking-platform` : `docker compose up -d`.
Dans des terminaux distincts :
- `cd customer-service && mvn spring-boot:run`
- `cd account-service && mvn spring-boot:run`
- `cd api-gateway && mvn spring-boot:run`
- `cd ai-assistant-service && mvn spring-boot:run` (clé OpenAI requise)
- `cd banking-dashboard && npm ci && npm start`

## Contrat de validation
- Les informations de client sont validées par Bean Validation (nom et email non vides ; format email).
- Les ressources absentes répondent 404 pour les routes clients et les lectures de comptes.
- Les requêtes sans message sont rejetées avant l'appel au modèle conversationnel.
- Ajouter les tests de non-régression dans les modules concernés avant publication.

## Validation et intégration continue
- Quatre jobs Java du workflow `.github/workflows/verification.yml` exécutent `mvn -B -ntp test`.
- Un job Angular effectue `npm ci` puis `npm run build`.
- `account-service` contient `AccountControllerTest` et `AccountToolsTest` (JUnit 5, Mockito et MockMvc) pour opérations valides, montants invalides, retrait avec solde insuffisant.
- Ne pas affirmer qu'un service a été vérifié de bout en bout sur la seule base d'une compilation réussie.

## Règles de modification
1. Modifier les composants métier et mettre à jour tests, README et diagrammes ensemble.
2. Maintenir compatibles les routes `/api/customers/**`, `/api/accounts/**`, `/api/chat/**` et les appels Angular.
3. Vérifier les propriétés de Consul, les ports et les URLs des connexions MCP en cas de changement.
4. Ne pas stocker de clé, `.env`, jeton, fichier `.key`, `.pem` ou mot de passe dans Git.
5. Respecter les licences applicables aux dépendances et bibliothèques utilisées.
6. Ne pas présenter les flux bancaires comme prêts pour la production.

## Risques connus
- Absence de mécanisme d'authentification et d'autorisation pour REST et MCP.
- Solde monétaire en `double`, stockage H2 volatil et pas de garantie d'atomicité de l'écriture solde + transaction.
- Dépôts et retraits centralisés dans `BankingTransactionService`, annoté `@Transactional` ; absence de contrôle explicite des écritures concurrentes.
- Gestion d'erreur hétérogène (exceptions génériques).
- Gateway avec CORS permissif ; ports en dur pour l'environnement local.
- Chatbot HTML autonome, non livré dans le build Angular.
- Le champ `conversationId` du DTO de requête chatbot n'est pas exploité dans le contrôleur.
- Aucun bot Telegram implémenté dans les fichiers analysés.

## Documents complémentaires
- [README principal](README.md)
- [Documentation technique de la plateforme](banking-platform/README.md)
- [Diagrammes et flux](docs/ARCHITECTURE.md)
- Guides de module : fichiers `README.md` dans chaque dossier sous `banking-platform/`.
