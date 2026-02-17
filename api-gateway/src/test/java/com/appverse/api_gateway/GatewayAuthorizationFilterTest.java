package com.appverse.api_gateway;

import com.appverse.api_gateway.client.DeveloperClient;
import com.appverse.api_gateway.client.UserClient;
import com.appverse.api_gateway.config.GatewayAuthorizationFilter;
import com.appverse.api_gateway.config.RoutePolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GatewayAuthorizationFilterTest {

    @Mock
    private UserClient userClient;

    @Mock
    private DeveloperClient developerClient;

    @Mock
    private HandlerFunction<ServerResponse> next;

    private GatewayAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new GatewayAuthorizationFilter(userClient, developerClient);
    }

    private ServerRequest createRequestWithPrincipal(JwtAuthenticationToken auth) {

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod(HttpMethod.GET.name());
        servletRequest.setRequestURI("/api/test");
        servletRequest.setUserPrincipal(auth);

        return ServerRequest.create(servletRequest, List.of());
    }

    private JwtAuthenticationToken createJwtWithRoles(String... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "123")
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .build();

        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void shouldReturn401WhenNoJwt() throws Exception {

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("GET");
        servletRequest.setRequestURI("/api/test");

        ServerRequest request = ServerRequest.create(servletRequest, List.of());

        ServerResponse response =
                filter.authorize(request, RoutePolicy.userOnly(), next);

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode());
        verify(next, never()).handle(any());
    }

    @Test
    void shouldReturn403WhenRoleNotAllowed() throws Exception {

        ServerRequest request =
                createRequestWithPrincipal(createJwtWithRoles("USER"));

        ServerResponse response =
                filter.authorize(request, RoutePolicy.developerOnly(), next);

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
        verify(next, never()).handle(any());
    }

    @Test
    void shouldReturn403WhenProfileMissing() throws Exception {

        ServerRequest request =
                createRequestWithPrincipal(createJwtWithRoles("USER"));

        when(userClient.checkUserExists("123"))
                .thenReturn(Map.of("exists", false));

        ServerResponse response =
                filter.authorize(request, RoutePolicy.userOnly(), next);

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode());
        verify(next, never()).handle(any());
    }

    @Test
    void shouldAllowWhenUserRoleAndProfileExists() throws Exception {

        ServerRequest request =
                createRequestWithPrincipal(createJwtWithRoles("USER"));

        when(userClient.checkUserExists("123"))
                .thenReturn(Map.of("exists", true));

        when(next.handle(any()))
                .thenReturn(ServerResponse.ok().build());

        ServerResponse response =
                filter.authorize(request, RoutePolicy.userOnly(), next);

        assertEquals(HttpStatus.OK, response.statusCode());
        verify(next, times(1)).handle(any());
    }

    @Test
    void shouldAllowPublicRoute() throws Exception {

        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("GET");
        servletRequest.setRequestURI("/public");

        ServerRequest request = ServerRequest.create(servletRequest, List.of());

        when(next.handle(any()))
                .thenReturn(ServerResponse.ok().build());

        ServerResponse response =
                filter.authorize(request, RoutePolicy.publicApi(), next);

        assertEquals(HttpStatus.OK, response.statusCode());
        verify(next, times(1)).handle(any());
    }
}
