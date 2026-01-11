package com.rh.javafx.service;

import com.rh.javafx.model.Employe;
import com.rh.javafx.model.ResponsableRH;
import com.rh.javafx.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service class for Authentication operations
 */
public class AuthService {

    public enum UserType {
        HR_MANAGER,
        EMPLOYE
    }

    private static ResponsableRH currentUser;
    private static Employe currentEmploye;
    private static UserType currentUserType;

    /**
     * Login with email and password - tries HR manager first, then employee
     */
    public boolean login(String email, String password) {
        // Try HR manager login first
        if (loginAsHR(email, password)) {
            return true;
        }
        // Try employee login
        return loginAsEmploye(email, password);
    }

    /**
     * Login as HR Manager
     */
    private boolean loginAsHR(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ResponsableRH> query = session.createQuery(
                    "FROM ResponsableRH r WHERE r.email = :email", ResponsableRH.class);
            query.setParameter("email", email);
            ResponsableRH responsable = query.uniqueResult();

            if (responsable != null) {
                String hashedPassword = hashPassword(password);
                if (responsable.getMotDePasseHash().equals(hashedPassword)) {
                    currentUser = responsable;
                    currentEmploye = null;
                    currentUserType = UserType.HR_MANAGER;
                    System.out.println("Connexion réussie (RH): " + responsable.getNomComplet());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Login as Employee
     */
    private boolean loginAsEmploye(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Employe> query = session.createQuery(
                    "FROM Employe e WHERE e.email = :email", Employe.class);
            query.setParameter("email", email);
            Employe employe = query.uniqueResult();

            if (employe != null && employe.getMotDePasseHash() != null) {
                String hashedPassword = hashPassword(password);
                if (employe.getMotDePasseHash().equals(hashedPassword)) {
                    currentEmploye = employe;
                    currentUser = null;
                    currentUserType = UserType.EMPLOYE;
                    System.out.println("Connexion réussie (Employé): " + employe.getNomComplet());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new HR manager account
     */
    public void createAccount(String nom, String prenom, String email, String password, String telephone) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Check if email already exists
            Query<Long> checkQuery = session.createQuery(
                    "SELECT COUNT(r) FROM ResponsableRH r WHERE r.email = :email", Long.class);
            checkQuery.setParameter("email", email);
            if (checkQuery.uniqueResult() > 0) {
                throw new RuntimeException("Un compte avec cet email existe déjà");
            }

            ResponsableRH responsable = new ResponsableRH(nom, prenom, email, hashPassword(password));
            responsable.setTelephone(telephone);

            session.save(responsable);
            transaction.commit();
            System.out.println("Compte créé avec succès pour: " + responsable.getNomComplet());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création du compte", e);
        }
    }

    /**
     * Get the currently logged-in HR user
     */
    public static ResponsableRH getCurrentUser() {
        return currentUser;
    }

    /**
     * Get the currently logged-in employee
     */
    public static Employe getCurrentEmploye() {
        return currentEmploye;
    }

    /**
     * Get the current user type
     */
    public static UserType getCurrentUserType() {
        return currentUserType;
    }

    /**
     * Logout the current user
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("Déconnexion de: " + currentUser.getNomComplet());
        } else if (currentEmploye != null) {
            System.out.println("Déconnexion de: " + currentEmploye.getNomComplet());
        }
        currentUser = null;
        currentEmploye = null;
        currentUserType = null;
    }

    /**
     * Check if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null || currentEmploye != null;
    }

    /**
     * Check if current user is HR manager
     */
    public static boolean isHRManager() {
        return currentUserType == UserType.HR_MANAGER;
    }

    /**
     * Check if current user is employee
     */
    public static boolean isEmploye() {
        return currentUserType == UserType.EMPLOYE;
    }

    /**
     * Hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    /**
     * Initialize default admin account if no users exist
     */
    public void initializeDefaultAdmin() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(r) FROM ResponsableRH r", Long.class);
            long count = query.uniqueResult();

            if (count == 0) {
                System.out.println("Aucun compte trouvé. Création du compte admin par défaut...");
                createAccount("Admin", "RH", "admin@rh.com", "admin123", "0600000000");
                System.out.println("Compte admin créé: admin@rh.com / admin123");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
