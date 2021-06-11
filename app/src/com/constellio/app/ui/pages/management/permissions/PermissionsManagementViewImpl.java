package com.constellio.app.ui.pages.management.permissions;

import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionConverter;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.constellio.app.ui.i18n.i18n.$;

public class PermissionsManagementViewImpl extends BaseViewImpl implements PermissionsManagementView {
	public static final String CREATE_ROLE = "create-role";
	public static final String SAVE = "save";
	public static final String REVERT = "revert";

	private final PermissionsManagementPresenter presenter;
	private RoleMatrix matrix;
	private VerticalLayout layout;
	private CreateRoleButton create;
	private Button save;
	private Button revert;

	public PermissionsManagementViewImpl() {

		presenter = new PermissionsManagementPresenter(this);
		create = new CreateRoleButton();
		create.setId("crateId");
		create.addStyleName(CREATE_ROLE);


		save = new Button($("PermissionsManagementView.save"));
		save.setId("saveId");
		save.addClickListener((ClickListener) event -> presenter.saveRequested(matrix.getModifiedRoles()));
		save.addStyleName(SAVE);
		save.setEnabled(false);


		revert = new Button($("PermissionsManagementView.revert"));
		revert.setId("revertId");
		revert.addClickListener((ClickListener) event -> presenter.revertRequested());
		revert.addStyleName(REVERT);
		revert.setEnabled(false);
	}

	@Override
	protected String getTitle() {
		return $("PermissionsManagementView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return event -> navigateTo().adminModule();
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<List<MenuItemAction>> getUseTheseActionsInQuickActionInsteadSupplier() {
				return () -> Arrays.asList(
						MenuItemActionConverter.toMenuItemAction(create),
						MenuItemActionConverter.toMenuItemAction(save),
						MenuItemActionConverter.toMenuItemAction(revert));
			}
		};
	}

	@Override
	public void setSaveAndRevertButtonStatus(boolean enabled) {
		save.setEnabled(enabled);
		revert.setEnabled(enabled);

		updateMenuAction(save.getId(), menuItemActionBuilder ->
				menuItemActionBuilder.state(new MenuItemActionState(save.isEnabled() ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.DISABLED))
						.build());

		updateMenuAction(revert.getId(), menuItemActionBuilder ->
				menuItemActionBuilder.state(new MenuItemActionState(revert.isEnabled() ? MenuItemActionStateStatus.VISIBLE : MenuItemActionStateStatus.DISABLED))
						.build());
	}

	@Override
	public void addRole(RoleVO role) {
		matrix.addRole(role);
		buildAllGroups();
	}

	@Override
	public void refreshView() {
		matrix = new RoleMatrix(presenter.getRoles());
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
		private final Map<Object, RoleVO> roles;

		public RoleMatrix(List<RoleVO> roles) {
			this.roles = new HashMap<>(roles.size());
			for (RoleVO role : roles) {
				this.roles.put(role.getCode(), role);
			}
		}

		public void attachTo(Table table) {
			table.addGeneratedColumn(PERMISSION_NAME_COLUMN, this);
			table.setColumnHeader(PERMISSION_NAME_COLUMN, "");
			table.setColumnWidth(PERMISSION_NAME_COLUMN, 280);
			for (RoleVO role : roles.values()) {
				String code = role.getCode();
				table.addGeneratedColumn(code, this);
				table.setColumnWidth(code, 60);
				table.setColumnAlignment(code, Align.CENTER);
			}
			table.setCellStyleGenerator(this);
		}

		public void addRole(RoleVO role) {
			roles.put(role.getCode(), role);
		}

		public List<RoleVO> getModifiedRoles() {
			ArrayList<RoleVO> result = new ArrayList<>();
			for (RoleVO role : roles.values()) {
				if (role.isDirty()) {
					result.add(role);
				}
			}
			return result;
		}

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			if (propertyId == null || PERMISSION_NAME_COLUMN.equals(propertyId)) {
				return null;
			}
			return roles.get(propertyId).isPermissionDirty((String) itemId) ?
				   "permission-dirty" : "permission-clean";
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			String permission = (String) itemId;
			return columnId.equals(PERMISSION_NAME_COLUMN) ?
				   buildCaption(permission) :
				   buildCheckBox(roles.get(columnId), permission, source);
		}

		private Object buildCaption(String permission) {
			return new Label($("perm." + permission), ContentMode.HTML);
		}

		private Object buildCheckBox(final RoleVO role, final String permission, final Table table) {
			final CheckBox checkBox = new CheckBox();
			checkBox.setValue(role.hasPermission(permission));
			checkBox.addStyleName(role.getCode() + "-" + permission.replaceAll("\\.", "_"));
			checkBox.addValueChangeListener((ValueChangeListener) event -> {
				if (checkBox.getValue()) {
					role.addPermission(permission);
				} else {
					role.removePermission(permission);
				}
				presenter.permissionsChanged(isDirty());
				table.refreshRowCache();
			});
			return checkBox;
		}

		private boolean isDirty() {
			for (RoleVO role : roles.values()) {
				if (role.isDirty()) {
					return true;
				}
			}
			return false;
		}
	}

	public class CreateRoleButton extends WindowButton {
		public static final String ROLE_CODE = "role-code";
		public static final String ROLE_TITLE = "role-title";

		@PropertyId("code") private BaseTextField code;
		@PropertyId("title") private BaseTextField title;

		public CreateRoleButton() {
			super($("PermissionsManagementView.createRole"), $("PermissionsManagementView.createRole"));
		}

		@Override
		protected Component buildWindowContent() {
			code = new BaseTextField($("PermissionsManagementView.code"));
			code.setRequired(true);
			code.setId(ROLE_CODE);

			title = new BaseTextField($("PermissionsManagementView.title"));
			title.setRequired(true);
			title.setId(ROLE_TITLE);

			return new BaseForm<RoleVO>(new RoleVO(), this, code, title) {
				@Override
				protected void saveButtonClick(RoleVO role) {
					getWindow().close();
					presenter.roleCreationRequested(role);
				}

				@Override
				protected void cancelButtonClick(RoleVO role) {
					getWindow().close();
				}
			};
		}
	}
}
