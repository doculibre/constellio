package com.constellio.app.ui.pages.management.authorizations;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.viewGroups.AdminViewGroup;
import com.constellio.model.entities.records.Record;

public interface ListAuthorizationsView extends BaseView, AdminViewGroup {
	void removeAuthorization(AuthorizationVO authorization);

	void addAuthorization(AuthorizationVO authorizationVO);

	void refresh();

	Record getAutorizationTarget();

	void setViewReadOnly(boolean isViewReadOnly);

	boolean isViewReadOnly();
}
