package ua.edu.onu.google;

public interface GoogleAccessService {
    String grantAccessToUser(String userEmail, String fileId, String role, String accessToken);
}
