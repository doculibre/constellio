package com.constellio.app.ui.pages.management.shares;

import com.constellio.app.ui.entities.AuthorizationVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.client.ui.ClickEventHandler;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ShareManagementViewImpl extends BaseViewImpl implements ShareManagementView {

		public static final String CREATE_ROLE = "create-role";
		public static final String SAVE = "save";
		public static final String REVERT = "revert";

		private final ShareManagementPresenter presenter;
		private RoleMatrix matrix;
		private VerticalLayout layout;
		private Button save;
		private Button revert;

		public ShareManagementViewImpl() {
			presenter = new ShareManagementPresenter(this);
		}

	@Override
	protected String getTitle() {
		return $("ShareManagementView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().adminModule();
			}
		};
	}

		@Override
		protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
			List<Button> buttons = super.buildActionMenuButtons(event);

//			Share create = new ShareButton();
//			create.addStyleName(CREATE_ROLE);
//			buttons.add(create);
//
//			save = new Button($("PermissionsManagementView.save"));
//			save.addClickListener(new ClickListener() {
//				@Override
//				public void buttonClick(ClickEvent event) {
//					presenter.saveRequested(matrix.getModifiedRoles());
//				}
//			});
//			save.addStyleName(SAVE);
//			save.setEnabled(false);
//			buttons.add(save);
//
//			revert = new Button($("PermissionsManagementView.revert"));
//			revert.addClickListener(new ClickListener() {
//				@Override
//				public void buttonClick(ClickEvent event) {
//					presenter.revertRequested();
//				}
//			});
//			revert.addStyleName(REVERT);
//			revert.setEnabled(false);
//			buttons.add(revert);
//
//			return buttons;
		return null;
		}

		@Override
		public void setSaveButton(boolean enabled) {

		}

		@Override
		public void addShare(AuthorizationVO authorization) {

		}

		@Override
		public void refreshView() {
			matrix = new RoleMatrix(presenter.getShares(null));
			buildAllGroups();
		}

		@Override
		protected Component buildMainComponent(ViewChangeEvent event) {
			layout = new VerticalLayout();
			layout.setSpacing(true);

			refreshView();

			return layout;
		}

		private void buildAllGroups() {
			layout.removeAllComponents();
			for (String group : presenter.getPermissionGroups()) {
				layout.addComponent(buildPermissionGroup(group));
			}
		}

		private Component buildPermissionGroup(String group) {
			Label caption = new Label($("perm.group." + group));
			caption.addStyleName(ValoTheme.LABEL_H2);

			Table table = new BaseTable(getClass().getName());
			for (String permission : presenter.getPermissionsInGroup(group)) {
				table.addItem(permission);
			}
			table.setPageLength(table.size());
			matrix.attachTo(table);

			VerticalLayout layout = new VerticalLayout(caption, table);
			layout.setSpacing(true);

			return layout;
		}

	public class RoleMatrix implements ColumnGenerator, CellStyleGenerator {
		public static final String PERMISSION_NAME_COLUMN = "permissionName";
		private final Map<Object, AuthorizationVO> authorizations;

		public RoleMatrix(List<AuthorizationVO> authorizations) {
			this.authorizations = new HashMap<>(authorizations.size());
			for (AuthorizationVO auth : authorizations) {
				this.authorizations.put(auth.getAuthId(), auth);
			}
		}

		public void attachTo(Table table) {
			table.addGeneratedColumn(PERMISSION_NAME_COLUMN, this);
			table.setColumnHeader(PERMISSION_NAME_COLUMN, "");
			table.setColumnWidth(PERMISSION_NAME_COLUMN, 280);
			for (AuthorizationVO role : authorizations.values()) {
				String code = role.getAuthId();
				table.addGeneratedColumn(code, this);
				table.setColumnWidth(code, 60);
				table.setColumnAlignment(code, Align.CENTER);
			}
			table.setCellStyleGenerator(this);
		}

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			if (propertyId == null || PERMISSION_NAME_COLUMN.equals(propertyId)) {
				return null;
			}
			return "";//authorizations.get(propertyId).isPermissionDirty((String) itemId) ?
				   //"permission-dirty" : "permission-clean";
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			String permission = (String) itemId;
			return columnId.equals(PERMISSION_NAME_COLUMN) ?
				   buildCaption(permission) :
				   buildCheckBox(authorizations.get(columnId), permission, source);
		}

		private Object buildCaption(String permission) {
			return new Label($("perm." + permission), ContentMode.HTML);
		}

		private Object buildCheckBox(final AuthorizationVO auth, final String permission, final Table table) {
			final CheckBox checkBox = new CheckBox();
			//checkBox.setValue(auth.hasPermission(permission));
			//checkBox.addStyleName(auth.getCode() + "-" + permission.replaceAll("\\.", "_"));
			checkBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					if (checkBox.getValue()) {
						//auth.addPermission(permission);
					} else {
						//auth.removePermission(permission);
					}
					//presenter.permissionsChanged(isDirty());
					table.refreshRowCache();
				}
			});
			return checkBox;
		}

		private boolean isDirty() {
			for (AuthorizationVO auth : authorizations.values()) {

			}
			return false;
		}
	}

	public class ShareButton extends WindowButton {
		public static final String SHARE_CODE = "share-code";
		public static final String SHARE_TITLE = "share-title";

		@PropertyId("code") private BaseTextField code;
		@PropertyId("title") private BaseTextField title;

		public ShareButton() {
			super($("PermissionsManagementView.createRole"), $("PermissionsManagementView.createRole"));
		}

		@Override
		protected Component buildWindowContent() {
			code = new BaseTextField($("PermissionsManagementView.code"));
			code.setRequired(true);
			code.setId(SHARE_CODE);

			title = new BaseTextField($("PermissionsManagementView.title"));
			title.setRequired(true);
			title.setId(SHARE_TITLE);

			return new BaseForm<AuthorizationVO>(null, this, code, title) {
				@Override
				protected void saveButtonClick(AuthorizationVO authorization)
						throws ValidationException {
					getWindow().close();
					//presenter.authCreationRequested(authorization);
				}

				@Override
				protected void cancelButtonClick(AuthorizationVO role) {
					getWindow().close();
				}
			};
		}
	}
}
