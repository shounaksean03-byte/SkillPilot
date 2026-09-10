package com.lms.dto;

import com.lms.entity.Role;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterResponse {
    private Long id;
    private String email;
    private Role role;
}
