package com.constellio.app.ui.pages.user;

import com.constellio.app.ui.entities.UserCredentialVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.UserCredentialVOLazyContainer;
import com.constellio.app.ui.framework.data.UserCredentialVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListUsersCredentialsViewImpl extends BaseViewImpl implements ListUsersCredentialsView {
	public static final String ADMIN = "admin";
	public static final String PENDING = "pending";
	public static final String SUSPENDED = "suspended";
	public static final String DELETED = "deleted";

	private ListUserCredentialsPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;
	private TabSheet sheet;

	private HorizontalLayout filterAndAddButtonLayout;
	private TableStringFilter tableFilter;
	private Table table;
	private UserCredentialStatus status;
	private final int batchSize = 100;

	public ListUsersCredentialsViewImpl() {
		this.presenter = new ListUserCredentialsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListUserCredentialsView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(new AddButton($("add")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		});
		return buttons;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);

		filterAndAddButtonLayout = new HorizontalLayout();
		filterAndAddButtonLayout.setWidth("100%");

		table = buildTable(UserCredentialStatus.ACTIVE);
		tableFilter = new TableStringFilter(table);
		OptionGroup statusFilter = new OptionGroup();
		statusFilter.addStyleName("horizontal");
		statusFilter.addStyleName("status");
		for (UserCredentialStatus status : UserCredentialStatus.values()) {
			statusFilter.addItem(status);
			statusFilter.setItemCaption(status, $("UserCredentialView.status." + status.getCode()));
			if (this.status == null) {
				statusFilter.setValue(UserCredentialStatus.ACTIVE);
			} else if (status == this.status) {
				statusFilter.setValue(status);
			}
		}
		statusFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				status = (UserCredentialStatus) event.getProperty().getValue();
				refreshTable();
			}
		});

		sheet = new TabSheet();
		sheet.setSizeFull();
		sheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				String selectedSheet = sheet.getSelectedTab().getId();
				status = getTAbId(selectedSheet);
				refreshTable();
			}
		});

		for (String tabId : presenter.getTabs()) {
			sheet.addTab(buildEmptyTab(tabId));
		}

		viewLayout.addComponents(sheet, tableFilter, table);
		viewLayout.setExpandRatio(table, 1);
		viewLayout.setComponentAlignment(tableFilter, Alignment.TOP_RIGHT);

		return viewLayout;
	}

	private UserCredentialStatus getTAbId(String selectedSheet) {
		switch (selectedSheet) {
			case PENDING:
				return UserCredentialStatus.PENDING;
			case SUSPENDED:
				return UserCredentialStatus.SUSPENDED;
			case DELETED:
				return UserCredentialStatus.DISABLED;
			default:
				return UserCredentialStatus.ACTIVE;
		}
	}

	private VerticalLayout buildEmptyTab(String tabId) {
		VerticalLayout tab = new VerticalLayout();
		tab.setCaption(presenter.getTabCaption(tabId));
		tab.setId(tabId);
		tab.setSpacing(true);
		return tab;
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

	Table buildTable(UserCredentialStatus status) {
		final UserCredentialVODataProvider dataProvider = presenter.getDataProvider();

		List<UserCredentialVO> userCredentialVOs = dataProvider.listUserCredentialVOsWithStatus(status);
		dataProvider.setUserCredentialVOs(userCredentialVOs);

		Filterable tableContainer = new UserCredentialVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(tableContainer, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		tableContainer = buttonsContainer;

		Table table = new RecordVOTable($("ListUserCredentialsView.viewTitle", dataProvider.size()), tableContainer) {
			@Override
			protected String getTableId() {
				return ListUsersCredentialsViewImpl.class.getName();
			}
		};
		table.setWidth("100%");
		table.setColumnHeader("username", $("ListUsersCredentialsView.usernameColumn"));
		table.setColumnHeader("firstName", $("ListUsersCredentialsView.firstNameColumn"));
		table.setColumnHeader("lastName", $("ListUsersCredentialsView.lastNameColumn"));
		table.setColumnHeader("email", $("ListUsersCredentialsView.emailColumn"));
		table.setColumnHeader(PROPERTY_BUTTONS, "");
		table.setColumnWidth(PROPERTY_BUTTONS, 120);
		table.setPageLength(Math.min(10, dataProvider.size()));
		return table;
	}

	private void addButtons(final UserCredentialVODataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						UserCredentialVO entity = getUserCredentialVO((Integer) itemId, provider);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				final UserCredentialVO entity = getUserCredentialVO((Integer) itemId, provider);
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(entity);

					}
				};
				if (ADMIN.equals(entity.getUsername())) {
					editButton.setEnabled(presenter.canModifyPassword(entity.getUsername()));
					editButton.setVisible(presenter.canModifyPassword(entity.getUsername()));
				} else {
					editButton.setEnabled(presenter.canAddOrModify());
					editButton.setVisible(presenter.canAddOrModify());
				}
				return editButton;
			}
		});
	}

	public void refreshTable() {
		Table newTable = buildTable(status);
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
		refreshFilter();
	}

	private void refreshFilter() {
		TableStringFilter newTableFilter = new TableStringFilter(table);
		viewLayout.replaceComponent(tableFilter, newTableFilter);
		tableFilter = newTableFilter;
	}

	private UserCredentialVO getUserCredentialVO(Integer itemId, UserCredentialVODataProvider provider) {
		Integer index = itemId;
		return provider.getUserCredentialVO(index);
	}
}
