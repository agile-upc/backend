# Agrotech Backend

Spring Boot backend for the Agrotech platform. It provides authentication, profiles, advisors, farmers, notifications, posts, appointments, reviews, and AI-assisted advisor recommendations.

## Stack
- Java 21
- Spring Boot 3
- Spring Security with JWT
- Spring Data JPA
- MySQL
- Google Cloud Storage for uploaded media
- Vertex AI Gemini for AI chat and structured advisor recommendations

## Main behavior
- Base API path: `/api/v1`
- Public routes: `/api/v1/authentication/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- All other routes require `Authorization: Bearer <token>`
- `POST /api/v1/authentication/sign-up` creates the user and initial profile in one multipart request
- Profile photos and post images are returned as direct public GCS URLs

## Advisory workflow
- Farmers use the advisor catalog and advisor detail APIs to evaluate advisors before booking.
- Advisors create, update, and delete available schedule slots through `/api/v1/available_dates`.
- Farmers book advisory sessions through `/api/v1/appointments` with an `availableDateId` and consultation `message`.
- Creating an appointment marks the selected available date as `UNAVAILABLE`.
- Booking checks both the available date status and existing appointments for the same slot before creating a new appointment.
- Appointment detail responses include the farmer/advisor profile summaries, selected schedule, meeting URL, status, and consultation message.
- AI recommendation endpoints return advisor candidates and a draft consultation message for handoff into the booking flow.

## Project docs
- API contract: [endpoints.md](./endpoints.md)
- Deployment guide: [deployment.md](./deployment.md)

## Environment variables
Create a local `.env` file with:

```properties
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=agrotech
MYSQL_USER=root
MYSQL_PASSWORD=secret
JWT_SECRET=change-me
GCS_BUCKET_NAME=your_gcs_bucket_name
GCS_PROJECT_ID=your_gcs_project_id
VERTEX_AI_PROJECT_ID=your_vertex_project_id
VERTEX_AI_LOCATION=global
VERTEX_AI_MODEL_ID=gemini-2.5-flash-preview-09-2025
```

## Local development
1. Start a MySQL database.
2. Create the database configured in `MYSQL_DATABASE`.
3. Authenticate Google Cloud Application Default Credentials if you want uploads and AI requests to work:

```bash
gcloud auth application-default login
```

4. Run the app:

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

## Build
Compile:

```bash
mvn -DskipTests compile
```

Package:

```bash
mvn package -DskipTests
```

## API docs
When the app is running:
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Media uploads
- Uploads are stored in Google Cloud Storage
- `Profile.photo` and `Post.image` use direct `https://storage.googleapis.com/...` URLs
- Uploaded images are resized and converted to WebP before upload
- The upload bucket must allow public object reads, otherwise browsers will not be able to render those images

## Notes
- Multipart upload limit is `10MB`
- Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`
- CORS currently allows any origin
