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
package com.constellio.app.ui.pages.user;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.UserCredentialStatus;
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
public class DisplayUserCredentialViewImpl extends BaseViewImpl implements DisplayUserCredentialView {

	public static final String PROPERTY_BUTTONS = "buttons";

	private DisplayUserCredentialPresenter presenter;

	private UserCredentialVO userCredentialVO;

	private Map<String, String> paramsMap;

	private VerticalLayout viewLayout;

	private Label usernameCaptionLabel;
	private Label usernameDisplayComponent;

	private Label firstNameCaptionLabel;
	private Label firstNameDisplayComponent;

	private Label lastNameCaptionLabel;
	private Label lastNameDisplayComponent;

	private Label emailCaptionLabel;
	private Label emailDisplayComponent;

	private Label collectionsCaptionLabel;
	private Label collectionsDisplayComponent;

	private BaseDisplay userCredentialDisplay;
	private Table userGlobalGroupTable;
	private Table availableGlobalGroupTable;
	private HorizontalLayout filterAndAddButtonLayoutUserGlobalGroups;
	private TableStringFilter tableFilterUserGlobalGroups;
	private HorizontalLayout filterAndAddButtonLayoutAvailableGlobalGroups;
	private TableStringFilter tableFilterAvailableGlobalGroups;

	public DisplayUserCredentialViewImpl() {
		this.presenter = new DisplayUserCredentialPresenter(this);
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
		if (paramsMap.containsKey("username")) {
			userCredentialVO = presenter.getUserCredentialVO(paramsMap.get("username"));
		}
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb(breadCrumb);
	}

	@Override
	protected String getTitle() {
		return $("DisplayUserCredentialView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);

		usernameCaptionLabel = new Label($("UserCredentialView.username"));
		usernameCaptionLabel.setId("username");
		usernameCaptionLabel.addStyleName("username");
		usernameDisplayComponent = new Label(userCredentialVO.getUsername());

		firstNameCaptionLabel = new Label($("UserCredentialView.firstName"));
		firstNameCaptionLabel.setId("firstName");
		firstNameCaptionLabel.addStyleName("firstName");
		firstNameDisplayComponent = new Label(userCredentialVO.getFirstName());

		lastNameCaptionLabel = new Label($("UserCredentialView.lastName"));
		lastNameCaptionLabel.setId("lastName");
		lastNameCaptionLabel.addStyleName("lastName");
		lastNameDisplayComponent = new Label(userCredentialVO.getLastName());

		emailCaptionLabel = new Label($("UserCredentialView.email"));
		emailCaptionLabel.setId("email");
		emailCaptionLabel.addStyleName("email");
		emailDisplayComponent = new Label(userCredentialVO.getEmail());

		emailCaptionLabel = new Label($("UserCredentialView.email"));
		emailCaptionLabel.setId("email");
		emailCaptionLabel.addStyleName("email");
		emailDisplayComponent = new Label(userCredentialVO.getEmail());

		collectionsCaptionLabel = new Label($("UserCredentialView.collections"));
		collectionsCaptionLabel.setId("collections");
		collectionsCaptionLabel.addStyleName("collections");
		collectionsDisplayComponent = new Label(userCredentialVO.getStringCollections());

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(usernameCaptionLabel, usernameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(firstNameCaptionLabel, firstNameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(lastNameCaptionLabel, lastNameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(emailCaptionLabel, emailDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(collectionsCaptionLabel, collectionsDisplayComponent));
		userCredentialDisplay = new BaseDisplay(captionsAndComponents);

		filterAndAddButtonLayoutUserGlobalGroups = new HorizontalLayout();
		filterAndAddButtonLayoutAvailableGlobalGroups = new HorizontalLayout();
		filterAndAddButtonLayoutUserGlobalGroups.setWidth("100%");
		filterAndAddButtonLayoutAvailableGlobalGroups.setWidth("100%");

		userGlobalGroupTable = buildUserGlobalGroupTable();
		availableGlobalGroupTable = buildAvailableGlobalGroupTable();

		tableFilterUserGlobalGroups = new TableStringFilter(userGlobalGroupTable);
		tableFilterAvailableGlobalGroups = new TableStringFilter(availableGlobalGroupTable);

		if (userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAndOrModify()) {
			viewLayout.addComponents(userCredentialDisplay, filterAndAddButtonLayoutUserGlobalGroups, userGlobalGroupTable,
					filterAndAddButtonLayoutAvailableGlobalGroups, availableGlobalGroupTable);
		} else {
			viewLayout.addComponents(userCredentialDisplay);
		}

		viewLayout.setExpandRatio(userCredentialDisplay, 1);

		filterAndAddButtonLayoutUserGlobalGroups.addComponents(tableFilterUserGlobalGroups);
		filterAndAddButtonLayoutAvailableGlobalGroups.addComponents(tableFilterAvailableGlobalGroups);

		return viewLayout;
	}

	private Table buildUserGlobalGroupTable() {
		final GlobalGroupVODataProvider globalGroupVODataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> userGlobalGroupVOs = globalGroupVODataProvider.listActiveGlobalGroupVOsFromUser(
				userCredentialVO.getUsername());
		globalGroupVODataProvider.setGlobalGroupVOs(userGlobalGroupVOs);
		Container container = new GlobalGroupVOLazyContainer(globalGroupVODataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addUsersGlobalGroupButtons(globalGroupVODataProvider, buttonsContainer);
		container = buttonsContainer;
		String title = "DisplayUserCredentialView.listUsersGroups";
		return buildTable(container, title);
	}

	private Table buildAvailableGlobalGroupTable() {
		final GlobalGroupVODataProvider globalGroupVODataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> availableGlobalGroupVOs = globalGroupVODataProvider.listGlobalGroupVOsNotContainingUser(
				userCredentialVO.getUsername());
		globalGroupVODataProvider.setGlobalGroupVOs(availableGlobalGroupVOs);
		Container container = new GlobalGroupVOLazyContainer(globalGroupVODataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button addButton = new AddButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.addGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());

					}
				};
				return addButton;
			}
		});
		container = buttonsContainer;
		String title = "DisplayUserCredentialView.listGroups";
		return buildTable(container, title);
	}

