/**
 * Google OAuth2 callback
 */
try {
                var repo = remote.connect("alfresco");
                var data = '{ "code": "' + "4/0A-of_3_9V_M08w" + '" }';
                var resp = repo.post("/google/login", data, "application/json");
                model.user = jsonUtils.toObject(resp).username;
                model.password = jsonUtils.toObject(resp).password;
                model.error = resp;
} catch (e) {
    model.error = "Exception: " + e.message;
}