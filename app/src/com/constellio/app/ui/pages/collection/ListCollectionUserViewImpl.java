package com.constellio.app.ui.pages.collection;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RoleVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.AuthorizationsButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.RolesButton;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupField;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

// After rename CollectionSecurityManagementViewImpl
public class ListCollectionUserViewImpl extends BaseViewImpl implements ListCollectionUserView {
	public static final String USER_LOOKUP = "user-lookup";
	public static final String ROLES_USERS_COMBO = "roles-users-combo";
	public static final String ROLES_GROUPS_COMBO = "roles-groups-combo";
	public static final String USER_ADD = "user-addUserRole";
	public static final String USER_TABLE = "users";
	public static final String GROUP_LOOKUP = "group-lookup";
	public static final String GROUP_ADD = "group-add";
	public static final String GROUP_TABLE = "groups";

	private final ListCollectionUserPresenter presenter;
	private Table usersTable;
	private TabSheet groupTabs;
	private TabWithTable activeGroupsTab;
	private TabWithTable inactiveGroupsTab;
	private VerticalLayout layout;
	private ComboBox comboboxUserRoles;
	private ComboBox comboboxGroupRoles;
	private Button addUserRole;
	private Button addGroupRole;
	private UserCredentialLookup lookupUser;
	private GlobalGroupLookup lookupGroup;
	private final int batchSize = 20;

	public ListCollectionUserViewImpl() {
		presenter = new ListCollectionUserPresenter(this);
	}

	public ListCollectionUserViewImpl(ViewChangeEvent event) {
		presenter = new ListCollectionUserPresenter(this);
		this.buildMainComponent(event);
	}

	@Override
	protected String getTitle() {
		return $("ListCollectionUserView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Label groupsCaption = new Label($("ListCollectionUserView.groupsCaption"));
		groupsCaption.addStyleName(ValoTheme.LABEL_H2);
		activeGroupsTab = new TabWithTable("activeGroupTab") {
			@Override
			public Table buildTable() {
				return buildGroupsTable(false);
			}
		};

		inactiveGroupsTab = new TabWithTable("inactiveGroupTab") {
			@Override
			public Table buildTable() {
				return buildGroupsTable(true);
			}
		};
		groupTabs = new TabSheet();
		groupTabs.addTab(activeGroupsTab.getTabLayout(), $("ListCollectionUserView.activeGroupTab"));
		groupTabs.addTab(inactiveGroupsTab.getTabLayout(), $("ListCollectionUserView.inactiveGroupTab"));

		Label usersCaption = new Label($("ListCollectionUserView.usersCaption"));
		usersCaption.addStyleName(ValoTheme.LABEL_H2);
		usersTable = buildUserTable();

		layout = new VerticalLayout(groupsCaption, buildGroupRolesAndAdder(), groupTabs, new Label(""), usersCaption, buildUserRolesAndAdder(),
				usersTable);
		layout.setSpacing(true);

		return layout;
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

	private HorizontalLayout buildUserRolesAndAdder() {
		lookupUser = new UserCredentialLookup(presenter.getUserLookupProvider());
		lookupUser.addStyleName(USER_LOOKUP);

		comboboxUserRoles = new BaseComboBox();
		comboboxUserRoles.setNullSelectionAllowed(false);
		comboboxUserRoles.addStyleName(ROLES_USERS_COMBO);
		for (RoleVO roleVO : presenter.getRoles()) {
			comboboxUserRoles.addItem(roleVO.getCode());
			comboboxUserRoles.setItemCaption(roleVO.getCode(), roleVO.getTitle());
		}
		comboboxUserRoles.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableOrDisableButton(lookupUser.getValue(), (String) comboboxUserRoles.getValue(), addUserRole);
			}
		});

