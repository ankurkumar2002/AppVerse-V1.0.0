package com.appverse.app_service.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.hibernate.validator.constraints.URL; // Common implementation, ensure dependency exists

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String tagline;

    @NotBlank
    private String description;

    @NotBlank
    @Size(max = 30)
    private String version;

    @NotBlank
    private String categoryId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Size(max = 10) // e.g., "USD", "EUR"
    private String currency;

    private boolean isFree; // Often derived, not directly validated on input

    @NotEmpty
    private List<@NotBlank String> platforms; // Ensures list isn't empty and elements aren't blank

    @NotBlank
    @URL
    private String accessUrl;

    @URL
    private String websiteUrl;

    @URL
    private String supportUrl;

    @NotBlank
    @URL
    private String thumbnailUrl;

    @NotNull // List can be empty, but not null
    @Valid // Tells validation engine to validate constraints within Screenshot objects
    private List<Screenshot> screenshots;

    @NotBlank
    private String developerId;

    private List<@NotBlank String> tags; // Allow empty list, but elements shouldn't be blank if present

    @NotBlank // Assuming status should always be set
    private String status;

    private Instant publishedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DecimalMin(value = "0.0", inclusive = true) // Assuming rating cannot be negative
    @DecimalMax(value = "5.0", inclusive = true) // Assuming a 0-5 rating scale
    private Double averageRating;

    @Min(0) // Cannot have negative rating counts
    private Integer ratingCount;
}