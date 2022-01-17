package com.example.api.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.api.dto.CreateUserDto;
import com.example.api.dto.UpdateUserDto;
import com.example.api.repository.UserRepository;
import com.example.api.security.SpringSecurityUtil.WithCustomMockUser;
import com.example.api.service.AuthenticationService;
import com.example.api.service.UserService;
import com.example.api.util.UserStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.api.DistributedTransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration
@WebMvcTest(UserController.class)
public class UserControllerTest {
  private static final String BASE_URL_PATH = "/users";
  private static final String MOCKED_USER_ID = "6695bdd7-ccb3-0468-35af-e804f79329b2";

  private MockMvc mockMvc;
  @MockBean private UserService userService;
  @MockBean private UserRepository userRepository;
  @MockBean DistributedTransactionManager manager;
  @Autowired UserController userController;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private WebApplicationContext context;
  @MockBean private AuthenticationService authenticationService;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @WithAnonymousUser
  void createUser_byAnonymousUser_shouldSuccess() throws Exception {
    CreateUserDto createUserDto = UserStub.getCreateUserDto();

    when(userService.createUser(createUserDto)).thenReturn(MOCKED_USER_ID);

    mockMvc
        .perform(
            post(BASE_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithCustomMockUser(role = "ROLE_ADMIN")
  void updateUser_byAdminUser_shouldSuccess() throws Exception {
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();

    mockMvc
        .perform(
            put(BASE_URL_PATH + "/" + MOCKED_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(userId = MOCKED_USER_ID)
  void updateUser_byOwnSelf_shouldSuccess() throws Exception {
    UpdateUserDto updateUserDto = UserStub.getUpdateUserDto();

    mockMvc
        .perform(
            put(BASE_URL_PATH + "/" + MOCKED_USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(role = "ROLE_ADMIN")
  void deleteUser_byAdminUser_shouldSuccess() throws Exception {
    mockMvc.perform(delete(BASE_URL_PATH + "/" + MOCKED_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(userId = MOCKED_USER_ID)
  void deleteUser_byOwnSelfUser_shouldSuccess() throws Exception {
    mockMvc.perform(delete(BASE_URL_PATH + "/" + MOCKED_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(userId = "inValidUserId")
  void deleteUser_byInvalidUser_thenAccessDenied() throws Exception {
    assertThrows(AccessDeniedException.class, () -> userController.deleteUser(MOCKED_USER_ID));
  }

  @Test
  @WithCustomMockUser(userId = MOCKED_USER_ID)
  void getUser_byOwnSelf_shouldSuccess() throws Exception {
    mockMvc.perform(get(BASE_URL_PATH + "/" + MOCKED_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(role = "ROLE_ADMIN")
  void getUser_byAdminUser_shouldSuccess() throws Exception {
    mockMvc.perform(get(BASE_URL_PATH + "/" + MOCKED_USER_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(userId = "inValidUserId")
  void getUser_byInvalidUser_thenAccessDenied() throws Exception {
    assertThrows(AccessDeniedException.class, () -> userController.getUser(MOCKED_USER_ID));
  }

  @Test
  @WithCustomMockUser(role = "ROLE_ADMIN")
  void listUsers_shouldSuccess() throws Exception {
    mockMvc.perform(get(BASE_URL_PATH)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser
  void lisUsers_byGeneralUser_thenAccessDenied() throws Exception {
    assertThrows(AccessDeniedException.class, () -> userController.listUsers());
  }
}
