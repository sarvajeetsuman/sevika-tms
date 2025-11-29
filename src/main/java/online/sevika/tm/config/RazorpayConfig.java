package online.sevika.tm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Razorpay payment gateway.
 */
@Configuration
@ConfigurationProperties(prefix = "razorpay")
@Data
public class RazorpayConfig {
    private String keyId;
    private String keySecret;
    private String webhookSecret;
    private Boolean testMode = true;
}
