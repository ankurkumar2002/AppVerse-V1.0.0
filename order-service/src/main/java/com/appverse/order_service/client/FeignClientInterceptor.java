package com.appverse.order_service.client;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component 
@RequiredArgsConstructor
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

      private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";

    private final ServiceTokenProvider serviceTokenProvider;

    @Override
    public void apply(RequestTemplate template) {

        if (template.headers().containsKey(AUTH_HEADER)) {
            return;
        }
        log.info("Getting token generated in next line.");
        String serviceToken = serviceTokenProvider.getServiceToken();
        log.info("Token Generated : "+ serviceToken);

        if (serviceToken == null) {
            throw new IllegalStateException("Unable to generate service token for inter-service call");
        }

        template.header(AUTH_HEADER, BEARER + serviceToken);
    }
}