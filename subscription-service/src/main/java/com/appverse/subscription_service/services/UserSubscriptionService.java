package com.appverse.subscription_service.services;

import java.util.List;

import com.appverse.subscription_service.dto.CreateUserSubscriptionRequest;
import com.appverse.subscription_service.dto.UserSubscriptionResponse;

public interface UserSubscriptionService {

    UserSubscriptionResponse subscribeUser(String userId, CreateUserSubscriptionRequest request);

    UserSubscriptionResponse cancelUserSubscription(String userId, String subscriptionId, String reason);

    List<UserSubscriptionResponse> getUserSubscriptions(String userId);
}
