# Banking Dashboard — Angular

Interface Angular et TypeScript permettant de consulter les comptes et les clients à travers la Gateway.

## Organisation
- `src/app/app.ts` : composant racine, onglets et signaux d'état.
- `src/app/app.html` : tableaux Comptes et Clients, messages d'erreur.
- `src/app/account.service.ts` : `GET http://localhost:8888/api/accounts`.
- `src/app/customer.service.ts` : `GET http://localhost:8888/api/customers`.
- `src/app/app.config.ts` : configuration du client HTTP Angular.
- `src/app/app.css`, `src/styles.css` : styles de l'application.

Les comptes affichent l'identifiant, le RIB, le solde et l'identifiant du client. Les clients affichent l'identifiant, le nom, l'adresse électronique et le téléphone.

## Installation et exécution
```bash
npm ci
npm start
```
Accès local : `http://localhost:4200`, avec Gateway et les services métier actifs.

## Compilation
```bash
npm run build
```
Le build a réussi dans GitHub Actions. Les tests UI Angular ne sont pas configurés dans `angular.json`.

## Périmètre
Les pages sont en lecture seule. Le fichier `../chatbot.html` est une page autonome et n'est pas installé comme asset du build Angular ; le lien relatif figurant dans l'en-tête doit être adapté pour ouvrir effectivement cet assistant.

Voir [l'architecture globale](../../docs/ARCHITECTURE.md).
