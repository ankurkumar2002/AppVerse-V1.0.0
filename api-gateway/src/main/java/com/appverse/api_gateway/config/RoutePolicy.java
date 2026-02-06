package com.appverse.api_gateway.config;


import java.util.Set;
public record RoutePolicy(
        Set<String> allowedRoles,
        boolean requireProfile
) {

    public static RoutePolicy publicApi() {
        return new RoutePolicy(Set.of(), false);
    }

    public static RoutePolicy authenticatedOnly() {
        return new RoutePolicy(Set.of(), false);
    }

    public static RoutePolicy userOnly() {
        return new RoutePolicy(Set.of("USER"), true);
    }

    public static RoutePolicy developerOnly() {
        return new RoutePolicy(Set.of("DEVELOPER"), true);
    }

    public static RoutePolicy userAndDeveloper() {
        return new RoutePolicy(Set.of("USER", "DEVELOPER"), true);
    }

    // 🔥 PROFILE CREATION
    public static RoutePolicy profileCreation() {
        return new RoutePolicy(Set.of(), false);
    }
}
