package com.rh.javafx.service;

import com.rh.javafx.model.Conge;
import com.rh.javafx.model.Conge.StatutConge;
import com.rh.javafx.model.Employe;
import com.rh.javafx.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Leave (Conge) operations
 */
public class CongeService {

    private final EmployeService employeService = new EmployeService();

    /**
     * Request a new leave
     */
    public void demanderConge(Integer employeId, LocalDate dateDebut, LocalDate dateFin, String typeConge, String motif) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Employe employe = session.get(Employe.class, employeId);
            if (employe == null) {
                throw new RuntimeException("Employé non trouvé");
            }

            Conge conge = new Conge(dateDebut, dateFin, typeConge, employe);
            conge.setMotif(motif);

            int duree = conge.calculerDuree();
            if (duree > employe.getJoursCongesRestants()) {
                throw new RuntimeException("Solde de congés insuffisant. Disponible: " +
                        employe.getJoursCongesRestants() + " jours, Demandé: " + duree + " jours");
            }

            session.save(conge);
            transaction.commit();
            System.out.println("Demande de congé créée pour: " + employe.getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la demande de congé", e);
        }
    }

    /**
     * Approve a leave request
     */
    public void approuverConge(Integer congeId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Conge conge = session.get(Conge.class, congeId);
            if (conge == null) {
                throw new RuntimeException("Congé non trouvé");
            }

            if (conge.getStatut() != StatutConge.EN_ATTENTE) {
                throw new RuntimeException("Ce congé a déjà été traité");
            }

            Employe employe = conge.getEmploye();
            int duree = conge.getDureeJours();

            if (duree > employe.getJoursCongesRestants()) {
                throw new RuntimeException("Solde de congés insuffisant");
            }

            conge.setStatut(StatutConge.APPROUVE);
            employe.setJoursCongesRestants(employe.getJoursCongesRestants() - duree);

            session.update(conge);
            session.update(employe);
            transaction.commit();

            System.out.println("Congé approuvé pour: " + employe.getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'approbation du congé", e);
        }
    }

    /**
     * Reject a leave request
     */
    public void rejeterConge(Integer congeId, String motifRejet) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Conge conge = session.get(Conge.class, congeId);
            if (conge == null) {
                throw new RuntimeException("Congé non trouvé");
            }

            if (conge.getStatut() != StatutConge.EN_ATTENTE) {
                throw new RuntimeException("Ce congé a déjà été traité");
            }

            conge.setStatut(StatutConge.REJETE);
            conge.setMotif(conge.getMotif() + " | Motif de rejet: " + motifRejet);

            session.update(conge);
            transaction.commit();

            System.out.println("Congé rejeté pour: " + conge.getEmploye().getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors du rejet du congé", e);
        }
    }

    /**
     * Get all leaves
     */
    public List<Conge> getAllConges() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c JOIN FETCH c.employe ORDER BY c.dateDebut DESC", Conge.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }
    }

    /**
     * Get pending leaves
     */
    public List<Conge> getPendingConges() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c JOIN FETCH c.employe WHERE c.statut = :statut ORDER BY c.dateDebut",
                    Conge.class);
            query.setParameter("statut", StatutConge.EN_ATTENTE);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés en attente", e);
        }
    }

    /**
     * Get leaves by employee
     */
    public List<Conge> getCongesByEmploye(Integer employeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c WHERE c.employe.id = :empId ORDER BY c.dateDebut DESC", Conge.class);
            query.setParameter("empId", employeId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }
    }

    /**
     * Get leaves by status
     */
    public List<Conge> getCongesByStatut(StatutConge statut) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c JOIN FETCH c.employe WHERE c.statut = :statut ORDER BY c.dateDebut DESC",
                    Conge.class);
            query.setParameter("statut", statut);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés", e);
        }
    }

    /**
     * Delete a leave request
     */
    public void deleteConge(Integer congeId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Conge conge = session.get(Conge.class, congeId);
            if (conge != null) {
                // If the leave was approved, restore the days
                if (conge.getStatut() == StatutConge.APPROUVE) {
                    Employe employe = conge.getEmploye();
                    employe.setJoursCongesRestants(
                            employe.getJoursCongesRestants() + conge.getDureeJours());
                    session.update(employe);
                }
                session.delete(conge);
                System.out.println("Congé supprimé");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du congé", e);
        }
    }

    /**
     * Get count of pending leaves
     */
    public long getPendingCongesCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(c) FROM Conge c WHERE c.statut = :statut", Long.class);
            query.setParameter("statut", StatutConge.EN_ATTENTE);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update an existing leave request
     */
    public void updateConge(Conge conge) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Conge existingConge = session.get(Conge.class, conge.getId());
            if (existingConge == null) {
                throw new RuntimeException("Congé non trouvé");
            }

            // Only allow updates if the leave is still pending
            if (existingConge.getStatut() != StatutConge.EN_ATTENTE) {
                throw new RuntimeException("Seuls les congés en attente peuvent être modifiés");
            }

            existingConge.setDateDebut(conge.getDateDebut());
            existingConge.setDateFin(conge.getDateFin());
            existingConge.setTypeConge(conge.getTypeConge());
            existingConge.setMotif(conge.getMotif());
            existingConge.setDureeJours(conge.calculerDuree());

            session.update(existingConge);
            transaction.commit();

            System.out.println("Congé mis à jour pour: " + existingConge.getEmploye().getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du congé", e);
        }
    }

    /**
     * Get total approved leave days
     */
    public int getTotalJoursConge() {
        List<Conge> conges = getAllConges();
        return conges.stream()
                .filter(c -> c.getStatut() == StatutConge.APPROUVE)
                .mapToInt(Conge::getDureeJours)
                .sum();
    }

    /**
     * Get average leave duration
     */
    public double getAverageDureeConge() {
        List<Conge> conges = getAllConges();
        return conges.stream()
                .filter(c -> c.getStatut() == StatutConge.APPROUVE)
                .mapToInt(Conge::getDureeJours)
                .average()
                .orElse(0.0);
    }

    /**
     * Get count of leaves grouped by type
     */
    public Map<String, Long> getCongesCountByType() {
        List<Conge> conges = getAllConges();
        return conges.stream()
                .collect(Collectors.groupingBy(
                        Conge::getTypeConge,
                        Collectors.counting()
                ));
    }

    /**
     * Get leaves within a date range
     */
    public List<Conge> getCongesByDateRange(LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c JOIN FETCH c.employe WHERE c.dateDebut >= :startDate AND c.dateFin <= :endDate ORDER BY c.dateDebut",
                    Conge.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés par date", e);
        }
    }

    /**
     * Count approved leaves
     */
    public long countCongesApprouves() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(c) FROM Conge c WHERE c.statut = :statut", Long.class);
            query.setParameter("statut", StatutConge.APPROUVE);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get leaves grouped by status
     */
    public Map<StatutConge, List<Conge>> getCongesGroupedByStatut() {
        List<Conge> conges = getAllConges();
        return conges.stream()
                .collect(Collectors.groupingBy(Conge::getStatut));
    }

    /**
     * Get leaves ordered by date
     */
    public List<Conge> getCongesOrderedByDate() {
        return getAllConges().stream()
                .sorted((c1, c2) -> c1.getDateDebut().compareTo(c2.getDateDebut()))
                .collect(Collectors.toList());
    }

    /**
     * Get leaves by type
     */
    public List<Conge> getCongesByType(String typeConge) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Conge> query = session.createQuery(
                    "FROM Conge c JOIN FETCH c.employe WHERE c.typeConge = :type ORDER BY c.dateDebut DESC",
                    Conge.class);
            query.setParameter("type", typeConge);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des congés par type", e);
        }
    }
}
