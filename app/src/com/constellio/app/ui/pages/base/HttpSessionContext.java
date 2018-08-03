package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.entities.UserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.*;

import static com.constellio.app.ui.pages.base.VaadinSessionContext.*;

public class HttpSessionContext extends BaseSessionContext {

	private Locale currentLocale;

	private HttpServletRequest request;

	private HttpSession session;

	public HttpSessionContext(HttpServletRequest request) {
		this.request = request;
		this.session = request.getSession();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		return (T) session.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
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

	@SuppressWarnings("unchecked")
	@Override
	protected List<String> ensureSelectedRecordIds() {
		List<String> selectedRecordIds = (List<String>) getAttribute(SELECTED_RECORD_IDS_ATTRIBUTE);
		if (selectedRecordIds == null) {
			selectedRecordIds = new ArrayList<>();
			setAttribute(SELECTED_RECORD_IDS_ATTRIBUTE, selectedRecordIds);
		}
		return selectedRecordIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Long> ensureSelectedRecordSchemaTypeCodes() {
		Map<String, Long> selectedRecordSchemaTypeCodes = (Map<String, Long>) getAttribute(SELECTED_RECORD_SCHEMA_TYPE_CODES_ATTRIBUTE);
		if (selectedRecordSchemaTypeCodes == null) {
			selectedRecordSchemaTypeCodes = new HashMap<>();
			setAttribute(SELECTED_RECORD_SCHEMA_TYPE_CODES_ATTRIBUTE, selectedRecordSchemaTypeCodes);
		}
		return selectedRecordSchemaTypeCodes;
	}
}
