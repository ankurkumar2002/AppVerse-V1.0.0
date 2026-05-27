package com.appverse.api_gateway.routes;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import com.appverse.api_gateway.config.RoutePolicy;
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
        RouterFunction<ServerResponse> applicationServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "application-service-api",
                                                "/api/apps/**",
                                                "http://localhost:8080",
                                                "applicationServiceCB"),
                                RoutePolicy.userAndDeveloper());
        }

        @Bean
        RouterFunction<ServerResponse> categoryServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "category-service-api",
                                                "/api/categories/**",
                                                "http://localhost:8080",
                                                "categoryServiceCB"),
                                RoutePolicy.userAndDeveloper());
        }

        @Bean
        RouterFunction<ServerResponse> developerProfileCreationApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "developer-profile-create",
                                                "/api/developers",
                                                "http://localhost:8081",
                                                "developerServiceCB"),
                                RoutePolicy.profileCreation() // 🔥 SAME AS USER
                );
        }

        @Bean
        RouterFunction<ServerResponse> developerServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "developer-service-api",
                                                "/api/developers/**",
                                                "http://localhost:8081",
                                                "developerServiceCB"),
                                RoutePolicy.developerOnly());
        }

        @Bean
        RouterFunction<ServerResponse> userProfileCreationApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "user-profile-create",
                                                "/api/v1/users",
                                                "http://localhost:8082",
                                                "userServiceCB"),
                                RoutePolicy.profileCreation() // ✅ NO ROLE CHECK
                );
        }

        @Bean
        RouterFunction<ServerResponse> userOnboardingApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "user-onboarding-api",
                                                "/api/v1/users/keycloak/**",
                                                "http://localhost:8082",
                                                "userServiceCB"),
                                RoutePolicy.profileCreation());
        }

        @Bean
        RouterFunction<ServerResponse> userServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "user-service-api",
                                                "/api/v1/users/**",
                                                "http://localhost:8082",
                                                "userServiceCB"),
                                RoutePolicy.userOnly());
        }

        @Bean
        RouterFunction<ServerResponse> cartServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "cart-service-api",
                                                "/api/v1/carts/**",
                                                "http://localhost:8083",
                                                "cartServiceCB"),
                                RoutePolicy.userOnly());
        }

        @Bean
        RouterFunction<ServerResponse> orderServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "order-service-api",
                                                "/api/v1/orders/**",
                                                "http://localhost:8084",
                                                "orderServiceCB"),
                                RoutePolicy.userOnly());
        }

        @Bean
        RouterFunction<ServerResponse> paymentServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "payment-service-api",
                                                "/api/v1/payments/**",
                                                "http://localhost:8085",
                                                "paymentServiceCB"),
                                RoutePolicy.userOnly());
        }

        @Bean
        RouterFunction<ServerResponse> subscriptionServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "subscription-service-api",
                                                "/api/v1/subscriptions/**",
                                                "http://localhost:8086",
                                                "subscriptionServiceCB"),
                                RoutePolicy.userOnly());
        }

        @Bean
        RouterFunction<ServerResponse> subscriptionPlanServiceApi() throws Exception {
                return apiHelper.apiRoute(
                                new ServiceConfig(
                                                "subscription-plan-service-api",
                                                "/api/v1/subscription-plans/**",
                                                "http://localhost:8086",
                                                "subscriptionPlanServiceCB"),
                                RoutePolicy.userOnly());
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
