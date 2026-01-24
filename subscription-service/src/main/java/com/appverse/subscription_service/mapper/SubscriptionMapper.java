// === In Subscription Service Project ===
package com.appverse.subscription_service.mapper;

import com.appverse.subscription_service.dto.*;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import java.util.List;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    // ===== SubscriptionPlan =====
    SubscriptionPlan toSubscriptionPlan(SubscriptionPlanRequest request);

    SubscriptionPlanResponse toSubscriptionPlanResponse(SubscriptionPlan plan);

    List<SubscriptionPlanResponse> toSubscriptionPlanResponseList(
            List<SubscriptionPlan> plans
    );

    
    void updateSubscriptionPlanFromRequest(
            SubscriptionPlanRequest request,
            @MappingTarget SubscriptionPlan plan
    );

    // ===== UserSubscription =====
    UserSubscriptionResponse toUserSubscriptionResponse(
            UserSubscription userSubscription
    );

    List<UserSubscriptionResponse> toUserSubscriptionResponseList(
            List<UserSubscription> userSubscriptions
    );
}
