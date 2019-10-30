package com.constellio.app.ui.pages.management.schemaRecords;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import static com.constellio.app.ui.i18n.i18n.$;

public class SearchSchemaRecordsViewImpl extends BaseViewImpl implements SearchSchemaRecordsView {

	SearchSchemaRecordsPresenter presenter;
	VerticalLayout viewLayout;
	Table table;
	private HorizontalLayout searchLayout;

	public SearchSchemaRecordsViewImpl() {
		this.presenter = new SearchSchemaRecordsPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaRecordsView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());

		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		buildSearch();

		table = buildRecordsTables();
		viewLayout.addComponents(searchLayout, table);
		return viewLayout;
	}

	private Table buildRecordsTables() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
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

		RecordVOTable table = new RecordVOTable(
				$("ListSchemaRecordsView.tableTitle", dataProvider.size(), dataProvider.getSchema().getLabel(
						getSessionContext().getCurrentLocale())),
				recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 120);
		//		table.setPageLength(table.getItemIds().size());
		table.setCaption(table.getPageLength() + " " + table.getCaption());

		return table;
	}

	public void refreshTable() {
		Table newTable = buildRecordsTables();
		viewLayout.replaceComponent(table, newTable);
		table = newTable;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo().listSchemaRecords(presenter.getSchemaCode());
			}
		};
	}

	private void buildSearch() {
		searchLayout = new HorizontalLayout();
		final TextField searchField = new BaseTextField();
		searchField.focus();
		searchField.setValue(presenter.getQueryExpression());
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
}
