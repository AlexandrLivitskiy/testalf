package ua.edu.onu.share.web;

import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GoogleSSOController extends AbstractWebScript {

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        HttpServletRequest request = ((WebScriptServletRequest) req).getHttpServletRequest();
        HttpServletResponse response = ((WebScriptServletResponse) res).getHttpServletResponse();

        String token = request.getParameter("token");

        if (token == null || token.isEmpty()) {
            response.sendError(401);
            return;
        }

        // TODO: request to Repo check token
        String username = validateGoogleToken(token);

        if (username == null) {
            response.sendError(401);
            return;
        }

//        UserFactory userFactory = (UserFactory) applicationContext.getBean("user.factory");
//        userFactory.authenticate(request, userId, null);

        AuthenticationUtil.login(
                request,
                response,
                username,
                true,
                true);

        response.sendRedirect(
                request.getContextPath() + "/page");

    }

    private String validateGoogleToken(String token) {

        return null;
    }
}
