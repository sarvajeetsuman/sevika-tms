package online.sevika.tm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for Sevika Task Management Application.
 * 
 * This application demonstrates software engineering best practices including:
 * - Clean Architecture
 * - SOLID Principles
 * - Domain-Driven Design
 * - RESTful API Design
 * - Security Best Practices
 * 
 * @author Sevika Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class SevikaTaskManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SevikaTaskManagementApplication.class, args);
    }

}
