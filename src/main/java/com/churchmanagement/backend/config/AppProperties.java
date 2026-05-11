package com.churchmanagement.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private DefaultAdmin defaultAdmin = new DefaultAdmin();
    private Notifications notifications = new Notifications();

    @Data
    public static class DefaultAdmin {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
    }

    @Data
    public static class Notifications {
        private Birthday birthday = new Birthday();

        @Data
        public static class Birthday {
            private List<String> emails = new ArrayList<>();
        }
    }
}
