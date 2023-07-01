package com.mycompany.dao;

import com.mycompany.config.HibernateSessionFactoryUtil;
import com.mycompany.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDaoImpl implements UserDao {
    @Override
    public void add(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.persist(user); // save() method deprecated
        tx1.commit();
        session.close();
    }

    @Override
    public void edit(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.merge(user); // update() method deprecated
        tx1.commit();
        session.close();
    }

    @Override
    public void delete(User user) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        session.remove(user); // delete() method deprecated
        tx1.commit();
        session.close();
    }

    @Override
    public User getByChatId(long chatId) {
        Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        User user = session.get(User.class, chatId);
        session.close();
        return user;
    }
}
