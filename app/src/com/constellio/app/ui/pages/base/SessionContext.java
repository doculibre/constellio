package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.entities.UserVO;

public interface SessionContext extends Serializable {

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
	
	void addSelectedRecordId(String recordId);
	
	void removeSelectedRecordId(String recordId);
	
	void clearSelectedRecordIds();
	
	public interface SelectedRecordIdsChangeListener extends Serializable {
		
		void recordIdAdded(String recordId);
		
		void recordIdRemoved(String recordId);
		
		void selectionCleared();
		
	}
	
	List<SelectedRecordIdsChangeListener> getSelectedRecordIdsChangeListeners();
	
	void addSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener);
	
	void removeSelectedRecordIdsChangeListener(SelectedRecordIdsChangeListener listener);
	
}
