package com.hardwarestore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "name must not be blank")
    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @Size(max = 255, message = "description must not exceed 255 characters")
    private String description;
}