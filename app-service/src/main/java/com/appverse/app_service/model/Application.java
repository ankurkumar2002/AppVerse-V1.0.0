package com.appverse.app_service.model;

import com.appverse.app_service.enums.MonetizationType; 
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.hibernate.validator.constraints.URL;

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

    @Size(max = 10)
    private String currency;

    private boolean isFree;

    @NotNull(message = "Monetization type cannot be null")
    private MonetizationType monetizationType;

    private List<String> associatedSubscriptionPlanIds;

    @NotEmpty
    private List<@NotBlank String> platforms;

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

    @NotNull
    @Valid
    private List<Screenshot> screenshots;

    @NotBlank
    private String developerId;

    private List<@NotBlank String> tags;

    @NotBlank
    private String status;

    private Instant publishedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "5.0", inclusive = true)
    private Double averageRating;

    @Min(0)
    private Integer ratingCount;

    private List<String> applicationSpecificSubscriptionPlanIds;

    public void ensureConsistency() {
        if (this.monetizationType == MonetizationType.FREE) {
            this.isFree = true;
            this.price = BigDecimal.ZERO;
            this.currency = null;
        } else if (this.monetizationType == MonetizationType.SUBSCRIPTION) {
            this.isFree = false;
        } else if (this.monetizationType == MonetizationType.ONE_TIME_PURCHASE
                || this.monetizationType == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) {
            if (this.price == null || this.price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be non-null and non-negative for purchasable items.");
            }
            if (this.price.compareTo(BigDecimal.ZERO) == 0) {
                this.isFree = true;
            } else {
                this.isFree = false;
                if (this.currency == null || this.currency.isBlank()) {
                    throw new IllegalArgumentException("Currency must be set for non-zero priced items.");
                }
            }
        }
    }
}