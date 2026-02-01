package com.appverse.api_gateway.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final ServiceTokenProvider serviceTokenProvider;

    @Override
    public void apply(RequestTemplate template) {

        // Prevent duplicate headers
        if (template.headers().containsKey(AUTH_HEADER)) {
            return;
        }

        String serviceToken = serviceTokenProvider.getServiceToken();

        if (serviceToken == null) {
            throw new IllegalStateException("Unable to generate service token for inter-service call");
        }

        template.header(AUTH_HEADER, BEARER + serviceToken);
    }
}
