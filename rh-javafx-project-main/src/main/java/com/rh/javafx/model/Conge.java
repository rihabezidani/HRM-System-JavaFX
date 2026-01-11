package com.rh.javafx.model;

import javafx.beans.property.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Conge Entity - Represents a leave request
 */
@Entity
@Table(name = "conge")
public class Conge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "type_conge", length = 50)
    private String typeConge;

    @Column(name = "motif", length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutConge statut;

    @Column(name = "duree_jours")
    private Integer dureeJours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    // JavaFX Properties
    private transient IntegerProperty idProperty;
    private transient ObjectProperty<LocalDate> dateDebutProperty;
    private transient ObjectProperty<LocalDate> dateFinProperty;
    private transient StringProperty typeCongeProperty;
    private transient ObjectProperty<StatutConge> statutProperty;
    private transient IntegerProperty dureeJoursProperty;

    // Enum for leave status
    public enum StatutConge {
        EN_ATTENTE("En Attente"),
        APPROUVE("Approuvé"),
        REJETE("Rejeté");

        private final String label;

        StatutConge(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Constructors
    public Conge() {
        this.statut = StatutConge.EN_ATTENTE;
    }

    public Conge(LocalDate dateDebut, LocalDate dateFin, String typeConge, Employe employe) {
        this();
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.typeConge = typeConge;
        this.employe = employe;
        this.dureeJours = calculerDuree();
    }

    // JavaFX Property Getters
    public IntegerProperty idProperty() {
        if (idProperty == null) {
            idProperty = new SimpleIntegerProperty(id != null ? id : 0);
        }
        return idProperty;
    }

    public ObjectProperty<LocalDate> dateDebutProperty() {
        if (dateDebutProperty == null) {
            dateDebutProperty = new SimpleObjectProperty<>(dateDebut);
        }
        return dateDebutProperty;
    }

    public ObjectProperty<LocalDate> dateFinProperty() {
        if (dateFinProperty == null) {
            dateFinProperty = new SimpleObjectProperty<>(dateFin);
        }
        return dateFinProperty;
    }

    public StringProperty typeCongeProperty() {
        if (typeCongeProperty == null) {
            typeCongeProperty = new SimpleStringProperty(typeConge);
        }
        return typeCongeProperty;
    }

    public ObjectProperty<StatutConge> statutProperty() {
        if (statutProperty == null) {
            statutProperty = new SimpleObjectProperty<>(statut);
        }
        return statutProperty;
    }

    public IntegerProperty dureeJoursProperty() {
        if (dureeJoursProperty == null) {
            dureeJoursProperty = new SimpleIntegerProperty(dureeJours != null ? dureeJours : 0);
        }
        return dureeJoursProperty;
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

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        if (dateDebutProperty != null) {
            dateDebutProperty.set(dateDebut);
        }
        this.dureeJours = calculerDuree();
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
        if (dateFinProperty != null) {
            dateFinProperty.set(dateFin);
        }
        this.dureeJours = calculerDuree();
    }

    public String getTypeConge() {
        return typeConge;
    }

    public void setTypeConge(String typeConge) {
        this.typeConge = typeConge;
        if (typeCongeProperty != null) {
            typeCongeProperty.set(typeConge);
        }
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public StatutConge getStatut() {
        return statut;
    }

    public void setStatut(StatutConge statut) {
        this.statut = statut;
        if (statutProperty != null) {
            statutProperty.set(statut);
        }
    }

    public Integer getDureeJours() {
        return dureeJours;
    }

    public void setDureeJours(Integer dureeJours) {
        this.dureeJours = dureeJours;
        if (dureeJoursProperty != null) {
            dureeJoursProperty.set(dureeJours);
        }
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    // Business Methods
    public int calculerDuree() {
        if (dateDebut != null && dateFin != null) {
            return (int) ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return 0;
    }

    public boolean estEnAttente() {
        return statut == StatutConge.EN_ATTENTE;
    }

    public boolean estApprouve() {
        return statut == StatutConge.APPROUVE;
    }

    public boolean estRejete() {
        return statut == StatutConge.REJETE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conge conge = (Conge) o;
        return Objects.equals(id, conge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Conge{" +
                "id=" + id +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", typeConge='" + typeConge + '\'' +
                ", statut=" + statut +
                ", dureeJours=" + dureeJours +
                '}';
    }
}
