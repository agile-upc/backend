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
  - `POST /api/v1/authentication/sign-up`
  - `POST /api/v1/profiles`
  - `PUT /api/v1/profiles/{id}`
  - `POST /api/v1/posts`
  - `PUT /api/v1/posts/{id}`
- Upload limit is `10MB`
- For `FormData`, do not set `Content-Type` manually

## Public media behavior
- `Profile.photo` and `Post.image` are returned as direct Google Cloud Storage URLs
- Those URLs are intended for direct browser rendering in the client
- This deployment assumes the upload bucket is configured for public object reads
- The backend does not generate signed URLs for these media fields
- Uploaded images are resized and converted to WebP before upload

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
`multipart/form-data`
- `username`: string, required
- `password`: string, required
- `role`: `ADMIN` | `ADVISOR` | `FARMER`, required
- `firstName`: string, required
- `lastName`: string, required
- `city`: string, required
- `country`: string, required
- `birthDate`: `YYYY-MM-DD`, required
- `description`: string, optional
- `photo`: file, optional
- `occupation`: string, optional
- `experience`: integer, optional

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

`photo` is a public browser-accessible GCS URL.

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

`GET /api/v1/advisors/{id}` returns the full advisor summary:

```json
{
  "advisorId": 1,
  "userId": 3,
  "rating": 4.8,
  "profile": {
    "profileId": 12,
    "userId": 3,
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
    "userId": 3,
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

`image` is a public browser-accessible GCS URL.

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
  "farmerProfile": {
    "profileId": 9,
    "userId": 7,
    "firstName": "Luis",
    "lastName": "Quispe",
    "city": "Cusco",
    "country": "Peru",
    "birthDate": "1995-03-21",
    "description": "Small-scale farmer",
    "photo": "https://storage.googleapis.com/...",
    "occupation": "Potato farmer",
    "experience": 6
  },
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

### AI recommendations
Returned by:
- `POST /api/v1/ai/recommendations`

Purpose:
- Recommend up to 3 advisors for the authenticated farmer
- Return a direct recommendation when the backend has enough confidence
- Ask at most 1 clarification question when the initial request is too vague
- Degrade gracefully if Gemini is unavailable or rate-limited

Authentication:
- Requires `Authorization: Bearer <token>`
- Intended for authenticated users with farmer context

How to use it:
1. Send the farmer's first message in `message`
2. Set `conversationId` to `null` or omit it on the first turn
3. Inspect `status`
4. If `status = READY`, use `selectedAdvisorId`, `matches`, and optionally `draftAppointmentMessage`
5. If `status = NEEDS_MORE_INFO`, show `clarifyingQuestion` to the user and send the next reply back with the returned `conversationId`
6. If `status = UNAVAILABLE`, show the summary and let the user try again later or reformulate the request

First-turn request:

```json
{
  "message": "Necesito ayuda con la fertilizacion del suelo",
  "conversationId": null
}
```

The backend uses the authenticated farmer profile location when available. If the profile does not have `city` or `country`, location is simply ignored in the ranking score.

Request fields:
- `message`: free-text farmer request
- `conversationId`: `null` on the first turn, or the value returned by a previous `NEEDS_MORE_INFO` response

Guidance for `message`:
- Better requests include the crop, problem, goal, or production stage
- Very short or vague requests such as `"ayuda"` may trigger `NEEDS_MORE_INFO`
- The endpoint still returns ranked matches even when it asks a clarification question

Example response when the backend is ready to recommend immediately:

```json
{
  "status": "READY",
  "selectedAdvisorId": 2,
  "matches": [
    {
      "advisorId": 2,
      "fullName": "Ana Lopez",
      "occupation": "Soil specialist",
      "rating": 4.8,
      "experience": 5,
      "city": "Cusco",
      "country": "Peru",
      "nextAvailableDate": "2026-05-14",
      "why": "esta en tu misma ciudad, su perfil se alinea con tu necesidad, tiene una calificacion de 4.8, 5 años de experiencia, tiene disponibilidad desde 2026-05-14."
    }
  ],
  "summary": "La mejor opcion es Ana Lopez por su cercania, experiencia y disponibilidad.",
  "clarifyingQuestion": null,
  "draftAppointmentMessage": "Hola Ana Lopez, necesito asesoria sobre la fertilizacion del suelo de mi cultivo. Me gustaria coordinar una cita para revisar mi caso.",
  "conversationId": null,
  "questionsAsked": 0,
  "maxQuestions": 1,
  "usedFallback": false
}
```

Example response when more detail is needed:

```json
{
  "status": "NEEDS_MORE_INFO",
  "selectedAdvisorId": null,
  "matches": [
    {
      "advisorId": 2,
      "fullName": "Ana Lopez",
      "occupation": "Soil specialist",
      "rating": 4.8,
      "experience": 5,
      "city": "Cusco",
      "country": "Peru",
      "nextAvailableDate": "2026-05-14",
      "why": "esta en tu misma ciudad, tiene una calificacion de 4.8, 5 anos de experiencia, tiene disponibilidad desde 2026-05-14."
    },
    {
      "advisorId": 5,
      "fullName": "Bruno Rojas",
      "occupation": "Crop advisor",
      "rating": 4.7,
      "experience": 6,
      "city": "Cusco",
      "country": "Peru",
      "nextAvailableDate": "2026-05-15",
      "why": "esta en tu misma ciudad, tiene una calificacion de 4.7, 6 anos de experiencia, tiene disponibilidad desde 2026-05-15."
    }
  ],
  "summary": "Te muestro las opciones mas cercanas, pero necesito un poco mas de detalle para recomendarte una sola.",
  "clarifyingQuestion": "Que cultivo, problema especifico o etapa del proceso agricola necesitas atender?",
  "draftAppointmentMessage": null,
  "conversationId": "9c0ee4bb-a4ee-47f9-8f7e-6c1d2c5b1024",
  "questionsAsked": 1,
  "maxQuestions": 1,
  "usedFallback": true
}
```

Second-turn clarification request:

```json
{
  "message": "Es para maiz y tengo problemas con la calidad del suelo",
  "conversationId": "9c0ee4bb-a4ee-47f9-8f7e-6c1d2c5b1024"
}
```

Example second-turn response:

```json
{
  "status": "READY",
  "selectedAdvisorId": 2,
  "matches": [
    {
      "advisorId": 2,
      "fullName": "Ana Lopez",
      "occupation": "Soil specialist",
      "rating": 4.8,
      "experience": 5,
      "city": "Cusco",
      "country": "Peru",
      "nextAvailableDate": "2026-05-14",
      "why": "esta en tu misma ciudad, su perfil se alinea con tu necesidad, tiene una calificacion de 4.8, 5 anos de experiencia, tiene disponibilidad desde 2026-05-14."
    }
  ],
  "summary": "La mejor opcion es Ana Lopez por su cercania, experiencia y disponibilidad dentro del shortlist evaluado.",
  "clarifyingQuestion": null,
  "draftAppointmentMessage": "Hola Ana Lopez, necesito asesoria sobre Necesito ayuda con la fertilizacion del suelo. Es para maiz y tengo problemas con la calidad del suelo. Me gustaria coordinar una cita para revisar mi caso.",
  "conversationId": "9c0ee4bb-a4ee-47f9-8f7e-6c1d2c5b1024",
  "questionsAsked": 1,
  "maxQuestions": 1,
  "usedFallback": true
}
```

Clarification flow rules:
- The backend allows at most 1 clarification question
- On the next request with the same valid `conversationId`, the backend must return `READY` or `UNAVAILABLE`; it will not ask another question
- The recommendation shortlist is cached server-side for 15 minutes
- If `conversationId` is unknown or expired, the backend starts a fresh session
- The second turn reuses the cached shortlist and does not rebuild advisor candidates from the database for that session

Response field rules:
- `status` is `READY`, `NEEDS_MORE_INFO`, or `UNAVAILABLE`
- `selectedAdvisorId` is only present when the service has enough confidence to recommend one advisor directly
- `matches` contains up to 3 ranked advisors
- `summary` is the main text the client should display for the result
- `clarifyingQuestion` is used when the request needs more detail
- `draftAppointmentMessage` is only intended for the booking flow
- `conversationId` is returned when the flow enters a clarification session and must be sent back on the next turn
- `questionsAsked` tracks how many clarification questions have already been used
- `maxQuestions` is currently always `1`
- `usedFallback` is `true` when Gemini was skipped or the backend degraded to deterministic templated output

How clients should interpret `usedFallback`:
- `false` means Gemini-assisted phrasing/scoring was available
- `true` means the endpoint still worked, but the final text and/or ranking flow fell back to deterministic backend logic
- Clients should not treat `usedFallback = true` as an error

Recommended client behavior:
- Always render `matches`, even if `status = NEEDS_MORE_INFO`
- Persist `conversationId` only for the active clarification flow
- Clear the stored `conversationId` after a final `READY` or `UNAVAILABLE` response
- If the backend returns `NEEDS_MORE_INFO` without a usable `conversationId`, treat it as a fresh session failure and ask the user to retry
- If a follow-up request gets a new `conversationId`, assume the previous session expired and continue with the new one

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
  - request: `SignUp` multipart body with account and profile fields
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
  - response: `Advisor catalog item`
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
- `POST /api/v1/ai/recommendations`
  - request: AI recommendations body
  - response: AI recommendations response

## Frontend implementation notes
- Use `userId`, `profileId`, `farmerId`, and `advisorId` from the auth response to decide which screens and queries to show
- Use `/api/v1/profiles`, `/api/v1/advisors`, `/api/v1/farmers`, and `/api/v1/notifications` as self endpoints for the logged-in user
- Use `/api/v1/advisors/catalog` when a farmer needs the advisor marketplace view
- A farmer account normally creates appointments and reviews
- An advisor account normally creates available dates and posts
- If you upload a new profile photo or post image, replace the previous URL with the new value returned by the backend
