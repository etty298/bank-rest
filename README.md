## Bank REST (Spring Boot)

Коротко: Backend для управления банковскими картами. JWT-аутентификация, роли ADMIN/USER, PostgreSQL, Liquibase, Docker, Swagger.

### Стек
- Java 17
- Spring Boot 3 (Web, Security, Data JPA, Validation)
- PostgreSQL 15
- Liquibase
- jjwt
- Lombok
- springdoc-openapi
- JUnit 5, Mockito

### Быстрый старт (Docker Compose)
1) Собрать приложение:
```bash
mvn -DskipTests package
```
2) Запустить контейнеры:
```bash
docker-compose up --build
```
3) Приложение: http://localhost:8080

### Доступ и роли
- Админ (по умолчанию): username: admin, password: admin
- Пользователь: username: user1, password: user

JWT: POST /api/auth/login {"username":"admin","password":"admin"} → вернется token.
Передавайте в Authorization: Bearer <token>.

### Основные эндпоинты
- Пользователь
  - GET /api/cards — список своих карт (пагинация)
  - GET /api/cards/{id} — карта по id (только своя)
  - GET /api/cards/balance/{id} — баланс
  - POST /api/cards/transfer — перевод между своими картами
- Администратор
  - POST /api/admin/cards — создать карту пользователю
  - PATCH /api/admin/cards/{id}/activate — активировать
  - PATCH /api/admin/cards/{id}/block — заблокировать
  - DELETE /api/admin/cards/{id} — удалить
  - GET /api/admin/cards — все карты
  - CRUD пользователей: /api/admin/users

### Миграции Liquibase
- Файл мастер-чейнджлога: `src/main/resources/db/migration/master-changelog.xml`
- Применяются автоматически при старте приложения.

### Swagger/OpenAPI
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: `docs/openapi.yaml`

### Конфигурация
- `src/main/resources/application.yml` — JDBC, JWT, Liquibase и т.д. для локального развертывания
- `src/main/resources/application-docker.yml` — JDBC, Liquibase и т.д. для Docker

### Запуск тестов
```bash
mvn test
```

### Маскирование и шифрование
- Номер карты хранится зашифрованным (AES-256). Отображение — только маска вида `**** **** **** 1234`.
