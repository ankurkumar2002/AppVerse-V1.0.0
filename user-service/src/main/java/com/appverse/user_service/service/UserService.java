package com.appverse.user_service.service;

import com.appverse.user_service.dto.UpdateUserProfileRequest;
import com.appverse.user_service.dto.UserRequest;
import com.appverse.user_service.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse updateUserProfile(UpdateUserProfileRequest userRequest);

    UserResponse getMyprofile();

    void deleteMyAccount();
    boolean checkUserExists(String keycloakId);

}