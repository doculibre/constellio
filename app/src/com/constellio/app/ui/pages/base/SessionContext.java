package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.externals.ExternalWebSignIn;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface SessionContext extends Serializable, ExternalWebSignIn {

	public static final String BYPASS_SSO = "bypassSso";

	<T extends Object> T getAttribute(String key);

	void setAttribute(String key, Object value);

	UserVO getCurrentUser();

	void setCurrentUser(UserVO user);

	String getCurrentCollection();

	void setCurrentCollection(String collection);

	Locale getCurrentLocale();

	void setCurrentLocale(Locale locale);

	String getCurrentUserIPAddress();

	boolean isForcedSignOut();

	void setForcedSignOut(boolean forcedSignOut);

	Principal getUserPrincipal();

	List<String> getSelectedRecordIds();

	public Map<String, Long> getSelectedRecordSchemaTypeCodes();

	void addSelectedRecordId(String recordId, String schemaTypeCode);

	void removeSelectedRecordId(String recordId, String schemaTypeCode);

	void clearSelectedRecordIds();

	public interface SelectedRecordIdsChangeListener extends Serializable {

		void recordIdAdded(String recordId);

		void recordIdRemoved(String recordId);

		void selectionCleared();

	}

	List<SelectedRecordIdsChangeListener> getSelectedRecordIdsChangeListeners();

	void addSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener);

	void removeSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener);
	
	void addVisited(String id);
	
	boolean isVisited(String id);
	
	void clearVisited(String id);
	
	void clearAllVisited();
}
