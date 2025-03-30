package org.order.bookservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.order.bookservice.dto.ProductResponse;
import org.order.bookservice.entity.Category;
import org.order.bookservice.entity.Genre;
import org.order.bookservice.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCategory")
    @Mapping(target = "genre", expression = "java(getFirstGenre(entity))")
    ProductResponse toDTO(Product entity);

    @Named("mapCategory")
    default String mapCategory(Category category) {
        return category != null ? category.getName() : null;
    }

    default String getFirstGenre(Product product) {
        return product.getGenres().stream()
                .map(Genre::getName)
                .findAny().orElse(null);
    }
}
