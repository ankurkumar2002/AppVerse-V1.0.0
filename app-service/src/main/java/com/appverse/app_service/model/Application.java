// === In app-service Project ===
package com.appverse.app_service.model;

import com.appverse.app_service.enums.MonetizationType; // Import the new enum
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

    // This 'price' field now primarily represents the ONE_TIME_PURCHASE price.
    // If MonetizationType is FREE or SUBSCRIPTION_ONLY, this should ideally be 0.
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Size(max = 10) // e.g., "USD", "EUR"
    private String currency; // Should be present if price > 0 and type is ONE_TIME_PURCHASE

    // 'isFree' can be derived, or set explicitly.
    // If monetizationType is FREE, this MUST be true.
    // If monetizationType is SUBSCRIPTION_ONLY, this is false (access is via paid
    // sub).
    private boolean isFree;

    @NotNull(message = "Monetization type cannot be null")
    private MonetizationType monetizationType;

    // Optional: List of subscription plan IDs (from SubscriptionService)
    // that would grant access to this app if monetizationType involves
    // SUBSCRIPTION.
    // This is more for informational purposes in app-service;
    // subscription-service is the authority on plan contents.
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

    @NotNull // List can be empty, but not null
    @Valid // Tells validation engine to validate constraints within Screenshot objects
    private List<Screenshot> screenshots; // Assuming Screenshot is another model class

    @NotBlank
    private String developerId;

    private List<@NotBlank String> tags; // Allow empty list, but elements shouldn't be blank if present

    @NotBlank // Assuming status should always be set
    private String status; // e.g., Published, Unpublished, UnderReview

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

    // In Application.java
    // ... other fields
    

    // List of SubscriptionPlan IDs created in subscription-service FOR THIS APP
    private List<String> applicationSpecificSubscriptionPlanIds;
    // ... rest of fields

    // Helper method to ensure consistency (call this before saving if isFree is not
    // explicitly set)
    // Or, better yet, handle this logic in your service layer when
    // creating/updating Application entities.
    public void ensureConsistency() {
        if (this.monetizationType == MonetizationType.FREE) {
            this.isFree = true;
            this.price = BigDecimal.ZERO;
            this.currency = null; // Or your default for free items
        } else if (this.monetizationType == MonetizationType.SUBSCRIPTION_ONLY) {
            this.isFree = false;
            // Price might be set to 0 or some base informational value,
            // but actual cost comes from the subscription plan.
            // this.price = BigDecimal.ZERO; // If you decide so
        } else if (this.monetizationType == MonetizationType.ONE_TIME_PURCHASE
                || this.monetizationType == MonetizationType.ONE_TIME_OR_SUBSCRIPTION) {
            if (this.price == null || this.price.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price must be non-null and non-negative for purchasable items.");
            }
            if (this.price.compareTo(BigDecimal.ZERO) == 0) {
                this.isFree = true; // A one-time purchase app with price 0 can be considered free
            } else {
                this.isFree = false;
                if (this.currency == null || this.currency.isBlank()) {
                    throw new IllegalArgumentException("Currency must be set for non-zero priced items.");
                }
            }
        }
        // If it's ONE_TIME_OR_SUBSCRIPTION and price is 0, it's effectively FREE or
        // SUBSCRIPTION.
    }
}