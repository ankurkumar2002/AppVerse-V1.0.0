package com.appverse.app_service.mapper;

import com.appverse.app_service.dto.ApplicationRequest;
import com.appverse.app_service.dto.ApplicationResponse;
import com.appverse.app_service.dto.UpdateApplicationRequest;
import com.appverse.app_service.model.Application;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    uses = {
        ScreenshotMapper.class,
        CategoryMapper.class
        // DeveloperMapper.class
    },
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ApplicationMapper {

ApplicationResponse toResponse(Application application);


    List<ApplicationResponse> toResponseList(List<Application> applications);

    Application toEntity(ApplicationRequest request);

    void updateFromDto(UpdateApplicationRequest dto, @MappingTarget Application entity);
}
