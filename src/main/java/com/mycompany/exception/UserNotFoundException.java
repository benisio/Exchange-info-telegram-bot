package com.mycompany.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(long id) {
    super(String.format("User with telegram_id = %s not found", id));
  }
}
