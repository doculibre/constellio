/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.base;

import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.vaadin.server.VaadinSession;

public class VaadinSessionContext implements SessionContext {

	private static final String CURRENT_USER_ATTRIBUTE = VaadinSessionContext.class.getName() + ".currentUser";
	private static final String CURRENT_COLLECTION_ATTRIBUTE = VaadinSessionContext.class.getName() + ".currentCollection";

	public VaadinSessionContext() {
	}

	@Override
	public UserVO getCurrentUser() {
		return (UserVO) VaadinSession.getCurrent().getAttribute(CURRENT_USER_ATTRIBUTE);
	}

	@Override
	public void setCurrentUser(UserVO user) {
		VaadinSession.getCurrent().setAttribute(CURRENT_USER_ATTRIBUTE, user);
	}

	@Override
	public String getCurrentCollection() {
		return (String) VaadinSession.getCurrent().getAttribute(CURRENT_COLLECTION_ATTRIBUTE);
	}

	@Override
	public void setCurrentCollection(String collection) {
		VaadinSession.getCurrent().setAttribute(CURRENT_COLLECTION_ATTRIBUTE, collection);
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

}
