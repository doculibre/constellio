package com.constellio.app.ui.pages.globalGroup;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.containers.UserCredentialVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_CONSULT;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_EDIT;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class DisplayGlobalGroupViewImpl extends BaseViewImpl implements DisplayGlobalGroupView {

	public static final String PROPERTY_BUTTONS = "buttons";
	public static final String GLOBAL_GROUP_CODE = "globalGroupCode";

	private DisplayGlobalGroupPresenter presenter;

	private GlobalGroupVO globalGroupVO;

	private Map<String, String> paramsMap;

	private VerticalLayout viewLayout;

	private Label codeCaptionLabel, codeDisplayComponent, nameCaptionLabel, nameDisplayComponent, parentCaptionLabel;

	private Button parentDisplayComponent;

	private final int batchSize = 100;

	private BaseDisplay globalGroupDisplay;
	private Table subGroupTable, userTable, availableUserTable;
	private HorizontalLayout filterAndSearchButtonLayoutSubGroups, filterAndSearchButtonLayoutGlobalGroupsUser, filterAndSearchButtonLayoutAvailableUsers;
	private TableStringFilter tableFilterSubGroups, tableFilterGlobalGroupsUser, tableFilterAvailableAvailableUsers;

	public DisplayGlobalGroupViewImpl() {
		this.presenter = new DisplayGlobalGroupPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		setupParamsAndVO(event);
	}

	private void setupParamsAndVO(ViewChangeEvent event) {
		String parameters = event.getParameters();
		int indexOfSlash = parameters.lastIndexOf("/");
		String breadCrumb = "";
		if (indexOfSlash != -1) {
			breadCrumb = parameters.substring(0, indexOfSlash);
		}
		paramsMap = ParamUtils.getParamsMap(parameters);
		if (paramsMap.containsKey(GLOBAL_GROUP_CODE)) {
			String groupCode = paramsMap.get("globalGroupCode");
			globalGroupVO = presenter.getGlobalGroupVO(groupCode);
			presenter.setPageGroup(groupCode);
		}
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb(breadCrumb);
	}

	@Override
	protected String getTitle() {
		return $("DisplayGlobalGroupView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();

		codeCaptionLabel = new Label($("GlobalGroup.Code"));
		codeCaptionLabel.setId("code");
		codeCaptionLabel.addStyleName("code");
		codeDisplayComponent = new Label(globalGroupVO.getCode());
		captionsAndComponents.add(new CaptionAndComponent(codeCaptionLabel, codeDisplayComponent));

		nameCaptionLabel = new Label($("GlobalGroup.Name"));
		nameCaptionLabel.setId("name");
		nameCaptionLabel.addStyleName("name");
		nameDisplayComponent = new Label(globalGroupVO.getName());
		captionsAndComponents.add(new CaptionAndComponent(nameCaptionLabel, nameDisplayComponent));

		if (StringUtils.isNotBlank(globalGroupVO.getParent())) {
			parentCaptionLabel = new Label($("GlobalGroup.Parent"));
			parentCaptionLabel.setId("parent");
			parentCaptionLabel.addStyleName("parent");

			Group parentGroup = presenter.getGroup(globalGroupVO.getParent());
			parentDisplayComponent = new BaseButton(parentGroup.getTitle()) {
				@Override
				protected void buttonClick(ClickEvent event) {
					navigate().to().displayGlobalGroup(parentGroup.getCode());
				}
			};
			parentDisplayComponent.addStyleName(ValoTheme.BUTTON_LINK);
			captionsAndComponents.add(new CaptionAndComponent(parentCaptionLabel, parentDisplayComponent));
		}

		globalGroupDisplay = new BaseDisplay(captionsAndComponents);

		filterAndSearchButtonLayoutSubGroups = new HorizontalLayout();
		filterAndSearchButtonLayoutSubGroups.setSpacing(true);

		filterAndSearchButtonLayoutGlobalGroupsUser = new HorizontalLayout();
		filterAndSearchButtonLayoutGlobalGroupsUser.setSpacing(true);

		filterAndSearchButtonLayoutAvailableUsers = new HorizontalLayout();
		filterAndSearchButtonLayoutAvailableUsers.setSpacing(true);

		filterAndSearchButtonLayoutSubGroups.setWidth("100%");
		filterAndSearchButtonLayoutGlobalGroupsUser.setWidth("100%");
		filterAndSearchButtonLayoutAvailableUsers.setWidth("100%");

		subGroupTable = buildSubGroupTable();
		userTable = buildUserTable();
		availableUserTable = buildAvailableUserTable();

		tableFilterSubGroups = new TableStringFilter(subGroupTable);
		tableFilterSubGroups.setWidth("50%");
		tableFilterGlobalGroupsUser = new TableStringFilter(userTable);
		tableFilterGlobalGroupsUser.setWidth("50%");
		tableFilterAvailableAvailableUsers = new TableStringFilter(availableUserTable);
		tableFilterAvailableAvailableUsers.setWidth("50%");

		viewLayout.addComponents(globalGroupDisplay, filterAndSearchButtonLayoutSubGroups, subGroupTable,
				filterAndSearchButtonLayoutGlobalGroupsUser, userTable,
				filterAndSearchButtonLayoutAvailableUsers, availableUserTable);
		viewLayout.setExpandRatio(globalGroupDisplay, 1);

		filterAndSearchButtonLayoutSubGroups.addComponent(tableFilterSubGroups);
		filterAndSearchButtonLayoutGlobalGroupsUser.addComponents(tableFilterGlobalGroupsUser);
		filterAndSearchButtonLayoutAvailableUsers.addComponents(tableFilterAvailableAvailableUsers);

		return viewLayout;
	}

	@Override
	public void partialRefresh() {
		refreshTable();
	}

	private Table buildSubGroupTable() {
		final GlobalGroupVODataProvider dataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> subGroupsVOs = dataProvider.listActiveSubGlobalGroupsVOsFromGroup(globalGroupVO.getCode());
		dataProvider.setGlobalGroupVOs(subGroupsVOs);
		Container container = new GlobalGroupVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addSubGroupButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;
		String title = "DisplayGlobalGroup.listSubGroups";
		return buildTable(container, title);
	}

	private Table buildUserTable() {
		final UserCredentialVODataProvider dataProvider = presenter.getUserCredentialVODataProvider(globalGroupVO.getCode());
		List<UserCredentialVO> availableUserCredentialVOs = dataProvider.listActifsUserCredentialVOsInGlobalGroup(
				globalGroupVO.getCode());
		dataProvider.setUserCredentialVOs(availableUserCredentialVOs);
		Container container = new UserCredentialVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addUserButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;
		String title = "DisplayGlobalGroup.listGroupsUserCredentials";
		return buildTable(container, title);
	}

	private Table buildAvailableUserTable() {

		final UserCredentialVODataProvider dataProvider = presenter.getUserCredentialVODataProvider(globalGroupVO.getCode());
		List<UserCredentialVO> availableUserCredentialVOs = dataProvider.listActifsUserCredentialVOsNotInGlobalGroup(
				globalGroupVO.getCode());
		dataProvider.setUserCredentialVOs(availableUserCredentialVOs);

		Container container = new UserCredentialVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
				Button addButton = new AddButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.addUserCredentialButtonClicked(globalGroupVO.getCode(), entity.getUsername());
					}
				};
				addButton.setEnabled(canChangeUserAssignment(entity));
				addButton.setVisible(canChangeUserAssignment(entity));
				return addButton;
			}
		});
		container = buttonsContainer;
		String title = "DisplayGlobalGroup.listUserCredentials";
		return buildTable(container, title);

	}

	private Table buildTable(Container container, String title) {
		int size = container.size();
		Table table = new BaseTable(getClass().getName(), $(title, size), container);
//		int tableSize = batchSize;
//		if (tableSize > table.getItemIds().size()) {
//			tableSize = table.getItemIds().size();
//		}
//		table.setPageLength(tableSize);
		if (size < 10) {
			table.setPageLength(size);
		} else {
			table.setPageLength(10);
		}
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addStyleName(title);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);

		table.setColumnHeader("code", $("DisplayGlobalGroupView.codeColumn"));
		table.setColumnHeader("name", $("DisplayGlobalGroupView.nameColumn"));
		table.setColumnHeader("username", $("DisplayGlobalGroupView.usernameColumn"));
		table.setColumnHeader("firstName", $("DisplayGlobalGroupView.firstNameColumn"));
		table.setColumnHeader("lastName", $("DisplayGlobalGroupView.lastNameColumn"));
		table.setColumnHeader("email", $("DisplayGlobalGroupView.emailColumn"));
		return table;
	}

	private void addUserButtons(final UserCredentialVODataProvider dataProvider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.displayUserCredentialButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.editUserCredentialButtonClicked(entity);

					}
				};
				return editButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteUserCredentialButtonClicked(entity, globalGroupVO.getCode());
					}
				};
				deleteButton.setEnabled(canChangeUserAssignment(entity));
				deleteButton.setVisible(canChangeUserAssignment(entity));
				return deleteButton;

			}
		});
	}

	private void addSubGroupButtons(final GlobalGroupVODataProvider dataProvider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button displaySubGroupButton = new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.displaySubGroupCliked(entity);
					}
				};
				displaySubGroupButton.addStyleName("DisplayGlobalGroupView.displaySubGroup");
				return displaySubGroupButton;
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button editSubGroupButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.editSubGroupButtonClicked(entity);

					}
				};
				editSubGroupButton.addStyleName("DisplayGlobalGroupView.editSubGroup");
				return editSubGroupButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
				Button deleteSubGroupButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteSubGroupButtonClicked(entity);
					}
				};
				deleteSubGroupButton.addStyleName("DisplayGlobalGroupView.deleteSubGroup");
				deleteSubGroupButton.setEnabled(canChangeGroupAssignment(entity));
				deleteSubGroupButton.setVisible(canChangeGroupAssignment(entity));
				return deleteSubGroupButton;
			}
		});
	}

	private boolean canChangeGroupAssignment(GlobalGroupVO groupVO) {
		boolean canModifyAssignment = globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE;
		boolean isBothSynced = !globalGroupVO.isLocallyCreated() && !groupVO.isLocallyCreated();
		return canModifyAssignment && !isBothSynced;
	}

	private boolean canChangeUserAssignment(UserCredentialVO userCredentialVO) {
		boolean canModifyAssignment = globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE;
		boolean isBothSynced = !globalGroupVO.isLocallyCreated()
							   && userCredentialVO.getSyncMode() == UserSyncMode.SYNCED;
		return canModifyAssignment && !isBothSynced;
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
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {

		List<String> excludedActionTypes = asList(GROUP_CONSULT.name(), GROUP_EDIT.name());

		List<MenuItemAction> menuItemActions = buildRecordVOActionButtonFactory(excludedActionTypes).buildMenuItemActions();
		menuItemActions.add(MenuItemAction.builder()
				.type("AddSubGroup")
				.caption($("DisplayGlobalGroupView.addSubGroup"))
				.icon(FontAwesome.GROUP)
				.command(recordIds -> presenter.addSubGroupClicked(globalGroupVO))
				.state(new MenuItemActionState(MenuItemActionStateStatus.VISIBLE))
				.priority(10)
				.build());

		menuItemActions.add(MenuItemAction.builder()
				.type("EditSubGroup")
				.caption($("edit"))
				.icon(FontAwesome.EDIT)
				.command(recordIds -> presenter.editGroupButtonClicked())
				.state(new MenuItemActionState(MenuItemActionStateStatus.VISIBLE))
				.priority(11)
				.build());

		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				menuItemActions
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {
		return new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getPageGroup().getSchema()::getTypeCode;
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return buildRecordVOActionButtonFactory()::buildMenuItemRecordProvider;
			}
		};
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory() {
		return buildRecordVOActionButtonFactory(Collections.emptyList());
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory(List<String> excludedActionTypes) {
		return new RecordVOActionButtonFactory(presenter.getPageGroup(), this, excludedActionTypes);
	}

	@Override
	protected boolean alwaysUseLayoutForActionMenu() {
		return true;
	}

	@Override
	protected String getActionMenuBarCaption() {
		return null;
	}

	@Override
	public void refreshTable() {
		Table newSubGroupTable = buildSubGroupTable();
		Table newUserTable = buildUserTable();
		Table newAvailableUserCredentialsTable = buildAvailableUserTable();
		viewLayout.replaceComponent(subGroupTable, newSubGroupTable);
		viewLayout.replaceComponent(userTable, newUserTable);
		viewLayout.replaceComponent(availableUserTable, newAvailableUserCredentialsTable);
		subGroupTable = newSubGroupTable;
		userTable = newUserTable;
		availableUserTable = newAvailableUserCredentialsTable;

		TableStringFilter newTableFilterSubGroups = new TableStringFilter(subGroupTable);
		newTableFilterSubGroups.setWidth("50%");
		TableStringFilter newTableFilterGlobalGroupsUser = new TableStringFilter(userTable);
		newTableFilterGlobalGroupsUser.setWidth("50%");
		TableStringFilter newTableFilterAvailableAvailableUsers = new TableStringFilter(availableUserTable);
		newTableFilterAvailableAvailableUsers.setWidth("50%");
		filterAndSearchButtonLayoutSubGroups.replaceComponent(tableFilterSubGroups, newTableFilterSubGroups);
		filterAndSearchButtonLayoutGlobalGroupsUser.replaceComponent(tableFilterGlobalGroupsUser, newTableFilterGlobalGroupsUser);
		filterAndSearchButtonLayoutAvailableUsers
				.replaceComponent(tableFilterAvailableAvailableUsers, newTableFilterAvailableAvailableUsers);
		tableFilterSubGroups = newTableFilterSubGroups;
		tableFilterGlobalGroupsUser = newTableFilterGlobalGroupsUser;
		tableFilterAvailableAvailableUsers = newTableFilterAvailableAvailableUsers;
	}

	@Override
	public DisplayGlobalGroupPresenter getPresenter() {
		return presenter;
	}

	@Override
	public String getBreadCrumb() {
		return presenter.getBreadCrumb();
	}
}
