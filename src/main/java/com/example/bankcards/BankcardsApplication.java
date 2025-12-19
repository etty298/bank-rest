package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Main entry point for the Bank Cards REST API application.
 * <p>
 * This Spring Boot application provides a secure backend for managing bank cards with the following features:
 * <ul>
 *   <li>JWT-based authentication and authorization</li>
 *   <li>Role-based access control (ADMIN and USER roles)</li>
 *   <li>Card management with encrypted card numbers (AES-256)</li>
 *   <li>Balance inquiries and transfers between cards</li>
 *   <li>PostgreSQL database with Liquibase migrations</li>
 *   <li>OpenAPI/Swagger documentation</li>
 * </ul>
 * <p>
 * The application uses Spring Security for authentication and authorization,
 * Spring Data JPA for database operations, and follows REST architectural principles.
 *
 * @author Bank Cards Development Team
 * @version 0.0.1-SNAPSHOT
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class BankcardsApplication {
    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(BankcardsApplication.class, args);
    }
}



