# Système de Gestion RH - JavaFX

Application de bureau professionnelle pour la gestion des ressources humaines développée avec JavaFX, Hibernate et MySQL.

**Interface Moderne** : Design d'entreprise avec thème vert professionnel (#1C2011), typographie soignée et expérience utilisateur optimale.

## Fonctionnalités

### Gestion des Employés
- Ajouter, modifier et supprimer des employés
- Rechercher des employés par nom, prénom, email ou matricule
- Afficher les informations détaillées (poste, département, salaire, congés restants)
- Suivi du solde de congés de chaque employé

### Gestion des Congés
- Créer des demandes de congé
- Approuver ou rejeter les demandes en attente
- Filtrer les congés par statut (En Attente, Approuvé, Rejeté)
- Calcul automatique de la durée des congés
- Déduction automatique des jours de congé lors de l'approbation

### Gestion des Bulletins de Paie
- Générer des bulletins de paie pour les employés
- Calculer automatiquement le salaire net (brut + primes - retenues)
- Visualiser les bulletins de paie détaillés
- Rechercher par période

### Authentification
- Connexion sécurisée avec email et mot de passe hashé (SHA-256)
- Création de compte pour les responsables RH
- Compte administrateur par défaut

### Tableau de bord
- Statistiques en temps réel :
  - Nombre total d'employés
  - Congés en attente
  - Bulletins générés
  - Salaire moyen

## Prérequis

- **Java 17** ou supérieur
- **Maven 3.6+**
- **MySQL 8.0** ou supérieur
- **JavaFX SDK 21** (installé dans `C:\javafx-sdk-21.0.9\lib`)

## Design & Interface

### Thème Professionnel Entreprise
- **Palette de couleurs moderne** :
  - Vert Principal : `#1C2011` (identité de marque)
  - Vert Secondaire : `#2D8659` (accents)
  - Arrière-plan : `#F8F9FA` (neutre et propre)
  - Texte : `#000000` (lisibilité maximale)
  - Texte Secondaire : `#6D7588` (subtil)

- **Design System** :
  - Cards avec ombres subtiles (box-shadow professionnelles)
  - Bordures arrondies (8-16px radius)
  - Typographie hiérarchisée (poids 400-700)
  - Espacement cohérent (système de 4px)
  - États interactifs (hover, focus, active)

- **Composants Enterprise** :
  - Tables avec en-têtes fixes et tri
  - Formulaires avec validation visuelle
  - Cartes statistiques avec gradients
  - Navigation par onglets professionnelle
  - Boutons d'action contextuels

## Technologies utilisées

- **JavaFX 21** - Interface utilisateur moderne
- **Hibernate 5.6.15** - ORM (Object-Relational Mapping)
- **MySQL 8.0** - Base de données relationnelle
- **Maven** - Gestion des dépendances et build
- **JPA** - Java Persistence API
- **ControlsFX** - Contrôles JavaFX avancés
- **CSS3** - Styles professionnels personnalisés

## Installation et Configuration

### 1. Configuration de la base de données

Assurez-vous que MySQL est installé et en cours d'exécution. Le projet créera automatiquement la base de données `rh_javafx_db` au premier démarrage.