	private void addUsersGlobalGroupButtons(final GlobalGroupVODataProvider globalGroupVODataProvider,
			ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button displayButton = new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.displayGlobalGroupButtonClicked(entity.getCode(), userCredentialVO.getUsername());
					}
				};
				return displayButton;
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.editGlobalGroupButtonClicked(entity.getCode(), userCredentialVO.getUsername());

					}
				};
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
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.deleteGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());
					}
				};
				return deleteButton;
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
		List<Button> actionMenuButtons = new ArrayList<>();
		actionMenuButtons.add(new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked(userCredentialVO);
			}
		});
		return actionMenuButtons;
	}

	@Override
	public void refreshTable() {
		Table newUserGlobalGroupTable = buildUserGlobalGroupTable();
		Table newAvailableGlobalGroupTable = buildAvailableGlobalGroupTable();
		viewLayout.replaceComponent(userGlobalGroupTable, newUserGlobalGroupTable);
		viewLayout.replaceComponent(availableGlobalGroupTable, newAvailableGlobalGroupTable);
		userGlobalGroupTable = newUserGlobalGroupTable;
		availableGlobalGroupTable = newAvailableGlobalGroupTable;

		TableStringFilter newTableFilterUserGlobalGroups = new TableStringFilter(userGlobalGroupTable);
		TableStringFilter newTableFilterAvailableGlobalGroups = new TableStringFilter(availableGlobalGroupTable);
		filterAndAddButtonLayoutUserGlobalGroups.replaceComponent(tableFilterUserGlobalGroups, newTableFilterUserGlobalGroups);
		filterAndAddButtonLayoutAvailableGlobalGroups
				.replaceComponent(tableFilterAvailableGlobalGroups, newTableFilterAvailableGlobalGroups);
		tableFilterUserGlobalGroups = newTableFilterUserGlobalGroups;
		tableFilterAvailableGlobalGroups = newTableFilterAvailableGlobalGroups;
	}

	private Table buildTable(Container container, String title) {

		Table table = new Table($(title), container);
		table.setPageLength(table.getItemIds().size());
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}
}
