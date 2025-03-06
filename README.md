# Wallet Application

Wallet Application — это RESTful сервис, разработанный на **Spring Boot** с использованием реактивной архитектуры (**Project Reactor**) для управления кошельками пользователей. 
Приложение позволяет выполнять операции депозита и снятия средств, а также получать текущий баланс кошелька. 
Оно использует **PostgreSQL** в качестве базы данных, **Liquibase** для миграций и **Testcontainers** для интеграционного тестирования.

## Основные возможности
- **Депозит средств:** Добавление средств на кошелек через `POST /api/v1/wallet`.
- **Снятие средств:** Снятие средств с кошелька через `POST /api/v1/wallet` с проверкой на достаточность средств.
- **Получение баланса:** Запрос текущего баланса через `GET /api/v1/wallets/{walletId}`.
- Реактивная архитектура с **Spring WebFlux** и **R2DBC**.
- Полное покрытие API интеграционными тестами.
- Гибкая настройка параметров через переменные окружения без пересборки контейнеров.

## Технологии
- **Java:** 17
- **Spring Boot:** 3.4.3
- **Spring Data R2DBC:** Реактивное взаимодействие с базой данных
- **PostgreSQL:** 15
- **Liquibase:** Управление миграциями
- **Testcontainers:** Интеграционное тестирование
- **Gradle:** 8.12.1
- **Docker:** Контейнеризация
- **Docker Compose:** Оркестрация сервисов



## Зависимости
Определены в `build.gradle.kts`:
- `org.springframework.boot:spring-boot-starter-webflux`
- `org.springframework.boot:spring-boot-starter-data-r2dbc`
- `io.r2dbc:r2dbc-postgresql`
- `org.liquibase:liquibase-core`
- `org.testcontainers:postgresql` (test)
- `org.springframework.boot:spring-boot-starter-test` (test)

## Установка и запуск

### Требования
- **JDK:** 17+
- **Gradle:** 8.12.1+ (или используйте `./gradlew`)
- **Docker:** Для контейнеризации и Testcontainers
- **Docker Compose:** Для запуска сервисов

### Локальный запуск
1. Сборка проекта:
   ```bash
   ./gradlew clean build
2.  Запуск приложения:
3. ```bash
   ./gradlew bootRun
Приложение доступно на http://localhost:8081 (порт можно изменить через переменную APP_PORT).

1. Сборка и запуск:
  ```bash
   docker compose up --build
   
2. Остановка:
  ```bash
  docker compose down
  
### Настройка параметров
Все ключевые параметры приложения и базы данных настраиваются через переменные окружения в docker-compose.yml или файл .env без пересборки контейнеров.

Пример .env:

APP_PORT=8082
DB_POOL_MAX_SIZE=400
POSTGRES_MAX_CONNECTIONS=450
POSTGRES_SHARED_BUFFERS=512MB
APP_TIMEOUT_MILLIS=1000#


## Структура проекта

src/
├── main/
│   ├── java/com/wallet/app/
│   │   ├── controller/       # REST-контроллеры
│   │   ├── dto/             # DTO для запросов/ответов
│   │   ├── entity/          # Сущности базы данных
│   │   ├── exception/       # Пользовательские исключения
│   │   ├── repository/      # R2DBC-репозитории
│   │   ├── service/         # Бизнес-логика
│   │   └── AppApplication.java  # Точка входа
│   └── resources/
│       ├── application.yml  # Конфигурация приложения
│       └── db/changelog/    # Миграции Liquibase
├── test/
│   └── java/com/wallet/app/
│       └── WalletControllerTest.java  # Интеграционные тесты
├── Dockerfile               # Dockerfile для сборки
├── docker-compose.yml       # Конфигурация Docker Compose
└── build.gradle.kts         # Конфигурация Gradle
