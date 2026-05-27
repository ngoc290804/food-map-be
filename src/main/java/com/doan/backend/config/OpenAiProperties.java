package com.doan.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {
    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private int maxRestaurants = 50;
}
