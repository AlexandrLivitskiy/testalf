/**
 * Google OAuth2 callback
 */
try {
    var code = args["code"];
    if (!code) {
        model.success = false;
        model.error = "Missing 'code' from Google.";
    } else {
        var repo = remote.connect("alfresco");
        var data = '{ "code": "' + code + '" }';
        var resp = repo.post("/google/login", data, "application/json");
        var error = jsonUtils.toObject(resp).error;
        if (error) {
            model.error = error;
        } else {
            model.user = jsonUtils.toObject(resp).username;
            model.password = jsonUtils.toObject(resp).password;
        }
    }
} catch (e) {
    model.success = false;
    model.error = "Exception: " + e.message;
}
//        var google = remote.connect("google");
//        var tokenResponse = google.post("/token",
//            "code=" + encodeURIComponent(code) +
//            "&client_id=" + encodeURIComponent(CLIENT_ID) +
//            "&client_secret=" + encodeURIComponent(CLIENT_SECRET) +
//            "&redirect_uri=" + encodeURIComponent(REDIRECT_URI) +
//            "&grant_type=authorization_code",
//            "application/x-www-form-urlencoded"
//        );
//
//        var token = jsonUtils.toObject(tokenResponse);
//        if (!token || !token.access_token) {
//            model.success = false;
//            model.error = "Failed to obtain access_token. Response: " + tokenResponse;
//        } else {
//            var googleApi = remote.connect("google-api");
//            var profileResponse = googleApi.get("/oauth2/v2/userinfo?access_token=" + token.access_token);
//            var profile = jsonUtils.toObject(profileResponse);
//
//            if (!profile || !profile.email) {
//                model.success = false;
//                model.error = "Failed to parse user profile. Response: " + profileResponse;
//            } else {
//                model.email = profile.email;
//                var username = model.email.replace("@", "_");
//                model.user = username;

//                var loginPayloadGoogle = jsonUtils.toJSONString({"username": "googleUser", "password": CLIENT_SECRET});
//                var loginRespGoogle = repo.post("/api/login", loginPayloadGoogle, "application/json");
//                var googleTicket = jsonUtils.toObject(loginRespGoogle).data.ticket;
//                var personResponse = repo.get("/api/people/" + encodeURIComponent(username) + "?alf_ticket=" + encodeURIComponent(googleTicket));
//                var userSearchData = jsonUtils.toObject(personResponse);
//                if (!(userSearchData && userSearchData.userName)) {
//                    var newUser = {
//                        userName: username,
//                        firstName: profile.given_name || "Google",
//                        lastName: profile.family_name || "User",
//                        email: model.email,
//                        password: CLIENT_SECRET
//                    };
//
//                    var createResp = repo.post("/api/people?alf_ticket=" + encodeURIComponent(googleTicket),
//                        jsonUtils.toJSONString(newUser),
//                        "application/json"
//                    );
//
//                    if (createResp.status != 200 && createResp.status != 201) {
//                        model.success = false;
//                        model.error = "Failed to create user: " + createResp;
//                        status.code = 500;
//                        status.message = model.error;
//                        status.redirect = true;
//                        throw model.error;
//                    }
//                }
//
//                var payload = '{ "username": "' + username + '", "password": "' + CLIENT_SECRET + '" }';
//                var loginResp = repo.post("/api/login", payload, "application/json");
//                var loginData = jsonUtils.toObject(loginResp);
//                if (loginData && loginData.data && loginData.data.ticket) {
////                    var tok = '{ "token": "' + token.access_token + '" }';
//                    var tok = '{ "token": "' + token.refresh_token + '" }';
//                    repo.post(
//                        "/google/saveToken?alf_ticket=" + encodeURIComponent(loginData.data.ticket),
//                        tok,
//                        "application/json"
//                    );
//                } else {
//                    model.success = false;
//                    model.error = "Login failed. Response: " + loginResp;
//                }
