package com.mycompany.service;

import com.mycompany.model.User;

public interface UserService {

    // create
    void add(User user);

    // read
    User getByChatId(long chatId);

    // update
    void update(User user);

    // delete
    void delete(User user);
}