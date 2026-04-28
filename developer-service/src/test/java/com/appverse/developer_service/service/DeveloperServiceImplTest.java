package com.appverse.developer_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.appverse.developer_service.client.IdentityClient;
import com.appverse.developer_service.config.CurrentUserProvider;
import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.IdentityUserResponse;
import com.appverse.developer_service.dto.MessageResponse;
import com.appverse.developer_service.enums.DeveloperStatus;
import com.appverse.developer_service.enums.DeveloperType;
import com.appverse.developer_service.enums.Role;
import com.appverse.developer_service.mapper.DeveloperMapper;
import com.appverse.developer_service.model.Developer;
import com.appverse.developer_service.repository.DeveloperRepository;
import com.appverse.developer_service.service.serviceImpl.DeveloperServiceImpl;

@ExtendWith(MockitoExtension.class)
public class DeveloperServiceImplTest {
    @Mock
    private DeveloperRepository developerRepository;

    @Mock
    private DeveloperMapper developerMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private IdentityClient identityClient;

    @InjectMocks
    private DeveloperServiceImpl developerService;

    private IdentityUserResponse currentUser;
    private Developer developer;
    private DeveloperRequest request;

    @BeforeEach
    void setup() {
        currentUser = new IdentityUserResponse("kc-123", "ankur", "ankur@singh.com", true, "Ankur", "Singh", true, List.of("USER"));

        request = new DeveloperRequest("https://test.com", "AppVerse", "Bio", "https://logo.com", "Pune", DeveloperType.INDIVIDUAL);

        developer = Developer.builder()
                            .id("dev-1")
                            .keycloakUserId("kc-123")
                            .email("ankur@singh.com")
                            .username("ankur")
                            .firstName("Ankur")
                            .lastName("Singh")
                            .developerType(DeveloperType.INDIVIDUAL)
                            .status(DeveloperStatus.ACTIVE)
                            .role(Role.DEVELOPER)
                            .build();
    }

    @Test
    void createDeveloper_success(){

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(developerMapper.toEntity(request)).thenReturn(developer);
        when(developerRepository.existsByKeycloakUserId(currentUser.id())).thenReturn(false);
        when(developerRepository.save(any())).thenReturn(developer);
        doNothing().when(identityClient).assignRoles(any(), any());
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        MessageResponse response = developerService.createDeveloper(request);

        verify(developerRepository).save(any());
        verify(identityClient).assignRoles(any(), any());
        verify(kafkaTemplate).send(anyString(), anyString(), any());

        assertEquals("Developer created successfully", response.str());
        assertEquals(developer.getId(), response.id());

    }

    @Test
    void updateDeveloper_Success() throws Exception{

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(developerRepository.findById("dev-1")).thenReturn(Optional.of(developer));
        when(null);
    }

}
