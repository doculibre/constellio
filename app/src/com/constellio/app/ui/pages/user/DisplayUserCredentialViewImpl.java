package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_CONSULT;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_EDIT;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

@SuppressWarnings("serial")
public class DisplayUserCredentialViewImpl extends BaseViewImpl implements DisplayUserCredentialView {

	public static final String PROPERTY_BUTTONS = "buttons";
	public static final String ADMIN = "admin";

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

	private Label jobTitleCaptionLabel;
	private Label jobTitleDisplayComponent;

	private Label phoneCaptionLabel;
	private Label phoneDisplayComponent;

	private Label faxCaptionLabel;
	private Label faxDisplayComponent;

	private Label addressCaptionLabel;
	private Label addressDisplayComponent;

	private Label personalEmailsCaptionLabel;
	private Label personalEmailsDisplayComponent;

	private Label collectionsCaptionLabel;
	private Label collectionsDisplayComponent;

	private BaseDisplay userCredentialDisplay;
	private Table userGlobalGroupTable;
	private Table availableGlobalGroupTable;
	private HorizontalLayout filterAndAddButtonLayoutUserGlobalGroups;
	private TableStringFilter tableFilterUserGlobalGroups;
	private HorizontalLayout filterAndAddButtonLayoutAvailableGlobalGroups;
	private TableStringFilter tableFilterAvailableGlobalGroups;
	private final int batchSize = 100;


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
			presenter.setUser(paramsMap.get("username"));
		}
		presenter.setParamsMap(paramsMap);
		presenter.setBreadCrumb(breadCrumb);
	}

	@Override
	protected String getTitle() {
		return $("DisplayUserCredentialView.viewTitle", userCredentialVO.getUsername());
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		viewLayout.setMargin(new MarginInfo(true));

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

		jobTitleCaptionLabel = new Label($("UserCredentialView.jobTitle"));
		jobTitleCaptionLabel.setId("jobTitle");
		jobTitleCaptionLabel.addStyleName("jobTitle");
		jobTitleDisplayComponent = new Label(userCredentialVO.getJobTitle());

		phoneCaptionLabel = new Label($("UserCredentialView.phone"));
		phoneCaptionLabel.setId("jobTitle");
		phoneCaptionLabel.addStyleName("jobTitle");
		phoneDisplayComponent = new Label(userCredentialVO.getPhone());

		faxCaptionLabel = new Label($("UserCredentialView.fax"));
		faxCaptionLabel.setId("jobTitle");
		faxCaptionLabel.addStyleName("jobTitle");
		faxDisplayComponent = new Label(userCredentialVO.getFax());

		addressCaptionLabel = new Label($("UserCredentialView.fax"));
		addressCaptionLabel.setId("jobTitle");
		addressCaptionLabel.addStyleName("jobTitle");
		addressDisplayComponent = new Label(userCredentialVO.getAddress());

		personalEmailsCaptionLabel = new Label($("UserCredentialView.personalEmails"));
		personalEmailsCaptionLabel.setId("personalEmails");
		personalEmailsCaptionLabel.addStyleName("email");
		if (userCredentialVO.getPersonalEmails() != null) {
			personalEmailsDisplayComponent = new Label(userCredentialVO.getPersonalEmails().replace("\n", "<br>"), ContentMode.HTML);
		} else {
			personalEmailsDisplayComponent = new Label(userCredentialVO.getPersonalEmails(), ContentMode.HTML);
		}


		collectionsCaptionLabel = new Label($("UserCredentialView.collections"));
		collectionsCaptionLabel.setId("collections");
		collectionsCaptionLabel.addStyleName("collections");
		collectionsDisplayComponent = new Label(userCredentialVO.getStringCollections());

		List<BaseDisplay.CaptionAndComponent> captionsAndComponents = new ArrayList<>();
		captionsAndComponents.add(new CaptionAndComponent(usernameCaptionLabel, usernameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(firstNameCaptionLabel, firstNameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(lastNameCaptionLabel, lastNameDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(emailCaptionLabel, emailDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(jobTitleCaptionLabel, jobTitleDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(phoneCaptionLabel, phoneDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(faxCaptionLabel, faxDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(addressCaptionLabel, addressDisplayComponent));
		captionsAndComponents.add(new CaptionAndComponent(personalEmailsCaptionLabel, personalEmailsDisplayComponent));
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

		viewLayout.addComponents(userCredentialDisplay, filterAndAddButtonLayoutUserGlobalGroups, userGlobalGroupTable,
				filterAndAddButtonLayoutAvailableGlobalGroups, availableGlobalGroupTable);

		viewLayout.setExpandRatio(userCredentialDisplay, 1);

		filterAndAddButtonLayoutUserGlobalGroups.addComponents(tableFilterUserGlobalGroups);
		filterAndAddButtonLayoutAvailableGlobalGroups.addComponents(tableFilterAvailableGlobalGroups);

		return viewLayout;
	}

	private Table buildUserGlobalGroupTable() {
		final GlobalGroupVODataProvider globalGroupVODataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> userGlobalGroupVOs = globalGroupVODataProvider.getGlobalGroupThatUserisIn(
				userCredentialVO.getUsername());
		globalGroupVODataProvider.setGlobalGroupVOs(userGlobalGroupVOs);
		Container container = new GlobalGroupVOLazyContainer(globalGroupVODataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addUsersGlobalGroupButtons(globalGroupVODataProvider, buttonsContainer);
		container = buttonsContainer;
		String title = $("DisplayUserCredentialView.listUsersGroups", userCredentialVO.getUsername());
		return buildTable(container, title);
	}

	private Table buildAvailableGlobalGroupTable() {
		final GlobalGroupVODataProvider globalGroupVODataProvider = presenter.getGlobalGroupVODataProvider();
		List<GlobalGroupVO> availableGlobalGroupVOs = globalGroupVODataProvider.getGlobalGroupThatUserisNotIn(
				userCredentialVO.getUsername());
		globalGroupVODataProvider.setGlobalGroupVOs(availableGlobalGroupVOs);
		Container container = new GlobalGroupVOLazyContainer(globalGroupVODataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
				Button addButton = new AddButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.addGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());
					}
				};
				addButton.setEnabled(canChangeGroupAssignment(entity));
				addButton.setVisible(canChangeGroupAssignment(entity));
				return addButton;
			}
		});
		container = buttonsContainer;
		String title = $("DisplayUserCredentialView.listGroups");
		return buildTable(container, title);
	}

	@Override
	public void partialRefresh() {
		refreshTable();
	}

	private void addUsersGlobalGroupButtons(final GlobalGroupVODataProvider globalGroupVODataProvider,
											ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
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
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
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
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());
					}
				};
				deleteButton.setEnabled(canChangeGroupAssignment(entity));
				deleteButton.setVisible(canChangeGroupAssignment(entity));
				return deleteButton;
			}
		});
	}

	private boolean canChangeGroupAssignment(GlobalGroupVO groupVO) {
		boolean canModifyAssignment = userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE
									  && presenter.canAddOrModify();
		boolean isBothSynced = userCredentialVO.getSyncMode() == UserSyncMode.SYNCED && !groupVO.isLocallyCreated();
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
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		return new RecordVOActionButtonFactory(presenter.getPageUserVO(), asList(USER_CONSULT.name(),
				USER_EDIT.name())).build();
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
	}

	@Override
	protected String getActionMenuBarCaption() {
		return null;
	}

	protected boolean alwaysUseLayoutForActionMenu() {
		return true;
	}

	protected List<Button> getQuickActionMenuButtons() {
		Button editFolderButton = new EditButton($("edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editFolderButtonClicked();
			}
		};
		return asList(editFolderButton);
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

		Table table = new BaseTable(getClass().getName(), title, container);
		int tableSize = batchSize;
		if (tableSize > table.getItemIds().size()) {
			tableSize = table.getItemIds().size();
		}
		table.setPageLength(tableSize);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		return table;
	}

	public DisplayUserCredentialPresenter getPresenter() {
		return presenter;
	}

	public String getBreadCrumb() {
		return presenter.getBreadCrumb();
	}
}