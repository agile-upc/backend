# Deployment Guide

## Recommended target
Deploy this backend to Google Cloud Run. It already fits a stateless container runtime, uses MySQL, uploads files to Google Cloud Storage, and can use Application Default Credentials for GCS without shipping a credentials file.

Recommended production stack:
- Cloud Run for the Spring Boot container
- Cloud SQL for MySQL
- Google Cloud Storage for uploaded images
- A dedicated Cloud Run service account with Storage access

## Required environment variables
Set these in Cloud Run:

```properties
MYSQL_HOST=<cloud-sql-host-or-private-ip>
MYSQL_PORT=3306
MYSQL_DATABASE=<database-name>
MYSQL_USER=<database-user>
MYSQL_PASSWORD=<database-password>
JWT_SECRET=<long-random-secret>
GCS_BUCKET_NAME=<bucket-name>
GCS_PROJECT_ID=<gcp-project-id>
GEMINI_API_KEY=<google-ai-api-key>
```

You do not need `GCS_CREDENTIALS_PATH` anymore. GCS auth now comes from Application Default Credentials, which Cloud Run provides through the attached service account.

## Production setup
1. Create or select a Google Cloud project.
2. Create a Cloud Storage bucket for uploads.
3. Create a Cloud SQL for MySQL instance and database.
4. Create a dedicated service account for the backend.
5. Grant that service account the minimum bucket permissions needed to create objects.
6. Build the application jar with `mvn package -DskipTests`.
7. Build the container from the existing `Dockerfile`.
8. Deploy the container to Cloud Run with the environment variables above.
9. Attach the dedicated service account to the Cloud Run service.
10. Configure the frontend to use the Cloud Run HTTPS URL as its base API URL.

## Service account and GCS access
The backend creates a `Storage` client from ADC. In Cloud Run, ADC resolves to the runtime service account automatically.

Minimum recommendation:
- Use one service account per backend service
- Grant access only to the upload bucket
- Prefer object-level write permissions instead of broad project-wide admin roles

Important: the backend returns direct `https://storage.googleapis.com/<bucket>/<object>` URLs. Those URLs are useful to browsers only if the bucket or objects are readable by clients. The current code does not generate signed URLs and does not change object ACLs during upload.

## Cloud SQL notes
The app expects standard MySQL connection settings:
- `MYSQL_HOST`
- `MYSQL_PORT`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`

Use Cloud SQL for MySQL in production. The deployment can connect through public IP with authorized networking or through a private setup, but the chosen `MYSQL_HOST` must match that network design.

## Local development
Use the same app locally with `.env`:

```properties
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=agrotech
MYSQL_USER=root
MYSQL_PASSWORD=secret
JWT_SECRET=change-me
GCS_BUCKET_NAME=your-bucket
GCS_PROJECT_ID=your-project
GEMINI_API_KEY=your-gemini-key
```

For local GCS access, authenticate once with:

```bash
gcloud auth application-default login
```

That creates local Application Default Credentials for the Java client libraries. No service-account key file is required for local development either.

## Build and deploy example
Build the jar:

```bash
mvn package -DskipTests
```

Build the image:

```bash
docker build -t agrotech-backend .
```

Deploy it to Cloud Run using your preferred workflow:
- manually from the Google Cloud console
- with `gcloud run deploy`
- from a CI pipeline that builds and pushes the image, then deploys a new revision

If you use `gcloud run deploy`, make sure you set:
- the service account
- all required environment variables
- the correct port `8080`
- Cloud SQL connectivity, if applicable

## Operational notes
- The app listens on port `8080`
- Multipart upload size is capped at `10MB`
- JWT signing depends entirely on `JWT_SECRET`; use a strong secret in production
- The backend is stateless, so horizontal scaling through Cloud Run is fine
- If you need private media instead of public bucket access, add signed URL support in a future change
