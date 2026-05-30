var result = {};

try {
    var conn = remote.connect("google");
    if (!conn) {
        result.error = "Remote 'google' not found";
    } else {
        var response = conn.get("/.well-known/openid-configuration");
        result.status = response.status;
        result.responseTextSnippet = response.responseText ? response.responseText.substring(0, 500) : null;
    }
} catch (e) {
    result.error = e.message;
}

model.result = result;