Modifiez les paramètres de connexion dans `src/main/resources/hibernate.cfg.xml` si nécessaire :

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/rh_javafx_db?createDatabaseIfNotExist=true&amp;useSSL=false&amp;serverTimezone=UTC</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password"></property>
```

### 2. Compilation du projet

```bash
cd rh-project-javafx
mvn clean compile
```

### 3. Exécution de l'application

#### Option 1 : Avec Maven et le plugin JavaFX

```bash
mvn javafx:run
```

#### Option 2 : Avec Java et le SDK JavaFX local

```bash
mvn clean package
java --module-path "C:\javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml -jar target/rh-project-javafx-1.0-SNAPSHOT.jar
```

#### Option 3 : Avec l'IDE (IntelliJ IDEA / Eclipse)

1. Ouvrez le projet dans votre IDE
2. Ajoutez les VM options suivantes dans la configuration de lancement :
   ```
   --module-path "C:\javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml
   ```
3. Exécutez la classe `MainApp`

## Compte par défaut

Lors du premier lancement, un compte administrateur par défaut est créé :

- **Email**: `admin@rh.com`
- **Mot de passe**: `admin123`

## Structure du projet

```
rh-project-javafx/
├── src/main/
│   ├── java/com/rh/javafx/
│   │   ├── controller/          # Contrôleurs JavaFX
│   │   │   ├── LoginController.java
│   │   │   └── DashboardController.java
│   │   │
│   │   ├── model/               # Entités JPA
│   │   │   ├── Employe.java
│   │   │   ├── Conge.java
│   │   │   ├── BulletinPaie.java
│   │   │   └── ResponsableRH.java
│   │   │
│   │   ├── service/             # Logique métier
│   │   │   ├── EmployeService.java
│   │   │   ├── CongeService.java
│   │   │   ├── BulletinPaieService.java
│   │   │   └── AuthService.java
│   │   │
│   │   ├── util/                # Utilitaires
│   │   │   └── HibernateUtil.java
│   │   │
│   │   └── MainApp.java         # Point d'entrée
│   │
│   └── resources/
│       ├── hibernate.cfg.xml    # Configuration Hibernate
│       └── com/rh/javafx/
│           ├── fxml/            # Fichiers FXML
│           │   ├── login.fxml
│           │   └── dashboard.fxml
│           │
│           └── css/             # Feuilles de style
│               └── style.css
│
└── pom.xml                      # Configuration Maven
```

## Utilisation

### Connexion
1. Lancez l'application
2. Saisissez votre email et mot de passe
3. Cliquez sur "Se connecter"
4. Pour créer un nouveau compte, cliquez sur "Créer un compte"

### Gestion des employés
1. Accédez à l'onglet "Employés"
2. Cliquez sur "+ Nouvel Employé" pour ajouter un employé
3. Utilisez la barre de recherche pour filtrer les employés
4. Utilisez les boutons "Modifier" ou "Supprimer" pour gérer les employés

### Gestion des congés
1. Accédez à l'onglet "Congés"
2. Cliquez sur "+ Nouvelle Demande" pour créer une demande
3. Utilisez le filtre pour afficher les congés par statut
4. Approuvez ou rejetez les demandes en attente

### Génération de bulletins de paie
1. Accédez à l'onglet "Bulletins de Paie"
2. Cliquez sur "+ Générer Bulletin"
3. Sélectionnez un employé, saisissez la période, les primes et retenues
4. Cliquez sur "Générer"
5. Utilisez le bouton "Voir" pour afficher le bulletin détaillé

## Fonctionnalités avancées

### JavaFX Properties
Les modèles utilisent des JavaFX Properties pour le binding bidirectionnel avec les TableView :
- Mise à jour automatique de l'interface
- Réactivité en temps réel
- Intégration native avec JavaFX

### Validation automatique
- Vérification du solde de congés avant approbation
- Validation des champs obligatoires
- Gestion des erreurs avec messages utilisateur

### Calculs automatiques
- Durée des congés (en jours)
- Salaire net (brut + primes - retenues)
- Statistiques du tableau de bord

## Dépannage

### Erreur : "Error: JavaFX runtime components are missing"
Solution : Assurez-vous d'ajouter les VM options :
```
--module-path "C:\javafx-sdk-21.0.9\lib" --add-modules javafx.controls,javafx.fxml
```

### Erreur de connexion à MySQL
1. Vérifiez que MySQL est démarré
2. Vérifiez les identifiants dans `hibernate.cfg.xml`
3. Assurez-vous que le port 3306 est accessible

### Erreur : "Table doesn't exist"
La propriété `hibernate.hbm2ddl.auto=update` créera automatiquement les tables au premier lancement.

## Améliorations futures

- Export des données en PDF/Excel
- Graphiques et rapports statistiques
- Gestion des rôles et permissions
- Historique des modifications
- Notifications par email
- Sauvegarde automatique de la base de données

## Licence

Ce projet est un exemple éducatif pour démontrer l'utilisation de JavaFX avec Hibernate.

## Auteur

Développé pour le système de gestion RH avec JavaFX et Hibernate.
