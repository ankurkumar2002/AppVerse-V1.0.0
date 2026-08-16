package com.appverse.user_service.service;

import com.appverse.user_service.dto.KeycloakUpdateRequest;
import com.appverse.user_service.dto.MessageResponse;
import com.appverse.user_service.dto.UpdatePasswordRequest;
import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;

public interface UserService {

    MessageResponse createUser(UserRequest userRequest);

    MessageResponse updateUserProfile(UpdateUserProfileRequest userRequest);

    UserResponse getMyprofile();

    void deleteMyAccount();
    boolean checkUserExists(String keycloakId);

    MessageResponse updatePassword( UpdatePasswordRequest request);

    MessageResponse updateUser( KeycloakUpdateRequest request);
}