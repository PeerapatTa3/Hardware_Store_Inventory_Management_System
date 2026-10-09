package com.hardwarestore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "name must not be blank")
    @Size(max = 150, message = "name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "phone must not be blank")
    @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "phone format is invalid")
    @Size(max = 20, message = "phone must not exceed 20 characters")
    private String phone;

    @Email(message = "email format is invalid")
    @Size(max = 150, message = "email must not exceed 150 characters")
    private String email;

    @Size(max = 500, message = "address must not exceed 500 characters")
    private String address;
}
