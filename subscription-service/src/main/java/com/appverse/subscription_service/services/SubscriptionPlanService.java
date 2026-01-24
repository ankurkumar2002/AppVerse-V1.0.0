package com.appverse.subscription_service.services;

import java.util.List;

import com.appverse.subscription_service.dto.InternalPlanCreationRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanResponse;
import com.appverse.subscription_service.dto.UserSubscriptionResponse;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createDeveloperPlan(InternalPlanCreationRequest request);

    void activatePlan(String planId);

    void deactivatePlan(String planId);

    List<SubscriptionPlanResponse> getPlansByApplicationId(String applicationId);
}
