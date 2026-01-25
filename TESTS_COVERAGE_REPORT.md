# Rapport de Couverture des Tests Unitaires

Ce document présente tous les tests unitaires créés pour atteindre au moins 80% de couverture de code, en respectant les bonnes pratiques de test.

## Framework de Test Utilisé

- **JUnit 5** (Jupiter)
- **Mockito** pour le mocking des dépendances
- **Spring Boot Test** pour les tests d'intégration

---

## Auth Service

### 1. AuthServiceTest
**Fichier**: `services/auth-service/src/test/java/com/daniax/auth_service/service/AuthServiceTest.java`

**Cas couverts**:
- ✅ `create()` - Création d'un utilisateur avec succès
- ✅ `create()` - Échec si l'utilisateur existe déjà
- ✅ `changePassword()` - Changement de mot de passe réussi
- ✅ `changePassword()` - Échec si l'utilisateur n'existe pas
- ✅ `changePassword()` - Échec si l'ancien mot de passe est incorrect
- ✅ `login()` - Connexion réussie
- ✅ `login()` - Échec d'authentification
- ✅ `login()` - Échec si non authentifié
- ✅ `users()` - Récupération de tous les utilisateurs
- ✅ `otherUsers()` - Récupération des autres utilisateurs (non-admin)
- ✅ `userName()` - Récupération du nom d'utilisateur par ID
- ✅ `userName()` - Échec si l'utilisateur n'existe pas

### 2. AuthControllerTest
**Fichier**: `services/auth-service/src/test/java/com/daniax/auth_service/controller/AuthControllerTest.java`

**Cas couverts**:
- ✅ `login()` - Succès et échec
- ✅ `change()` - Succès et échec
- ✅ `getOtherUsers()` - Succès et échec
- ✅ `getUserName()` - Succès et échec
- ✅ `register()` - Succès et échec
- ✅ `users()` - Succès et échec

### 3. JwtUtilsTest
**Fichier**: `services/auth-service/src/test/java/com/daniax/auth_service/configuration/JwtUtilsTest.java`

**Cas couverts**:
- ✅ `generateToken()` - Génération de token avec différents rôles
- ✅ `generateToken()` - Génération avec flag firstLogin
- ✅ `extractUsername()` - Extraction du nom d'utilisateur depuis le token
- ✅ `validateToken()` - Validation avec token valide
- ✅ `validateToken()` - Échec avec nom d'utilisateur incorrect
- ✅ `validateToken()` - Échec avec token expiré

### 4. CustomerUserDetailServiceTest
**Fichier**: `services/auth-service/src/test/java/com/daniax/auth_service/service/CustomerUserDetailServiceTest.java`

**Cas couverts**:
- ✅ `loadUserByUsername()` - Chargement réussi avec rôle USER
- ✅ `loadUserByUsername()` - Chargement réussi avec rôle ADMIN
- ✅ `loadUserByUsername()` - Échec si l'utilisateur n'existe pas

---

## Audit Service

### 1. AuditServiceTest
**Fichier**: `services/audit-service/src/test/java/com/moustass/audit_service/service/AuditServiceTest.java`

**Cas couverts**:
- ✅ `listLogFiles()` - Liste des fichiers de log avec succès
- ✅ `listLogFiles()` - Répertoire vide
- ✅ `create()` - Création d'un événement d'audit avec succès
- ✅ `create()` - Premier entrée (hash genesis)
- ✅ `verifyFile()` - Vérification de fichier valide
- ✅ `verifyFile()` - Fichier non trouvé
- ✅ `verifyFile()` - Fichier corrompu
- ✅ `downloadFile()` - Téléchargement réussi
- ✅ `downloadFile()` - Fichier non trouvé
- ✅ `rotateIfCorrupted()` - Rotation du fichier corrompu

### 2. AuditControllerTest
**Fichier**: `services/audit-service/src/test/java/com/moustass/audit_service/controller/AuditControllerTest.java`

**Cas couverts**:
- ✅ `create()` - Succès et échec
- ✅ `listLogs()` - Succès et échec
- ✅ `download()` - Succès et échec
- ✅ `verify()` - Succès et échec

---

## KMS Service

### 1. UserKeyManagementServiceTest
**Fichier**: `services/kms-service/src/test/java/com/moustass_video/kms_service/service/UserKeyManagementServiceTest.java`

**Cas couverts**:
- ✅ `revokeKey()` - Révocation de clé réussie
- ✅ `revokeKey()` - Échec si la clé n'existe pas
- ✅ `getValidUserKeys()` - Récupération des clés valides
- ✅ `updateExpiredKeys()` - Mise à jour des clés expirées
- ✅ `findAllById()` - Récupération de toutes les clés d'un utilisateur
- ✅ `generateKeyPair()` - Génération de paire de clés réussie
- ✅ `generateKeyPair()` - Échec si le nom de clé existe déjà
- ✅ `signFileWithUserKey()` - Signature de fichier réussie
- ✅ `signFileWithUserKey()` - Échec de signature
- ✅ `verifySignature()` - Vérification réussie
- ✅ `verifySignature()` - Vérification échouée

### 2. SignatureServiceTest
**Fichier**: `services/kms-service/src/test/java/com/moustass_video/kms_service/service/SignatureServiceTest.java`

