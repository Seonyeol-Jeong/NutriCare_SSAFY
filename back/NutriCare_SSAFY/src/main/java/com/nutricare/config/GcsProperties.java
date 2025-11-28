package com.nutricare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcs")
public class GcsProperties {
    private String bucketName;
    private String baseUrl;
    private String credentialsPath;
    private String prefixBoard;
    private String prefixPhoto;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    public String getPrefixBoard() {
        return prefixBoard;
    }

    public void setPrefixBoard(String prefixBoard) {
        this.prefixBoard = prefixBoard;
    }

    public String getPrefixPhoto() {
        return prefixPhoto;
    }

    public void setPrefixPhoto(String prefixPhoto) {
        this.prefixPhoto = prefixPhoto;
    }
}
