package com.reliaquest.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Input data for creating a new employee")
public class CreateEmployeeInput {

    @NotBlank
    @Schema(description = "Employee's full name", example = "John Smith", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Positive @NotNull @Schema(
            description = "Employee's annual salary in USD",
            example = "75000",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer salary;

    @Min(16)
    @Max(75)
    @NotNull @Schema(
            description = "Employee's age",
            example = "30",
            minimum = "16",
            maximum = "75",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer age;

    @NotBlank
    @Schema(
            description = "Employee's job title",
            example = "Software Engineer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}
