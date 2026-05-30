package ua.edu.onu.google;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GrantGoogleAccessToUser extends AbstractWebScript {

    private static final Map<String, String> ROLES = new HashMap<>();

    static {
        ROLES.put("writer", "writer");
        ROLES.put("commenter", "commenter");
        ROLES.put("reader", "reader");
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        JSONObject jsonBody = new JSONObject(req.getContent().getContent());
        String userEmail = jsonBody.optString("userEmail", null);
        String fileId = jsonBody.optString("fileId", null);
        String role = jsonBody.optString("role", ROLES.get("reader"));

        if (userEmail == null || fileId == null || role == null || !ROLES.containsKey(role)) {
            res.setStatus(400);
            res.getWriter().write("Missing or invalid parameters: userEmail, fileId, role");
            return;
        }

        String accessToken;
        try {
            accessToken = getAccessToken();
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("Failed to get access token");
            return;
        }
        if (accessToken.isEmpty()) {
            res.setStatus(500);
            res.getWriter().write("Failed to obtain access token");
            return;
        }

        res.setContentType("application/json");
        try {
            res.getWriter().write(grantFullAccessToUser(accessToken, userEmail, fileId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --------------------------------------------------

    private String getAccessToken() throws Exception {
        return "";
    }

    private String grantFullAccessToUser(String accessToken, String userEmail, String fileId) throws Exception {
        String url = "https://www.googleapis.com/drive/v3/files/"
                + fileId
                + "/permissions?sendNotificationEmail=true";

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", "Bearer " + accessToken);
        post.setHeader("Content-Type", "application/json");

        JsonObject payload = new JsonObject();
        payload.addProperty("type", "user");
        payload.addProperty("role", "writer");
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

