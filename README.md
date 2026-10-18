# Banking Microservices — Projet d'évaluation

**Présentation et adaptations pour l'évaluation : Fatimaezzahra Zahnoune**

Projet pédagogique d'architecture bancaire distribuée utilisant **Spring Boot**, **Spring Cloud**, **Consul**, **OpenFeign**, **Spring AI / MCP** et **Angular**.

## Accéder à l'application

Le code se trouve dans [`banking-microservices-app/`](./banking-microservices-app/).

- [Documentation technique détaillée](./banking-microservices-app/README.md)
- [Gestion des clients](./banking-microservices-app/customer-service/)
- [Comptes et transactions](./banking-microservices-app/ebank-service/)
- [API Gateway](./banking-microservices-app/gateway-service/)
- [Agent IA et MCP](./banking-microservices-app/ebank-chatbot-service/)
- [Interface Angular](./banking-microservices-app/ebank-dashboard/)

## Fonctionnalités

- Gestion des clients, comptes et opérations bancaires de démonstration
- Découverte des services avec Consul et communication interservices via OpenFeign
- Routage avec Spring Cloud Gateway
- Outils métier MCP exploitables par un agent Spring AI
- Consultation des comptes et clients dans une interface Angular
- Validation des montants des dépôts et retraits, avec tests JUnit

## Démarrage rapide

Depuis le dossier `banking-microservices-app`, lancer Consul :

```powershell
docker compose up -d
```

Démarrer les modules Spring Boot dans des terminaux distincts avec `mvn spring-boot:run` depuis leurs dossiers respectifs. Le chatbot requiert une variable d'environnement `OPENAI_API_KEY`. Toutes les instructions et les ports sont détaillés dans la [documentation technique](./banking-microservices-app/README.md).

**État de validation :** les fichiers ont été inspectés ; les suites de tests et l'exécution complète des microservices restent à confirmer.

## Références pédagogiques et provenance

- [Tutoriel microservices — partie 1](https://www.youtube.com/watch?v=cdFz3VemT1Q)
- [Tutoriel microservices — partie 2](https://www.youtube.com/watch?v=JHvxSZTWSsY)
- [Dépôt public consulté comme point de départ](https://github.com/beks36/ebank-microservices-mcp)

Cette présentation distingue la base pédagogique des adaptations apportées dans ce dépôt. Le dépôt de référence ne comporte pas de licence standard identifiée ; vérifier les permissions de réutilisation applicables.
