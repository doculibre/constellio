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
package com.constellio.app.ui.pages.management.authorizations;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;

public class ShareContentViewImpl extends BaseViewImpl implements ShareContentView {
	private final ShareContentPresenter presenter;
	private RecordVO record;

	@PropertyId("users") private ListAddRemoveRecordLookupField users;
	@PropertyId("groups") private ListAddRemoveRecordLookupField groups;
	@PropertyId("accessRoles") private ListOptionGroup accessRoles;
	@PropertyId("userRoles") private ListOptionGroup userRoles;
	@PropertyId("startDate") private JodaDateField startDate;
	@PropertyId("endDate") private JodaDateField endDate;

	public ShareContentViewImpl() {
		presenter = new ShareContentPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		record = presenter.forRequestParams(event.getParameters()).getRecordVO();
	}

	@Override
	protected String getTitle() {
		return $("ShareContentView.viewTitle", record.getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				returnFromPage();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		buildUsersAndGroupsField();
		buildAccessField();
		buildRolesField();
		buildDateFields();
		return new BaseForm<AuthorizationVO>(
				AuthorizationVO.forContent(record.getId()), this, users, groups, accessRoles, userRoles, startDate, endDate) {
			@Override
			protected void saveButtonClick(AuthorizationVO authorization)
					throws ValidationException {
				presenter.authorizationCreationRequested(authorization);
			}

			@Override
			protected void cancelButtonClick(AuthorizationVO authorization) {
				returnFromPage();
			}
		};
	}

	@Override
	public void returnFromPage() {
		presenter.backButtonClicked(record.getSchema().getCode());
	}

	private void buildUsersAndGroupsField() {
		users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
		users.setCaption($("AuthorizationsView.users"));
		users.setId("users");

		groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
		groups.setCaption($("AuthorizationsView.groups"));
		groups.setId("groups");
	}

	private void buildAccessField() {
		accessRoles = new ListOptionGroup($("AuthorizationsView.access"));
		for (String accessCode : presenter.getAllowedAccesses()) {
			accessRoles.addItem(accessCode);
			accessRoles.setItemCaption(accessCode, $("AuthorizationsView." + accessCode));
		}
		accessRoles.setRequired(true);
		accessRoles.setMultiSelect(true);
		accessRoles.setId("accessRoles");
	}

	private void buildRolesField() {
		userRoles = new ListOptionGroup($("AuthorizationsView.userRoles"));
		for (String roleCode : presenter.getAllowedRoles()) {
			userRoles.addItem(roleCode);
			userRoles.setItemCaption(roleCode, roleCode);
		}
		userRoles.setEnabled(presenter.seeRolesField());
		userRoles.setVisible(presenter.seeRolesField());
		userRoles.setMultiSelect(true);
		userRoles.setId("userRoles");
	}

	private void buildDateFields() {
		startDate = new JodaDateField();
		startDate.setCaption($("AuthorizationsView.startDate"));
		startDate.setId("startDate");

		endDate = new JodaDateField();
		endDate.setCaption($("AuthorizationsView.endDate"));
		endDate.setId("endDate");
	}

}
