package com.agrotech.api.shared.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

@Service
public class GoogleStorageService {
    private static final int MAX_IMAGE_DIMENSION = 1600;
    private static final float WEBP_QUALITY = 0.8f;

    @Autowired
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    @Value("${gcs.project.id}")
    private String projectId;

    // This service returns direct GCS object URLs. Deployments must make uploaded
    // objects publicly readable at the bucket level for browsers to render them.
    public String uploadFile(MultipartFile file) throws IOException {
        ensureBucketExists();
        ProcessedUpload processedUpload = processUpload(file);
        String extension = processedUpload.extension().isBlank() ? "" : "." + processedUpload.extension();
        String uniqueFileName = "uploads/" + UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, uniqueFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(processedUpload.contentType())
                .build();

        storage.create(blobInfo, processedUpload.bytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, uniqueFileName);
    }

    private void ensureBucketExists() {
        try {
            if (storage.get(bucketName) == null) {
                throw missingBucketException();
            }
        } catch (StorageException exception) {
            if (exception.getCode() == 404) {
                throw missingBucketException();
            }
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to access upload bucket '" + bucketName + "' in project '" + projectId + "'. Check GCS credentials and bucket permissions.",
                    exception
            );
        }
    }

    private ResponseStatusException missingBucketException() {
        return new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Upload bucket '" + bucketName + "' does not exist in project '" + projectId + "'. Create it in Google Cloud Storage or fix GCS_BUCKET_NAME."
        );
    }

    private ProcessedUpload processUpload(MultipartFile file) throws IOException {
        String contentType = Objects.requireNonNullElse(file.getContentType(), "application/octet-stream");

        if (!contentType.startsWith("image/")) {
            return new ProcessedUpload(file.getBytes(), contentType, "");
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                return new ProcessedUpload(file.getBytes(), contentType, "");
            }

            BufferedImage resizedImage = resizeIfNeeded(originalImage);
            byte[] bytes = writeWebp(resizedImage);
            return new ProcessedUpload(bytes, "image/webp", "webp");
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int maxDimension = Math.max(width, height);

        if (maxDimension <= MAX_IMAGE_DIMENSION) {
            return source;
        }

        double scale = (double) MAX_IMAGE_DIMENSION / maxDimension;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        int imageType = source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    private byte[] writeWebp(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IOException("No WebP writer available");
        }

        ImageWriter writer = writers.next();
        try (MemoryCacheImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] compressionTypes = params.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    params.setCompressionType(selectCompressionType(compressionTypes));
                }
                params.setCompressionQuality(WEBP_QUALITY);
            }
            writer.write(null, new IIOImage(image, null, null), params);
        } finally {
            writer.dispose();
        }

        return outputStream.toByteArray();
    }

    private String selectCompressionType(String[] compressionTypes) {
        for (String compressionType : compressionTypes) {
            if ("Lossy".equalsIgnoreCase(compressionType)) {
                return compressionType;
            }
        }
        return compressionTypes[0];
    }

    private record ProcessedUpload(byte[] bytes, String contentType, String extension) {
    }
}
