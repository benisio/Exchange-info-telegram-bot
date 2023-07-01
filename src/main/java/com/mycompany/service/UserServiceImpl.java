package com.mycompany.service;

import com.mycompany.dao.UserDao;
import com.mycompany.dao.UserDaoImpl;
import com.mycompany.model.User;

public class UserServiceImpl implements UserService {

    private UserDao userDao = new UserDaoImpl();

    @Override
    public void add(User user) {
        userDao.add(user);
    }

    @Override
    public User getByChatId(long chatId) {
        return userDao.getByChatId(chatId);
    }

    @Override
    public void update(User user) {
        userDao.edit(user);
    }

    @Override
    public void delete(User user) {
        userDao.delete(user);
    }
}