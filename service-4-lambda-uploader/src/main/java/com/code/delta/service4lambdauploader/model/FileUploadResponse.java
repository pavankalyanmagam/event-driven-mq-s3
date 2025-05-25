package com.code.delta.service4lambdauploader.model;

public class FileUploadResponse {
    private String message;
    private String s3ObjectKey;

    public FileUploadResponse() {
    }

    public FileUploadResponse(String message, String s3ObjectKey) {
        this.message = message;
        this.s3ObjectKey = s3ObjectKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getS3ObjectKey() {
        return s3ObjectKey;
    }

    public void setS3ObjectKey(String s3ObjectKey) {
        this.s3ObjectKey = s3ObjectKey;
    }
}
