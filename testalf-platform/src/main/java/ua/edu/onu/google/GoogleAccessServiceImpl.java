package ua.edu.onu.google;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GoogleAccessServiceImpl implements GoogleAccessService {
    private static final Map<String, String> ROLES = new HashMap<>();

    static {
        ROLES.put("writer", "writer");
        ROLES.put("commenter", "commenter");
        ROLES.put("reader", "reader");
    }
    @Override
    public String grantAccessToUser(String userEmail, String fileId, String role, String accessToken) {
        if (userEmail == null || fileId == null || role == null || !ROLES.containsKey(role)) {
            throw new RuntimeException("Missing or invalid parameters: userEmail, fileId, role");
        }

        if (accessToken == null) {
            throw new RuntimeException("Failed to obtain access token");
        }

        try {
            return grantFullAccessToUser(accessToken, userEmail, fileId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------------------------------------

    private String grantFullAccessToUser(String accessToken, String userEmail, String fileId) throws Exception {
        String url = "https://www.googleapis.com/drive/v3/files/"
                + fileId
                + "/permissions?sendNotificationEmail=false";

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", "Bearer " + accessToken);
        post.setHeader("Content-Type", "application/json");

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "user");
        payload.addProperty("role", ROLES.get("writer"));
        payload.addProperty("emailAddress", userEmail);

        post.setEntity(new StringEntity(payload.toString(), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(post)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }

    private String listPermissions(String accessToken, String fileId) throws Exception {

        String url = "https://www.googleapis.com/drive/v3/files/"
                + fileId
                + "/permissions";

        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(get)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }

    private void revokeAccess(
            String accessToken,
            String fileId,
            String permissionId
    ) throws Exception {

        String url = "https://www.googleapis.com/drive/v3/files/"
                + fileId
                + "/permissions/"
                + permissionId;

        HttpDelete delete = new HttpDelete(url);
        delete.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            client.execute(delete);
        }
    }

    private String updatePermissionRole(
            String accessToken,
            String fileId,
            String permissionId,
            String newRole
    ) throws Exception {

        String url = "https://www.googleapis.com/drive/v3/files/"
                + fileId
                + "/permissions/"
                + permissionId;

        HttpPatch patch = new HttpPatch(url);
        patch.setHeader("Authorization", "Bearer " + accessToken);
        patch.setHeader("Content-Type", "application/json");

        JsonObject payload = new JsonObject();
        payload.addProperty("role", newRole);

        patch.setEntity(new StringEntity(payload.toString(), StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(patch)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }
}
