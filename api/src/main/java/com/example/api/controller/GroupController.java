package com.example.api.controller;

import com.example.api.dto.CreateGroupDto;
import com.example.api.dto.GetGroupDto;
import com.example.api.dto.GroupUserDto;
import com.example.api.service.AuthenticationService.AccountUser;
import com.example.api.service.GroupService;
import com.scalar.db.exception.transaction.TransactionException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/groups")
public class GroupController {
  private static final String PATH_GROUP_ID = "group_id";
  private static final String PATH_USER_ID = "user_id";

  @Autowired GroupService groupService;

  @PostMapping()
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
  @ResponseStatus(HttpStatus.CREATED)
  public String createGroup(
      @RequestBody CreateGroupDto createGroupDto, @AuthenticationPrincipal AccountUser user)
      throws TransactionException {
    return groupService.createGroup(createGroupDto, user.getUserId());
  }

  @PutMapping("/{group_id}/group-users")
  @PreAuthorize(
      "hasRole('ROLE_ADMIN') or @webSecurityConfig.isGroupUser(principal.groupIdList, #groupId)")
  @ResponseStatus(HttpStatus.OK)
  public void addGroupUsers(
      @PathVariable(PATH_GROUP_ID) String groupId, @RequestBody GroupUserDto groupUser)
      throws TransactionException {
    groupService.addGroupUser(groupId, groupUser);
  }

  @PutMapping("/{group_id}/group-users/{user_id}")
  @PreAuthorize(
      "hasRole('ROLE_ADMIN') or @webSecurityConfig.isGroupUser(principal.groupIdList, #groupId)")
  @ResponseStatus(HttpStatus.OK)
  public void deleteGroupUser(
      @PathVariable(PATH_GROUP_ID) String groupId, @PathVariable(PATH_USER_ID) String userId)
      throws TransactionException {
    groupService.deleteGroupUser(groupId, userId);
  }

  @DeleteMapping("/{group_id}")
  @PreAuthorize(
      "hasRole('ROLE_ADMIN') or @webSecurityConfig.isGroupUser(principal.groupIdList, #groupId)")
  @ResponseStatus(HttpStatus.OK)
  public void deleteGroup(@PathVariable(PATH_GROUP_ID) String groupId) throws TransactionException {
    groupService.deleteGroup(groupId);
  }

  @GetMapping("/{group_id}/group-users")
  @PreAuthorize(
      "hasRole('ROLE_ADMIN') or @webSecurityConfig.isGroupUser(principal.groupIdList, #groupId)")
  @ResponseStatus(HttpStatus.OK)
  public List<GroupUserDto> listGroupUsers(@PathVariable(PATH_GROUP_ID) String groupId)
      throws TransactionException {
    return groupService.listGroupUsers(groupId);
  }

  @GetMapping()
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
  @ResponseStatus(HttpStatus.OK)
  public List<GetGroupDto> listGroups() throws TransactionException {
    return groupService.listGroups();
  }
}
