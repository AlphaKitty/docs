package com.expertlink;

import com.expertlink.config.BootstrapAdminProperties;
import com.expertlink.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class, BootstrapAdminProperties.class})
public class ExpertLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpertLinkApplication.class, args);
    }
}