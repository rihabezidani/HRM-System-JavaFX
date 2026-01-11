# Architecture du Projet RH JavaFX avec Docker

## Vue d'ensemble

Ce projet est une application de gestion des ressources humaines développée en JavaFX avec une architecture conteneurisée utilisant Docker.

## Architecture Globale

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOCKER ENVIRONMENT                           │
│                                                                 │
│  ┌──────────────────────────┐  ┌─────────────────────────────┐ │
│  │   Container: rh-mysql    │  │  Container: rh-javafx-app   │ │
│  │  ┌────────────────────┐  │  │  ┌───────────────────────┐  │ │
│  │  │   MySQL 8.0        │  │  │  │   VNC Server :5901    │  │ │
│  │  │   Port: 3306       │  │  │  │   (TightVNC)          │  │ │
│  │  │   Database:        │  │  │  └───────────────────────┘  │ │
│  │  │   rh_javafx_db     │◄─┼──┼──┤   JavaFX Application    │  │
│  │  │                    │  │  │  │   (JDK 17 + JavaFX)    │  │
│  │  └────────────────────┘  │  │  │                         │  │
│  │   Volume: mysql_data    │  │  │   Built from Maven      │  │
│  └──────────────────────────┘  │  └───────────────────────┘  │ │
│                                │   Exposed Port: 5901        │ │
│                                └─────────────────────────────┘ │
│                                                                 │
│  Network: rh-network (Bridge Driver)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │  VNC Client      │
                    │  localhost:5901  │
                    └──────────────────┘
```

## Structure du Projet

```
rh-project-javafx/
│
├── src/
│   ├── main/
│   │   ├── java/com/rh/javafx/
│   │   │   ├── MainApp.java                    # Point d'entrée de l'application
│   │   │   │
│   │   │   ├── controller/                     # Couche Contrôleur (MVC)
│   │   │   │   ├── DashboardController.java    # Gestion du tableau de bord RH
│   │   │   │   ├── EmployeeDashboardController.java  # Tableau de bord employé
│   │   │   │   └── LoginController.java        # Authentification
│   │   │   │
│   │   │   ├── model/                          # Couche Modèle (Entités JPA)
│   │   │   │   ├── Employe.java               # Entité Employé
│   │   │   │   ├── Conge.java                 # Entité Congé
│   │   │   │   ├── BulletinPaie.java          # Entité Bulletin de paie
│   │   │   │   └── ResponsableRH.java         # Entité Responsable RH
│   │   │   │
│   │   │   ├── service/                        # Couche Service (Logique métier)
│   │   │   │   ├── AuthService.java           # Service d'authentification
│   │   │   │   ├── EmployeService.java        # CRUD + logique employés
│   │   │   │   ├── CongeService.java          # Gestion des congés
│   │   │   │   └── BulletinPaieService.java   # Gestion des bulletins
│   │   │   │
│   │   │   └── util/                           # Utilitaires
│   │   │       └── HibernateUtil.java         # Configuration Hibernate
│   │   │
│   │   └── resources/
│   │       ├── com/rh/javafx/
│   │       │   ├── fxml/                       # Vues (FXML)
│   │       │   │   ├── login.fxml             # Interface de connexion
│   │       │   │   ├── dashboard.fxml         # Interface RH
│   │       │   │   └── employee-dashboard.fxml # Interface employé
│   │       │   │
│   │       │   └── css/
│   │       │       └── style.css              # Styles globaux
│   │       │
│   │       └── hibernate.cfg.xml              # Configuration Hibernate
│
├── Docker Files/
│   ├── Dockerfile                             # Image multi-stage pour l'app
│   ├── docker-compose.yml                     # Orchestration des services
│   ├── docker-entrypoint.sh                   # Script de démarrage (généré)
│   ├── .dockerignore                          # Fichiers à ignorer
│   └── .env                                   # Variables d'environnement
│
├── Documentation/
│   ├── README.md                              # Guide principal
│   ├── DOCKER-GUIDE.md                        # Guide Docker
│   ├── QUICK-START.md                         # Démarrage rapide
│   ├── DESIGN-SYSTEM.md                       # Système de design
│   ├── MODEL-COMPARISON.md                    # Comparaison des modèles
│   ├── VALIDATION-FIX.md                      # Corrections de validation
│   ├── CHANGELOG-FIELDS.md                    # Changelog des champs
│   └── ARCHITECTURE.md                        # Ce document
│
├── pom.xml                                    # Configuration Maven
├── run.bat                                    # Script de lancement Windows
└── .gitignore                                 # Fichiers ignorés par Git
```

## Architecture Applicative en Couches

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐  │
│  │  login.fxml  │  │dashboard.fxml│  │employee-dashboard   │  │
│  │  + style.css │  │  + style.css │  │.fxml + style.css    │  │
│  └──────────────┘  └──────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CONTROLLER LAYER                             │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────────┐  │
│  │ LoginController│  │DashboardCtrl   │  │EmployeeDashboard │  │
│  │                │  │                │  │Controller        │  │
│  │ - handleLogin()│  │ - handleAdd*() │  │ - viewConges()   │  │
│  │                │  │ - handleEdit*()│  │ - viewBulletins()│  │
│  └────────────────┘  └────────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                                │
│  ┌───────────────┐  ┌───────────────┐  ┌──────────────────┐   │
│  │ AuthService   │  │EmployeService │  │ CongeService     │   │
│  │               │  │               │  │                  │   │
│  │ - login()     │  │ - getAllEmp() │  │ - demander()     │   │
│  │ - logout()    │  │ - saveEmp()   │  │ - approuver()    │   │
│  │               │  │ - deleteEmp() │  │ - rejeter()      │   │
│  └───────────────┘  └───────────────┘  └──────────────────┘   │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │          BulletinPaieService                             │  │
│  │  - genererBulletin()  - getAllBulletins()               │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Hibernate ORM (JPA)                         │  │
│  │              HibernateUtil.java                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MODEL LAYER (Entities)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────────┐  │
│  │   Employe    │  │    Conge     │  │   BulletinPaie      │  │
│  │              │  │              │  │                     │  │
│  │ - id         │  │ - id         │  │ - id                │  │
│  │ - matricule  │  │ - dateDebut  │  │ - periode           │  │
│  │ - nom        │  │ - dateFin    │  │ - salaireBrut       │  │
│  │ - prenom     │  │ - typeConge  │  │ - salaireNet        │  │
│  │ - email      │  │ - statut     │  │ - employe (FK)      │  │
│  │ - poste      │  │ - employe(FK)│  │                     │  │
│  └──────────────┘  └──────────────┘  └─────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           ResponsableRH                                  │  │
│  │  - id  - nomComplet  - email  - motDePasseHash          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              MySQL 8.0 (rh_javafx_db)                    │  │
│  │                                                           │  │
│  │  Tables:                                                 │  │
│  │  - employe                                               │  │
│  │  - conge                                                 │  │
│  │  - bulletin_paie                                         │  │
│  │  - responsable_rh                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Architecture Docker

### 1. **Multi-Stage Build**

```
Stage 1: BUILDER
├── Base: maven:3.9-eclipse-temurin-17
├── Tâches:
│   ├── Téléchargement des dépendances Maven
│   ├── Compilation du code source
│   └── Génération du JAR: rh-project-javafx-*.jar

