package com.constellio.app.ui.pages.management.shares;

public class ShareManagementViewImpl /*extends BaseViewImpl implements ShareManagementView */ {

	//	public static final String CREATE_ROLE = "create-role";
	//	public static final String SAVE = "save";
	//	public static final String REVERT = "revert";
	//
	//	private final ShareManagementPresenter presenter;
	//	private RoleMatrix matrix;
	//	private VerticalLayout layout;
	//	private Button save;
	//	private Button revert;
	//
	//	public ShareManagementViewImpl() {
	//		presenter = new ShareManagementPresenter(this);
	//	}
	//
	//	@Override
	//	protected String getTitle() {
	//		return $("PermissionsManagementView.viewTitle");
	//	}
	//
	//	@Override
	//	protected ClickListener getBackButtonClickListener() {
	//		return new ClickListener() {
	//			@Override
	//			public void buttonClick(ClickEventHandler event) {
	//				navigateTo().adminModule();
	//			}
	//		};
	//	}
	//
	//	@Override
	//	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
	//		List<Button> buttons = super.buildActionMenuButtons(event);
	//
	//		Share create = new CreateRoleButton();
	//		create.addStyleName(CREATE_ROLE);
	//		buttons.add(create);
	//
	//		save = new Button($("PermissionsManagementView.save"));
	//		save.addClickListener(new ClickListener() {
	//			@Override
	//			public void buttonClick(ClickEvent event) {
	//				presenter.saveRequested(matrix.getModifiedRoles());
	//			}
	//		});
	//		save.addStyleName(SAVE);
	//		save.setEnabled(false);
	//		buttons.add(save);
	//
	//		revert = new Button($("PermissionsManagementView.revert"));
	//		revert.addClickListener(new ClickListener() {
	//			@Override
	//			public void buttonClick(ClickEvent event) {
	//				presenter.revertRequested();
	//			}
	//		});
	//		revert.addStyleName(REVERT);
	//		revert.setEnabled(false);
	//		buttons.add(revert);
	//
	//		return buttons;
	//	}
	//
	//	@Override
	//	public void setSaveAndRevertButtonStatus(boolean enabled) {
	//		save.setEnabled(enabled);
	//		revert.setEnabled(enabled);
	//	}
	//
	//
	//	@Override
	//	public void setSaveButton(boolean enabled) {
	//
	//	}
	//
	//	@Override
	//	public void addShare(AuthorizationVO authorization) {
	//
	//	}
	//
	//	@Override
	//	public void refreshView() {
	//		matrix = new RoleMatrix(presenter.getShares());
	//		buildAllGroups();
	//	}
	//
	//	@Override
	//	protected Component buildMainComponent(ViewChangeEvent event) {
	//		layout = new VerticalLayout();
	//		layout.setSpacing(true);
	//
	//		refreshView();
	//
	//		return layout;
	//	}
	//
	//	private void buildAllGroups() {
	//		layout.removeAllComponents();
	//		for (String group : presenter.getPermissionGroups()) {
	//			layout.addComponent(buildPermissionGroup(group));
	//		}
	//	}
	//
	//	private Component buildPermissionGroup(String group) {
	//		Label caption = new Label($("perm.group." + group));
	//		caption.addStyleName(ValoTheme.LABEL_H2);
	//
	//		Table table = new BaseTable(getClass().getName());
	//		for (String permission : presenter.getPermissionsInGroup(group)) {
	//			table.addItem(permission);
	//		}
	//		table.setPageLength(table.size());
	//		matrix.attachTo(table);
	//
	//		VerticalLayout layout = new VerticalLayout(caption, table);
	//		layout.setSpacing(true);
	//
	//		return layout;
	//	}
	//
	//	public class RoleMatrix implements ColumnGenerator, CellStyleGenerator {
	//		public static final String PERMISSION_NAME_COLUMN = "permissionName";
	//		private final Map<Object, AuthorizationVO> authorizations;
	//
	//		public RoleMatrix(List<AuthorizationVO> authorizations) {
	//			this.authorizations = new HashMap<>(authorizations.size());
	//			for (AuthorizationVO auth : authorizations) {
	//				this.authorizations.put(auth.getAuthId(), auth);
	//			}
	//		}
	//
	//		public void attachTo(Table table) {
	//			table.addGeneratedColumn(PERMISSION_NAME_COLUMN, this);
	//			table.setColumnHeader(PERMISSION_NAME_COLUMN, "");
	//			table.setColumnWidth(PERMISSION_NAME_COLUMN, 280);
	//			for (AuthorizationVO role : authorizations.values()) {
	//				String code = role.getAuthId();
	//				table.addGeneratedColumn(code, this);
	//				table.setColumnWidth(code, 60);
	//				table.setColumnAlignment(code, Align.CENTER);
	//			}
	//			table.setCellStyleGenerator(this);
	//		}
	//
	//		public List<RoleVO> getModifiedRoles() {
	//			ArrayList<RoleVO> result = new ArrayList<>();
	//			for (RoleVO role : roles.values()) {
	//				if (role.isDirty()) {
	//					result.add(role);
	//				}
	//			}
	//			return result;
	//		}
	//
	//		@Override
	//		public String getStyle(Table source, Object itemId, Object propertyId) {
	//			if (propertyId == null || PERMISSION_NAME_COLUMN.equals(propertyId)) {
	//				return null;
	//			}
	//			return roles.get(propertyId).isPermissionDirty((String) itemId) ?
	//				   "permission-dirty" : "permission-clean";
	//		}
	//
	//		@Override
	//		public Object generateCell(Table source, Object itemId, Object columnId) {
	//			String permission = (String) itemId;
	//			return columnId.equals(PERMISSION_NAME_COLUMN) ?
	//				   buildCaption(permission) :
	//				   buildCheckBox(roles.get(columnId), permission, source);
	//		}
	//
	//		private Object buildCaption(String permission) {
	//			return new Label($("perm." + permission), ContentMode.HTML);
	//		}
	//
	//		private Object buildCheckBox(final RoleVO role, final String permission, final Table table) {
	//			final CheckBox checkBox = new CheckBox();
	//			checkBox.setValue(role.hasPermission(permission));
	//			checkBox.addStyleName(role.getCode() + "-" + permission.replaceAll("\\.", "_"));
	//			checkBox.addValueChangeListener(new ValueChangeListener() {
	//				@Override
	//				public void valueChange(ValueChangeEvent event) {
	//					if (checkBox.getValue()) {
	//						role.addPermission(permission);
	//					} else {
	//						role.removePermission(permission);
	//					}
	//					presenter.permissionsChanged(isDirty());
	//					table.refreshRowCache();
	//				}
	//			});
	//			return checkBox;
	//		}
	//
	//		private boolean isDirty() {
	//			for (RoleVO role : roles.values()) {
	//				if (role.isDirty()) {
	//					return true;
	//				}
	//			}
	//			return false;
	//		}
	//	}
	//
	//	public class CreateRoleButton extends WindowButton {
	//		public static final String ROLE_CODE = "role-code";
	//		public static final String ROLE_TITLE = "role-title";
	//
	//		@PropertyId("code") private BaseTextField code;
	//		@PropertyId("title") private BaseTextField title;
	//
	//		public CreateRoleButton() {
	//			super($("PermissionsManagementView.createRole"), $("PermissionsManagementView.createRole"));
	//		}
	//
	//		@Override
	//		protected Component buildWindowContent() {
	//			code = new BaseTextField($("PermissionsManagementView.code"));
	//			code.setRequired(true);
	//			code.setId(ROLE_CODE);
	//
	//			title = new BaseTextField($("PermissionsManagementView.title"));
	//			title.setRequired(true);
	//			title.setId(ROLE_TITLE);
	//
	//			return new BaseForm<RoleVO>(new RoleVO(), this, code, title) {
	//				@Override
	//				protected void saveButtonClick(RoleVO role)
	//						throws ValidationException {
	//					getWindow().close();
	//					presenter.roleCreationRequested(role);
	//				}
	//
	//				@Override
	//				protected void cancelButtonClick(RoleVO role) {
	//					getWindow().close();
	//				}
	//			};
	//		}
	//	}
}
