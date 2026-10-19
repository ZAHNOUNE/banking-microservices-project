# API Gateway — Routage des microservices

Point d'entrée de l'application, avec Spring Cloud Gateway et découverte Consul.

| Route entrante | Service Consul | Port cible |
|---|---|---|
| `/api/customers/**` | `customer-service` | 8081 |
| `/api/accounts/**` | `ebank-service` | 8082 |
| `/api/chat/**` | `ebank-chatbot-service` | 8083 |

Chaque route applique `StripPrefix=1`, ce qui supprime le préfixe `/api` avant de transmettre la requête. Les cibles `lb://` sont résolues via le registre Consul.

## Configuration
- Service sur le port 8888, enregistré auprès de Consul sur 8500.
- `application.yml` fournit les routes et les règles CORS.
- Actuator expose `/actuator/health` et les informations configurées.

## Démarrage
Démarrer Consul, puis depuis ce dossier : `mvn spring-boot:run`.

## Limite de sécurité
La configuration CORS actuelle accepte toutes les origines (`allowedOrigins: "*"`). Cela convient seulement à un environnement pédagogique local et doit être durci avant exposition publique.

Voir [l'architecture globale](../../docs/ARCHITECTURE.md).
