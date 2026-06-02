package com.yeginamgim.place.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSearchRequest {
    @NotNull(message = "latitude is required.")
    @DecimalMin(value = "-90.0", message = "latitude must be at least -90.")
    @DecimalMax(value = "90.0", message = "latitude must be at most 90.")
    private Double latitude;

    @NotNull(message = "longitude is required.")
    @DecimalMin(value = "-180.0", message = "longitude must be at least -180.")
    @DecimalMax(value = "180.0", message = "longitude must be at most 180.")
    private Double longitude;

    @Min(value = 1, message = "radius must be at least 1.")
    @Max(value = 20000, message = "radius must be at most 20000.")
    private Integer radius;

    @NotBlank(message = "category is required.")
    private String category;

    private String query;

    @Min(value = 1, message = "page must be at least 1.")
    private Integer page;

    @Min(value = 1, message = "limit must be at least 1.")
    @Max(value = 15, message = "limit must be at most 15.")
    private Integer limit;
}
