package com.example.api.controller;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.GetUserDto;
import com.example.api.dto.UpdateUserDto;
import com.example.api.service.UserService;
import com.scalar.db.exception.transaction.TransactionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private static final String PATH_USER_ID = "user_id";

  @Autowired UserService userService;

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public String createUser(@RequestBody CreateUserDto createUserDto) throws TransactionException {
    return userService.createUser(createUserDto);
  }

  @PutMapping("/{user_id}")
  @PreAuthorize("hasRole('ROLE_ADMIN') or principal.userId == #userId")
  @ResponseStatus(HttpStatus.OK)
  public void updateUser(
      @PathVariable(PATH_USER_ID) String userId, @RequestBody UpdateUserDto updateUserDto)
      throws TransactionException {
    userService.updateUser(userId, updateUserDto);
  }

  @DeleteMapping("/{user_id}")
  @PreAuthorize("hasRole('ROLE_ADMIN') or principal.userId == #userId")
  @ResponseStatus(HttpStatus.OK)
  public void deleteUser(@PathVariable(PATH_USER_ID) String userId) throws TransactionException {
    userService.deleteUser(userId);
  }

  @GetMapping("/{user_id}")
  @PreAuthorize("hasRole('ROLE_ADMIN') or principal.userId == #userId")
  @ResponseStatus(HttpStatus.OK)
  public GetUserDto getUser(@PathVariable(PATH_USER_ID) String userId) throws TransactionException {
    return userService.getUser(userId);
  }

  @GetMapping()
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public List<GetUserDto> listUsers() throws TransactionException {
    return userService.listUsers();
  }
}