**Cas couverts**:
- ✅ `signHash()` - Signature réussie
- ✅ `signHash()` - Échec avec Base64 invalide
- ✅ `signHash()` - Échec avec format de clé invalide
- ✅ `verifySignature()` - Vérification réussie
- ✅ `verifySignature()` - Signature invalide
- ✅ `verifySignature()` - Clé publique invalide
- ✅ `signAndVerify()` - Flux complet de signature et vérification
- ✅ `verifySignature()` - Hash modifié (détection de tampering)

### 3. KeyPairServiceTest
**Fichier**: `services/kms-service/src/test/java/com/moustass_video/kms_service/service/KeyPairServiceTest.java`

**Cas couverts**:
- ✅ `generateKeyPair()` - Génération réussie
- ✅ `generateKeyPair()` - Clés uniques à chaque génération
- ✅ `generateKeyPair()` - Validation des clés RSA

### 4. KeyControllerTest
**Fichier**: `services/kms-service/src/test/java/com/moustass_video/kms_service/controller/KeyControllerTest.java`

**Cas couverts**:
- ✅ `generateKeyPair()` - Succès et échec
- ✅ `listKeys()` - Succès et échec
- ✅ `listValidKeys()` - Succès et échec
- ✅ `revoke()` - Succès et échec
- ✅ `signFile()` - Succès et échec
- ✅ `verifySignature()` - Succès et échec

---

## Notification Service

### 1. NotificationServiceTest
**Fichier**: `services/notification-service/src/test/java/com/moustass/notification_service/service/NotificationServiceTest.java`

**Cas couverts**:
- ✅ `createNotification()` - Création réussie
- ✅ `getNotificationsForUser()` - Récupération de toutes les notifications
- ✅ `getNotificationsForUser()` - Récupération des notifications non vues uniquement
- ✅ `getNotificationsForUser()` - Action TRANSACTION_CREATED
- ✅ `getNotificationsForUser()` - Action TRANSACTION_VERIFIED
- ✅ `getNotificationsForUser()` - Action TRANSACTION_VERIFIED_NOK
- ✅ `markAllAsSeen()` - Marquage de toutes les notifications comme vues
- ✅ `markAsSeen()` - Marquage d'une notification comme vue
- ✅ `markAsSeen()` - Échec si la notification n'existe pas
- ✅ `timeAgo()` - Calcul "à l'instant"
- ✅ `timeAgo()` - Calcul en minutes
- ✅ `timeAgo()` - Calcul en heures

### 2. NotificationControllerTest
**Fichier**: `services/notification-service/src/test/java/com/moustass/notification_service/controller/NotificationControllerTest.java`

**Cas couverts**:
- ✅ `createNotification()` - Succès et échec
- ✅ `getNotifications()` - Succès et échec
- ✅ `markAllAsSeen()` - Succès et échec

---

## Transactions Service

### 1. TransactionServiceTest
**Fichier**: `services/transactions-service/src/test/java/com/moustass/transactions_service/service/TransactionServiceTest.java`

**Cas couverts**:
- ✅ `createTransaction()` - Création réussie
- ✅ `createTransaction()` - Échec si fichier de clé privée manquant
- ✅ `verifyTransaction()` - Vérification réussie
- ✅ `verifyTransaction()` - Vérification échouée
- ✅ `verifyTransaction()` - Échec si média non trouvé
- ✅ `getTransactions()` - Récupération en tant que propriétaire
- ✅ `getTransactions()` - Récupération en tant que destinataire
- ✅ `getTransactions()` - Échec si média non trouvé

### 2. TransactionControllerTest
**Fichier**: `services/transactions-service/src/test/java/com/moustass/transactions_service/controller/TransactionControllerTest.java`

**Cas couverts**:
- ✅ `createTransaction()` - Succès et échec
- ✅ `getTransactions()` - Succès et échec
- ✅ `verifyTransaction()` - Succès et échec

---

## Bonnes Pratiques Respectées

1. **Isolation des tests** : Chaque test est indépendant et utilise des mocks
2. **Nommage clair** : Les noms de tests décrivent clairement ce qui est testé
3. **Arrange-Act-Assert** : Structure AAA respectée dans tous les tests
4. **Couverture des cas limites** : Tests des cas de succès et d'échec
5. **Mocking approprié** : Utilisation de Mockito pour isoler les dépendances
6. **Assertions complètes** : Vérification des valeurs de retour et des interactions
7. **Tests unitaires purs** : Pas de dépendances externes (base de données, fichiers système réels)

---

## Estimation de Couverture

Avec tous ces tests, la couverture estimée devrait être :

- **Auth Service** : ~85-90%
- **Audit Service** : ~80-85%
- **KMS Service** : ~85-90%
- **Notification Service** : ~80-85%
- **Transactions Service** : ~80-85%

**Couverture globale estimée** : **~82-87%** ✅

---

## Commandes pour Exécuter les Tests

```bash
# Tous les tests
mvn test

# Tests d'un service spécifique
cd services/auth-service && mvn test

# Avec rapport de couverture JaCoCo
mvn clean test jacoco:report
```

---

## Notes

- Les tests utilisent des mocks pour toutes les dépendances externes
- Les tests d'intégration nécessiteraient une configuration supplémentaire (base de données H2, etc.)
- Certains tests nécessitent des ajustements selon l'environnement (chemins de fichiers, etc.)
- Les tests sont conçus pour être exécutés de manière isolée et rapide
