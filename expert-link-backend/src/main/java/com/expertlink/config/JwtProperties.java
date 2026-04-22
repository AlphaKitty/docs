package com.expertlink.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "change-me";
    private long expiration = 86400000L;
    private String issuer = "expert-link-backend";
}
