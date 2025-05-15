package com.appverse.payment_service.repository;


import com.appverse.payment_service.enums.PaymentGatewayType;
import com.appverse.payment_service.enums.StoredPaymentMethodStatus;
import com.appverse.payment_service.model.PaymentTransaction;
import com.appverse.payment_service.model.StoredPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;;

@Repository
public interface StoredPaymentMethodRepository extends JpaRepository<StoredPaymentMethod, String> {
    List<StoredPaymentMethod> findByUserIdAndStatusNot(String userId, StoredPaymentMethodStatus status);
    List<StoredPaymentMethod> findByUserIdAndPaymentGatewayAndStatusNot(String userId, PaymentGatewayType paymentGateway, StoredPaymentMethodStatus status);
    Optional<StoredPaymentMethod> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE StoredPaymentMethod spm SET spm.isDefault = false WHERE spm.userId = :userId AND spm.paymentGateway = :gateway AND spm.isDefault = true")
    void clearDefaultForUserAndGateway(@Param("userId") String userId, @Param("gateway") PaymentGatewayType gateway);
}
