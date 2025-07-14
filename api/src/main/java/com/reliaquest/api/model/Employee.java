package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Employee data model")
public class Employee {

    @Schema(description = "Unique identifier for the employee", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @JsonProperty("employee_name")
    @Schema(description = "Employee's full name", example = "John Smith")
    private String name;

    @JsonProperty("employee_salary")
    @Schema(description = "Employee's annual salary in USD", example = "75000")
    private Integer salary;

    @JsonProperty("employee_age")
    @Schema(description = "Employee's age", example = "30", minimum = "16", maximum = "75")
    private Integer age;

    @JsonProperty("employee_title")
    @Schema(description = "Employee's job title", example = "Software Engineer")
    private String title;

    @JsonProperty("employee_email")
    @Schema(description = "Employee's email address", example = "john.smith@company.com")
    private String email;
}
