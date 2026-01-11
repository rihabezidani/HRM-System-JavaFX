package com.rh.javafx.service;

import com.rh.javafx.model.BulletinPaie;
import com.rh.javafx.model.Employe;
import com.rh.javafx.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class for Payslip (BulletinPaie) operations
 */
public class BulletinPaieService {

    /**
     * Generate a payslip for an employee
     */
    public BulletinPaie genererBulletin(Integer employeId, String periode, BigDecimal primes, BigDecimal retenues) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Employe employe = session.get(Employe.class, employeId);
            if (employe == null) {
                throw new RuntimeException("Employé non trouvé");
            }

            BulletinPaie bulletin = new BulletinPaie(periode, employe.getSalaireBase(), employe);
            bulletin.setPrimes(primes != null ? primes : BigDecimal.ZERO);
            bulletin.setRetenues(retenues != null ? retenues : BigDecimal.ZERO);
            bulletin.setDateEmission(LocalDate.now());
            bulletin.setSalaireNet(bulletin.calculerNet());

            session.save(bulletin);
            transaction.commit();

            System.out.println("Bulletin de paie généré pour: " + employe.getNomComplet() +
                    " | Période: " + periode);
            return bulletin;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du bulletin", e);
        }
    }

    /**
     * Update a payslip
     */
    public void updateBulletin(BulletinPaie bulletin) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            bulletin.setSalaireNet(bulletin.calculerNet());
            session.update(bulletin);
            transaction.commit();
            System.out.println("Bulletin de paie mis à jour");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour du bulletin", e);
        }
    }

    /**
     * Get a payslip by ID
     */
    public BulletinPaie getBulletinById(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BulletinPaie.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération du bulletin", e);
        }
    }

    /**
     * Get all payslips
     */
    public List<BulletinPaie> getAllBulletins() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BulletinPaie> query = session.createQuery(
                    "FROM BulletinPaie b JOIN FETCH b.employe ORDER BY b.dateEmission DESC",
                    BulletinPaie.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bulletins", e);
        }
    }

    /**
     * Get payslips by employee
     */
    public List<BulletinPaie> getBulletinsByEmploye(Integer employeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BulletinPaie> query = session.createQuery(
                    "FROM BulletinPaie b WHERE b.employe.id = :empId ORDER BY b.dateEmission DESC",
                    BulletinPaie.class);
            query.setParameter("empId", employeId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bulletins", e);
        }
    }

    /**
     * Get payslips by period
     */
    public List<BulletinPaie> getBulletinsByPeriode(String periode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BulletinPaie> query = session.createQuery(
                    "FROM BulletinPaie b JOIN FETCH b.employe WHERE b.periode = :periode",
                    BulletinPaie.class);
            query.setParameter("periode", periode);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bulletins", e);
        }
    }

    /**
     * Delete a payslip
     */
    public void deleteBulletin(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BulletinPaie bulletin = session.get(BulletinPaie.class, id);
            if (bulletin != null) {
                session.delete(bulletin);
                System.out.println("Bulletin de paie supprimé");
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression du bulletin", e);
        }
    }

    /**
     * Get total payslips count
     */
    public long getTotalBulletinsCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(b) FROM BulletinPaie b", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get total salary paid for a period
     */
    public BigDecimal getTotalSalaryForPeriode(String periode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(b.salaireNet) FROM BulletinPaie b WHERE b.periode = :periode",
                    BigDecimal.class);
            query.setParameter("periode", periode);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get total gross salary for a period
     */
    public BigDecimal getTotalSalaireBrutPeriode(String periode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(b.salaireBrut) FROM BulletinPaie b WHERE b.periode = :periode",
                    BigDecimal.class);
            query.setParameter("periode", periode);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get total deductions for a period
     */
    public BigDecimal getTotalRetenues(String periode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(b.retenues) FROM BulletinPaie b WHERE b.periode = :periode",
                    BigDecimal.class);
            query.setParameter("periode", periode);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get total bonuses for a period
     */
    public BigDecimal getTotalPrimes(String periode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(b.primes) FROM BulletinPaie b WHERE b.periode = :periode",
                    BigDecimal.class);
            query.setParameter("periode", periode);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get payslips ordered by net salary (descending)
     */
    public List<BulletinPaie> getBulletinsOrderedBySalaireNet() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BulletinPaie> query = session.createQuery(
                    "FROM BulletinPaie b JOIN FETCH b.employe ORDER BY b.salaireNet DESC",
                    BulletinPaie.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bulletins", e);
        }
    }

    /**
     * Get payslips grouped by period
     */
    public Map<String, List<BulletinPaie>> getBulletinsGroupedByPeriode() {
        List<BulletinPaie> bulletins = getAllBulletins();
        return bulletins.stream()
                .collect(Collectors.groupingBy(BulletinPaie::getPeriode));
    }

    /**
     * Get count of payslips by period
     */
    public Map<String, Long> getBulletinsCountByPeriode() {
        List<BulletinPaie> bulletins = getAllBulletins();
        return bulletins.stream()
                .collect(Collectors.groupingBy(
                        BulletinPaie::getPeriode,
                        Collectors.counting()
                ));
    }

    /**
     * Get average net salary
     */
    public BigDecimal getAverageSalaireNet() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                    "SELECT AVG(b.salaireNet) FROM BulletinPaie b", Double.class);
            Double result = query.uniqueResult();
            return result != null ? BigDecimal.valueOf(result) : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get payslips with net salary above minimum
     */
    public List<BulletinPaie> getBulletinsBySalaireNetMin(BigDecimal minSalaire) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BulletinPaie> query = session.createQuery(
                    "FROM BulletinPaie b JOIN FETCH b.employe WHERE b.salaireNet >= :minSalaire ORDER BY b.salaireNet DESC",
                    BulletinPaie.class);
            query.setParameter("minSalaire", minSalaire);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des bulletins", e);
        }
    }

    /**
     * Get total wage bill (all net salaries)
     */
    public BigDecimal getTotalMasseSalariale() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BigDecimal> query = session.createQuery(
                    "SELECT SUM(b.salaireNet) FROM BulletinPaie b", BigDecimal.class);
            BigDecimal result = query.uniqueResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    /**
     * Count payslips by employee
     */
    public long countBulletinsByEmploye(Integer employeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(b) FROM BulletinPaie b WHERE b.employe.id = :empId", Long.class);
            query.setParameter("empId", employeId);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Display payslip receipt
     */
    public String afficherRecu(Integer bulletinId) {
        BulletinPaie bulletin = getBulletinById(bulletinId);
        if (bulletin != null) {
            return bulletin.genererRecu();
        }
        return "Bulletin non trouvé";
    }
}
