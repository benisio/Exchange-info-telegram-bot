package com.mycompany.service;

import com.mycompany.entity.User;
import com.mycompany.exception.UserNotFoundException;
import com.mycompany.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public User getById(long userId) {
        return repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }

  @Override
  public List<Long> getAllUserIds() {
    return repository.findAll().stream().map(User::getId).toList();
  }

  @Override
  public boolean exists(long id) {
    return repository.existsById(id);
  }
}