package com.rh.javafx.service;

import com.rh.javafx.model.Employe;
import com.rh.javafx.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class for Employee operations
 */
public class EmployeService {

    /**
     * Save or update an employee
     */
    public void saveEmploye(Employe employe) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(employe);
            transaction.commit();
            System.out.println("Employé sauvegardé: " + employe.getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'employé", e);
        }
    }

    /**
     * Get an employee by ID
     */
    public Employe getEmployeById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Employe.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération de l'employé", e);
        }
    }

    /**
     * Get an employee by matricule
     */
    public Employe getEmployeByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employe> query = session.createQuery(
                    "FROM Employe e WHERE e.matricule = :matricule", Employe.class);
            query.setParameter("matricule", matricule);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération de l'employé", e);
        }
    }

    /**
     * Get all employees
     */
    public List<Employe> getAllEmployes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employe> query = session.createQuery("FROM Employe ORDER BY nom, prenom", Employe.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des employés", e);
        }
    }

    /**
     * Search employees by name
     */
    public List<Employe> searchEmployes(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employe> query = session.createQuery(
                    "FROM Employe e WHERE LOWER(e.nom) LIKE :search OR LOWER(e.prenom) LIKE :search " +
                            "OR LOWER(e.email) LIKE :search OR LOWER(e.matricule) LIKE :search",
                    Employe.class);
            query.setParameter("search", "%" + searchTerm.toLowerCase() + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la recherche d'employés", e);
        }
    }

    /**
     * Get employees by department
     */
    public List<Employe> getEmployesByDepartement(String departement) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employe> query = session.createQuery(
                    "FROM Employe e WHERE e.departement = :dept", Employe.class);
            query.setParameter("dept", departement);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des employés", e);
        }
    }

    /**
     * Delete an employee
     */
    public void deleteEmploye(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Employe employe = session.get(Employe.class, id);
            if (employe != null) {
                session.delete(employe);
                System.out.println("Employé supprimé: " + employe.getNomComplet());
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'employé", e);
        }
    }

    /**
     * Get total number of employees
     */
    public long getTotalEmployes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(e) FROM Employe e", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get average salary
     */
    public BigDecimal getAverageSalary() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT AVG(e.salaireBase) FROM Employe e", BigDecimal.class);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get total salary budget
     */
    public BigDecimal getTotalSalaryBudget() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(e.salaireBase) FROM Employe e", BigDecimal.class);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }
}
