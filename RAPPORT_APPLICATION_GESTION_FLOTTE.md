# Rapport Application Gestion de Flotte - Jbel Annour

## 1. Presentation generale

L'application **vehicle-reconciliation** est une application interne developpee pour **Jbel Annour**. Elle a evolue d'un outil de rapprochement entre les vehicules NARSA et Sage X3 vers une application complete de gestion de flotte.

L'objectif principal est de centraliser les donnees liees aux vehicules, chauffeurs, infractions routieres, consommation gasoil, rapprochement NARSA/Sage et automatisations via n8n.

## 2. Technologies utilisees

### Backend

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Apache POI pour l'import/export Excel
- Spring Security
- JWT pour l'authentification
- BCrypt pour le chiffrement des mots de passe

### Frontend

- Angular standalone
- TypeScript
- Angular Router
- HttpClient
- Interceptor JWT
- Guard de protection des routes

### Automatisation

- n8n pour les workflows, notifications et webhooks

## 3. Architecture backend

Le backend est organise en modules fonctionnels afin de garder une architecture propre et evolutive.

```text
jbel.annour.vehiclereconciliation
  auth
  security
  vehicle
  driver
  infraction
  fuel
  reconciliation
  automation
  dashboard
  controller
  service
  repository
  entity
```

## 4. Modules fonctionnels

### 4.1 Module Authentification

Le module authentification permet :

- Creation de compte
- Connexion
- Generation de token JWT
- Deconnexion cote frontend
- Protection des pages Angular
- Protection des endpoints backend

Deux roles sont prevus :

- `USER`
- `ADMIN`

Actuellement, les roles existent dans le modele utilisateur. Une evolution future consistera a appliquer des permissions differentes selon le role.

### 4.2 Module Vehicules

Ce module permet de gerer le parc vehicules.

Fonctionnalites :

- Ajouter un vehicule
- Modifier un vehicule
- Supprimer un vehicule
- Lister les vehicules
- Suivre le statut du vehicule
- Associer des references Sage et NARSA

Endpoint principal :

```text
/api/vehicles
```

### 4.3 Module Chauffeurs

Ce module permet de gerer les chauffeurs de la flotte.

Fonctionnalites :

- Ajouter un chauffeur
- Modifier un chauffeur
- Supprimer un chauffeur
- Lister les chauffeurs
- Suivre CIN, telephone, permis et statut

Endpoint principal :

```text
/api/drivers
```

### 4.4 Module Infractions Routieres

Ce module permet de suivre les infractions liees aux vehicules et chauffeurs.

Fonctionnalites :

- Ajouter une infraction
- Modifier une infraction
- Supprimer une infraction
- Suivre le statut de paiement
- Identifier les infractions non payees

Endpoint principal :

```text
/api/infractions
```

### 4.5 Module Consommation Gasoil

Ce module permet de suivre la consommation gasoil par vehicule et chauffeur.

Fonctionnalites :

- Ajouter une consommation gasoil
- Modifier une consommation
- Supprimer une consommation
- Renseigner station, litres, montant et date
- Uploader une photo du ticket gasoil
- Detecter des anomalies simples comme montant ou litres invalides

Endpoint principal :

```text
/api/fuel-consumptions
```

Upload ticket :

```text
POST /api/fuel-consumptions/{id}/receipt
```

### 4.6 Module Rapprochement NARSA / Sage X3

Le module existant est conserve afin de ne pas casser les fonctionnalites deja disponibles.

Fonctionnalites :

- Import Excel NARSA
- Import Excel Sage
- Comparaison entre les deux sources
- Detection des statuts :
  - `MATCH`
  - `ABSENT_IN_SAGE`
  - `ABSENT_IN_NARSA`
- Dashboard de rapprochement
- Export Excel des resultats

Endpoints existants :

```text
/api/narsa/upload
/api/narsa/sage/upload
/api/narsa/compare
/api/narsa/results
/api/narsa/stats
/api/narsa/results/export
/api/narsa/reset
```

### 4.7 Module Automatisation n8n

Ce module prepare l'integration avec n8n.

Webhooks disponibles :

```text
POST /api/automations/webhooks/fuel-consumption
POST /api/automations/webhooks/infraction
POST /api/automations/webhooks/weekly-report
```

Objectifs :

- Notifier le manager lors d'une nouvelle infraction
- Notifier en cas de consommation gasoil anormale
- Generer un rapport hebdomadaire
- Envoyer des notifications email ou WhatsApp via n8n

Les appels webhook sont historises dans `AutomationLog`.

## 5. Dashboard global

Le dashboard global presente une vue synthetique de la flotte.

Cards disponibles :

- Total vehicules
- Vehicules conformes
- Infractions non payees
- Total consommation gasoil
- Gasoil ce mois
- Anomalies detectees
- Vehicules absents Sage
- Vehicules absents NARSA

Endpoint :

```text
GET /api/dashboard/stats
```

## 6. Architecture frontend

Le frontend Angular est organise avec une sidebar fixe et des pages par module.

```text
src/app
  auth
  layout
  pages
    login
    register
    fleet-dashboard
    vehicles
    drivers
    reconciliation
    infractions
    fuel-consumptions
    automations
    placeholder-page
  services
```

## 7. Sidebar Angular

La sidebar contient :

- Dashboard
- Vehicules
- Chauffeurs
- Rapprochement NARSA / Sage
- Infractions routieres
- Consommation gasoil
- Rapports
- Automatisations n8n
- Parametres
- Deconnexion

## 8. Securite

La securite repose sur :

- Authentification JWT
- Stockage du token dans `localStorage`
- Interceptor Angular pour envoyer :

```text
Authorization: Bearer TOKEN
```

- Guard Angular pour proteger les routes
- BCrypt pour encoder les mots de passe
- Spring Security pour proteger les endpoints backend

## 9. Points forts de l'application

- Architecture modulaire
- Application evolutive
- Separation claire backend/frontend
- Conservation du module NARSA/Sage existant
- Preparation pour automatisation n8n
- Dashboard global pour pilotage rapide
- Authentification moderne avec JWT

## 10. Ameliorations futures

Les prochaines evolutions recommandees sont :

1. Appliquer les permissions selon les roles `ADMIN` et `USER`
2. Ajouter une gestion des utilisateurs pour l'administrateur
3. Ajouter des filtres et recherche dans les tableaux
4. Ajouter export PDF/Excel pour les rapports flotte
5. Ajouter graphiques de consommation gasoil
6. Ajouter alertes automatiques avancees
7. Ajouter historique complet des operations
8. Ajouter stockage cloud ou MinIO pour les tickets gasoil
9. Ajouter integration WhatsApp via n8n
10. Ajouter tests unitaires et tests d'integration plus complets

## 11. Conclusion

L'application **vehicle-reconciliation** constitue maintenant une base solide pour une solution interne de gestion de flotte. Elle permet de gerer les vehicules, chauffeurs, infractions, consommation gasoil, rapprochement NARSA/Sage et automatisations n8n.

La structure modulaire mise en place facilite les evolutions futures et permet d'ajouter progressivement des fonctionnalites plus avancees comme les rapports, les notifications automatiques et la gestion fine des permissions.
