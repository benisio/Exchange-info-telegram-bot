package com.mycompany.service;

import com.mycompany.entity.User;
import com.mycompany.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public void add(User user) {
        repository.save(user);
    }

    @Override
    public Optional<User> getByChatId(long chatId) {
        return repository.findById(chatId);
    }

    @Override
    public void delete(User user) {
        repository.delete(user);
    }
}