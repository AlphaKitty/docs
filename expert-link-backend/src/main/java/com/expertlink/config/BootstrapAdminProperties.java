package com.expertlink.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.bootstrap-admin")
public class BootstrapAdminProperties {
    private boolean enabled = true;
    private String username = "admin";
    private String email = "admin@expertlink.local";
    /** Plain text; encoded on startup. Change in production via env. */
    private String password = "ChangeMe!Admin1";
}
