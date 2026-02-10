package com.appverse.subscription_service.services.serviceImpl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appverse.subscription_service.dto.InternalPlanCreationRequest;
import com.appverse.subscription_service.dto.SubscriptionPlanResponse;
import com.appverse.subscription_service.enums.SubscriptionPlanBillingInterval;
import com.appverse.subscription_service.enums.SubscriptionPlanStatus;
import com.appverse.subscription_service.exception.ResourceNotFoundException;
import com.appverse.subscription_service.mapper.SubscriptionMapper;
import com.appverse.subscription_service.model.SubscriptionPlan;
import com.appverse.subscription_service.repository.SubscriptionPlanRepository;
import com.appverse.subscription_service.services.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionMapper mapper;

    @Override
    @CacheEvict(value = "plansByApplication", key = "#request.applicationId()")
    @Transactional
    public SubscriptionPlanResponse createDeveloperPlan(InternalPlanCreationRequest request) {

        SubscriptionPlanBillingInterval interval = SubscriptionPlanBillingInterval
                .valueOf(request.billingInterval().toUpperCase());

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.displayName())
                .description(request.description())
                .price(request.price())
                .currency(request.currency())
                .billingInterval(interval)
                .billingIntervalCount(request.billingIntervalCount())
                .trialPeriodDays(request.trialPeriodDays() != null ? request.trialPeriodDays() : 0)
                .applicationId(request.applicationId())
                .developerId(request.developerId())
                .status(SubscriptionPlanStatus.ACTIVE)
                .build();

        return mapper.toSubscriptionPlanResponse(planRepository.save(plan));
    }

    @Override
    @CacheEvict(value = "plansByApplication", allEntries = true)
    public void activatePlan(String planId) {
        SubscriptionPlan plan = getPlan(planId);
        plan.setStatus(SubscriptionPlanStatus.ACTIVE);
        planRepository.save(plan);
    }

    @Override
    public void deactivatePlan(String planId) {
        SubscriptionPlan plan = getPlan(planId);
        plan.setStatus(SubscriptionPlanStatus.INACTIVE);
        planRepository.save(plan);
    }

    @Override
    @Cacheable(value = "plansByApplication", key = "#applicationId")
    public List<SubscriptionPlanResponse> getPlansByApplicationId(String applicationId) {
        return planRepository.findByApplicationIdAndStatus(applicationId, SubscriptionPlanStatus.ACTIVE)
                .stream()
                .map(mapper::toSubscriptionPlanResponse)
                .toList();
    }

    private SubscriptionPlan getPlan(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
    }
}
