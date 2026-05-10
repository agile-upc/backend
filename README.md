# Agrotech Backend

Spring Boot backend for the Agrotech platform. It provides authentication, profiles, advisors, farmers, notifications, posts, appointments, reviews, and AI-assisted advisor recommendations.

## Stack
- Java 21
- Spring Boot 3
- Spring Security with JWT
- Spring Data JPA
- MySQL
- Google Cloud Storage for uploaded media
- Gemini API for AI chat

## Main behavior
- Base API path: `/api/v1`
- Public routes: `/api/v1/authentication/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- All other routes require `Authorization: Bearer <token>`
- `POST /api/v1/authentication/sign-up` creates the user and initial profile in one multipart request
- Profile photos and post images are returned as direct public GCS URLs

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
GEMINI_API_KEY=your_gemini_api_key
GCS_BUCKET_NAME=your_gcs_bucket_name
GCS_PROJECT_ID=your_gcs_project_id
```

## Local development
1. Start a MySQL database.
2. Create the database configured in `MYSQL_DATABASE`.
3. Authenticate Google Cloud Application Default Credentials if you want uploads to work:

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
