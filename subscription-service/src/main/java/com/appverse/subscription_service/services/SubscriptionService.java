// === In Subscription Service Project ===
package com.appverse.subscription_service.services;

import com.appverse.subscription_service.dto.*;
import java.util.List;

public interface SubscriptionService {

    // --- Plan Management (Admin) ---
    SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request);
    SubscriptionPlanResponse updatePlan(String planId, SubscriptionPlanRequest request);
    SubscriptionPlanResponse getPlanById(String planId);
    List<SubscriptionPlanResponse> getAllPlans(boolean activeOnly);
    void deactivatePlan(String planId);
    void activatePlan(String planId);

    // --- User Subscription Management ---
    /**
     * Initiates a new subscription for a user to a specific plan.
     * Handles trial periods and initial payment processing via PaymentService.
     */
    UserSubscriptionResponse createUserSubscription(String userId, CreateUserSubscriptionRequest request);

    /**
     * Retrieves a user's active or specific subscription.
     */
    UserSubscriptionResponse getUserSubscriptionById(String userId, String subscriptionId); // User can only get their own
    UserSubscriptionResponse getActiveUserSubscriptionForPlan(String userId, String planId); // Check active sub for a plan
    List<UserSubscriptionResponse> getAllUserSubscriptions(String userId);


    /**
     * Cancels a user's subscription.
     * Cancellation typically takes effect at the end of the current billing period.
     */
    UserSubscriptionResponse cancelUserSubscription(String userId, String subscriptionId, String reason);

    /**
     * Reactivates a previously cancelled (but not yet expired) subscription if autoRenew was off.
     * Or allows user to change their mind before period end.
     */
    UserSubscriptionResponse reactivateUserSubscription(String userId, String subscriptionId);


    /**
     * Processes payment outcome for an initial subscription payment.
     * Called by PaymentService (e.g., via webhook listener or direct callback).
     */
    void processInitialPaymentOutcome(String userSubscriptionId, String paymentTransactionId, boolean paymentSuccessful, String storedPaymentMethodIdUsed);

    /**
     * Processes payment outcome for a subscription renewal.
     * Called by PaymentService or internal renewal job.
     */
    void processRenewalPaymentOutcome(String userSubscriptionId, String paymentTransactionId, boolean paymentSuccessful);


    /**
     * Handles scheduled renewal attempts for active, auto-renewing subscriptions.
     * This method would be called by a scheduler.
     */
    void processScheduledRenewals();

    // TODO:
    // - Change plan (upgrade/downgrade)
    // - Pause/Resume subscription
    // - Handle dunning (failed renewal retries)
    // - Admin methods to manage user subscriptions

    SubscriptionPlanResponse createDeveloperPlan(InternalPlanCreationRequest request);
}