package vn.iotstar.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Predicate;
import vn.iotstar.config.JpaConfig;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.entity.User;
import vn.iotstar.entity.User.OtpPurpose;

public class UserDao implements IUserDao {
    private static final int OTP_MINUTES = 5;
    private static final int OTP_RESEND_SECONDS = 30;
    private static final int OTP_MAX_ATTEMPTS = 5;

    @Override
    public void insert(User user) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            em.persist(user);
            trans.commit();
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean issueOtp(int userId, OtpPurpose purpose, String code) {
        return withLockedUser(userId, purpose, user -> {
            LocalDateTime now = LocalDateTime.now();
            if (user.getOtpPurpose() == purpose && user.getOtpExpiry() != null
                    && user.getOtpExpiry().minusMinutes(OTP_MINUTES).plusSeconds(OTP_RESEND_SECONDS).isAfter(now)) {
                return false;
            }
            user.setOtpCode(code);
            user.setOtpExpiry(now.plusMinutes(OTP_MINUTES));
            user.setOtpPurpose(purpose);
            user.setOtpAttempts(0);
            return true;
        });
    }

    @Override
    public boolean consumeOtp(int userId, OtpPurpose purpose, String code, String newPasswordHash) {
        return withLockedUser(userId, purpose, user -> {
            if (user.getOtpPurpose() != purpose || user.getOtpCode() == null
                    || user.getOtpExpiry() == null || !user.getOtpExpiry().isAfter(LocalDateTime.now())
                    || user.getOtpAttempts() >= OTP_MAX_ATTEMPTS) {
                return false;
            }
            if (!user.getOtpCode().equals(code)) {
                user.setOtpAttempts(user.getOtpAttempts() + 1);
                return false;
            }
            if (purpose == OtpPurpose.ACTIVATION) {
                user.setActive(true);
            } else {
                user.setPassword(newPasswordHash);
            }
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            user.setOtpPurpose(null);
            user.setOtpAttempts(0);
            return true;
        });
    }

    private boolean withLockedUser(int userId, OtpPurpose purpose, Predicate<User> operation) {
        EntityManager em = JpaConfig.getEntityManager();
        EntityTransaction trans = em.getTransaction();
        try {
            trans.begin();
            User user = em.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
            boolean changed = user != null && user.isActive() == (purpose == OtpPurpose.PASSWORD_RESET)
                    && operation.test(user);
            trans.commit();
            return changed;
        } catch (Exception e) {
            if (trans.isActive()) trans.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public User findByUsername(String username) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public User findByEmail(String email) {
        EntityManager em = JpaConfig.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE lower(u.email) = :email", User.class);
            query.setParameter("email", email.toLowerCase(Locale.ROOT));
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
