package ua.edu.onu.google;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.webscripts.*;
import org.springframework.web.context.ContextLoader;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;

public class LoginByGoogle extends DeclarativeWebScript {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private MutableAuthenticationService authenticationService;
    public void setAuthenticationService(
            MutableAuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    private TransactionService transactionService;
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    private PersonService personService;
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>();
        JSONParser parser = new JSONParser();
        GoogleCommonService googleCommonService = (GoogleCommonService) ContextLoader
                    .getCurrentWebApplicationContext()
                    .getBean("googleCommonService");

        try {
            String body = req.getContent().getContent();
            JSONObject bodyJson = (JSONObject) parser.parse(body);
            String code = (String) bodyJson.get("code");

            // ----------------------------------------
            // 1. Exchange code → token (HTTP raw)
            // ----------------------------------------
            String accessToken = googleCommonService.getAccessTokenByAuthCode(code);
            // ----------------------------------------
            // 2. Get user profile
            // ----------------------------------------
            String profileResponse = getGoogleUserInfo(accessToken);
            JSONObject profile = (JSONObject) parser.parse(profileResponse);
            String email = (String) profile.get("email");
            String firstName = (String) profile.get("given_name");
            String lastName = (String) profile.get("family_name");
            String username = email.replace("@", "_");
            String password = generatePassword(32);
            model.put("username", username);
            model.put("password", password);

            // ----------------------------------------
            // 3. Check user in Alfresco
            // ----------------------------------------
            // (call internal repo API)
            boolean exists = userExists(username);
            model.put("existedUser", Boolean.toString(exists));
            transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                if (!exists) {
                    createUser(username, password, firstName, lastName, email);
                } else {
                    authenticationService.setAuthentication(username, password.toCharArray());
                }
                return null;
            }, false, true);
        } catch (Exception e) {
            model.put("error", e.getMessage());
        }

        return model;
    }

    // ----------------------------
    // HTTP helpers
    // ----------------------------

    private String getGoogleUserInfo(String accessToken) throws Exception {
        HttpGet get = new HttpGet("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken);
        get.setHeader("Accept", "application/json");
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(get)) {
            return EntityUtils.toString(resp.getEntity());
        }
    }

    // ----------------------------
    // Alfresco user API
    // ----------------------------
    private boolean userExists(String username) {
        return AuthenticationUtil.runAsSystem(() -> personService.personExists(username));
    }

    private void createUser(String username, String password, String firstName, String lastName, String email) {
        AuthenticationUtil.runAsSystem(() -> {
            authenticationService.createAuthentication(
                    username,
                    password.toCharArray());

            Map<QName, Serializable> props = new HashMap<>();

            props.put(ContentModel.PROP_USERNAME, username);
            props.put(ContentModel.PROP_FIRSTNAME, firstName);
            props.put(ContentModel.PROP_LASTNAME, lastName);
            props.put(ContentModel.PROP_EMAIL, email);

            personService.createPerson(props);
            return null;
        });
    }

    private String generatePassword(int length) {

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}
