package com.verve.guard.entity;


import com.verve.guard.commons.CommonEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "users")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends CommonEntity implements UserDetails {

    @NotBlank(message = "First name is required")
    @NotNull(message = "First name is required")
    @Size(min = 3, max = 30, message = "First Name must be between 3 to 30 letters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @NotNull(message = "Last name is required")
    @Size(min = 3, max = 30, message = "Last Name must be between 3 to 30 letters")
    private String lastName;

    @NotNull(message = "Account Number is required")
    @Column(unique = true)
    private Long accountNumber;

    @NotBlank(message = "First name is required")
    @NotNull(message = "First name is required")
    @Email(message = "Your email is required")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @NotNull(message = "Password is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    @NotNull(message = "Phone number is required")
    @Size(min = 3, max = 30, message = "Phone number is required")
    @Column(unique = true)
    private String phone;

    @NotBlank(message = "Merchant Id is required")
    @NotNull(message = "Merchant Id is required")
    @Size(min = 3, max = 30, message = "Merchant Id is required")
    @Column(unique = true)
    private String merchantId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public @NonNull String getUsername() {
        return this.getEmail();
    }
}
