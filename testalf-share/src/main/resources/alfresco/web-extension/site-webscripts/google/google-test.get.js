var repo = remote.connect("alfresco");
var CLIENT_ID     = "";
var CLIENT_SECRET = "";

// 1. get token
var tokenResp = repo.get("/google/getToken?format=json");
var tokenObj = JSON.parse(tokenResp);
model.token = tokenObj.token;
var refreshToken = tokenObj.token;
var google = remote.connect("google");
var tokenResponse = google.post(
    "/token",
    "refresh_token=" + encodeURIComponent(refreshToken) +
    "&client_id=" + encodeURIComponent(CLIENT_ID) +
    "&client_secret=" + encodeURIComponent(CLIENT_SECRET) +
    "&grant_type=refresh_token",
    "application/x-www-form-urlencoded"
);
var token = jsonUtils.toObject(tokenResponse);
if (!token || !token.access_token) {
    model.error = "Failed to refresh access_token. Response: " + tokenResponse;
} else {
    model.error = "NO ERROR";
    model.accessToken = token.access_token;
}
var accessToken = model.accessToken;

// 2. get user info
var googleApi = remote.connect("google-api");
var userInfoResp = googleApi.get("/oauth2/v2/userinfo?access_token=" + accessToken);
model.userinfo = userInfoResp;

// 3. get files
var filesResp = googleApi.get(
                    "/drive/v3/files"
                    + "?pageSize=20"
                    + "&fields=files(id,name,mimeType,modifiedTime,size)"
                    + "&q=trashed=false"
                    + "&access_token=" + accessToken
                );
model.files = filesResp;
