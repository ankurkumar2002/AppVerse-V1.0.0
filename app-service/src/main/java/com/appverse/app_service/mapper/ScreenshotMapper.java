package com.appverse.app_service.mapper;

import com.appverse.app_service.dto.ScreenshotRequest;
import com.appverse.app_service.dto.ScreenshotResponse;
import com.appverse.app_service.model.Screenshot;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ScreenshotMapper {
    
    
    ScreenshotResponse toResponse(Screenshot screenshot);

    List<ScreenshotResponse> toResponseList(List<Screenshot> screenshots);

    Screenshot toEntity(ScreenshotRequest request);

    List<Screenshot> toEntityList(List<ScreenshotRequest> requests);

    void updateFromDto(ScreenshotRequest dto, @MappingTarget Screenshot screenshot);

}
