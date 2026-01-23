package com.appverse.api_gateway.routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.appverse.api_gateway.config.ServiceConfig;
import com.appverse.api_gateway.routes.HelperMethods.ApiDocsHelperMethod;
import com.appverse.api_gateway.routes.HelperMethods.ApiHelperMethod;

@Configuration
public class Routes {

    private final ApiHelperMethod apiHelper;
    private final ApiDocsHelperMethod docsHelper;

    public Routes(ApiHelperMethod apiHelper,
                  ApiDocsHelperMethod docsHelper) {
        this.apiHelper = apiHelper;
        this.docsHelper = docsHelper;
    }

    /* ===================== API ROUTES ===================== */

    @Bean
    RouterFunction<ServerResponse> applicationServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "application-service-api",
                "/api/apps/**",
                "http://localhost:8080",
                "applicationServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> categoryServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "category-service-api",
                "/api/categories/**",
                "http://localhost:8080",
                "categoryServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> developerServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "developer-service-api",
                "/api/developers/**",
                "http://localhost:8081",
                "developerServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> userServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "user-service-api",
                "/api/v1/users/**",
                "http://localhost:8082",
                "userServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> cartServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "cart-service-api",
                "/api/v1/carts/**",
                "http://localhost:8083",
                "cartServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> orderServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "order-service-api",
                "/api/v1/orders/**",
                "http://localhost:8084",
                "orderServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> paymentServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "payment-service-api",
                "/api/v1/payments/**",
                "http://localhost:8085",
                "paymentServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> subscriptionServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "subscription-service-api",
                "/api/v1/subscriptions/**",
                "http://localhost:8086",
                "subscriptionServiceCB"));
    }

    @Bean
    RouterFunction<ServerResponse> subscriptionPlanServiceApi() {
        return apiHelper.apiRoute(new ServiceConfig(
                "subscription-plan-service-api",
                "/api/v1/subscription-plans/**",
                "http://localhost:8086",
                "subscriptionPlanServiceCB"));
    }

    /* ===================== API DOC ROUTES ===================== */

    @Bean
    RouterFunction<ServerResponse> applicationDocs() {
        return docsHelper.apiDocsRoute(
                "application-service-apidocs",
                "/aggregate/application-service/v3/api-docs",
                "http://localhost:8080");
    }

    @Bean
    RouterFunction<ServerResponse> developerDocs() {
        return docsHelper.apiDocsRoute(
                "developer-service-apidocs",
                "/aggregate/developer-service/v3/api-docs",
                "http://localhost:8081");
    }

    @Bean
    RouterFunction<ServerResponse> userDocs() {
        return docsHelper.apiDocsRoute(
                "user-service-apidocs",
                "/aggregate/user-service/v3/api-docs",
                "http://localhost:8082");
    }

    @Bean
    RouterFunction<ServerResponse> cartDocs() {
        return docsHelper.apiDocsRoute(
                "cart-service-apidocs",
                "/aggregate/cart-service/v3/api-docs",
                "http://localhost:8083");
    }

    @Bean
    RouterFunction<ServerResponse> orderDocs() {
        return docsHelper.apiDocsRoute(
                "order-service-apidocs",
                "/aggregate/order-service/v3/api-docs",
                "http://localhost:8084");
    }

    @Bean
    RouterFunction<ServerResponse> paymentDocs() {
        return docsHelper.apiDocsRoute(
                "payment-service-apidocs",
                "/aggregate/payment-service/v3/api-docs",
                "http://localhost:8085");
    }

    @Bean
    RouterFunction<ServerResponse> subscriptionDocs() {
        return docsHelper.apiDocsRoute(
                "subscription-service-apidocs",
                "/aggregate/subscription-service/v3/api-docs",
                "http://localhost:8086");
    }
}
