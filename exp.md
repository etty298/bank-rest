# Bank Cards REST API - Application Explanation

## Table of Contents
1. [Overview](#overview)
2. [Application Architecture](#application-architecture)
3. [Application Startup Flow](#application-startup-flow)
4. [Security Architecture](#security-architecture)
5. [Request Flow Examples](#request-flow-examples)
6. [Database Schema](#database-schema)
7. [Key Components Explained](#key-components-explained)
8. [Common Scenarios](#common-scenarios)

---

## Overview

The Bank Cards REST API is a Spring Boot application that provides a secure backend for managing bank cards. It implements:

- **JWT-based authentication** for stateless security
- **Role-based access control** (ADMIN and USER roles)
- **Encrypted storage** of sensitive card data (AES-256)
- **RESTful API design** following best practices
- **Transaction management** for data consistency

### Technology Stack
- **Java 17** - Programming language
- **Spring Boot 3.3.4** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access
- **PostgreSQL 15** - Relational database
- **Liquibase** - Database migration management
- **JWT (jjwt 0.11.5)** - JSON Web Token implementation
- **Lombok** - Boilerplate code reduction
- **BCrypt** - Password hashing
- **AES-256** - Card number encryption

---

## Application Architecture

The application follows a **layered architecture** pattern:

```
Controllers (REST API)
    |
    v
Services (Business Logic)
    |
    v
Repositories (Data Access)
    |
    v
Database (PostgreSQL)
```

### Package Structure

```
com.example.bankcards/
├── config/              # Configuration classes (Security)
├── controller/          # REST API endpoints
├── dto/                 # Data Transfer Objects
├── entity/             # JPA entities (database models)
├── exception/          # Custom exceptions and handlers
├── repository/         # Database access interfaces
├── security/           # Security components (JWT, filters)
├── service/            # Business logic
└── util/               # Utility classes (crypto, masking)
```

---

## Application Startup Flow

### 1. Spring Boot Initialization

**Entry Point:** `BankcardsApplication.java`

When you run the application, here's what happens step by step:

1. **Component Scanning** - Spring scans all packages under `com.example.bankcards` for:
   - `@Component`, `@Service`, `@Repository`, `@Controller` annotations
   - Automatically creates beans (instances) of these classes

2. **Configuration Loading** - Reads configuration from:
   - `application.yml` (default profile)
   - `application-docker.yml` (Docker profile)
   - Properties include: database URL, JWT secret, encryption key, etc.

3. **Database Connection** - Establishes connection to PostgreSQL using JPA/Hibernate

4. **Liquibase Migrations** - Automatically runs database migrations:
   - Located in: `src/main/resources/db/migration/`
   - Creates tables: `users`, `cards`
   - Loads initial seed data (admin user, test users, test cards)

5. **Security Configuration** - Initializes Spring Security:
   - Sets up JWT authentication filter
   - Configures public endpoints (e.g., `/api/auth/login`)
   - Enables method-level security (`@PreAuthorize`)

6. **Web Server Start** - Tomcat embedded server starts on port 8080

### 2. Bean Creation Order

Spring creates beans in dependency order:

```
1. Configuration Beans
   - SecurityConfig
   - Application Properties

2. Utility Beans
   - CryptoUtil (encryption key from config)
   - PasswordEncoder (BCrypt)

3. Security Beans
   - JwtTokenProvider (JWT secret from config)
   - CustomUserDetailsService

4. Repository Beans
   - UserRepository
   - CardRepository

5. Service Beans
   - UserService
   - CardService
   - AuthService

6. Controller Beans
   - AuthController
   - CardController
   - AdminCardController
   - AdminUserController

7. Security Filters
   - JwtAuthenticationFilter
```

---

## Security Architecture

### How JWT Authentication Works

The application uses **stateless JWT authentication**. Here's the complete flow:

#### Step 1: User Login

```
Client                 AuthController        AuthService       Database
  |                         |                     |               |
  |--POST /api/auth/login-->|                     |               |
  | {username, password}    |                     |               |
  |                         |--login()----------->|               |
  |                         |                     |--find user--->|
  |                         |                     |<--user data---|
  |                         |                     |               |
  |                         |                     | (verify password)
  |                         |                     | (generate JWT)
  |                         |<--JwtResponse-------|               |
  |<--200 OK + JWT token----|                     |               |
```

**What happens:**
1. User sends username and password
2. System finds user in database
3. Compares password hash using BCrypt
4. If valid, generates JWT token
5. Returns token to client
6. Client stores token (e.g., localStorage)

#### Step 2: Authenticated Requests

```
Client              JwtFilter         Controller       Service        Database
  |                     |                 |               |               |
  |--GET /api/cards---->|                 |               |               |
  | Header: Bearer JWT  |                 |               |               |
  |                     | (extract JWT)   |               |               |
  |                     | (validate JWT)  |               |               |
  |                     | (load user)     |               |               |
  |                     |                 |               |               |
  |                     |--request------->|               |               |
  |                     |                 |--getOwn()---->|               |
  |                     |                 |               |--query cards->|
  |                     |                 |               |<--card data---|
  |                     |                 |<--CardResponse|               |
  |<--200 OK + cards----|<--response------|               |               |
```

**What happens:**
1. Client sends request with JWT in header: `Authorization: Bearer <token>`
2. JwtAuthenticationFilter intercepts request
3. Extracts and validates JWT token
4. Loads user details and sets authentication
5. Request proceeds to controller
6. Controller calls service
7. Service executes business logic
8. Response returned to client

### JWT Token Structure

A JWT token has three parts separated by dots:

```
eyJhbGc...header.eyJzdWI...payload.5K7xP0e...signature
```

**Example decoded:**

Header:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

Payload:
```json
{
  "sub": "admin",
  "role": "ADMIN",
  "iat": 1698765432,
  "exp": 1698769032
}
```

### Authorization Levels

**Public Endpoints** (no authentication needed):
- `POST /api/auth/login`
- `/swagger-ui/**`

**User Endpoints** (authentication required):
- `GET /api/cards` - View own cards
- `POST /api/cards/transfer` - Transfer money

**Admin Endpoints** (ADMIN role required):
- `POST /api/admin/cards` - Create cards
- `POST /api/admin/users` - Create users
- All admin operations

---

## Request Flow Examples

### Example 1: User Login

**Request:**
```http
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
  "username": "user1",
  "password": "user"
}
```

**Processing Steps:**
1. Request reaches `AuthController.login()`
2. Validation: username and password not blank
3. `AuthService.login()` is called
4. Password verified with BCrypt
5. JWT token generated
6. Response returned

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user1",
  "role": "USER"
}
```

### Example 2: View Own Cards

**Request:**
```http
GET /api/cards?page=0&size=10 HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Processing Steps:**
1. JWT filter validates token
2. User authenticated
3. Controller calls service
4. Service gets current user from SecurityContext
5. Queries database for user's cards
6. Decrypts card numbers
7. Masks card numbers (shows only last 4 digits)
8. Returns response

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "maskedCardNumber": "**** **** **** 1234",
      "expirationDate": "2025-12-31",
      "status": "ACTIVE",
      "balance": 1000.00
    }
  ],
  "totalElements": 1
}
```

### Example 3: Transfer Money

**Request:**
```http
POST /api/cards/transfer HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromCardId": 1,
  "toCardId": 2,
  "amount": 100.00
}
```

**Processing Steps:**
1. JWT authentication
2. Validation checks:
   - Cards are different
   - Amount is positive
   - Both cards belong to current user
   - Both cards are ACTIVE
   - Source card has sufficient balance
3. Transaction starts
4. Subtract from source card
5. Add to destination card
6. Save both cards
7. Commit transaction

**Response:**
```http
HTTP/1.1 200 OK
```

---

## Database Schema

### Entity Relationship Diagram

```
users (1) ----< (many) cards

users table:
- id (PK)
- username (UNIQUE)
- password (hashed with BCrypt)
- role (ADMIN or USER)
- enabled (true/false)

cards table:
- id (PK)
- encrypted_card_number (UNIQUE, encrypted with AES-256)
- owner_id (FK -> users.id)
- expiration_date
- status (ACTIVE, BLOCKED, EXPIRED)
- balance (DECIMAL)
```

### Table: users

| Column   | Type         | Description                    |
|----------|--------------|--------------------------------|
| id       | BIGINT       | Primary key, auto-increment    |
| username | VARCHAR(100) | Unique login name              |
| password | VARCHAR(255) | BCrypt hashed password         |
| role     | VARCHAR(20)  | ADMIN or USER                  |
| enabled  | BOOLEAN      | Account active status          |

### Table: cards

| Column                | Type          | Description                    |
|-----------------------|---------------|--------------------------------|
| id                    | BIGINT        | Primary key, auto-increment    |
| encrypted_card_number | VARCHAR(512)  | AES-256 encrypted card number  |
| owner_id              | BIGINT        | Foreign key to users           |
| expiration_date       | DATE          | Card expiration date           |
| status                | VARCHAR(20)   | ACTIVE, BLOCKED, or EXPIRED    |
| balance               | DECIMAL(19,2) | Current balance                |

### Initial Data

The application starts with pre-loaded test data:

**Users:**
- `admin` / `admin` - ADMIN role
- `user1` / `user` - USER role
- `user2` / `user` - USER role

**Cards:**
- Multiple test cards with different statuses

---

## Key Components Explained

### 1. CryptoUtil - Card Number Encryption

**Why encrypt card numbers?**
- Protects sensitive data in database
- Complies with PCI DSS requirements
- Prevents data breaches

**How it works:**

```java
// Encryption
1. Generate random IV (Initialization Vector)
2. Use AES-256 with CBC mode
3. Encrypt card number
4. Combine IV + encrypted data
5. Base64 encode for storage

// Decryption
1. Base64 decode
2. Extract IV from first 16 bytes
3. Extract encrypted data
4. Decrypt using IV and key
5. Return plain text
```

**Example:**
- Input: `"1234567890123456"`
- Encrypted: `"kQ7x...Base64String...9mL="`
- Stored in database: `kQ7x...Base64String...9mL=`
- Displayed to user: `"**** **** **** 3456"`

### 2. MaskingUtil - Card Number Masking

**Purpose:** Show only last 4 digits of card number

```java
Input:  "1234567890123456"
Output: "**** **** **** 3456"
```

**When used:**
- Every API response with card data
- Ensures PCI DSS compliance
- Users never see full card numbers

### 3. Transaction Management

**The @Transactional Annotation:**

This ensures ACID properties:
- **Atomicity:** All operations succeed or all fail
- **Consistency:** Database stays valid
- **Isolation:** Transactions don't interfere
- **Durability:** Committed changes persist

**Example Scenario:**

```java
@Transactional
public void transfer(TransferRequest request) {
    // Step 1: Subtract 100 from Card A
    cardA.setBalance(1000 - 100); // 900
    
    // Step 2: Add 100 to Card B
    cardB.setBalance(500 + 100);  // 600
    
    // If exception occurs here, BOTH changes are rolled back
    // Card A remains 1000, Card B remains 500
}
```

### 4. Exception Handling

All exceptions are caught by `GlobalExceptionHandler`:

| Exception                    | HTTP Status | Message              |
|------------------------------|-------------|----------------------|
| NotFoundException            | 404         | Resource not found   |
| AccessDeniedException        | 403         | Access denied        |
| IllegalArgumentException     | 400         | Invalid request      |
| BadCredentialsException      | 401         | Wrong credentials    |
| ValidationException          | 400         | Validation failed    |

**Example:**
```java
// Service throws exception
throw new NotFoundException("Card not found: " + id);

// Handler converts to HTTP response
{
  "error": "NOT_FOUND",
  "message": "Card not found: 123"
}
```

### 5. Validation

**Two levels of validation:**

**1. Bean Validation (at Controller level):**
```java
public record LoginRequest(
    @NotBlank String username,  // Must not be empty
    @NotBlank String password   // Must not be empty
) {}
```

**2. Business Validation (at Service level):**
```java
// Check card ownership
if (!card.getOwner().getId().equals(currentUser.getId())) {
    throw new AccessDeniedException("Not your card");
}

// Check sufficient balance
if (card.getBalance().compareTo(amount) < 0) {
    throw new IllegalArgumentException("Insufficient funds");
}
```

---

## Common Scenarios

### Scenario 1: New User Joins

```
1. Admin logs in
2. Admin creates user via POST /api/admin/users
3. Password is hashed with BCrypt
4. User is saved to database
5. User can now login with credentials
6. Admin creates card for user
7. Admin activates card
8. User can now use card for transfers
```

### Scenario 2: User Makes Transfer

```
1. User logs in and receives JWT token
2. User views cards to check balance
3. User initiates transfer
4. System validates:
   - Token is valid
   - User owns both cards
   - Cards are ACTIVE
   - Sufficient balance
5. Transaction executed atomically
6. Balances updated
7. Success response returned
```

### Scenario 3: Admin Manages Cards

```
1. Admin logs in
2. Admin views all cards in system
3. Admin finds blocked card
4. Admin activates card via PATCH /api/admin/cards/{id}/activate
5. Card status changes to ACTIVE
6. User can now use the card
```

### Scenario 4: Security Prevention

**Attempt: User tries to access another user's card**
```
1. User A tries: GET /api/cards/999 (belongs to User B)
2. Service checks ownership
3. Throws AccessDeniedException
4. Returns 403 Forbidden
5. No data leaked
```

**Attempt: Invalid JWT token**
```
1. Client sends expired/invalid token
2. JwtAuthenticationFilter validates
3. Token validation fails
4. Returns 401 Unauthorized
5. Request blocked
```

---

## Configuration Explained

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bankcards
    username: postgres
    password: postgres
    
  jpa:
    hibernate:
      ddl-auto: none  # Liquibase handles schema, not Hibernate
      
  liquibase:
    change-log: classpath:db/migration/master-changelog.xml

app:
  security:
    jwt:
      secret: your-secret-key-min-256-bits  # For signing JWT
      expiration-minutes: 60                # Token valid for 1 hour
      
  crypto:
    aesSecret: 32-byte-key-for-aes-256      # For encrypting cards
```

**Important Settings:**
- `ddl-auto: none` - Database schema managed by Liquibase, not auto-generated
- `jwt.secret` - Must be strong, minimum 256 bits
- `jwt.expiration-minutes` - Token lifetime before re-login needed
- `crypto.aesSecret` - Must be exactly 32 bytes for AES-256

---

## Security Best Practices in This App

### 1. Password Security
- Never store plain text passwords
- BCrypt hashing with salt
- Secure password comparison

### 2. Card Number Security
- Encrypted in database (AES-256)
- Masked in API responses
- Never logged or exposed

### 3. JWT Security
- Signed tokens prevent tampering
- Expiration prevents old token use
- Stateless (no server sessions)

### 4. Access Control
- Authentication required for most endpoints
- Role-based authorization
- Ownership verification for resources
- Method-level security

### 5. Data Validation
- Input validation at controller level
- Business rule validation at service level
- Prevents SQL injection (parameterized queries)

---

## Troubleshooting Common Issues

### Issue: "Unauthorized" (401)
**Cause:** Missing or invalid JWT token

**Solution:**
1. Login to get valid token
2. Include token in header: `Authorization: Bearer <token>`
3. Check token hasn't expired

### Issue: "Forbidden" (403)
**Cause:** Insufficient permissions

**Solution:**
1. Check if admin role is required
2. Verify you're accessing your own resources
3. Admin endpoints need ADMIN role

### Issue: "Card not found" (404)
**Cause:** Card doesn't exist or wrong ownership

**Solution:**
1. Verify card ID is correct
2. Check you own the card
3. Admin can see all cards

### Issue: "Insufficient funds" (400)
**Cause:** Not enough balance for transfer

**Solution:**
1. Check current balance first
2. Ensure source card has enough money

### Issue: "Both cards must be ACTIVE" (400)
**Cause:** One or both cards are BLOCKED/EXPIRED

**Solution:**
1. Check card status
2. Admin can activate BLOCKED cards
3. EXPIRED cards cannot be reactivated

---

## How the Application Processes a Request

Let's trace a complete request from start to finish:

### Complete Flow: GET /api/cards

```
1. HTTP Request arrives
   GET /api/cards
   Authorization: Bearer eyJhbGc...
   
2. JwtAuthenticationFilter (before controller)
   - Extract JWT from header
   - Validate JWT signature
   - Check expiration
   - Extract username from token
   - Load user from database
   - Set authentication in SecurityContext
   
3. Spring Security checks authorization
   - Endpoint requires authentication: YES
   - User is authenticated: YES
   - Proceed to controller
   
4. CardController.getMyCards()
   - Method called with Pageable parameter
   - Calls CardService.getOwn()
   
5. CardService.getOwn()
   - Get current user from SecurityContext
   - Query database: SELECT * FROM cards WHERE owner_id = ?
   - For each card:
     * Decrypt card number
     * Mask card number (**** **** **** 1234)
     * Create CardResponse DTO
   - Return Page<CardResponse>
   
6. Spring converts to JSON
   - Serialize CardResponse objects
   - Create JSON response
   
7. HTTP Response sent
   HTTP/1.1 200 OK
   Content-Type: application/json
   { "content": [...], "totalElements": 3 }
```

---

## Conclusion

This Bank Cards REST API demonstrates a complete, production-ready Spring Boot application with:

1. **Secure Authentication** - JWT-based, stateless
2. **Role-Based Authorization** - ADMIN and USER roles
3. **Data Encryption** - AES-256 for card numbers
4. **Transaction Management** - ACID compliance
5. **Error Handling** - Centralized exception handling
6. **Input Validation** - Multiple validation layers
7. **Clean Architecture** - Layered design
8. **Database Migrations** - Liquibase for version control

The application follows Spring Boot best practices and is suitable for understanding how to build secure financial applications.

**Key Takeaways:**
- Security is implemented at multiple levels
- Sensitive data is always encrypted and masked
- Transactions ensure data consistency
- Clear separation of concerns makes code maintainable
- Comprehensive error handling provides good user experience

---

**End of Documentation**

For more information, see the Javadoc comments in the source code.

