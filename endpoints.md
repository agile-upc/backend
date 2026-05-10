# Backend Integration Guide

## Base URL and auth
- Local base URL: `http://localhost:8080`
- Every app endpoint is under `/api/v1`
- Public routes: `/api/v1/authentication/**`, Swagger docs, and OpenAPI docs
- Every other route requires `Authorization: Bearer <token>`
- CORS currently allows any origin and the methods `HEAD, GET, POST, PUT, DELETE, PATCH`

## Request content types
- Use `application/json` unless an endpoint is explicitly marked as `multipart/form-data`
- `multipart/form-data` is used by:
  - `POST /api/v1/profiles`
  - `PUT /api/v1/profiles/{id}`
  - `POST /api/v1/posts`
  - `PUT /api/v1/posts/{id}`
- Upload limit is `10MB`
- For `FormData`, do not set `Content-Type` manually

## Shared structures

### Authenticated session
Returned by:
- `POST /api/v1/authentication/sign-up`
- `POST /api/v1/authentication/sign-in`

```json
{
  "userId": 1,
  "profileId": 1,
  "username": "user@example.com",
  "role": "FARMER",
  "farmerId": 1,
  "advisorId": null,
  "token": "jwt-token"
}
```

Request bodies:

`SignUp`
```json
{
  "username": "user@example.com",
  "password": "secret123",
  "role": "FARMER"
}
```

`SignIn`
```json
{
  "username": "user@example.com",
  "password": "secret123"
}
```

`role` must be one of `ADMIN`, `ADVISOR`, or `FARMER`.

### Profile
Returned by:
- `GET /api/v1/profiles`
- `GET /api/v1/profiles/{id}`
- `POST /api/v1/profiles`
- `PUT /api/v1/profiles/{id}`

```json
{
  "id": 1,
  "userId": 1,
  "firstName": "Ana",
  "lastName": "Lopez",
  "city": "Cusco",
  "country": "Peru",
  "birthDate": "1998-04-15",
  "description": "Agricultural engineer",
  "photo": "https://storage.googleapis.com/...",
  "occupation": "Soil specialist",
  "experience": 5
}
```

Request body for `POST /api/v1/profiles` (`multipart/form-data`):
- `firstName`: string, required
- `lastName`: string, required
- `city`: string, required
- `country`: string, required
- `birthDate`: `YYYY-MM-DD`, required
- `description`: string, optional
- `photo`: file, required
- `occupation`: string, optional
- `experience`: integer, optional

Request body for `PUT /api/v1/profiles/{id}` is the same structure, but `photo` is optional.

### Advisor
Returned by:
- `GET /api/v1/advisors`
- `GET /api/v1/advisors/{id}`

```json
{
  "id": 1,
  "userId": 3,
  "rating": 4.8
}
```

### Farmer
Returned by:
- `GET /api/v1/farmers`
- `GET /api/v1/farmers/{id}`

```json
{
  "id": 1,
  "userId": 7
}
```

### Advisor catalog item
Returned by:
- `GET /api/v1/advisors/catalog`

This endpoint returns an array of this structure:

```json
{
  "advisorId": 1,
  "userId": 3,
  "rating": 4.8,
  "profile": {
    "profileId": 12,
    "firstName": "Ana",
    "lastName": "Lopez",
    "city": "Cusco",
    "country": "Peru",
    "description": "Agricultural engineer",
    "photo": "https://storage.googleapis.com/...",
    "occupation": "Soil specialist",
    "experience": 5
  }
}
```

### Notification
Returned by:
- `GET /api/v1/notifications`
- `GET /api/v1/notifications/{id}`
- `POST /api/v1/notifications`

```json
{
  "id": 1,
  "userId": 7,
  "title": "Appointment reminder",
  "message": "Your meeting starts soon",
  "sendAt": "2026-05-09T18:00:00.000+00:00"
}
```

Request body for `POST /api/v1/notifications`:

```json
{
  "userId": 7,
  "title": "Appointment reminder",
  "message": "Your meeting starts soon",
  "sendAt": "2026-05-09T18:00:00.000+00:00"
}
```

### Post
Returned by:
- `GET /api/v1/posts`
- `GET /api/v1/posts/{id}`
- `POST /api/v1/posts`
- `PUT /api/v1/posts/{id}`

