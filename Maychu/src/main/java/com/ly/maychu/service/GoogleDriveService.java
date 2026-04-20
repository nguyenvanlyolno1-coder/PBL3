package com.ly.maychu.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;

@Service
public class GoogleDriveService {

    @Value("${google.drive.credentials-path:classpath:credentials.json}")
    private String credentialsPath;

    @Value("${google.drive.folder-id}")
    private String driveFolderId;

    private Drive getDriveService() throws Exception {
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("ExamGuard")
                .build();
    }

    /**
     * Upload file zip lên Google Drive
     * @return fileId trên Drive
     */
    public String uploadZip(java.io.File zipFile) throws Exception {
        Drive drive = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(zipFile.getName());
        fileMetadata.setParents(Collections.singletonList(driveFolderId));

        com.google.api.client.http.FileContent mediaContent =
                new com.google.api.client.http.FileContent("application/zip", zipFile);

        File uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute();

        System.out.println("☁️ Đã upload lên Drive: " + uploadedFile.getWebViewLink());
        return uploadedFile.getId();
    }
}