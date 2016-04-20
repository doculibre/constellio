package com.constellio.app.ui.pages.globalGroup;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.vaadin.ui.*;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.GlobalGroupVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.TableStringFilter;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.GlobalGroupVOLazyContainer;
import com.constellio.app.ui.framework.data.GlobalGroupVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import java.util.List;

public class ListGlobalGroupsViewImpl extends BaseViewImpl implements ListGlobalGroupsView {

	private ListGlobalGroupsPresenter presenter;
	private static final String PROPERTY_BUTTONS = "buttons";
	private VerticalLayout viewLayout;
	private Table table;
	private HorizontalLayout filterAndAddButtonLayout;
	private TableStringFilter tableFilter;
	private GlobalGroupStatus status;
	private final int batchSize = 100;
	private TabSheet sheet;
    public static final String AJOUTER = "Ajouter";


	public ListGlobalGroupsViewImpl() {
		this.presenter = new ListGlobalGroupsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListGlobalGroupsView.viewTitle");
	}

    @Override
    protected List<Button> buildActionMenuButtons(ViewChangeEvent event){
        List<Button> buttons = super.buildActionMenuButtons(event);
        buttons.add(new AddButton(AJOUTER) {
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

		table = buildTable(GlobalGroupStatus.ACTIVE);

		filterAndAddButtonLayout = new HorizontalLayout();
		filterAndAddButtonLayout.setWidth("100%");

		tableFilter = new TableStringFilter(table);

		OptionGroup statusFilter = new OptionGroup();
		statusFilter.addStyleName("horizontal");
		statusFilter.addStyleName("status");
		for (GlobalGroupStatus status : GlobalGroupStatus.values()) {
			statusFilter.addItem(status);
			statusFilter.setItemCaption(status, $("GlobalGroupView.status." + status));
			if (this.status == null) {
				this.status = GlobalGroupStatus.ACTIVE;
				statusFilter.setValue(GlobalGroupStatus.ACTIVE);
			} else if (status == this.status) {
				statusFilter.setValue(status);
			}
		}
		statusFilter.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				status = (GlobalGroupStatus) event.getProperty().getValue();
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

    private GlobalGroupStatus getTAbId(String selectedSheet) {
        switch (selectedSheet){
            case "inactive":
                return GlobalGroupStatus.INACTIVE;
            default:
                return GlobalGroupStatus.ACTIVE;
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

	private Table buildTable(GlobalGroupStatus status) {
		final GlobalGroupVODataProvider dataProvider = presenter.getDataProvider();
		dataProvider.setGlobalGroupVOs(dataProvider.listBaseGlobalGroupsVOsWithStatus(status));
		Container container = new GlobalGroupVOLazyContainer(dataProvider, batchSize);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, PROPERTY_BUTTONS);
		addButtons(dataProvider, buttonsContainer);
		container = buttonsContainer;

		Table table = new RecordVOTable($("ListGlobalGroupsView.viewTitle", dataProvider.size()), container);
        table.setWidth("100%");
        table.setColumnHeader("code", $("ListGlobalGroupsView.codeColumn"));
        table.setColumnHeader("name", $("ListGlobalGroupsView.nameColumn"));
        table.setColumnHeader(PROPERTY_BUTTONS, "");
        table.setColumnWidth(PROPERTY_BUTTONS, 120);
        table.setPageLength(Math.min(15, dataProvider.size()));
		return table;
	}

	private void addButtons(final GlobalGroupVODataProvider provider, ButtonsContainer buttonsContainer) {
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
						presenter.displayButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
				final GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
				Button editButton = new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(entity);

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
				final GlobalGroupVO entity = getGlobalGroupVO((Integer) itemId, provider);
				Button deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(entity);
					}
				};
				deleteButton.setVisible(entity.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				deleteButton.setEnabled(entity.getStatus() == GlobalGroupStatus.ACTIVE && presenter.canAddOrModify());
				return deleteButton;
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
		filterAndAddButtonLayout.replaceComponent(tableFilter, newTableFilter);
		tableFilter = newTableFilter;
	}

	private GlobalGroupVO getGlobalGroupVO(Integer itemId, GlobalGroupVODataProvider provider) {
		Integer index = itemId;
		return provider.getGlobalGroupVO(index);
	}

}