```json
{
  "id": 1,
  "advisorId": 2,
  "title": "Soil care tips",
  "description": "Practical recommendations for this week",
  "image": "https://storage.googleapis.com/..."
}
```

Request body for `POST /api/v1/posts` (`multipart/form-data`):
- `title`: string, required
- `description`: string, required
- `image`: file, required

Request body for `PUT /api/v1/posts/{id}` is the same structure, but `image` is optional.

### Available date
Returned by:
- `GET /api/v1/available_dates`
- `GET /api/v1/available_dates/{id}`
- `POST /api/v1/available_dates`
- `PUT /api/v1/available_dates/{id}`

```json
{
  "id": 1,
  "advisorId": 2,
  "scheduledDate": "2026-05-12",
  "startTime": "09:00",
  "endTime": "10:00",
  "status": "AVAILABLE"
}
```

Request body for both `POST /api/v1/available_dates` and `PUT /api/v1/available_dates/{id}`:

```json
{
  "scheduledDate": "2026-05-12",
  "startTime": "09:00",
  "endTime": "10:00"
}
```

Rules:
- `scheduledDate` must be today or later
- `startTime` and `endTime` use `HH:mm`
- `startTime` must be before `endTime`
- `status` is `AVAILABLE` or `UNAVAILABLE`

### Appointment
Returned by:
- `GET /api/v1/appointments`
- `GET /api/v1/appointments/{id}`
- `POST /api/v1/appointments`
- `PUT /api/v1/appointments/{id}`

```json
{
  "id": 1,
  "farmerId": 4,
  "availableDate": {
    "id": 1,
    "advisorId": 2,
    "scheduledDate": "2026-05-12",
    "startTime": "09:00",
    "endTime": "10:00",
    "status": "UNAVAILABLE"
  },
  "message": "I need help with irrigation planning",
  "status": "PENDING",
  "meetingUrl": "https://..."
}
```

Request body for `POST /api/v1/appointments`:

```json
{
  "availableDateId": 1,
  "message": "I need help with irrigation planning"
}
```

Request body for `PUT /api/v1/appointments/{id}`:

```json
{
  "message": "Updated details",
  "status": "ONGOING"
}
```

Notes:
- `status` values are `PENDING`, `ONGOING`, `COMPLETED`
- create uses the currently authenticated farmer
- the backend generates `meetingUrl`
- `GET /api/v1/appointments` uses the authenticated user to decide the list scope

### Review
Returned by:
- `GET /api/v1/reviews`
- `GET /api/v1/reviews/{id}`
- `POST /api/v1/reviews`
- `PUT /api/v1/reviews/{id}`

```json
{
  "id": 1,
  "advisorId": 2,
  "farmerId": 4,
  "comment": "Very helpful session",
  "rating": 5
}
```

Request body for `POST /api/v1/reviews`:

```json
{
  "advisorId": 2,
  "comment": "Very helpful session",
  "rating": 5
}
```

Request body for `PUT /api/v1/reviews/{id}`:

```json
{
  "comment": "Updated feedback",
  "rating": 4
}
```

Rules:
- create uses the currently authenticated farmer
- `rating` must be between `0` and `5`
- only one review is allowed per advisor/farmer pair

### AI chat
Returned by:
- `POST /api/v1/ai/chat`

Request body:

```json
{
  "message": "I need advice about soil quality"
}
```

Response body:

```json
{
  "response": "Advisor recommendation text",
  "advisorId": 2
}
```

## Error response structure
When the backend returns an error body, it uses:

```json
{
  "error": "400 BAD_REQUEST",
  "message": "Detailed error message"
}
```

## Delete responses
All delete endpoints return plain text, not JSON.

- `DELETE /api/v1/profiles/{id}` -> `"Profile with id {id} deleted successfully"`
- `DELETE /api/v1/advisors/{id}` -> `"Advisor with id {id} deleted successfully"`
- `DELETE /api/v1/farmers/{id}` -> `"Farmer with id {id} deleted successfully"`
- `DELETE /api/v1/notifications/{id}` -> `"Notification with id: {id} deleted successfully"`
- `DELETE /api/v1/posts/{id}` -> `"Post with id {id} successfully deleted"`
- `DELETE /api/v1/available_dates/{id}` -> `"Available Date with id {id} deleted successfully"`
- `DELETE /api/v1/appointments/{id}` -> `"Appointment with id {id} deleted successfully"`
- `DELETE /api/v1/reviews/{id}` -> `"Review with id {id} deleted successfully"`

