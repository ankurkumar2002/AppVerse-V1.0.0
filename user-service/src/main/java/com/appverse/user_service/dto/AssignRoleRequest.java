package com.appverse.user_service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AssignRoleRequest(

    @NotEmpty(message = "At least one role must be provided")
    List<String> roles

) {}
