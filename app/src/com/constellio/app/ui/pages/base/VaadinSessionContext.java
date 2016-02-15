package com.constellio.app.ui.pages.base;

import java.security.Principal;
import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.vaadin.server.VaadinSession;

public class VaadinSessionContext implements SessionContext {

	public static final String CURRENT_USER_ATTRIBUTE = VaadinSessionContext.class.getName() + ".currentUser";
	public static final String CURRENT_COLLECTION_ATTRIBUTE = VaadinSessionContext.class.getName() + ".currentCollection";
	public static final String USER_PRINCIPAL_ATTRIBUTE = VaadinSessionContext.class.getName() + ".userPrincipal";
	public static final String FORCED_SIGN_OUT_ATTRIBUTE = VaadinSessionContext.class.getName() + ".forcedSignOut";

	public VaadinSessionContext() {
	}

	private Object getAttribute(String name) {
		VaadinSession vaadinSession = VaadinSession.getCurrent();
		return vaadinSession.getSession().getAttribute(name);
	}

	private void setAttribute(String name, Object object) {
		VaadinSession vaadinSession = VaadinSession.getCurrent();
		vaadinSession.getSession().setAttribute(name, object);
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
		return VaadinSession.getCurrent().getLocale();
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		VaadinSession.getCurrent().setLocale(locale);
	}

	@Override
	public String getCurrentUserIPAddress() {
		return ConstellioUI.getCurrent().getPage().getWebBrowser().getAddress();
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
