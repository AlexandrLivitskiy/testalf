package ua.edu.onu.google;

import java.io.InputStream;

public interface GoogleDocService {
    String createDoc(String type, String name, String accessToken);
    String uploadFileToDrive(InputStream inputStream, String fileName, String mimeType, String accessToken);
}
