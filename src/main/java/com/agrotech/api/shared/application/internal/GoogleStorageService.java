package com.agrotech.api.shared.application.internal;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
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

    public String uploadFile(MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String uniqueFileName = "uploads/" + UUID.randomUUID().toString() + extension;

        BlobId blobId = BlobId.of(bucketName, uniqueFileName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, uniqueFileName);
    }
}
