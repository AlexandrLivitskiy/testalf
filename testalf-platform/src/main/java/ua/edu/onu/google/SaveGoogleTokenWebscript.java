package ua.edu.onu.google;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class SaveGoogleTokenWebscript extends DeclarativeWebScript {

    private PersonService personService;
    private NodeService nodeService;

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();

        try {
            String body = req.getContent().getContent();
            JSONObject json = new JSONObject(body);
            String token = json.optString("token", null);

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Token not found in request body: " + body);
            }
            model.put("debug_body", body);

            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            model.put("debug_username", username);
            NodeRef person = personService.getPerson(username);

            if (person == null) {
                throw new RuntimeException("User node not found: " + username);
            }
            if (nodeService == null) {
                throw new RuntimeException("nodeService IS NULL");
            }
            model.put("debug_person", person);

            QName PROP_TOKEN = QName.createQName(
                    "http://www.testalf.onu.edu.ua/model/google/1.0",
                    "tokenValue"
            );
            QName ASPECT = QName.createQName(
                    "http://www.testalf.onu.edu.ua/model/google/1.0",
                    "tokenAspect"
            );

            if (!nodeService.hasAspect(person, ASPECT)) {
                nodeService.addAspect(person, ASPECT, null);
            }

            nodeService.setProperty(person, PROP_TOKEN, token);

            model.put("success", true);
            model.put("token", token);

        } catch (Exception ex) {
            model.put("success", false);
            model.put("token", "token ex");
            model.put("error", ex.toString());
            model.put("cause", ex.getCause());
        }

        return model; // Alfresco convert to JSON itself
    }
}
