package com.example.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.repository.UserRepository;
import com.example.api.security.SpringSecurityUtil.WithCustomMockUser;
import com.example.api.service.AuthenticationService;
import com.example.api.service.GroupService;
import com.example.api.util.GroupStub;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scalar.db.api.DistributedTransactionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration
@WebMvcTest(GroupController.class)
public class GroupControllerTest {
  private static final String BASE_URL_PATH = "/groups";
  private static final String GROUP_USERS_URL_PATH = "/group-users";
  private static final String MOCKED_GROUP_ID = "d8484f03-4a96-e137-12a0-25a794c4b622";
  private static final String MOCKED_USER_ID = "6695bdd7-ccb3-0468-35af-e804f79329b2";
  private static final String MOCKED_TYPE = "mockedType";
  private static final String MOCKED_GROUP_NAME = "mockedGroupName";

  private MockMvc mockMvc;
  @MockBean private GroupService groupService;
  @MockBean private UserRepository userRepository;
  @MockBean private DistributedTransactionManager manager;
  @Autowired GroupController groupController;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private WebApplicationContext context;
  @MockBean private AuthenticationService authenticationService;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  @WithCustomMockUser(role = "ROLE_USER")
  void createGroup_byGeneralUser() throws Exception {
    CreateGroupDto createGroupDto = GroupStub.getCreateGroupDto();

    mockMvc
        .perform(
            post(BASE_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createGroupDto)))
        .andExpect(status().isCreated());
  }

  @Test
  @WithCustomMockUser(groupId = MOCKED_GROUP_ID)
  void addGroupUser_byGroupUser_shouldSuccess() throws Exception {
    GroupUserDto groupUserDto = GroupStub.getGroupUserDto(MOCKED_USER_ID);
    mockMvc
        .perform(
            put(BASE_URL_PATH + "/" + MOCKED_GROUP_ID + GROUP_USERS_URL_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUserDto)))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser
  void addGroupUser_byNonGroupUser_thenAccessDenied() throws Exception {
    GroupUserDto groupUserDto = GroupStub.getGroupUserDto(MOCKED_USER_ID);

    assertThrows(
        AccessDeniedException.class,
        () -> groupController.addGroupUsers(MOCKED_GROUP_ID, groupUserDto));
  }

  @Test
  @WithCustomMockUser(groupId = MOCKED_GROUP_ID)
  void deleteGroupUser_byGroupUser_shouldSuccess() throws Exception {
    mockMvc
        .perform(
            put(
                BASE_URL_PATH
                    + "/"
                    + MOCKED_GROUP_ID
                    + GROUP_USERS_URL_PATH
                    + "/"
                    + MOCKED_USER_ID))
        .andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser
  void deleteGroupUser_byNonGroupUser_thenAccessDenied() throws Exception {
    assertThrows(
        AccessDeniedException.class,
        () -> groupController.deleteGroupUser(MOCKED_GROUP_ID, MOCKED_USER_ID));
  }

  @Test
  @WithCustomMockUser(groupId = MOCKED_GROUP_ID)
  void listGroupUsers_byGroupUser_shouldSuccess() throws Exception {
    List<GroupUserDto> groupUsers =
        new ArrayList<GroupUserDto>(Arrays.asList(GroupStub.getGroupUserDto(MOCKED_USER_ID)));
    when(groupService.listGroupUsers(MOCKED_GROUP_ID)).thenReturn(groupUsers);

    mockMvc
        .perform(get(BASE_URL_PATH + "/" + MOCKED_GROUP_ID + GROUP_USERS_URL_PATH))
        .andExpect(status().isOk());

    List<GroupUserDto> actualGroupUsers = groupController.listGroupUsers(MOCKED_GROUP_ID);

    assertEquals(1, actualGroupUsers.size());
    assertEquals(MOCKED_TYPE, actualGroupUsers.get(0).getType());
  }

  @Test
  @WithCustomMockUser
  void listGroupUsers_byNonGroupUser_thenAccessDenied() throws Exception {
    assertThrows(
        AccessDeniedException.class, () -> groupController.listGroupUsers(MOCKED_GROUP_ID));
  }

  @Test
  @WithCustomMockUser(groupId = MOCKED_GROUP_ID)
  void deleteGroup_byGroupUser_shouldSuccess() throws Exception {
    mockMvc.perform(delete(BASE_URL_PATH + "/" + MOCKED_GROUP_ID)).andExpect(status().isOk());
  }

  @Test
  @WithCustomMockUser(role = "ROLE_USER")
  void listGroups_byGeneralUser_shouldSuccess() throws Exception {
    List<GetGroupDto> groups =
        new ArrayList<GetGroupDto>(Arrays.asList(GroupStub.getGetGroupDto(MOCKED_GROUP_ID)));
    when(groupService.listGroups()).thenReturn(groups);

    mockMvc.perform(get(BASE_URL_PATH)).andExpect(status().isOk());

    List<GetGroupDto> actualGroups = groupController.listGroups();

    assertEquals(1, actualGroups.size());
    assertEquals(MOCKED_GROUP_NAME, actualGroups.get(0).getGroupName());
  }
}
