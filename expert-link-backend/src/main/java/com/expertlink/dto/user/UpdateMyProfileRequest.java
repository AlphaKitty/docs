package com.expertlink.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateMyProfileRequest {

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 2000)
    private String bio;

    @Size(max = 500)
    private String avatar;

    private UpdateMyExpertProfileRequest expertProfile;
}
