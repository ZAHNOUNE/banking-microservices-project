# Banking Microservices — Projet d'évaluation

**Dossier d'évaluation présenté par : Fatimaezzahra Zahnoune**

Projet pédagogique d'architecture bancaire distribuée utilisant **Spring Boot**, **Spring Cloud**, **Consul**, **OpenFeign**, **Spring AI / MCP** et **Angular**.

## Accéder à l'application

Le code se trouve dans [`banking-platform/`](./banking-platform/).

- [Documentation technique détaillée](./banking-platform/README.md)
- [Gestion des clients](./banking-platform/customer-service/)
- [Comptes et transactions](./banking-platform/account-service/)
- [API Gateway](./banking-platform/api-gateway/)
- [Agent IA et MCP](./banking-platform/ai-assistant-service/)
- [Interface Angular](./banking-platform/banking-dashboard/)

## Fonctionnalités

- Gestion des clients, comptes et opérations bancaires de démonstration
- Découverte des services avec Consul et communication interservices via OpenFeign
- Routage avec Spring Cloud Gateway
- Outils métier MCP exploitables par un agent Spring AI
- Consultation des comptes et clients dans une interface Angular
- Contrôle des montants des dépôts et retraits sur les routes REST et les outils MCP

## Démarrage rapide

Depuis le dossier `banking-platform`, lancer Consul :

```powershell
docker compose up -d
```

Démarrer les modules Spring Boot dans des terminaux distincts avec `mvn spring-boot:run` depuis leurs dossiers respectifs. Le chatbot requiert une variable d'environnement `OPENAI_API_KEY`. Toutes les instructions et les ports sont détaillés dans la [documentation technique](./banking-platform/README.md).

## Références

- [Tutoriel microservices — partie 1](https://www.youtube.com/watch?v=cdFz3VemT1Q)
- [Tutoriel microservices — partie 2](https://www.youtube.com/watch?v=JHvxSZTWSsY)
- [Dépôt public consulté comme point de départ](https://github.com/beks36/ebank-microservices-mcp)

