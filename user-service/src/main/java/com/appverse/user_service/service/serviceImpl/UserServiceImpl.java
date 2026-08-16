package com.appverse.user_service.service.serviceImpl;

import com.appverse.user_service.client.IdentityClient;
import com.appverse.user_service.dto.AssignRoleRequest;
import com.appverse.user_service.dto.IdentityUserResponse;
import com.appverse.user_service.dto.KeycloakUpdateRequest;
import com.appverse.user_service.dto.MessageResponse;
import com.appverse.user_service.dto.UpdatePasswordRequest;
import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;
import com.appverse.user_service.enums.Role;
import com.appverse.user_service.enums.UserStatus;
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

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final IdentityClient identityClient;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @CacheEvict(value = "userProfileByKeycloakId", key = "#root.target.currentUserProvider.getCurrentUser().id()")
    public MessageResponse updateUserProfile(UpdateUserProfileRequest userRequest) {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        if (userRequest.phone() != null && !userRequest.phone().isBlank()) {
            user.setPhone(userRequest.phone());
        }
        if (user.getRole() == Role.DEVELOPER) {
            return new MessageResponse("You already have a user profile with this account.", keycloakUserId);
        }
        User updatedUser = userRepository.save(user);
        return new MessageResponse("User profile created successfully!", user.getKeycloakUserId());
    }

    @Override
    @CacheEvict(value = "userProfileByKeycloakId", key = "#root.target.currentUserProvider.getCurrentUser().id()")
    @Transactional
    public void deleteMyAccount() {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        log.info(keycloakUserId + " - keycloak user Id");

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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new IntegrationException("Failed to disable Keycloak user", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyprofile() {
        log.info("function is called");
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        log.info(keycloakUserId + " - keycloak user Id");
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException(keycloakUserId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResourceNotFoundException("User is not active");
        }
        return userMapper.toResponse(user);
    }

    @Cacheable(value = "userProfileByKeycloakId", key = "#keycloakUserId")
    public UserResponse getProfileByKeycloakId(
            String keycloakUserId) {

        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not Found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public MessageResponse createUser(UserRequest userRequest) {

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

        if (identityUser.username() == null || identityUser.email() == null) {
            throw new IntegrationException(
                    "Incomplete user data received from identity-service");
        }
        if (identityUser.roles().contains("USER")) {
            throw new DuplicateResourceException("You already have a user profile with this account. " + keycloakUserId);
        }

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

        User savedUser = userRepository.save(user);
        userRepository.flush();

        log.info("✅ User created with id {}", savedUser.getId());

        identityClient.assignRoles(
                keycloakUserId,
                new AssignRoleRequest(List.of("USER")));

        log.info("Before mapping response");

        // UserResponse response = userMapper.toResponse(savedUser);

        log.info("After mapping response");

        return new MessageResponse("User created successfully!", keycloakUserId);

    }

    public boolean checkUserExists(String keycloakId) {
        return userRepository.existsByKeycloakUserId(keycloakId);
    }

    @Transactional
    public MessageResponse updateUser(KeycloakUpdateRequest request) {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();
        log.info("request reached in this function");
        log.info("Sending email to identity service = {}", request.getEmail());
        try {
            identityClient.updateUser(keycloakUserId, request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        log.info("request sent to keycloak service and completed successfully");
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find User: " + keycloakUserId));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return new MessageResponse("Details Updated Successfully! ", keycloakUserId);
    }

    public MessageResponse updatePassword(UpdatePasswordRequest request) {
        String keycloakUserId = currentUserProvider.getCurrentUser().id();

        ResponseEntity<Void> response = identityClient.updatePassword(keycloakUserId, request);

        if (response.getStatusCode().is2xxSuccessful()) {
            return new MessageResponse("Password Updation Successfull! ", keycloakUserId);
        }

        return new MessageResponse("Some Error Occurred please try again later! ", keycloakUserId);

    }

}