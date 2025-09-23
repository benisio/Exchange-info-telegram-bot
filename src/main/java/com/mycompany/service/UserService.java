package com.mycompany.service;

import com.mycompany.entity.User;

import java.util.List;

public interface UserService {

  void save(User user);

  User getById(long chatId);

  void delete(User user);

  List<Long> getAllUserIds();

  boolean exists(long id);
}