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
package com.constellio.app.ui.pages.collection;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

public class CollectionUserRolesViewImpl extends BaseViewImpl implements CollectionUserRolesView {
	public static final String USER_ROLES = User.DEFAULT_SCHEMA + "_" + User.ROLES;
	public static final String ADD_ROLE = "add-role";
	public static final String ROLE_SELECTOR = "role-selector";
	public static final String ROLES = "roles";

	private final CollectionUserRolesPresenter presenter;
	private RecordVO user;
	private ComboBox availableRoles;
	private Table roles;

	public CollectionUserRolesViewImpl() {
		presenter = new CollectionUserRolesPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forRequestParams(event.getParameters());
		user = presenter.getUser();
	}

	@Override
	protected String getTitle() {
		return $("CollectionUserRolesView.viewTitle", user.getTitle());
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		List<String> userRoles = user.get(USER_ROLES);

		final Button add = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.roleAdditionRequested((String) availableRoles.getValue());
			}
		};
		add.setEnabled(false);
		add.addStyleName(ADD_ROLE);

		availableRoles = new ComboBox();
		for (RoleVO roleVO : presenter.getRoles()) {
			if (!userRoles.contains(roleVO.getCode())) {
				availableRoles.addItem(roleVO.getCode());
				availableRoles.setItemCaption(roleVO.getCode(), roleVO.getTitle());
			}
		}
		availableRoles.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				add.setEnabled(availableRoles.getValue() != null);
			}
		});
		availableRoles.addStyleName(ROLE_SELECTOR);

		HorizontalLayout adder = new HorizontalLayout(availableRoles, add);

		roles = new Table();
		for (String roleCode : userRoles) {
			roles.addItem(roleCode);
		}
		roles.addStyleName(ROLES);
		roles.setWidth("100%");
		roles.setPageLength(roles.size());
		new RoleDisplay().attachTo(roles);

		VerticalLayout layout = new VerticalLayout(adder, roles);
		layout.setSpacing(true);

		return layout;
	}

	@Override
	public void roleAdded(String roleCode) {
		availableRoles.removeItem(roleCode);
		roles.addItem(roleCode);
		roles.setPageLength(roles.size());
	}

	@Override
	public void roleRemoved(String roleCode) {
		availableRoles.addItem(roleCode);
		availableRoles.setItemCaption(roleCode, presenter.getRoleTitle(roleCode));
		roles.removeItem(roleCode);
		roles.setPageLength(roles.size());
	}

	public class RoleDisplay implements ColumnGenerator {
		public static final String TITLE = "title";
		public static final String REMOVE = "remove";

		public void attachTo(Table table) {
			table.addGeneratedColumn(TITLE, this);
			table.setColumnHeader(TITLE, $("CollectionUserView.roleTitle"));
			table.setColumnExpandRatio(TITLE, 1);
			table.addGeneratedColumn(REMOVE, this);
			table.setColumnHeader(REMOVE, "");
			table.setColumnWidth(REMOVE, 50);
		}

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			if (columnId.equals(TITLE)) {
				return presenter.getRoleTitle((String) itemId);
			}
			if (columnId.equals(REMOVE)) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.roleRemovalRequested((String) itemId);
					}
				};
			}
			return null;
		}
	}
}
