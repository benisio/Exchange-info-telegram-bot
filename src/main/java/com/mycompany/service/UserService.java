package com.mycompany.service;

import com.mycompany.entity.User;

import java.util.Optional;

public interface UserService {

    // create
    void add(User user);

    // read
    Optional<User> getByChatId(long chatId);

    // delete
    void delete(User user);
}