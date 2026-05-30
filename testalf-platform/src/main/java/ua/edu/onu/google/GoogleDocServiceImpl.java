package ua.edu.onu.google;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GoogleDocServiceImpl implements GoogleDocService {
    private static Log log = LogFactory.getLog(GoogleDocServiceImpl.class);

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("doc", "application/vnd.google-apps.document");
        MIME_TYPES.put("sheet", "application/vnd.google-apps.spreadsheet");
        MIME_TYPES.put("slide", "application/vnd.google-apps.presentation");
    }

    private static final List<String> templateIds = new ArrayList<>(Arrays.asList(
            "14r4cPLaEcqoktRD2VxEdiXLhZapEPXJS886mNTOTd1w",
            "1Kd7dl0LqYrqxcBM5aMREJ-iJ8v5cHTLa-bfvBsNWK0w",
            "1RVKR8NBCDgE4-w1G8JIOTXa5yOlJ1Z4BwH-PV1aOlzo"
    ));

    @Override
    public String createDoc(String type, String name, String accessToken) {
        log.info("Creating Google Doc type=" + type + ", name=" + name);
        boolean isTemplate = templateIds.contains(type);

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        if (!isTemplate) {
            body.addProperty("mimeType", MIME_TYPES.get(type));
        }
        body.add("parents", new Gson().toJsonTree(
                Collections.singletonList("1K19X7kpneUEX6_8WCsqmDcB0pFs3SZ59")
        ));

        String driveURL = getDriveURL(isTemplate, type);

        HttpPost post = new HttpPost(driveURL);
        post.setHeader("Authorization", "Bearer " + accessToken);
        post.setHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(body.toString(), "UTF-8"));

        String json = "";
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(post)) {
            json = EntityUtils.toString(resp.getEntity());
        } catch (IOException e) {
            throw new RuntimeException("Google Doc creation failed " + accessToken);
        }
        return json;
    }

    @Override
    public String uploadFileToDrive(InputStream inputStream, String fileName, String mimeType, String accessToken) {
        HttpPost post = new HttpPost("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart" +
                "&fields=id,name,kind,mimeType,webViewLink");
        post.setHeader("Authorization", "Bearer " + accessToken);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//        builder.setMimeSubtype("related");

        // metadata
        String metadata = "{ \"name\": \"" + fileName + "\", \"parents\": [\"1K19X7kpneUEX6_8WCsqmDcB0pFs3SZ59\"] }";

        builder.addTextBody("metadata", metadata, ContentType.APPLICATION_JSON);

        // file content (!)
        builder.addBinaryBody(
                "file",
                inputStream,
                ContentType.create(mimeType),
                fileName
        );

        post.setEntity(builder.build());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {

            return EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }
    // --------------------------------------------------

    private String getDriveURL(boolean isTemplate, String type) {
        return "https://www.googleapis.com/drive/v3/files" + (isTemplate ? "/" + type + "/copy" : "");
    }
}

