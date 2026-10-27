package com.greenocean.backend.assistance.dto;
import jakarta.validation.constraints.*;
import com.greenocean.backend.auth.service.EmailIdentity;

public record PendingEmailRequest(@NotBlank @Size(max=320) @Pattern(regexp=EmailIdentity.PATTERN) String newEmail,
                                 @NotBlank @Size(max=256) String password) {
    public PendingEmailRequest {newEmail=EmailIdentity.normalize(newEmail);}
}
