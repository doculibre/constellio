package com.constellio.app.modules.es.ui.pages;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.Metadata;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class ListConnectorInstancesViewImpl extends BaseViewImpl implements ListConnectorInstancesView {

	private ListConnectorInstancesPresenter presenter;
	private VerticalLayout viewLayout;
	private Table table;
	private Button addButton;

	public ListConnectorInstancesViewImpl() {
		this.presenter = new ListConnectorInstancesPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListConnectorInstancesView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		table = buildRecordsTables();
		viewLayout.addComponents(addButton, table);
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Table buildRecordsTables() {
		final RecordVODataProvider dataProvider = presenter.getDataProvider();
		Container recordsContainer = new RecordVOLazyContainer(dataProvider);
		for(Metadata toRemove: presenter.columnToRemove()) {
			recordsContainer.removeContainerProperty(toRemove);
		}
		ButtonsContainer buttonsContainer = new ButtonsContainer(recordsContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
			protected Button newButtonInstance(final Object itemId) {
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
//		buttonsContainer.addButton(new ContainerButton() {
//			@Override
//			protected Button newButtonInstance(final Object itemId) {
//				return new EditSchemaButton() {
//					@Override
//					protected void buttonClick(ClickEvent event) {
//						Integer index = (Integer) itemId;
//						RecordVO entity = dataProvider.getRecordVO(index);
//						presenter.editSchemasButtonClicked(entity);
//					}
//				};
//			}
//		});
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
		recordsContainer = buttonsContainer;

		RecordVOTable table = new RecordVOTable(
				$("ListConnectorInstancesView.tableTitle", dataProvider.size(), dataProvider.getSchema().getLabel(
						getSessionContext().getCurrentLocale())),
				recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnHeader(ConnectorInstance.CODE, $("code"));
		table.setColumnHeader(ConnectorInstance.CONNECTOR_TYPE, $("connectorType"));
		table.setColumnHeader(ConnectorInstance.ENABLED, $("enable"));
		//table.setColumnHeader(ConnectorInstance.TRAVERSAL_CODE, $("traversalCode"));
		table.setColumnHeader(ConnectorInstance.LAST_TRAVERSAL_ON, $("lastTraversalOn"));
		table.setColumnHeader("documentsCount", $("documentsCount"));
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 200);
		table.setColumnWidth("buttons", 200);
		table.setPageLength(table.getItemIds().size());
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
				navigateTo().adminModule();
			}
		};
	}

}
