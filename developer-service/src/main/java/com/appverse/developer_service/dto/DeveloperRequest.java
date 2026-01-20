package com.appverse.developer_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import com.appverse.developer_service.enums.DeveloperType;
import com.appverse.developer_service.validation.OnCreate;
import com.appverse.developer_service.validation.OnUpdate;

public record DeveloperRequest(

    @URL(message = "Website must be a valid URL")
    @Size(max = 255)
    String website,

    @Size(max = 150)
    String companyName,

    String bio,

    @URL(message = "Logo URL must be a valid URL")
    @Size(max = 255)
    String logoUrl,

    @Size(max = 100)
    String location,

    @NotNull(message = "Developer type is required",
             groups = { OnCreate.class, OnUpdate.class })
    DeveloperType developerType
) {}
