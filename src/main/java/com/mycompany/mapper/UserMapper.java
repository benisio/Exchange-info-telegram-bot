package com.mycompany.mapper;

import com.mycompany.dto.UserDto;
import com.mycompany.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring") // или "default",  если без Spring
public interface UserMapper {

  UserDto toDto(User entity);

  User toEntity(UserDto dto);

  List<UserDto> toDtoList(List<User> entities);
  List<User> toEntityList(List<UserDto> dtos);
}
