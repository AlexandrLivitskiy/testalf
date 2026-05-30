package ua.edu.onu.google;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class GetGoogleTokenWebscript extends DeclarativeWebScript {

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
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            model.put("username", username);

            NodeRef person = personService.getPerson(username);

            if (person == null) {
                throw new RuntimeException("User node not found: " + username);
            }

            QName prop = QName.createQName("http://www.testalf.onu.edu.ua/model/google/1.0", "tokenValue");

            String token = (String) nodeService.getProperty(person, prop);
            if (token == null) token = "";

            model.put("success", true);
            model.put("token", token);

        } catch (Exception ex) {
            model.put("success", false);
            model.put("token", "");
            model.put("error", ex.getMessage());
        }

        return model;
    }
}
