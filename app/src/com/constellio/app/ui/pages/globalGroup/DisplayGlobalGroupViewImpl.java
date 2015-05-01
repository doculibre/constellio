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
package com.constellio.app.ui.pages.globalGroup;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

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
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.containers.UserCredentialVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DisplayGlobalGroupViewImpl extends BaseViewImpl implements DisplayGlobalGroupView {

	public static final String PROPERTY_BUTTONS = "buttons";

	private DisplayGlobalGroupPresenter presenter;

	private GlobalGroupVO globalGroupVO;

	private Map<String, String> paramsMap;

	private VerticalLayout viewLayout;

	private Label codeCaptionLabel, codeDisplayComponent, nameCaptionLabel, nameDisplayComponent;

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
		if (paramsMap.containsKey("globalGroupCode")) {
			globalGroupVO = presenter.getGlobalGroupVO(paramsMap.get("globalGroupCode"));
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

		codeCaptionLabel = new Label($("GlobalGroup.Code"));
		codeCaptionLabel.setId("code");
		codeCaptionLabel.addStyleName("code");
		codeDisplayComponent = new Label(globalGroupVO.getCode());

		nameCaptionLabel = new Label($("GlobalGroup.Name"));
		nameCaptionLabel.setId("name");
		nameCaptionLabel.addStyleName("name");
		nameDisplayComponent = new Label(globalGroupVO.getName());

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(codeCaptionLabel, codeDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(nameCaptionLabel, nameDisplayComponent));
		globalGroupDisplay = new BaseDisplay(captionsAndComponents);

		filterAndSearchButtonLayoutSubGroups = new HorizontalLayout();
		filterAndSearchButtonLayoutGlobalGroupsUser = new HorizontalLayout();
		filterAndSearchButtonLayoutAvailableUsers = new HorizontalLayout();
		filterAndSearchButtonLayoutSubGroups.setWidth("100%");
		filterAndSearchButtonLayoutGlobalGroupsUser.setWidth("100%");
		filterAndSearchButtonLayoutAvailableUsers.setWidth("100%");

		subGroupTable = buildSubGroupTable();
		userTable = buildUserTable();
		availableUserTable = buildAvailableUserTable();

		tableFilterSubGroups = new TableStringFilter(subGroupTable);
		tableFilterGlobalGroupsUser = new TableStringFilter(userTable);
		tableFilterAvailableAvailableUsers = new TableStringFilter(availableUserTable);

		viewLayout.addComponents(globalGroupDisplay, filterAndSearchButtonLayoutSubGroups, subGroupTable,
				filterAndSearchButtonLayoutGlobalGroupsUser, userTable,
				filterAndSearchButtonLayoutAvailableUsers, availableUserTable);
		viewLayout.setExpandRatio(globalGroupDisplay, 1);

		filterAndSearchButtonLayoutSubGroups.addComponent(tableFilterSubGroups);
		filterAndSearchButtonLayoutGlobalGroupsUser.addComponents(tableFilterGlobalGroupsUser);
		filterAndSearchButtonLayoutAvailableUsers.addComponents(tableFilterAvailableAvailableUsers);

		return viewLayout;
	}

	private Table buildSubGroupTable() {
		final GlobalGroupVODataProvider dataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> subGroupsVOs = dataProvider.listActiveSubGlobalGroupsVOsFromGroup(globalGroupVO.getCode());
		dataProvider.setGlobalGroupVOs(subGroupsVOs);
		Container container = new GlobalGroupVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addSubGroupButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;
		String title = "DisplayGlobalGroup.listSubGroups";
		return buildTable(container, title);
	}

	private Table buildUserTable() {
		final UserCredentialVODataProvider dataProvider = presenter.getUserCredentialVODataProvider(globalGroupVO.getCode());
		Container container = new UserCredentialVOLazyContainer(dataProvider);
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
		Container container = new UserCredentialVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button addButton = new AddButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.addUserCredentialButtonClicked(globalGroupVO.getCode(), entity.getUsername());

					}
				};
				addButton.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				addButton.setVisible(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return addButton;
			}
		});
		container = buttonsContainer;
		String title = "DisplayGlobalGroup.listUserCredentials";
		return buildTable(container, title);

	}

	private Table buildTable(Container container, String title) {
		Table table = new Table($(title), container);
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.addStyleName(title);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	private void addUserButtons(final UserCredentialVODataProvider dataProvider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.displayUserCredentialButtonClicked(entity, globalGroupVO.getCode());
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.editUserCredentialButtonClicked(entity, globalGroupVO.getCode());

					}
				};
				editButton.setEnabled(presenter.canAddOrModify());
				editButton.setVisible(presenter.canAddOrModify());
				return editButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						UserCredentialVO entity = dataProvider.getUserCredentialVO(index);
						presenter.deleteUserCredentialButtonClicked(entity, globalGroupVO.getCode());
					}
				};
				deleteButton.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				deleteButton.setVisible(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return deleteButton;

			}
		});
	}

	private void addSubGroupButtons(final GlobalGroupVODataProvider dataProvider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
			protected Button newButtonInstance(final Object itemId) {
				Button editSubGroupButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.editSubGroupButtonClicked(entity);

					}
				};
				editSubGroupButton.addStyleName("DisplayGlobalGroupView.editSubGroup");
				editSubGroupButton
						.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				editSubGroupButton
						.setVisible(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return editSubGroupButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button deleteSubGroupButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = dataProvider.getGlobalGroupVO(index);
						presenter.deleteSubGroupButtonClicked(entity);
					}
				};
				deleteSubGroupButton.addStyleName("DisplayGlobalGroupView.deleteSubGroup");
				deleteSubGroupButton
						.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				deleteSubGroupButton
						.setVisible(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return deleteSubGroupButton;
			}
		});
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();
		Button addSubGroupButton = new BaseButton($("DisplayGlobalGroupView.addSubGroup")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addSubGroupClicked(globalGroupVO);
			}
		};
		addSubGroupButton.addStyleName("DisplayGlobalGroupView.addSubGroup");
		addSubGroupButton.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
		actionMenuButtons.add(addSubGroupButton);
		Button editButton = new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked(globalGroupVO);
			}
		};
		editButton.setEnabled(presenter.canAddOrModify());
		actionMenuButtons.add(editButton);
		Button deleteButton = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked(globalGroupVO);
			}
		};
		deleteButton.setEnabled(globalGroupVO.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
		actionMenuButtons.add(deleteButton);

		return actionMenuButtons;
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
		TableStringFilter newTableFilterGlobalGroupsUser = new TableStringFilter(userTable);
		TableStringFilter newTableFilterAvailableAvailableUsers = new TableStringFilter(availableUserTable);
		filterAndSearchButtonLayoutSubGroups.replaceComponent(tableFilterSubGroups, newTableFilterSubGroups);
		filterAndSearchButtonLayoutGlobalGroupsUser.replaceComponent(tableFilterGlobalGroupsUser, newTableFilterGlobalGroupsUser);
		filterAndSearchButtonLayoutAvailableUsers
				.replaceComponent(tableFilterAvailableAvailableUsers, newTableFilterAvailableAvailableUsers);
		tableFilterSubGroups = newTableFilterSubGroups;
		tableFilterGlobalGroupsUser = newTableFilterGlobalGroupsUser;
		tableFilterAvailableAvailableUsers = newTableFilterAvailableAvailableUsers;
	}
}
