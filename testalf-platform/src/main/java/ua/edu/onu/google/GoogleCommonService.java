package ua.edu.onu.google;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class GoogleCommonService {

    private String tokenUrl;
    private String driveUrl;
    private static final String clientId = "429716899671-3frpeh79koph1r4dt57bo88ntna467pl.apps.googleusercontent.com";
    private static final String clientSecret = "";
    private static final String redirectUri = "https://testalf.onu.edu.ua/share/service/google/callback";
    private static final String refreshToken = "";

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getDriveUrl() {
        return driveUrl;
    }

    public void setDriveUrl(String driveUrl) {
        this.driveUrl = driveUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAlfrescoAccessToken() throws Exception {
        return getAccessToken("client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + refreshToken +
                "&grant_type=refresh_token");
    }

    public String getAccessTokenByAuthCode(String code) throws Exception {
        return getAccessToken("code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code");
    }

    private String getAccessToken(String body) throws Exception {
        HttpPost post = new HttpPost(tokenUrl);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(post)) {

            String json = EntityUtils.toString(resp.getEntity());
            JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
            return obj.get("access_token").getAsString();
        }
    }
}
