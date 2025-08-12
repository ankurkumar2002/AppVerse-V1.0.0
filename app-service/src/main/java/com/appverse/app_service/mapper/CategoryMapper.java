package com.appverse.app_service.mapper;

import com.appverse.app_service.dto.CategoryRequest;
import com.appverse.app_service.dto.CategoryResponse;
import com.appverse.app_service.model.Category;
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
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    Category toEntity(CategoryRequest request);

    void updateFromDto(CategoryRequest dto, @MappingTarget Category entity);
}
