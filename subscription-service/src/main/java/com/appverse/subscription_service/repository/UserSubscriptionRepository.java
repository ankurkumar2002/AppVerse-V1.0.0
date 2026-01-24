// === In Subscription Service Project ===
package com.appverse.subscription_service.repository;

import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
    List<UserSubscription> findByUserIdOrderByStartDateDesc(String userId);

    Optional<UserSubscription> findByIdAndUserId(String id, String userId);

    Optional<UserSubscription> findByUserIdAndSubscriptionPlanIdAndStatusIn(String userId, String planId,
            List<UserSubscriptionStatus> statuses);

    List<UserSubscription> findByStatusAndAutoRenewAndCurrentPeriodEndDateLessThanEqual(
            UserSubscriptionStatus status, boolean autoRenew, Instant dateThreshold);

    boolean existsByUserIdAndSubscriptionPlanIdAndStatus(String userId, String id, UserSubscriptionStatus active);
}