package com.example.userservice.mapper;

import com.example.userservice.dto.RegisterDto;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
    User registerDtoToUser(RegisterDto registerDto);
}
