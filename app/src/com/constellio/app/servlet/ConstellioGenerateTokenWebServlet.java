package com.constellio.app.servlet;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConstellioGenerateTokenWebServlet extends HttpServlet {

	public static final String TEXT_XML_CHARSET_UTF_8 = "text/xml;charset=UTF-8";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String DURATION = "duration";
	public static final String AS_USER = "asUser";

	public static final String BAD_DURATION = "Bad Duration. Example : 14d or 24h";
	public static final String PARAM_USERNAME_REQUIRED = "Parameter 'username' required";
	public static final String PARAM_PASSWORD_REQUIRED = "Parameter 'password' required";
	public static final String PARAM_DURATION_REQUIRED = "Parameter 'duration' required";
	public static final String BAD_USERNAME_PASSWORD = "Bad username/password";
	public static final String BAD_ASUSER = "Bad asUser value";
	public static final String REQUIRE_ADMIN_RIGHTS = "asUser requires system admin rights";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String username = req.getParameter(USERNAME);
		String password = req.getParameter(PASSWORD);
		String duration = req.getParameter(DURATION);
		String asUser = req.getParameter(AS_USER);

		if (StringUtils.isBlank(username)) {
			username = req.getHeader(USERNAME);
		}

		if (StringUtils.isBlank(password)) {
			password = req.getHeader(PASSWORD);
		}

		if (StringUtils.isBlank(duration)) {
			duration = req.getHeader(DURATION);
		}

		if (StringUtils.isBlank(asUser)) {
			asUser = req.getHeader(AS_USER);
		}

		if (StringUtils.isBlank(username)) {
			resp.getWriter().write(PARAM_USERNAME_REQUIRED);
			return;
		}

		if (StringUtils.isBlank(duration)) {
			resp.getWriter().write(PARAM_DURATION_REQUIRED);
			return;
		}

		if (StringUtils.isBlank(password)) {
			resp.getWriter().write(PARAM_PASSWORD_REQUIRED);
			return;
		}

		if (StringUtils.isBlank(asUser) || "null".equalsIgnoreCase(asUser)) {
			asUser = null;
		}

		int tokenDurationInHours = getTokenDurationInHours(duration);
		if (tokenDurationInHours <= 0) {
			resp.getWriter().write(BAD_DURATION);
			return;
		}

		UserServices userServices = ConstellioFactories.getInstance().getModelLayerFactory().newUserServices();
		AuthenticationService authService = ConstellioFactories.getInstance().getModelLayerFactory().newAuthenticationService();
		if (!authService.authenticate(username, password)) {
			resp.getWriter().write(BAD_USERNAME_PASSWORD);
			return;
		}

		String token;
		UserCredential userCredential;
		UserCredential serviceUserCredential;
		synchronized (username.intern()) {
			userCredential = userServices.getUserCredential(username);
			serviceUserCredential = userCredential;
			if (asUser != null) {
				if (userCredential.isSystemAdmin()) {
					try {
						userCredential = userServices.getUserCredential(asUser);
						if (userCredential == null) {
							throw new UserServicesRuntimeException_NoSuchUser(asUser);
						}
					} catch (UserServicesRuntimeException_NoSuchUser e) {
						resp.getWriter().write(BAD_ASUSER);
						return;
					}
				} else {
					resp.getWriter().write(REQUIRE_ADMIN_RIGHTS);
					return;
				}
			}

			if (userCredential.getServiceKey() == null) {

				userServices.execute(userServices.addUpdate(userCredential.getUsername()).setServiceKey("agent_" + userCredential.getUsername()));
				userCredential = userServices.getUserConfigs(userCredential.getUsername());

			}

			token = userServices.generateToken(userCredential.getUsername(), Duration.standardHours(tokenDurationInHours));
		}

		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<response><serviceKey>");
		sb.append(userCredential.getServiceKey());
		sb.append("</serviceKey>");
		sb.append("<token>");
		sb.append(token);
		sb.append("</token></response>");

		resp.setContentType(TEXT_XML_CHARSET_UTF_8);
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.getWriter().write(sb.toString());

	}

	private int getTokenDurationInHours(String duration) {
		try {
			int durationInHours;
			if (duration.endsWith("d") || duration.endsWith("j")) {
				int durationInDays = Integer.valueOf(duration.substring(0, duration.length() - 1));
				durationInHours = durationInDays * 24;

			} else if (duration.endsWith("h")) {
				durationInHours = Integer.valueOf(duration.substring(0, duration.length() - 1));

			} else {
				durationInHours = Integer.valueOf(duration);
			}
			return durationInHours;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}
}