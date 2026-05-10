package com.agrotech.api.shared.infrastructure.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
public class GoogleStorageService {
    @Autowired
    private Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    // This service returns direct GCS object URLs. Deployments must make uploaded
    // objects publicly readable at the bucket level for browsers to render them.
    public String uploadFile(MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String uniqueFileName = "uploads/" + UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, uniqueFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, uniqueFileName);
    }
}
