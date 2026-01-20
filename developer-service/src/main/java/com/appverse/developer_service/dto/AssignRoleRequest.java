package com.appverse.developer_service.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record AssignRoleRequest(

    @NotEmpty(message = "At least one role must be provided")
    List<String> roles

) {}
