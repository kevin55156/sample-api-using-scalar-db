package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonDeserialize(builder = CreateUserDto.CreateUserDtoBuilder.class)
public class CreateUserDto {
  @JsonProperty("email")
  @NotNull
  @Email
  String email;
}
