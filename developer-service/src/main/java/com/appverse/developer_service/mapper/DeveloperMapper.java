package com.appverse.developer_service.mapper;


import com.appverse.developer_service.dto.DeveloperRequest;
import com.appverse.developer_service.dto.DeveloperResponse;
import com.appverse.developer_service.model.Developer;

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
public interface DeveloperMapper {

    DeveloperResponse toResponse(Developer developer);

    List<DeveloperResponse> toResponseList(List<Developer> developers);

    Developer toEntity(DeveloperRequest request);

    void updateFromDto(DeveloperRequest dto, @MappingTarget Developer entity);
}
