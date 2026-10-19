# AI Assistant Service — Spring AI et MCP

Service conversationnel HTTP utilisant Spring AI, OpenAI et deux serveurs MCP.

## Fonctionnement
1. `ChatController` reçoit `POST /chat/ask` avec un JSON contenant `message`.
2. `ChatbotConfig` crée un `ChatClient` avec le prompt système et les callbacks MCP.
3. La configuration du client MCP cible les serveurs locaux `http://localhost:8081` et `http://localhost:8082`.
4. Le modèle OpenAI configuré est `gpt-4o-mini`, température `0.3`.
5. La réponse est rendue dans `ChatResponse.reply`.

Les messages vides ou absents sont refusés avec HTTP 400 avant tout appel au modèle.

## Types et code
- `ChatRequest(String message, String conversationId)` : requête entrante, le champ `conversationId` n'est pas actuellement exploité par le contrôleur.
- `ChatResponse(String reply)` : résultat JSON.
- `ChatController` : endpoint `/chat/ask`.
- `ChatbotConfig` : construction `ChatClient` et exposition des outils.
- `EbankChatbotServiceApplication` : entrée du service Spring Boot.

## Prérequis
Java 21, Maven, Consul et les deux serveurs MCP lancés. Définir localement `OPENAI_API_KEY` : ne jamais placer sa valeur dans Git.

## Démarrage
Dans ce dossier : `mvn spring-boot:run`.

## Exemple
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8083/chat/ask" -ContentType "application/json" -Body '{"message":"Quel est le solde du compte 1 ?"}'
```

## Limites
Les outils MCP peuvent modifier des soldes sans authentification métier. Aucune intégration Telegram n'est implémentée dans ce dépôt. L'interface HTML autonome du chatbot est dans `banking-platform/chatbot.html`.

Voir [l'architecture](../../docs/ARCHITECTURE.md).
