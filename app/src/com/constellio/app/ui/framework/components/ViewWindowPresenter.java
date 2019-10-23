package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.ui.pages.extrabehavior.ProvideSecurityWithNoUrlParamSupport;
import com.constellio.app.ui.framework.exception.UserException.UserDoesNotHaveAccessException;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Component;

public class ViewWindowPresenter {
	private ViewWindow viewWindow;
	private BasePresenterUtils basePresenterUtils;

	public ViewWindowPresenter(ViewWindow viewWindow) {
		this.viewWindow = viewWindow;
		this.basePresenterUtils = new BasePresenterUtils(viewWindow.getConstellioFactories(), viewWindow.getSessionContext());
	}

	public void hasPageAccess(Component baseView) throws UserDoesNotHaveAccessException {
		if (baseView instanceof ProvideSecurityWithNoUrlParamSupport) {
			User currentUser = basePresenterUtils.getCurrentUser();
			boolean haveAccess = ((ProvideSecurityWithNoUrlParamSupport) baseView)
					.getSecurityWithNoUrlParamSupport().hasPageAccess(currentUser);
			if (!haveAccess) {
				viewWindow.showErrorMessage(i18n.$("ViewWindow.accessDenied"));
				throw new UserDoesNotHaveAccessException(currentUser.getUsername(), baseView.getClass().getSimpleName());
			}
		}
	}
}
