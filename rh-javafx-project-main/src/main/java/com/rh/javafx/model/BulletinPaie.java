package com.rh.javafx.model;

import javafx.beans.property.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * BulletinPaie Entity - Represents a payslip
 */
@Entity
@Table(name = "bulletin_paie")
public class BulletinPaie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "periode", nullable = false, length = 20)
    private String periode;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "salaire_brut", precision = 10, scale = 2, nullable = false)
    private BigDecimal salaireBrut;

    @Column(name = "retenues", precision = 10, scale = 2)
    private BigDecimal retenues;

    @Column(name = "primes", precision = 10, scale = 2)
    private BigDecimal primes;

    @Column(name = "salaire_net", precision = 10, scale = 2)
    private BigDecimal salaireNet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    // JavaFX Properties
    private transient IntegerProperty idProperty;
    private transient StringProperty periodeProperty;
    private transient ObjectProperty<LocalDate> dateEmissionProperty;
    private transient ObjectProperty<BigDecimal> salaireBrutProperty;
    private transient ObjectProperty<BigDecimal> retenuesProperty;
    private transient ObjectProperty<BigDecimal> primesProperty;
    private transient ObjectProperty<BigDecimal> salaireNetProperty;

    // Constructors
    public BulletinPaie() {
        this.retenues = BigDecimal.ZERO;
        this.primes = BigDecimal.ZERO;
        this.dateEmission = LocalDate.now();
    }

    public BulletinPaie(String periode, BigDecimal salaireBrut, Employe employe) {
        this();
        this.periode = periode;
        this.salaireBrut = salaireBrut;
        this.employe = employe;
        this.salaireNet = calculerNet();
    }

    // JavaFX Property Getters
    public IntegerProperty idProperty() {
        if (idProperty == null) {
            idProperty = new SimpleIntegerProperty(id != null ? id : 0);
        }
        return idProperty;
    }

    public StringProperty periodeProperty() {
        if (periodeProperty == null) {
            periodeProperty = new SimpleStringProperty(periode);
        }
        return periodeProperty;
    }

    public ObjectProperty<LocalDate> dateEmissionProperty() {
        if (dateEmissionProperty == null) {
            dateEmissionProperty = new SimpleObjectProperty<>(dateEmission);
        }
        return dateEmissionProperty;
    }

    public ObjectProperty<BigDecimal> salaireBrutProperty() {
        if (salaireBrutProperty == null) {
            salaireBrutProperty = new SimpleObjectProperty<>(salaireBrut);
        }
        return salaireBrutProperty;
    }

    public ObjectProperty<BigDecimal> retenuesProperty() {
        if (retenuesProperty == null) {
            retenuesProperty = new SimpleObjectProperty<>(retenues);
        }
        return retenuesProperty;
    }

    public ObjectProperty<BigDecimal> primesProperty() {
        if (primesProperty == null) {
            primesProperty = new SimpleObjectProperty<>(primes);
        }
        return primesProperty;
    }

    public ObjectProperty<BigDecimal> salaireNetProperty() {
        if (salaireNetProperty == null) {
            salaireNetProperty = new SimpleObjectProperty<>(salaireNet);
        }
        return salaireNetProperty;
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

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
        if (periodeProperty != null) {
            periodeProperty.set(periode);
        }
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
        if (dateEmissionProperty != null) {
            dateEmissionProperty.set(dateEmission);
        }
    }

    public BigDecimal getSalaireBrut() {
        return salaireBrut;
    }

    public void setSalaireBrut(BigDecimal salaireBrut) {
        this.salaireBrut = salaireBrut;
        if (salaireBrutProperty != null) {
            salaireBrutProperty.set(salaireBrut);
        }
        this.salaireNet = calculerNet();
    }

    public BigDecimal getRetenues() {
        return retenues;
    }

    public void setRetenues(BigDecimal retenues) {
        this.retenues = retenues;
        if (retenuesProperty != null) {
            retenuesProperty.set(retenues);
        }
        this.salaireNet = calculerNet();
    }

    public BigDecimal getPrimes() {
        return primes;
    }

    public void setPrimes(BigDecimal primes) {
        this.primes = primes;
        if (primesProperty != null) {
            primesProperty.set(primes);
        }
        this.salaireNet = calculerNet();
    }

    public BigDecimal getSalaireNet() {
        return salaireNet;
    }

    public void setSalaireNet(BigDecimal salaireNet) {
        this.salaireNet = salaireNet;
        if (salaireNetProperty != null) {
            salaireNetProperty.set(salaireNet);
        }
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }

    // Business Methods
    public BigDecimal calculerNet() {
        if (salaireBrut == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = salaireBrut;
        if (primes != null) {
            total = total.add(primes);
        }
        if (retenues != null) {
            total = total.subtract(retenues);
        }
        return total;
    }

    public String genererRecu() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder recu = new StringBuilder();
        recu.append("======================================\n");
        recu.append("       BULLETIN DE PAIE\n");
        recu.append("======================================\n\n");
        recu.append("Employé: ").append(employe.getNomComplet()).append("\n");
        recu.append("Matricule: ").append(employe.getMatricule()).append("\n");
        recu.append("Période: ").append(periode).append("\n");
        recu.append("Date d'émission: ").append(dateEmission.format(formatter)).append("\n\n");
        recu.append("--------------------------------------\n");
        recu.append("Salaire Brut: ").append(String.format("%,.2f", salaireBrut)).append(" DH\n");
        recu.append("Primes: ").append(String.format("%,.2f", primes)).append(" DH\n");
        recu.append("Retenues: ").append(String.format("%,.2f", retenues)).append(" DH\n");
        recu.append("--------------------------------------\n");
        recu.append("Salaire Net: ").append(String.format("%,.2f", salaireNet)).append(" DH\n");
        recu.append("======================================\n");
        return recu.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BulletinPaie that = (BulletinPaie) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BulletinPaie{" +
                "id=" + id +
                ", periode='" + periode + '\'' +
                ", salaireBrut=" + salaireBrut +
                ", salaireNet=" + salaireNet +
                '}';
    }
}
