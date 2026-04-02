# Mousmous Video

Plateforme microservices pour la gestion d'utilisateurs, de cles, de transactions video, de notifications et de traces d'audit.

## Stack technique

- Backend: Spring Boot 4, Spring Cloud Gateway, Spring Security, Flyway, MySQL
- Frontend: Next.js
- Stockage video: MinIO
- Orchestration locale: Docker Compose

## Architecture

Le projet est compose des services suivants:

- `api-gateway` sur `http://localhost:8080`
- `auth-service` sur `http://localhost:8081`
- `notification-service` sur `http://localhost:8082`
- `kms-service` sur `http://localhost:8083`
- `transactions-service` sur `http://localhost:8084`
- `audit-service` sur `http://localhost:8085`
- `frontend` sur `http://localhost:3000`
- `minio` sur `http://localhost:9000`
- Console MinIO sur `http://localhost:9001` pour voir les videos

Chaque microservice met en place sa propre base MySQL via `docker-compose.yml`.

## Demarrage rapide

Depuis la racine du projet:

```bash
docker compose up --build
```

Pour lancer en arriere-plan:

```bash
docker compose up -d --build
```

Pour arreter l'environnement:

```bash
docker compose down
```

## Acces par defaut

### Compte administrateur

Un administrateur est cree automatiquement au demarrage du `auth-service`.

- `adminName = admin`
- `adminMdp = admin123`

Ce compte permet notamment de se connecter a l'application et de creer d'autres utilisateurs.

### MinIO

MinIO est utilise pour le stockage des videos.

- API/stockage: `http://localhost:9000`
- Console web: `http://localhost:9001`
- Identifiant: `admin`
- Mot de passe: `admin123`

Le service de transactions utilise MinIO pour stocker et lire les videos.

## Utilisation de l'application

### Interface web

L'interface est disponible sur:

```text
http://localhost:3000
```

Le frontend communique avec le gateway, qui redistribue les appels vers les microservices.

### Connexion admin

Vous pouvez vous connecter avec:

```text
username: admin
password: admin123
```

## Creation d'utilisateurs

La creation d'utilisateurs se fait avec un compte administrateur.

### Depuis le frontend

Connectez-vous avec le compte admin puis utilisez l'ecran de creation d'utilisateur.

### Depuis l'API

1. Se connecter pour recuperer un token JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"userName\":\"admin\",\"mdp\":\"admin123\"}"
```

2. Creer un utilisateur avec le token recu:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d "{\"userName\":\"user1\",\"email\":\"user1@example.com\",\"mdp\":\"password123\",\"role\":0}"
```

Remarque:

- `role: 0` correspond a `USER`
- `role: 1` correspond a `ADMIN`

## Principales routes utiles

Toutes les routes ci-dessous sont accessibles via le gateway `http://localhost:8080`.

### Authentification

- `POST /api/auth/login`
- `POST /api/auth/change`
- `POST /api/auth/register`
- `GET /api/auth/users`
- `GET /api/auth/others`

### Gestion des cles

- `POST /api/keys/generate`
- `GET /api/keys/getkeys`
- `GET /api/keys/getvalidekeys`
- `POST /api/keys/revokekey`
- `POST /api/keys/sign`
- `POST /api/keys/verify`

### Transactions video

- `POST /api/transactions/create`
- `POST /api/transactions/verify`
- `GET /api/transactions/all`
- `GET /api/transactions/videos/{objectName}`

### Notifications

- `GET /api/notifications/all`
- `GET /api/notifications/not-seen`
- `PUT /api/notifications/mark-all-seen`
- `POST /api/notifications/create`

### Audit

- `POST /api/audit/create`
- `GET /api/audit/logs`
- `GET /api/audit/download`
- `GET /api/audit/verify`

## Autres utilitaires disponibles

En local, `docker compose` demarre aussi plusieurs utilitaires utiles:

- Les bases MySQL dediees a chaque microservice
- Le gateway pour centraliser les appels API
- Le service d'audit pour consulter, telecharger et verifier les logs
- MinIO pour le stockage et la lecture des videos

## Structure du depot

```text
.
|-- api-gateway/
|-- frontend/
|-- services/
|   |-- auth-service/
|   |-- audit-service/
|   |-- kms-service/
|   |-- notification-service/
|   |-- transactions-service/
|-- docker-compose.yml
```

## Prerequis hors Docker

Si vous souhaitez lancer les services manuellement:

- Java 17
- Maven
- Node.js
- Docker

Le mode recommande pour un demarrage rapide reste toutefois:

```bash
docker compose up --build
```
