package com.nutricare.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Configuration
public class StorageConfig {

    private final GcsProperties gcsProperties;

    public StorageConfig(GcsProperties gcsProperties) {
        this.gcsProperties = gcsProperties;
    }

    @Bean
    public Storage storage() throws IOException {
        String credPath = gcsProperties.getCredentialsPath();
        if (credPath != null && !credPath.isBlank()) {
            Path path = Path.of(credPath);
            if (!Files.exists(path)) {
                throw new IllegalStateException("GCS credentials file not found: " + path.toAbsolutePath());
            }
            GoogleCredentials creds = GoogleCredentials.fromStream(new FileInputStream(path.toFile()));
            return StorageOptions.newBuilder().setCredentials(creds).build().getService();
        }
        // fallback to ADC (GOOGLE_APPLICATION_CREDENTIALS 등)
        return StorageOptions.getDefaultInstance().getService();
    }
}
