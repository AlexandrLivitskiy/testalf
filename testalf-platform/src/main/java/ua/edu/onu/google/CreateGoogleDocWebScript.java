package ua.edu.onu.google;

import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.*;
import org.springframework.web.context.ContextLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateGoogleDocWebScript extends AbstractWebScript {

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("doc", "application/vnd.google-apps.document");
        MIME_TYPES.put("sheet", "application/vnd.google-apps.spreadsheet");
        MIME_TYPES.put("slide", "application/vnd.google-apps.presentation");
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        JSONObject jsonBody = new JSONObject(req.getContent().getContent());
        String name = jsonBody.optString("name", "New document");
        String type = jsonBody.optString("type", "doc"); // doc | sheet | slide

        if (name == null || type == null || !MIME_TYPES.containsKey(type)) {
            res.setStatus(400);
            res.getWriter().write("Missing or invalid parameters: name, type");
            return;
        }

        ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
        GoogleDocService googleDocService = ctx.getBean("googleDocService", GoogleDocService.class);
        res.setContentType("application/json");
        res.getWriter().write(googleDocService.createDoc(type, name, "accessToken")); // TODO: Fix accessToken
    }
}
