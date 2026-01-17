package com.appverse.app_service;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            // Try to get the Docker client - if it fails, Docker is not available
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            // Docker is not available
            return false;
        }
    }
}