		addUserRole = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked((UserCredentialVO) lookupUser.getValue(), (String) comboboxUserRoles.getValue());
			}
		};
		addUserRole.addStyleName(USER_ADD);
		addUserRole.setEnabled(false);
		lookupUser.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableOrDisableButton(lookupUser.getValue(), (String) comboboxUserRoles.getValue(), addUserRole);
			}
		});

		HorizontalLayout layout = new HorizontalLayout(lookupUser, comboboxUserRoles, addUserRole);
		layout.setSpacing(true);
		return layout;
	}

	private void enableOrDisableButton(Object vo, String role, Button button) {
		if (vo != null && role != null) {
			button.setEnabled(true);
		} else {
			button.setEnabled(false);
		}
	}

	private Table buildUserTable() {
		RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container container = buildContainer(dataProvider);
		RecordVOTable table = new RecordVOTable($("ListCollectionUserView.tableTitle", dataProvider.size()), container);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 158);
		int tableSize = batchSize;
		if (tableSize > table.size()) {
			tableSize = table.size();
		}
		table.setPageLength(tableSize);
		table.addStyleName(USER_TABLE);

		return table;
	}

	private HorizontalLayout buildGroupRolesAndAdder() {
		lookupGroup = new GlobalGroupLookup(presenter.getGlobalGroupLookupProvider());
		lookupGroup.addStyleName(GROUP_LOOKUP);
		lookupGroup.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableOrDisableButton(lookupGroup.getValue(), (String) comboboxGroupRoles.getValue(), addGroupRole);
			}
		});

		comboboxGroupRoles = new BaseComboBox();
		comboboxGroupRoles.setNullSelectionAllowed(false);
		comboboxGroupRoles.addStyleName(ROLES_GROUPS_COMBO);
		for (RoleVO roleVO : presenter.getRoles()) {
			comboboxGroupRoles.addItem(roleVO.getCode());
			comboboxGroupRoles.setItemCaption(roleVO.getCode(), roleVO.getTitle());
		}
		comboboxGroupRoles.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				enableOrDisableButton(lookupGroup.getValue(), (String) comboboxGroupRoles.getValue(), addGroupRole);
			}
		});

		addGroupRole = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addGlobalGroupButtonClicked((GlobalGroupVO) lookupGroup.getValue(), (String) comboboxGroupRoles.getValue());
			}
		};
		addGroupRole.addStyleName(GROUP_ADD);
		addGroupRole.setEnabled(false);

		HorizontalLayout layout = new HorizontalLayout(lookupGroup, comboboxGroupRoles, addGroupRole);
		layout.setSpacing(true);
		return layout;
	}

	private Table buildGroupsTable(boolean showInactiveGroups) {
		GlobalGroupVODataProvider globalGroupVODataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> globalGroupsVO;
		if (!showInactiveGroups) {
			globalGroupsVO = globalGroupVODataProvider.listActiveGlobalGroupVOsWithUsersInCollection(getCollection());
		} else {
			globalGroupsVO = globalGroupVODataProvider.listInactiveGlobalGroupVOsWithUsersInCollection(getCollection());
		}

		globalGroupVODataProvider.setGlobalGroupVOs(globalGroupsVO);
		Container container = buildGroupContainer(globalGroupVODataProvider);
		BaseTable table = new BaseTable("ListCollectionUserView.globalGroupsTableTitle", $("ListCollectionUserView.globalGroupsTableTitle", globalGroupVODataProvider.size()), container);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("code", $("ListCollectionUserView.groupCodeColumn"));
		table.setColumnHeader("name", $("ListCollectionUserView.groupNameColumn"));
		table.setColumnWidth("", 120);
		table.setColumnWidth("buttons", 158);
		int tableSize = batchSize;
		if (tableSize > table.getItemIds().size()) {
			tableSize = table.getItemIds().size();
		}
		table.setPageLength(tableSize);
		table.addStyleName(GROUP_TABLE);

		return table;
	}

	private Container buildContainer(final RecordVODataProvider dataProvider) {
		RecordVOLazyContainer records = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer container = new ButtonsContainer<>(records, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});
		if (presenter.isRMModuleEnabled()) {
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new AuthorizationsButton(true) {
						@Override
						protected void buttonClick(ClickEvent event) {
							Integer index = (Integer) itemId;
							RecordVO entity = dataProvider.getRecordVO(index);
							presenter.accessAuthorizationsButtonClicked(entity);
						}
					};
				}
			});
		}
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new RolesButton(true) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.permissionsButtonClicked(entity);
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
			}
		});
		return container;
	}

	private Container buildGroupContainer(final GlobalGroupVODataProvider dataProvider) {
		GlobalGroupVOLazyContainer globalGroupsContainer = new GlobalGroupVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer container = new ButtonsContainer<>(globalGroupsContainer, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button displayButton = new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.displayGlobalGroupButtonClicked(entity);
					}
				};
				return displayButton;
			}
		});
		if (presenter.isRMModuleEnabled()) {
			container.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new AuthorizationsButton(true) {
						@Override
						protected void buttonClick(ClickEvent event) {
							Integer index = (Integer) itemId;
							GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
							presenter.accessAuthorizationsGlobalGroupButtonClicked(entity);
						}
					};
				}
			});
		}
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new RolesButton(true) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.permissionsGlobalGroupButtonClicked(entity);
					}
				};
			}
		});
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.deleteGlobalGroupButtonClicked(entity);
					}
				};
				return deleteButton;
			}
		});

		return container;
	}

	@Override
	public void refreshTable() {
		Table newUsersTable = buildUserTable();
		//		Table newGlobalGroupsTable = buildGroupsTable();
		layout.replaceComponent(usersTable, newUsersTable);
		//		layout.replaceComponent(groupsTable, newGlobalGroupsTable);
		activeGroupsTab.refreshTable();
		inactiveGroupsTab.refreshTable();
		usersTable = newUsersTable;
		//		groupsTable = newGlobalGroupsTable;

		cleanUserAndRoles();
		cleanGroupsAndRoles();
	}

	public void cleanUserAndRoles() {
		comboboxUserRoles.setValue(null);
		lookupUser.setValue(null);
	}

	public void cleanGroupsAndRoles() {
		comboboxGroupRoles.setValue(null);
		lookupUser.setValue(null);
	}

	public static class UserCredentialLookup extends LookupField<UserCredentialVO> {
		public UserCredentialLookup(TextInputDataProvider<UserCredentialVO> suggestInputDataProvider) {
			super(suggestInputDataProvider);
			setItemConverter(new Converter<String, UserCredentialVO>() {
				@Override
				public UserCredentialVO convertToModel(String value, Class<? extends UserCredentialVO> targetType,
													   Locale locale)
						throws ConversionException {
					return null;
				}

				@Override
				public String convertToPresentation(UserCredentialVO value, Class<? extends String> targetType,
													Locale locale)
						throws ConversionException {
					String title = value.getFirstName() + " " + value.getLastName();
					if (StringUtils.isBlank(title)) {
						title = value.getUsername();
					}
					return title;
				}

				@Override
				public Class<UserCredentialVO> getModelType() {
					return UserCredentialVO.class;
				}

				@Override
				public Class<String> getPresentationType() {
					return String.class;
				}
			});
		}

		@Override
		public Class<? extends UserCredentialVO> getType() {
			return UserCredentialVO.class;
		}
	}

	public static class GlobalGroupLookup extends LookupField<GlobalGroupVO> {
		public GlobalGroupLookup(TextInputDataProvider<GlobalGroupVO> suggestInputDataProvider) {
			super(suggestInputDataProvider);
			setItemConverter(new Converter<String, GlobalGroupVO>() {
				@Override
				public GlobalGroupVO convertToModel(String value, Class<? extends GlobalGroupVO> targetType,
													Locale locale)
						throws ConversionException {
					return null;
				}

				@Override
				public String convertToPresentation(GlobalGroupVO value, Class<? extends String> targetType,
													Locale locale)
						throws ConversionException {
					return value.getName();
				}

				@Override
				public Class<GlobalGroupVO> getModelType() {
					return GlobalGroupVO.class;
				}

				@Override
				public Class<String> getPresentationType() {
					return String.class;
				}
			});
		}

		@Override
		public Class<? extends GlobalGroupVO> getType() {
			return GlobalGroupVO.class;
		}
	}
}
