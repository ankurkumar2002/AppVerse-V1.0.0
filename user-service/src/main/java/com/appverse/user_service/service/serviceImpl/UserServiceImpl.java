
package com.appverse.user_service.service.serviceImpl;

import com.appverse.user_service.client.IdentityClient;
import com.appverse.user_service.dto.AssignRoleRequest;
import com.appverse.user_service.dto.IdentityUserResponse;
import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
import com.appverse.user_service.exception.DatabaseOperationException;
import com.appverse.user_service.exception.DuplicateResourceException;
import com.appverse.user_service.exception.IntegrationException;
import com.appverse.user_service.exception.ResourceNotFoundException;
import com.appverse.user_service.mapper.UserMapper;
import com.appverse.user_service.model.User;
import com.appverse.user_service.repository.UserRepository;
import com.appverse.user_service.service.UserService;
import com.appverse.user_service.userDetailProvider.CurrentUserProvider;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdentityClient identityClient;
    private final CurrentUserProvider currentUserProvider;

    // private String getCurrentKeycloakUserId() {
    // Jwt jwt = (Jwt) SecurityContextHolder
    // .getContext()
    // .getAuthentication()
    // .getPrincipal();
    // return jwt.getSubject();
    // }

    @Override
    public UserResponse updateUserProfile(UpdateUserProfileRequest userRequest) {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        if (userRequest.phone() != null && !userRequest.phone().isBlank()) {
            user.setPhone(userRequest.phone());
        }
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteMyAccount() {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        log.info(keycloakUserId+ " - keycloak user Id");

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            return;
        }

        user.setStatus(UserStatus.DEACTIVATED);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        try {
            identityClient.disableUser(keycloakUserId);
        } catch (Exception e) {
            log.error("Failed to disable Keycloak user {}", keycloakUserId, e);
        }
    }

    @Override
    public UserResponse getMyprofile() {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        log.info(keycloakUserId+ " - keycloak user Id");
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(keycloakUserId));
        if (user.getStatus() == UserStatus.DEACTIVATED || UserStatus.DELETED == user.getStatus()) {
            return null;
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {

        log.info("🔥 createUser() started");

        String keycloakUserId = currentUserProvider.getCurrentUser().id();

        if (userRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new DuplicateResourceException(
                    "User already exists for this account");
        }

        IdentityUserResponse identityUser;
        try {
            identityUser = identityClient.getUserById(keycloakUserId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException(
                    "Keycloak user not found for id: " + keycloakUserId);
        } catch (FeignException e) {
            throw new IntegrationException(
                    "Failed to communicate with identity-service", e);
        }

        // 4. Basic sanity check (defensive)
        if (identityUser.username() == null || identityUser.email() == null) {
            throw new IntegrationException(
                    "Incomplete user data received from identity-service");
        }

        // 5. Persist user in app database
        User user = User.builder()
                .keycloakUserId(identityUser.id())
                .username(identityUser.username())
                .email(identityUser.email())
                .emailVerified(identityUser.emailVerified())
                .firstName(identityUser.firstName())
                .lastName(identityUser.lastName())
                .phone(userRequest.phone())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        try {
            User savedUser = userRepository.save(user);
            userRepository.flush();

            log.info("✅ User created with id {}", savedUser.getId());

            try {
                identityClient.assignRoles(
                        keycloakUserId,
                        new AssignRoleRequest(List.of("USER")));
            } catch (FeignException e) {
                log.error("⚠️ Failed to assign role USER to keycloak user {}",
                        keycloakUserId, e);
            }

            return userMapper.toResponse(savedUser);
        } catch (DataAccessException e) {
            log.error("❌ Database error while creating user: {}", e.getMessage(), e);
            throw new DatabaseOperationException("Failed to create user due to database error: " + e.getMessage());
        }

    }

}