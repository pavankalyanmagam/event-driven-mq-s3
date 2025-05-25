package com.code.delta.service4lambdauploader.model;

public class FileUploadRequest {
    private String fileName;
    private String fileContent; // Base64 encoded file content

    public FileUploadRequest() {
    }

    public FileUploadRequest(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}
