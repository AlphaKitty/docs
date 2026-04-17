package com.expertlink.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserRolesRequest {

    @NotEmpty
    private Set<String> roles;
}
