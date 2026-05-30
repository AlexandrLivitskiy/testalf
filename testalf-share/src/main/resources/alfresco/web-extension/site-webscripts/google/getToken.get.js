var tokenNode = null;
var token = "";
model.resp = "Def resp...";
model.customData = "headers...";
model.customData2 = "customData2...";

try {
    var repo = remote.connect("alfresco");
    var resp = repo.get("/google/getToken?format=json");
    model.resp = resp
    var obj = JSON.parse(resp);

    if (obj && obj.token) {
        token = obj.token;
    } else {
        token = "(empty)";
    }

var result = "";
for (var k in headers) {
   result += "[" + k + "] = " + headers[k] + "\n";
}
model.customData = result;
model.customData2 =
    "user=" + user.name +
    ", remote=" + headers["remote_user"];
} catch(e) {
    token = "Error: " + e.message;
}

model.token = token;
