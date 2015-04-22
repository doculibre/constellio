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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.ListOptionGroup;
import com.constellio.app.ui.framework.components.fields.date.JodaDateField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public abstract class ListAuthorizationsViewImpl extends BaseViewImpl implements ListAuthorizationsView {
	public static final String INHERITED_AUTHORIZATIONS = "authorizations-inherited";
	public static final String AUTHORIZATIONS = "authorizations";

	public enum AuthorizationSource {
		INHERITED, OWN
	}

	public enum DisplayMode {
		CONTENT, PRINCIPALS
	}

	protected ListAuthorizationsPresenter presenter;
	protected RecordVO record;
	private Table authorizations;

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		record = presenter.forRequestParams(event.getParameters()).getRecordVO();
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);
		result.add(buildAddButton());
		return result;
	}

	protected abstract Button buildAddButton();

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked(record.getSchema().getCode());
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth("100%");

		buildInheritedAuthorizations(layout);
		buildOwnAuthorizations(layout);

		return layout;
	}

	@Override
	public void addAuthorization(AuthorizationVO authorizationVO) {
		authorizations.addItem(authorizationVO);
		updateAuthorizationsTable();
	}

	@Override
	public void removeAuthorization(AuthorizationVO authorization) {
		authorizations.removeItem(authorization);
		updateAuthorizationsTable();
	}

	protected abstract DisplayMode getDisplayMode();

	private void buildOwnAuthorizations(VerticalLayout layout) {
		Label label = new Label($("ListAuthorizationsView.ownAuthorizations"));
		label.addStyleName(ValoTheme.LABEL_H2);
		authorizations = buildAuthorizationTable(presenter.getOwnAuthorizations(), AuthorizationSource.OWN);
		layout.addComponents(label, authorizations);
	}

	private void buildInheritedAuthorizations(VerticalLayout layout) {
		List<AuthorizationVO> authorizationVOs = presenter.getInheritedAuthorizations();
		if (authorizationVOs.isEmpty()) {
			return;
		}

		Label label = new Label($("ListAuthorizationsView.inheritedAuthorizations"));
		label.addStyleName(ValoTheme.LABEL_H2);
		authorizations = buildAuthorizationTable(authorizationVOs, AuthorizationSource.INHERITED);
		layout.addComponents(label, authorizations);
	}

	private Table buildAuthorizationTable(List<AuthorizationVO> authorizationVOs, AuthorizationSource source) {
		Container container = buildAuthorizationContainer(authorizationVOs, source == AuthorizationSource.OWN);
		Table table = new Table($("ListAuthorizationsView.authorizations", container.size()), container);
		table.setPageLength(container.size());
		table.addStyleName(source == AuthorizationSource.OWN ? AUTHORIZATIONS : INHERITED_AUTHORIZATIONS);
		new Authorizations(source, getDisplayMode(), presenter.seeRolesField()).attachTo(table);
		return table;
	}

	private Container buildAuthorizationContainer(List<AuthorizationVO> authorizationVOs, boolean editable) {
		BeanItemContainer<AuthorizationVO> authorizations = new BeanItemContainer<>(AuthorizationVO.class, authorizationVOs);
		return editable ? addButtons(authorizations) : authorizations;
	}

	private Container addButtons(BeanItemContainer<AuthorizationVO> authorizations) {
		ButtonsContainer container = new ButtonsContainer<>(authorizations, Authorizations.BUTTONS);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(Object itemId) {
				AuthorizationVO authorization = (AuthorizationVO) itemId;
				EditAuthorizationButton button = new EditAuthorizationButton(authorization);
				button.setVisible(!authorization.isSynched());
				return button;
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				AuthorizationVO authorization = (AuthorizationVO) itemId;
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked((AuthorizationVO) itemId);
					}
				};
				deleteButton.setVisible(!authorization.isSynched());
				return deleteButton;
			}
		});
		return container;
	}

	private void updateAuthorizationsTable() {
		authorizations.setCaption($("ListAuthorizationsView.authorizations", authorizations.size()));
		authorizations.setPageLength(authorizations.size());
	}

	public abstract class AddAuthorizationButton extends WindowButton {
		@PropertyId("accessRoles") protected ListOptionGroup accessRoles;
		@PropertyId("userRoles") protected ListOptionGroup userRoles;
		@PropertyId("startDate") protected JodaDateField startDate;
		@PropertyId("endDate") protected JodaDateField endDate;

		public AddAuthorizationButton() {
			super($("ListAuthorizationsView.add"), $("ListAuthorizationsView.add"),
					WindowConfiguration.modalDialog("40%", "65%"));
		}

		protected void buildAccessField() {
			accessRoles = new ListOptionGroup($("AuthorizationsView.access"));
			for (String accessCode : presenter.getAllowedAccesses()) {
				accessRoles.addItem(accessCode);
				accessRoles.setItemCaption(accessCode, $("AuthorizationsView." + accessCode));
			}
			accessRoles.setRequired(true);
			accessRoles.setMultiSelect(true);
			accessRoles.setId("accessRoles");
		}

		protected void buildRolesField() {
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

		protected void buildDateFields() {
			startDate = new JodaDateField();
			startDate.setCaption($("AuthorizationsView.startDate"));
			startDate.setId("startDate");

			endDate = new JodaDateField();
			endDate.setCaption($("AuthorizationsView.endDate"));
			endDate.setId("endDate");
		}
	}

	public class EditAuthorizationButton extends WindowButton {
		private final AuthorizationVO authorization;

		@PropertyId("users") private ListAddRemoveRecordLookupField users;
		@PropertyId("groups") private ListAddRemoveRecordLookupField groups;

		public EditAuthorizationButton(AuthorizationVO authorization) {
			super(EditButton.ICON_RESOURCE, $("ListAuthorizationsView.edit"), true,
					WindowConfiguration.modalDialog("50%", "50%"));
			this.authorization = authorization;
		}

		@Override
		protected Component buildWindowContent() {
			buildUsersAndGroupsField();
			return new BaseForm<AuthorizationVO>(authorization, this, users, groups) {
				@Override
				protected void saveButtonClick(AuthorizationVO viewObject)
						throws ValidationException {
					getWindow().close();
					presenter.authorizationModificationRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO viewObject) {
					getWindow().close();
				}
			};
		}

		private void buildUsersAndGroupsField() {
			users = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
			users.setValue(authorization.getUsers());
			users.setVisible(!authorization.getUsers().isEmpty());
			users.setCaption($("AuthorizationsView.users"));
			users.setId("users");

			groups = new ListAddRemoveRecordLookupField(Group.SCHEMA_TYPE);
			groups.setValue(authorization.getGroups());
			groups.setVisible(!authorization.getGroups().isEmpty());
			groups.setCaption($("AuthorizationsView.groups"));
			groups.setId("groups");
		}
	}

	public static class Authorizations implements ColumnGenerator {
		public static final String PRINCIPALS = "principal";
		public static final String CONTENT = "content";
		public static final String ACCESS = "access";
		public static final String USER_ROLES = "userRoles";
		public static final String START_DATE = "startDate";
		public static final String END_DATE = "endDate";
		public static final String BUTTONS = "buttons";

		private final AuthorizationSource source;
		private final DisplayMode mode;
		private boolean seeRolesField;
		private final JodaDateToStringConverter converter;

		public Authorizations(AuthorizationSource source, DisplayMode mode, boolean seeRolesField) {
			this.source = source;
			this.mode = mode;
			this.seeRolesField = seeRolesField;
			converter = new JodaDateToStringConverter();
		}

		public void attachTo(Table table) {
			String primary;
			if (mode == DisplayMode.CONTENT) {
				table.addGeneratedColumn(CONTENT, this);
				table.setColumnHeader(CONTENT, $("AuthorizationsView.content"));
				primary = CONTENT;
			} else {
				table.addGeneratedColumn(PRINCIPALS, this);
				table.setColumnHeader(PRINCIPALS, $("AuthorizationsView.principals"));
				primary = PRINCIPALS;
			}
			table.setColumnExpandRatio(primary, 1);

			table.addGeneratedColumn(ACCESS, this);
			table.setColumnHeader(ACCESS, $("AuthorizationsView.access"));

			if (seeRolesField) {
				table.addGeneratedColumn(USER_ROLES, this);
				table.setColumnHeader(USER_ROLES, $("AuthorizationsView.userRoles"));
			}

			table.addGeneratedColumn(START_DATE, this);
			table.setColumnHeader(START_DATE, $("AuthorizationsView.startDate"));

			table.addGeneratedColumn(END_DATE, this);
			table.setColumnHeader(END_DATE, $("AuthorizationsView.endDate"));

			if (source == AuthorizationSource.OWN) {
				table.setColumnHeader(BUTTONS, "");
				table.setColumnWidth(BUTTONS, 80);
				if (seeRolesField) {
					table.setVisibleColumns(primary, ACCESS, USER_ROLES, START_DATE, END_DATE, BUTTONS);
				} else {
					table.setVisibleColumns(primary, ACCESS, START_DATE, END_DATE, BUTTONS);
				}
			} else {
				if (seeRolesField) {
					table.setVisibleColumns(primary, ACCESS, USER_ROLES, START_DATE, END_DATE);
				} else {
					table.setVisibleColumns(primary, ACCESS, START_DATE, END_DATE);
				}
			}

			table.setWidth("100%");
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			AuthorizationVO authorization = (AuthorizationVO) itemId;
			switch ((String) columnId) {
			case CONTENT:
				return new ReferenceDisplay(authorization.getRecord());
			case PRINCIPALS:
				return buildPrincipalColumn(authorization.getGroups(), authorization.getUsers());
			case ACCESS:
				return buildAccessColumn(authorization.getAccessRoles());
			case USER_ROLES:
				return buildUserRolesColumn(authorization.getUserRoles(), authorization.getUserRolesTitles());
			default:
				LocalDate date = (LocalDate) source.getItem(itemId).getItemProperty(columnId).getValue();
				return converter.convertToPresentation(
						date, String.class, ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			}
		}

		private Object buildPrincipalColumn(List<String> groups, List<String> users) {
			List<ReferenceDisplay> results = new ArrayList<>();
			for (String groupId : groups) {
				results.add(new ReferenceDisplay(groupId));
			}
			for (String userId : users) {
				results.add(new ReferenceDisplay(userId));
			}
			return new VerticalLayout(results.toArray(new Component[results.size()]));
		}

		private Component buildAccessColumn(List<String> roles) {
			List<String> accesses = new ArrayList<>(3);
			List<String> shortened = new ArrayList<>(3);
			if (roles.contains(Role.READ)) {
				accesses.add($("AuthorizationsView.READ"));
				shortened.add($("AuthorizationsView.short.READ"));
			}
			if (roles.contains(Role.WRITE)) {
				accesses.add($("AuthorizationsView.WRITE"));
				shortened.add($("AuthorizationsView.short.WRITE"));
			}
			if (roles.contains(Role.DELETE)) {
				accesses.add($("AuthorizationsView.DELETE"));
				shortened.add($("AuthorizationsView.short.DELETE"));
			}
			Label label = new Label(StringUtils.join(shortened, "/"));
			label.setDescription(StringUtils.join(accesses, ", "));
			return label;
		}

		private Component buildUserRolesColumn(List<String> roleCodes, List<String> roleTitles) {
			Label label = new Label(StringUtils.join(roleCodes, "/"));
			label.setDescription(StringUtils.join(roleTitles, ", "));
			return label;
		}
	}
}
