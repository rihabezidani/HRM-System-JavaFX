package com.rh.javafx.model;

import javafx.beans.property.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Employe Entity - Represents an employee in the HR system
 */
@Entity
@Table(name = "employe")
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "matricule", unique = true, nullable = false, length = 50)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "poste", length = 100)
    private String poste;

    @Column(name = "departement", length = 100)
    private String departement;

    @Column(name = "salaire_base", precision = 10, scale = 2)
    private BigDecimal salaireBase;

    @Column(name = "jours_conges_restants")
    private Integer joursCongesRestants;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "mot_de_passe_hash", length = 255)
    private String motDePasseHash;

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Conge> conges = new HashSet<>();

    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BulletinPaie> bulletinsPaie = new HashSet<>();

    // JavaFX Properties for TableView binding
    private transient IntegerProperty idProperty;
    private transient StringProperty matriculeProperty;
    private transient StringProperty nomProperty;
    private transient StringProperty prenomProperty;
    private transient StringProperty emailProperty;
    private transient ObjectProperty<BigDecimal> salaireBaseProperty;
    private transient IntegerProperty joursCongesRestantsProperty;
    private transient ObjectProperty<LocalDate> dateEmbaucheProperty;
    private transient ObjectProperty<LocalDate> dateNaissanceProperty;

    // Constructors
    public Employe() {
        this.joursCongesRestants = 18; // Default 18 days
        this.salaireBase = BigDecimal.ZERO;
    }

    public Employe(String matricule, String nom, String prenom, String email, BigDecimal salaireBase) {
        this();
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.salaireBase = salaireBase;
    }

    // JavaFX Property Getters
    public IntegerProperty idProperty() {
        if (idProperty == null) {
            idProperty = new SimpleIntegerProperty(id != null ? id : 0);
        }
        return idProperty;
    }

    public StringProperty matriculeProperty() {
        if (matriculeProperty == null) {
            matriculeProperty = new SimpleStringProperty(matricule);
        }
        return matriculeProperty;
    }

    public StringProperty nomProperty() {
        if (nomProperty == null) {
            nomProperty = new SimpleStringProperty(nom);
        }
        return nomProperty;
    }

    public StringProperty prenomProperty() {
        if (prenomProperty == null) {
            prenomProperty = new SimpleStringProperty(prenom);
        }
        return prenomProperty;
    }

    public StringProperty emailProperty() {
        if (emailProperty == null) {
            emailProperty = new SimpleStringProperty(email);
        }
        return emailProperty;
    }

    public ObjectProperty<BigDecimal> salaireBaseProperty() {
        if (salaireBaseProperty == null) {
            salaireBaseProperty = new SimpleObjectProperty<>(salaireBase);
        }
        return salaireBaseProperty;
    }

    public IntegerProperty joursCongesRestantsProperty() {
        if (joursCongesRestantsProperty == null) {
            joursCongesRestantsProperty = new SimpleIntegerProperty(joursCongesRestants != null ? joursCongesRestants : 0);
        }
        return joursCongesRestantsProperty;
    }

    public ObjectProperty<LocalDate> dateEmbaucheProperty() {
        if (dateEmbaucheProperty == null) {
            dateEmbaucheProperty = new SimpleObjectProperty<>(dateEmbauche);
        }
        return dateEmbaucheProperty;
    }

    public ObjectProperty<LocalDate> dateNaissanceProperty() {
        if (dateNaissanceProperty == null) {
            dateNaissanceProperty = new SimpleObjectProperty<>(dateNaissance);
        }
        return dateNaissanceProperty;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        if (idProperty != null) {
            idProperty.set(id);
        }
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
        if (matriculeProperty != null) {
            matriculeProperty.set(matricule);
        }
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
        if (nomProperty != null) {
            nomProperty.set(nom);
        }
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
        if (prenomProperty != null) {
            prenomProperty.set(prenom);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        if (emailProperty != null) {
            emailProperty.set(email);
        }
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public BigDecimal getSalaireBase() {
        return salaireBase;
    }

    public void setSalaireBase(BigDecimal salaireBase) {
        this.salaireBase = salaireBase;
        if (salaireBaseProperty != null) {
            salaireBaseProperty.set(salaireBase);
        }
    }

    public Integer getJoursCongesRestants() {
        return joursCongesRestants;
    }

    public void setJoursCongesRestants(Integer joursCongesRestants) {
        this.joursCongesRestants = joursCongesRestants;
        if (joursCongesRestantsProperty != null) {
            joursCongesRestantsProperty.set(joursCongesRestants);
        }
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
        if (dateEmbaucheProperty != null) {
            dateEmbaucheProperty.set(dateEmbauche);
        }
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
        if (dateNaissanceProperty != null) {
            dateNaissanceProperty.set(dateNaissance);
        }
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public Set<Conge> getConges() {
        return conges;
    }

    public void setConges(Set<Conge> conges) {
        this.conges = conges;
    }

    public Set<BulletinPaie> getBulletinsPaie() {
        return bulletinsPaie;
    }

    public void setBulletinsPaie(Set<BulletinPaie> bulletinsPaie) {
        this.bulletinsPaie = bulletinsPaie;
    }

    // Business Methods
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public BigDecimal calculerSolde() {
        return bulletinsPaie.stream()
                .map(BulletinPaie::getSalaireNet)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employe employe = (Employe) o;
        return Objects.equals(matricule, employe.matricule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricule);
    }

    @Override
    public String toString() {
        return "Employe{" +
                "id=" + id +
                ", matricule='" + matricule + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", poste='" + poste + '\'' +
                ", salaireBase=" + salaireBase +
                '}';
    }
}
