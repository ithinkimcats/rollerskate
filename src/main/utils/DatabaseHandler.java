import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DatabaseHandler {

    public void saveRole(CustomRole role) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(role);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void deleteRole(CustomRole role) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(role);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public List<CustomRole> getRoles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from CustomRole", CustomRole.class).list();
        }
    }

    public CustomRole getRoleByUser(long id) {
        CustomRole role = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query query = session.createQuery("from CustomRole C WHERE C.user = :userid");
            query.setParameter("userid", id);
            role = (CustomRole) query.getSingleResult();
            return role;
        } catch (Exception ignored) {

        }
        return role;
    }
}