Stage 2: RUNTIME
├── Base: eclipse-temurin:17-jdk
├── Installations:
│   ├── TightVNC Server (serveur VNC)
│   ├── Fluxbox (gestionnaire de fenêtres léger)
│   ├── OpenJFX (runtime JavaFX)
│   ├── Bibliothèques graphiques (Mesa, GTK, X11)
│   └── Utilitaires (procps, net-tools)
├── Configuration:
│   ├── VNC startup script (~/.vnc/xstartup)
│   ├── Copie du JAR depuis le stage builder
│   └── Script d'entrée (docker-entrypoint.sh)
└── Exposition du port 5901 (VNC)
```

### 2. **Docker Compose Services**

```yaml
services:
  mysql:
    - Image: mysql:8.0
    - Port: 3306
    - Volume persistant: mysql_data
    - Healthcheck: mysqladmin ping
    - Variables d'environnement:
      * MYSQL_ROOT_PASSWORD
      * MYSQL_DATABASE: rh_javafx_db

  app:
    - Build: Multi-stage Dockerfile
    - Port: 5901 (VNC)
    - Dépend de: mysql (healthcheck)
    - Variables d'environnement:
      * DB_HOST, DB_PORT, DB_NAME
      * VNC_PASSWORD, VNC_RESOLUTION
```

### 3. **Réseau Docker**

```
rh-network (Bridge)
├── mysql (rh-mysql)
│   └── IP interne: dynamique
└── app (rh-javafx-app)
    └── IP interne: dynamique
    └── Peut résoudre 'mysql' par nom DNS
```

## Flux de Données

### 1. **Authentification**

```
User (VNC) → LoginController → AuthService
    → Hibernate → MySQL (responsable_rh / employe)
    → Session créée → Navigation vers Dashboard
```

### 2. **Gestion des Employés**

```
DashboardController → EmployeService
    ├── getAllEmployes() → Hibernate → SELECT * FROM employe
    ├── saveEmploye() → Hibernate → INSERT/UPDATE employe
    └── deleteEmploye() → Hibernate → DELETE FROM employe
