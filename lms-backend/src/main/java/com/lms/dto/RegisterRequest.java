package com.lms.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    // No role field: public self-registration always creates a STUDENT account.
    // ADMIN accounts must be provisioned directly in the database for now.
}
