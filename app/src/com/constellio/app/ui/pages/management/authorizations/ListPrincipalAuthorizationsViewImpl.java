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

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class ListPrincipalAuthorizationsViewImpl extends ListAuthorizationsViewImpl implements ListPrincipalAuthorizationsView {

	public ListPrincipalAuthorizationsViewImpl() {
		presenter = new ListPrincipalAuthorizationsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListPrincipalAuthorizationsView.viewTitle", record.getTitle());
	}

	@Override
	protected Button buildAddButton() {
		return new AddPrincipalAuthorizationButton();
	}

	@Override
	protected DisplayMode getDisplayMode() {
		return DisplayMode.CONTENT;
	}

	public class AddPrincipalAuthorizationButton extends AddAuthorizationButton {
		@PropertyId("record") protected LookupRecordField content;

		@Override
		protected Component buildWindowContent() {
			buildRecordField();
			buildAccessField();
			buildRolesField();
			buildDateFields();
			return new BaseForm<AuthorizationVO>(
					AuthorizationVO.forPrincipal(record.getId()), this, content, accessRoles, userRoles, startDate, endDate) {
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

		protected void buildRecordField() {
			content = new LookupRecordField(AdministrativeUnit.SCHEMA_TYPE);
			content.setCaption($("AuthorizationsView.content"));
			content.setRequired(true);
			content.setId("content");
		}
	}
}
