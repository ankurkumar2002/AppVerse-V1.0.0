// === In Subscription Service Project ===
package com.appverse.subscription_service.repository;

import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    boolean existsByName(String name);
    List<SubscriptionPlan> findByStatus(SubscriptionPlanStatus status);
    Optional<SubscriptionPlan> findByIdAndStatus(String id, SubscriptionPlanStatus status);
}

