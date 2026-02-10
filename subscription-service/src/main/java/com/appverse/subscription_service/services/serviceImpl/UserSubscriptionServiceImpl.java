package com.appverse.subscription_service.services.serviceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appverse.subscription_service.dto.CreateUserSubscriptionRequest;
import com.appverse.subscription_service.dto.UserSubscriptionResponse;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import com.appverse.subscription_service.enums.UserSubscriptionStatus;
import com.appverse.subscription_service.exception.ResourceNotFoundException;
import com.appverse.subscription_service.exception.SubscriptionActionNotAllowedException;
import com.appverse.subscription_service.mapper.SubscriptionMapper;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.model.UserSubscription;
import com.appverse.subscription_service.repository.SubscriptionPlanRepository;
import com.appverse.subscription_service.repository.UserSubscriptionRepository;
import com.appverse.subscription_service.services.UserSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {
        private static final Logger log = LoggerFactory.getLogger(UserSubscriptionServiceImpl.class);
        private final UserSubscriptionRepository subscriptionRepository;
        private final SubscriptionPlanRepository planRepository;
        private final SubscriptionMapper mapper;

        public UserSubscriptionServiceImpl(UserSubscriptionRepository subscriptionRepository,
                        SubscriptionPlanRepository planRepository,
                        SubscriptionMapper mapper) {
                this.subscriptionRepository = subscriptionRepository;
                this.planRepository = planRepository;
                this.mapper = mapper;
        }

        @Override
        @CacheEvict(value = "subscriptionsByUser", key = "#userId")
        @Transactional
        public UserSubscriptionResponse subscribeUser(String userId, CreateUserSubscriptionRequest request) {

                SubscriptionPlan plan = planRepository
                                .findByIdAndStatus(request.subscriptionPlanId(), SubscriptionPlanStatus.ACTIVE)
                                .orElseThrow(() -> new ResourceNotFoundException("Plan not active"));

                boolean exists = subscriptionRepository
                                .existsByUserIdAndSubscriptionPlanIdAndStatus(
                                                userId, plan.getId(), UserSubscriptionStatus.ACTIVE);

                if (exists) {
                        throw new SubscriptionActionNotAllowedException("Already subscribed");
                }

                Instant now = Instant.now();

                UserSubscription sub = UserSubscription.builder()
                                .userId(userId)
                                .subscriptionPlanId(plan.getId())
                                .status(plan.getTrialPeriodDays() > 0
                                                ? UserSubscriptionStatus.TRIALING
                                                : UserSubscriptionStatus.ACTIVE)
                                .startDate(now)
                                .currentPeriodStartDate(now)
                                .currentPeriodEndDate(now.plus(plan.getTrialPeriodDays(), ChronoUnit.DAYS))
                                .autoRenew(true)
                                .build();

                return mapper.toUserSubscriptionResponse(subscriptionRepository.save(sub));
        }

        @Override
        @CacheEvict(value = "subscriptionsByUser", key = "#userId")
        @Transactional
        public UserSubscriptionResponse cancelUserSubscription(String userId, String subscriptionId, String reason) {

                UserSubscription sub = subscriptionRepository
                                .findByIdAndUserId(subscriptionId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

                sub.setStatus(UserSubscriptionStatus.CANCELLED);
                sub.setCancelledAt(Instant.now());
                sub.setAutoRenew(false);
                sub.setCancellationReason(reason);

                return mapper.toUserSubscriptionResponse(subscriptionRepository.save(sub));
        }

        @Override
        @Cacheable(value = "subscriptionsByUser", key = "#userId")
        public List<UserSubscriptionResponse> getUserSubscriptions(String userId) {
                return subscriptionRepository.findByUserIdOrderByStartDateDesc(userId)
                                .stream()
                                .map(mapper::toUserSubscriptionResponse)
                                .collect(Collectors.toList());
        }

}
