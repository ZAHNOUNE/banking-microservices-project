# Customer Service — Gestion des clients

Service REST et serveur d'outils MCP pour gérer les clients.

## Responsabilités
- Modèle JPA `Customer` : `id`, `name`, `email`, `phone`.
- `CustomerRepository` hérite de `JpaRepository<Customer,Long>`.
- `CustomerController` réalise le CRUD REST.
- `CustomerTools` expose deux outils MCP : `getCustomerById`, `getAllCustomers`.
- `CustomerServiceApplication` expose les outils et initialise trois clients de démonstration.

## Routes (port 8081)
| Méthode | Chemin | Résultat |
|---|---|---|
| GET | `/customers` | Liste |
| GET | `/customers/{id}` | Client |
| POST | `/customers` | Création |
| PUT | `/customers/{id}` | Mise à jour |
| DELETE | `/customers/{id}` | Suppression |

La Gateway permet l'accès via `http://localhost:8888/api/customers`.

## Infrastructure
- Spring Boot 3.5.6, Java 21, Spring Data JPA, H2 en mémoire.
- Consul : `customer-service`, port 8500.
- Serveur MCP HTTP : `/mcp` sur le port 8081.
- Contrôle d'état Actuator : `/actuator/health`.

## Lancer
Démarrer Consul depuis `banking-platform/` avec `docker compose up -d`, puis dans ce dossier : `mvn spring-boot:run`.

## Limites
Le stockage H2 est volatil ; aucune authentification/autorisation n'est intégrée. Les ressources absentes génèrent actuellement des exceptions génériques.

Voir [l'architecture générale](../../docs/ARCHITECTURE.md).
