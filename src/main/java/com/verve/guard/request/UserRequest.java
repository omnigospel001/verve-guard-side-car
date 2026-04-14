package com.verve.guard.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "First name is required")
    @NotNull(message = "First name is required")
    @Size(min = 3, max = 30, message = "First Name must be between 3 to 30 letters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @NotNull(message = "Last name is required")
    @Size(min = 3, max = 30, message = "Last Name must be between 3 to 30 letters")
    private String lastName;

    @NotBlank(message = "First name is required")
    @NotNull(message = "First name is required")
    @Email(message = "Your email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @NotNull(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    @NotNull(message = "Phone number is required")
    @Size(min = 3, max = 30, message = "Phone number is required")
    private String phone;
}
