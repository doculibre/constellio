package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.BaseDisplay.CaptionAndComponent;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.vaadin.data.Container;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

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
		List<GlobalGroupVO> userGlobalGroupVOs = globalGroupVODataProvider.listActiveGlobalGroupVOsFromUser(
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
		List<GlobalGroupVO> availableGlobalGroupVOs = globalGroupVODataProvider.listGlobalGroupVOsNotContainingUser(
				userCredentialVO.getUsername());
		globalGroupVODataProvider.setGlobalGroupVOs(availableGlobalGroupVOs);
		Container container = new GlobalGroupVOLazyContainer(globalGroupVODataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button addButton = new AddButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.addGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());

					}
				};
				addButton.setEnabled(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
				addButton.setVisible(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
				return addButton;
			}
		});
		container = buttonsContainer;
		String title = $("DisplayUserCredentialView.listGroups");
		return buildTable(container, title);
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
				editButton.setEnabled(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
				editButton.setVisible(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
				return editButton;
			}
		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						GlobalGroupVO entity = globalGroupVODataProvider.getGlobalGroupVO(index);
						presenter.deleteGlobalGroupButtonClicked(userCredentialVO.getUsername(), entity.getCode());
					}
				};
				deleteButton
						.setEnabled(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
				deleteButton
						.setVisible(userCredentialVO.getStatus() == UserCredentialStatus.ACTIVE && presenter.canAddOrModify());
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
		Button editButton = new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked(userCredentialVO);
			}
		};
		actionMenuButtons.add(editButton);

		editButton.setEnabled(presenter.canAddOrModify());

		Button serviceKeyTokenButton = buildServiceKeyAndTokenButton();
		actionMenuButtons.add(serviceKeyTokenButton);
		//


		//		return actionMenuButtons;
		return new RecordVOActionButtonFactory(userCredentialVO).build();
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

	private Button buildServiceKeyAndTokenButton() {
		return new WindowButton($("DisplayUserCredentialView.generateTokenButton"),
				$("DisplayUserCredentialView.generateToken")) {
			@Override
			protected Component buildWindowContent() {

				//				final BaseIntegerField durationField = new BaseIntegerField($("DisplayUserCredentialView.Duration"));
				final TextField durationField = new TextField($("DisplayUserCredentialView.Duration"));

				final ComboBox unitTimeCombobox = new BaseComboBox();
				unitTimeCombobox.setNullSelectionAllowed(false);
				unitTimeCombobox.setCaption($("DisplayUserCredentialView.unitTime"));
				unitTimeCombobox.addItem("hours");
				unitTimeCombobox.setItemCaption("hours", $("DisplayUserCredentialView.hours"));
				unitTimeCombobox.setValue("hours");
				unitTimeCombobox.addItem("days");
				unitTimeCombobox.setItemCaption("days", $("DisplayUserCredentialView.days"));

				HorizontalLayout horizontalLayoutFields = new HorizontalLayout();
				horizontalLayoutFields.setSpacing(true);
				horizontalLayoutFields.addComponents(durationField, unitTimeCombobox);

				//
				final Label label = new Label($("DisplayUserCredentialView.serviceKey"));
				final Label labelValue = new Label(presenter.getServiceKey(userCredentialVO.getUsername()));
				final HorizontalLayout horizontalLayoutServiceKey = new HorizontalLayout();
				horizontalLayoutServiceKey.setSpacing(true);
				horizontalLayoutServiceKey.addComponents(label, labelValue);

				final Label tokenLabel = new Label($("DisplayUserCredentialView.token"));
				final Label tokenValue = new Label();
				final HorizontalLayout horizontalLayoutToken = new HorizontalLayout();
				horizontalLayoutToken.setSpacing(true);
				horizontalLayoutToken.addComponents(tokenLabel, tokenValue);

				final Link linkTest = new Link($("DisplayUserCredentialView.test"), new ExternalResource(""));
				linkTest.setTargetName("_blank");

				final VerticalLayout verticalLayoutGenerateValues = new VerticalLayout();
				verticalLayoutGenerateValues
						.addComponents(horizontalLayoutServiceKey, horizontalLayoutToken, linkTest);
				verticalLayoutGenerateValues.setSpacing(true);
				verticalLayoutGenerateValues.setVisible(false);

				final BaseButton generateTokenButton = new BaseButton($("DisplayUserCredentialView.generateToken")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						int durationValue;
						try {
							if (durationField.getValue() != null) {
								durationValue = Integer.valueOf(durationField.getValue());
								String serviceKey = presenter.getServiceKey(userCredentialVO.getUsername());
								labelValue.setValue(serviceKey);
								String token = presenter
										.generateToken(userCredentialVO.getUsername(), (String) unitTimeCombobox.getValue(),
												durationValue);
								tokenValue.setValue(token);
								String constellioUrl = presenter.getConstellioUrl();
								String linkValue = constellioUrl + "select?token=" + token + "&serviceKey=" + serviceKey
												   + "&fq=-type_s:index" + "&q=*:*";
								linkTest.setResource(new ExternalResource(linkValue));

								verticalLayoutGenerateValues.setVisible(true);
							}
						} catch (Exception e) {
						}
					}
				};
				generateTokenButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				generateTokenButton.setEnabled(false);

				durationField.addTextChangeListener(new TextChangeListener() {
					@Override
					public void textChange(TextChangeEvent event) {
						enableOrDisableButton(event.getText(), generateTokenButton);
					}
				});

				//
				VerticalLayout mainVerticalLayout = new VerticalLayout();
				mainVerticalLayout
						.addComponents(horizontalLayoutFields, generateTokenButton, verticalLayoutGenerateValues);
				mainVerticalLayout.setSpacing(true);

				return mainVerticalLayout;
			}
		};
	}

	private void enableOrDisableButton(String value, BaseButton generateTokenButton) {
		boolean enable = false;
		if (value != null) {
			int durationValue;
			try {
				durationValue = Integer.valueOf(value);
				if (durationValue > 0) {
					enable = true;
				}
			} catch (NumberFormatException e) {
			}
		}
		generateTokenButton.setEnabled(enable);
	}

	public DisplayUserCredentialPresenter getPresenter() {
		return presenter;
	}

	public String getBreadCrumb() {
		return presenter.getBreadCrumb();
	}
}