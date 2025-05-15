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
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
    List<PaymentTransaction> findByUserIdOrderByInitiatedAtDesc(String userId);
    List<PaymentTransaction> findByReferenceIdOrderByInitiatedAtDesc(String referenceId);
    Optional<PaymentTransaction> findByGatewayTransactionIdAndPaymentGateway(String gatewayTransactionId, PaymentGatewayType paymentGateway);
    Optional<PaymentTransaction> findByGatewayPaymentIntentIdAndPaymentGateway(String gatewayPaymentIntentId, PaymentGatewayType paymentGateway);
}
