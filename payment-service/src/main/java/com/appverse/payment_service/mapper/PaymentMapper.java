// === In Payment Service Project ===
package com.appverse.payment_service.mapper;

import com.appverse.payment_service.dto.PaymentResponse;
import com.appverse.payment_service.dto.StoredPaymentMethodResponse;
import com.appverse.payment_service.model.PaymentTransaction;
import com.appverse.payment_service.model.StoredPaymentMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = ObjectMapper.class)
public interface PaymentMapper {
    Logger log = LoggerFactory.getLogger(PaymentMapper.class);

    @Mapping(source = "metadata", target = "metadata", qualifiedByName = "stringToMap")
    PaymentResponse toPaymentResponse(PaymentTransaction transaction);

    @Mapping(source = "billingDetailsSnapshot", target = "billingDetails", qualifiedByName = "stringToMapOfString")
    StoredPaymentMethodResponse toStoredPaymentMethodResponse(StoredPaymentMethod storedPaymentMethod);


    @Named("stringToMap")
    default Map<String, Object> stringToMap(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            // You'd need ObjectMapper injected or accessible if not using 'uses'
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            log.error("Error parsing JSON string to Map<String, Object>: {}", jsonString, e);
            return Collections.emptyMap(); // Or throw an exception
        }
    }

    @Named("stringToMapOfString")
    default Map<String, String> stringToMapOfString(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            log.error("Error parsing JSON string to Map<String, String>: {}", jsonString, e);
            return Collections.emptyMap();
        }
    }
}