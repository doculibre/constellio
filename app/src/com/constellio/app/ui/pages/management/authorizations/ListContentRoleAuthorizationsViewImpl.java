package com.constellio.app.ui.pages.management.authorizations;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class ListContentRoleAuthorizationsViewImpl extends ListAuthorizationsViewImpl implements
																						ListContentRoleAuthorizationsView {

	public ListContentRoleAuthorizationsViewImpl() {
		presenter = new ListContentRoleAuthorizationsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListContentRoleAuthorizationsView.viewTitle", record.getTitle());
	}

	@Override
	protected Button buildAddButton() {
		return new AddContentAuthorizationButton();
	}

	@Override
	protected DisplayMode getDisplayMode() {
		return DisplayMode.PRINCIPALS;
	}

	public class AddContentAuthorizationButton extends AddAuthorizationButton {
		@PropertyId("users") protected ListAddRemoveRecordLookupField users;
		@PropertyId("groups") protected ListAddRemoveRecordLookupField groups;

		@Override
		protected Component buildWindowContent() {
			buildUsersAndGroupsField();
			buildAccessField();
			buildRolesField();
			buildDateFields();
			return new BaseForm<AuthorizationVO>(
					AuthorizationVO.forContent(record.getId()), this, users, groups, accessRoles, userRoles, startDate, endDate) {
				@Override
				protected void saveButtonClick(AuthorizationVO authorization)
						throws ValidationException {
					getWindow().close();
					presenter.authorizationCreationRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO authorization) {
					getWindow().close();
				}
			};
		}

		private void buildUsersAndGroupsField() {
			users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
			users.setCaption($("AuthorizationsView.users"));
			users.setId("users");

			groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
			groups.setCaption($("AuthorizationsView.groups"));
			groups.setId("groups");
		}
	}
}
