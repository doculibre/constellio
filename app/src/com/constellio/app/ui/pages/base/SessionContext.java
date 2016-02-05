package com.constellio.app.ui.pages.base;

import java.io.Serializable;
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
}