```

### 3. **Gestion des Congés**

```
DashboardController → CongeService
    ├── demanderConge() → Validation → INSERT conge (EN_ATTENTE)
    ├── approuverConge() → UPDATE conge (APPROUVE) + UPDATE employe
    └── rejeterConge() → UPDATE conge (REJETE)
```

### 4. **Génération de Bulletins**

```
DashboardController → BulletinPaieService
    └── genererBulletin()
        ├── Récupération employé
        ├── Calculs: brut, primes, retenues, net
        └── INSERT bulletin_paie
```

## Technologies Utilisées

### Frontend
- **JavaFX 17**: Framework UI
- **FXML**: Définition déclarative des interfaces
- **CSS**: Stylisation (système de design professionnel vert)

### Backend
- **Java 17**: Langage de programmation
- **Hibernate 6.2.7**: ORM (Object-Relational Mapping)
- **JPA**: Spécification de persistance

### Base de données
- **MySQL 8.0**: Système de gestion de base de données

### Build & Dépendances
- **Maven 3.9**: Gestion de projet et dépendances

### Conteneurisation
- **Docker**: Conteneurisation de l'application
- **Docker Compose**: Orchestration multi-conteneurs
- **TightVNC**: Serveur VNC pour accès graphique
- **Fluxbox**: Gestionnaire de fenêtres léger

## Ports et Endpoints

| Service | Port | Description |
|---------|------|-------------|
| MySQL | 3306 | Base de données (interne au réseau Docker) |
| VNC Server | 5901 | Accès graphique à l'application |

## Volumes Persistants

| Volume | Type | Usage |
|--------|------|-------|
| mysql_data | Named Volume | Stockage persistant des données MySQL |

## Variables d'Environnement

### MySQL Container
- `MYSQL_ROOT_PASSWORD`: Mot de passe root MySQL
- `MYSQL_DATABASE`: Nom de la base de données

### Application Container
- `DB_HOST`: Hôte de la base de données (mysql)
- `DB_PORT`: Port de la base de données (3306)
- `DB_NAME`: Nom de la base de données
- `DB_USER`: Utilisateur de la base de données
- `DB_PASSWORD`: Mot de passe de la base de données
- `VNC_PASSWORD`: Mot de passe VNC (limité à 8 caractères)
- `VNC_RESOLUTION`: Résolution de l'écran VNC (ex: 1920x1080)
- `DISPLAY`: Display X11 (:1)

## Sécurité

### Base de données
- ✅ Mot de passe root configurable
- ✅ Base de données isolée dans le réseau Docker
- ✅ Pas d'exposition directe sur l'hôte (sauf pour développement)

### Application
- ✅ Hachage SHA-256 des mots de passe utilisateurs
- ✅ Accès VNC protégé par mot de passe
- ✅ Validation des entrées utilisateur (côté service)

### Docker
- ✅ Multi-stage build (réduction de la taille de l'image)
- ✅ Réseau bridge isolé
- ✅ Healthcheck sur MySQL
- ✅ Variables d'environnement pour les secrets

## Démarrage du Projet

### Avec Docker (Recommandé)

```bash
# 1. Créer le fichier .env
MYSQL_ROOT_PASSWORD=rootpass123
DB_PASSWORD=rootpass123
VNC_PASSWORD=haFI99D#
VNC_RESOLUTION=1920x1080

# 2. Lancer les services
docker-compose up -d --build

# 3. Se connecter via VNC
# VNC Viewer → localhost:5901
# Mot de passe: haFI99D#
```

### Sans Docker (Développement local)

```bash
# 1. Installer MySQL localement
# 2. Créer la base de données rh_javafx_db
# 3. Lancer l'application
mvn clean javafx:run
```

## Maintenance et Développement

### Rebuild de l'application
```bash
docker-compose down
docker-compose up -d --build
```

### Logs
```bash
docker-compose logs -f app
docker-compose logs -f mysql
```

### Accès à la base de données
```bash
docker exec -it rh-mysql mysql -u root -p
```

### Sauvegarde de la base
```bash
docker exec rh-mysql mysqldump -u root -prootpass123 rh_javafx_db > backup.sql
```

## Évolutions Futures

- [ ] Ajouter un reverse proxy (Nginx)
- [ ] Implémenter HTTPS pour VNC (via websockify)
- [ ] Ajouter des tests automatisés
- [ ] Mettre en place CI/CD (GitHub Actions)
- [ ] Créer une interface web (Spring Boot + REST API)
- [ ] Ajouter l'export PDF des bulletins de paie
- [ ] Notifications par email
- [ ] Tableau de bord avec graphiques statistiques
