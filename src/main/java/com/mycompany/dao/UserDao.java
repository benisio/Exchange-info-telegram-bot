package com.mycompany.dao;

import com.mycompany.model.User;

public interface UserDao {
    void add(User user);

    void edit(User user);

    void delete(User user);

    User getByChatId(long chatId);
}