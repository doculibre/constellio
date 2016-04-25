package com.constellio.app.ui.pages.base;

import static com.constellio.app.ui.pages.base.VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE;
import static com.constellio.app.ui.pages.base.VaadinSessionContext.CURRENT_USER_ATTRIBUTE;
import static com.constellio.app.ui.pages.base.VaadinSessionContext.FORCED_SIGN_OUT_ATTRIBUTE;
import static com.constellio.app.ui.pages.base.VaadinSessionContext.USER_PRINCIPAL_ATTRIBUTE;

import java.security.Principal;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.constellio.app.ui.entities.UserVO;

public class HttpSessionContext implements SessionContext {
	
	private Locale currentLocale;
	
	private HttpServletRequest request;
	
	private HttpSession session;
	
	public HttpSessionContext(HttpServletRequest request) {
		this.request = request;
		this.session = request.getSession();
	}

	private Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	private void setAttribute(String name, Object object) {
		session.setAttribute(name, object);
	}

	@Override
	public UserVO getCurrentUser() {
		return (UserVO) getAttribute(CURRENT_USER_ATTRIBUTE);
	}

	@Override
	public void setCurrentUser(UserVO user) {
		setAttribute(CURRENT_USER_ATTRIBUTE, user);
	}

	@Override
	public String getCurrentCollection() {
		return (String) getAttribute(CURRENT_COLLECTION_ATTRIBUTE);
	}

	@Override
	public void setCurrentCollection(String collection) {
		setAttribute(CURRENT_COLLECTION_ATTRIBUTE, collection);
	}

	@Override
	public Locale getCurrentLocale() {
		return currentLocale != null ? currentLocale : request.getLocale();
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		this.currentLocale = locale;
	}

	@Override
	public String getCurrentUserIPAddress() {
		return request.getRemoteAddr();
	}

	@Override
	public Principal getUserPrincipal() {
		return (Principal) getAttribute(USER_PRINCIPAL_ATTRIBUTE);
	}

	@Override
	public boolean isForcedSignOut() {
		return Boolean.TRUE.equals(getAttribute(FORCED_SIGN_OUT_ATTRIBUTE));
	}

	@Override
	public void setForcedSignOut(boolean forcedSignOut) {
		setAttribute(FORCED_SIGN_OUT_ATTRIBUTE, forcedSignOut);
	}

}
