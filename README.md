# Reminder App - REST API Documentation

Spring Boot приложение для управления напоминаниями с аутентификацией, уведомлениями по email/Telegram и планировщиком задач.

---

## Технологии

- **Backend**: Spring Boot 3.3.5, JDK 17, Spring Security, Spring Data JPA
- **Database**: PostgreSQL + Liquibase migrations
- **Authentication**: JWT tokens
- **Notifications**: JavaMailSender, Telegram Bot API
- **Scheduling**: Quartz Scheduler
- **Testing**: JUnit 5, Mockito, MockMvc
- **Documentation**: Swagger/OpenAPI

---

## JSON 


## Users ##

## Registration 
POST /api/auth/register
Content-Type: application/json

{
"username": "testuser",
"email": "test@example.com",
"password": "SecurePass123!",
"firstname": "Ivan",
"lastname": "Petrov",
"birthDate": "1990-05-15"
}

## Login
POST /api/auth/login
Content-Type: application/json

{
"username": "testuser",
"password": "SecurePass123!"
}

## Get Current User Profiles
GET /api/users/profile
Authorization: Bearer YOUR_JWT_TOKEN

## Update Profiles
PUT /api/users/1
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
"firstname": "Иван",
"lastname": "Петров",
"birthDate": "1990-05-15"
}

## List All Users
GET /api/users?page=0&size=10
Authorization: Bearer YOUR_JWT_TOKEN

## Get User By Id
GET /api/users/1
Authorization: Bearer YOUR_JWT_TOKEN

## Delete Users
DELETE /api/users/1
Authorization: Bearer YOUR_JWT_TOKEN


## Reminders ##

## Create Reminders
POST /api/reminders/reminder/create
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
"title": "Встреча с клиентом",
"description": "Обсудить условия контракта",
"remindAt": "2026-01-10T14:30:00+03:00",
"type": "EMAIL"
}

## Get All Reminders
GET /api/reminders?page=0&size=20&sort=remindAt,asc
Authorization: Bearer YOUR_JWT_TOKEN

## Get All Reminders By Filters
GET /api/reminders?status=PENDING&type=EMAIL&dateFrom=2026-01-01&dateTo=2026-12-31
Authorization: Bearer YOUR_JWT_TOKEN

## Get Reminders By Id
GET /api/reminders/1
Authorization: Bearer YOUR_JWT_TOKEN

## Update Reminders
PUT /api/reminders/1
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
"title": "Встреча перенесена",
"description": "Новое время - 16:00",
"remindAt": "2026-01-10T16:00:00+03:00",
"status": "PENDING"
}

## Delete Reminders
DELETE /api/reminders/1
Authorization: Bearer YOUR_JWT_TOKEN


## Additional endpoints for reminders ##

## Sort
GET /api/reminders/v1/sort?by=name&page=0&size=10
Authorization: Bearer YOUR_JWT_TOKEN

## Filters by date
GET /api/reminders/v1/filter?date=2026-01-10&page=0&size=10
Authorization: Bearer YOUR_JWT_TOKEN

## Filters by time
GET /api/reminders/v1/filter?time=14:30:00
Authorization: Bearer YOUR_JWT_TOKEN

## Filters by date and time
GET /api/reminders/v1/filter?date=2026-01-10&time=14:30:00
Authorization: Bearer YOUR_JWT_TOKEN

## Paginated List
GET /api/reminders/v1/list?page=0&size=5
Authorization: Bearer YOUR_JWT_TOKEN

## Search by description and title
GET /api/reminders/v1/search?query=встреча&page=0&size=10
Authorization: Bearer YOUR_JWT_TOKEN