## Endpoint groups

### IAM
- `POST /api/v1/authentication/sign-up`
  - request: `SignUp`
  - response: `Authenticated session`
- `POST /api/v1/authentication/sign-in`
  - request: `SignIn`
  - response: `Authenticated session`

### Profiles
- `GET /api/v1/profiles`
  - response: `Profile`
- `GET /api/v1/profiles/{id}`
  - response: `Profile`
- `POST /api/v1/profiles`
  - request: `Profile` create multipart body
  - response: `Profile`
- `PUT /api/v1/profiles/{id}`
  - request: same as profile create, with optional `photo`
  - response: `Profile`
- `DELETE /api/v1/profiles/{id}`
  - response: plain text success message

### Advisors and farmers
- `GET /api/v1/advisors/catalog`
  - response: `Array<Advisor catalog item>`
- `GET /api/v1/advisors`
  - response: `Advisor`
- `GET /api/v1/advisors/{id}`
  - response: `Advisor`
- `DELETE /api/v1/advisors/{id}`
  - response: plain text success message
- `GET /api/v1/farmers`
  - response: `Farmer`
- `GET /api/v1/farmers/{id}`
  - response: `Farmer`
- `DELETE /api/v1/farmers/{id}`
  - response: plain text success message

### Notifications
- `GET /api/v1/notifications`
  - response: `Array<Notification>`
- `GET /api/v1/notifications/{id}`
  - response: `Notification`
- `POST /api/v1/notifications`
  - request: notification create body
  - response: `Notification`
- `DELETE /api/v1/notifications/{id}`
  - response: plain text success message

### Posts
- `GET /api/v1/posts`
  - response: `Array<Post>`
- `GET /api/v1/posts/{id}`
  - response: `Post`
- `POST /api/v1/posts`
  - request: post create multipart body
  - response: `Post`
- `PUT /api/v1/posts/{id}`
  - request: same as post create, with optional `image`
  - response: `Post`
- `DELETE /api/v1/posts/{id}`
  - response: plain text success message

### Available dates
- `GET /api/v1/available_dates`
  - query params: optional `advisorId`, optional `isAvailable`
  - response: `Array<Available date>`
- `GET /api/v1/available_dates/{id}`
  - response: `Available date`
- `POST /api/v1/available_dates`
  - request: available date create body
  - response: `Available date`
- `PUT /api/v1/available_dates/{id}`
  - request: same as available date create
  - response: `Available date`
- `DELETE /api/v1/available_dates/{id}`
  - response: plain text success message

### Appointments
- `GET /api/v1/appointments`
  - response: `Array<Appointment>`
- `GET /api/v1/appointments/{id}`
  - response: `Appointment`
- `POST /api/v1/appointments`
  - request: appointment create body
  - response: `Appointment`
- `PUT /api/v1/appointments/{id}`
  - request: appointment update body
  - response: `Appointment`
- `DELETE /api/v1/appointments/{id}`
  - response: plain text success message

### Reviews
- `GET /api/v1/reviews`
  - query params: optional `advisorId`, optional `farmerId`
  - response: `Array<Review>`
- `GET /api/v1/reviews/{id}`
  - response: `Review`
- `POST /api/v1/reviews`
  - request: review create body
  - response: `Review`
- `PUT /api/v1/reviews/{id}`
  - request: review update body
  - response: `Review`
- `DELETE /api/v1/reviews/{id}`
  - response: plain text success message

### AI
- `POST /api/v1/ai/chat`
  - request: AI chat body
  - response: AI chat response

## Frontend implementation notes
- Use `userId`, `profileId`, `farmerId`, and `advisorId` from the auth response to decide which screens and queries to show
- Use `/api/v1/profiles`, `/api/v1/advisors`, `/api/v1/farmers`, and `/api/v1/notifications` as self endpoints for the logged-in user
- Use `/api/v1/advisors/catalog` when a farmer needs the advisor marketplace view
- A farmer account normally creates appointments and reviews
- An advisor account normally creates available dates and posts
- If you upload a new profile photo or post image, replace the previous URL with the new value returned by the backend
