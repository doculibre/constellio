package com.constellio.app.ui.pages.management.valueDomains;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisableButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.EnableButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListValueDomainRecordsViewImpl extends BaseViewImpl implements ListValueDomainRecordsView {

	ListValueDomainRecordsPresenter presenter;
	VerticalLayout viewLayout;
	TabSheet tables;
	private HorizontalLayout searchAndAddButtonLayout;
	private HorizontalLayout searchLayout;

	public ListValueDomainRecordsViewImpl() {
		this.presenter = new ListValueDomainRecordsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaRecordsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		final String schemaName = event.getParameters();
		presenter.forSchema(schemaName);

		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		buildSearch();
		searchAndAddButtonLayout = new HorizontalLayout();
		searchAndAddButtonLayout.setWidth("100%");
		searchAndAddButtonLayout.addComponents(searchLayout);
		searchAndAddButtonLayout.setSpacing(true);

		tables = buildRecordsTables();
		viewLayout.addComponents(searchAndAddButtonLayout, tables);
		return viewLayout;
	}

	private TabSheet buildRecordsTables() {
		TabSheet tabSheet = new TabSheet();
		Table activeRecordsTable = buildRecordsTable(true);
		Table inactiveRecordsTable = buildRecordsTable(false);

		VerticalLayout activeLayout = new VerticalLayout();
		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addLinkClicked();
			}
		};
		activeLayout.addComponent(addButton);
		activeLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		activeLayout.addComponent(activeRecordsTable);

		VerticalLayout inactiveLayout = new VerticalLayout();
		Button deleteAllUnused = new DeleteButton($("ListValueDomainRecordsViewImpl.deleteAllUnused")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteAllUnused();
			}
		};
		inactiveLayout.addComponent(deleteAllUnused);
		inactiveLayout.setComponentAlignment(deleteAllUnused, Alignment.TOP_RIGHT);
		inactiveLayout.addComponent(inactiveRecordsTable);

		int size = activeRecordsTable.size();

		tabSheet.addTab(activeLayout, $("ListValueDomainRecordsViewImpl.actives", activeRecordsTable.size()));
		tabSheet.addTab(inactiveLayout, $("ListValueDomainRecordsViewImpl.inactives", inactiveRecordsTable.size()));

		return tabSheet;
	}

	private Table buildRecordsTable(boolean active) {
		final RecordVODataProvider dataProvider = presenter.getDataProvider(active);
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
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

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});

		if (active) {
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new DisableButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							Integer index = (Integer) itemId;
							RecordVO entity = dataProvider.getRecordVO(index);
							presenter.disableButtonClick(entity);
						}
					};
				}
			});
		} else {
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new EnableButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							Integer index = (Integer) itemId;
							RecordVO entity = dataProvider.getRecordVO(index);
							presenter.enableButtonClick(entity);
						}
					};
				}
			});
		}

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				DeleteButton deleteButton = new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						Integer index = (Integer) itemId;
						RecordVO entity = dataProvider.getRecordVO(index);
						presenter.deleteButtonClicked(entity);
					}
				};
				return deleteButton;
			}
		});
		recordsContainer = buttonsContainer;

		RecordVOTable table = new RecordVOTable($("ListSchemaRecordsView.tableTitle", dataProvider.size()), recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 160);
		table.setPageLength(Math.min(15, dataProvider.size()));

		return table;
	}

	public void refreshTables() {
		TabSheet newTables = buildRecordsTables();

		viewLayout.replaceComponent(tables, newTables);
		newTables.setSelectedTab(tables.getTabIndex());
		tables = newTables;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().listValueDomains();
			}
		};
	}

	private void buildSearch() {
		searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		final TextField searchField = new BaseTextField();
		searchField.focus();
		searchField.setNullRepresentation("");
		Button searchButton = new SearchButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.search(searchField.getValue());
			}
		};
		searchLayout.addComponents(searchField, searchButton);

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.search(searchField.getValue());
			}
		};
		onEnterHandler.installOn(searchField);
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Arrays.asList(BreadcrumbTrailUtil.valueDomain());
			}
		};
	}
}