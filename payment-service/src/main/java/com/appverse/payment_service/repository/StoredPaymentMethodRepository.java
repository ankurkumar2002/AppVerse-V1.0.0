// === In Payment Service Project ===
package com.appverse.payment_service.repository;

import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;
// Remove: import com.appverse.payment_service.model.PaymentTransaction; // Not used here
import com.appverse.payment_service.model.StoredPaymentMethod;

// Remove: import jakarta.validation.constraints.NotNull; // Not used here

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoredPaymentMethodRepository extends JpaRepository<StoredPaymentMethod, String> {
    List<StoredPaymentMethod> findByUserIdAndStatusNot(String userId, StoredPaymentMethodStatus status);
    List<StoredPaymentMethod> findByUserIdAndPaymentGatewayAndStatusNot(String userId, PaymentGatewayType paymentGateway, StoredPaymentMethodStatus status);
    Optional<StoredPaymentMethod> findByIdAndUserId(String id, String userId);

    @Modifying // This query is fine as is
    @Query("UPDATE StoredPaymentMethod spm SET spm.isDefault = false WHERE spm.userId = :userId AND spm.paymentGateway = :gateway AND spm.isDefault = true")
    void clearDefaultForUserAndGateway(@Param("userId") String userId, @Param("gateway") PaymentGatewayType gateway);

    // VVVVVVVVVV CORRECTED METHOD SIGNATURE VVVVVVVVVV
    // Add @Param annotations to clarify the parameters for this derived query
    Optional<StoredPaymentMethod> findByUserIdAndPaymentGatewayAndIsDefaultTrueAndStatus(
            @Param("userId") String userId,
            @Param("paymentGateway") PaymentGatewayType paymentGateway, // Parameter name matches derived query part
            @Param("status") StoredPaymentMethodStatus status // Parameter name matches derived query part
    );
    // ^^^^^^^^^^ CORRECTED METHOD SIGNATURE ^^^^^^^^^^
}