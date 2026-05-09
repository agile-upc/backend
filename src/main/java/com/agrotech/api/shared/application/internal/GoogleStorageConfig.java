package com.agrotech.api.shared.application.internal;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GoogleStorageConfig {
    @Value("${gcs.credentials.file-path}")
    private Resource credentialsFile;

    @Value("${gcs.project.id}")
    private String projectId;

    @Bean
    public Storage storage() throws IOException {
        InputStream credentialsStream = credentialsFile.getInputStream();

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
