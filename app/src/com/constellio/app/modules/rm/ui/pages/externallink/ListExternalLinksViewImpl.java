package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.List;
import java.util.Map.Entry;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListExternalLinksViewImpl extends BaseViewImpl implements ListExternalLinksView {
	private final ListExternalLinksPresenter presenter;

	private TabSheet tabSheet;

	public ListExternalLinksViewImpl() {
		presenter = new ListExternalLinksPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	protected String getTitle() {
		return $("ListExternalLinksView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};
		mainLayout.addComponents(addButton);
		mainLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);

		tabSheet = new TabSheet();
		mainLayout.addComponent(tabSheet);

		refreshTables();

		return mainLayout;
	}

	public void addSource(ExternalLinkSource source) {
		presenter.addSource(source);
	}

	// TODO::JOLA --> Set default column (Title, Type, Action)
	private Component buildTable(RecordVODataProvider dataProvider) {
		ButtonsContainer recordsContainer = new ButtonsContainer<>(new RecordVOLazyContainer(dataProvider), "buttons");
		/*recordsContainer.addButton(new ContainerButton() {
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
		});*/
		recordsContainer.addButton(new ContainerButton() {
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

		RecordVOTable table = new RecordVOTable($(dataProvider.getSchema().getLabel(), dataProvider.getSchema().getCode()), recordsContainer);
		table.setWidth("100%");
		table.setColumnHeader("buttons", "");
		table.setColumnWidth(dataProvider.getSchema().getCode() + "_id", 120);
		table.setColumnWidth("buttons", 40);
		table.setColumnCollapsible("buttons", false);
		table.setColumnExpandRatio(dataProvider.getSchema().getCode() + "_" + Schemas.TITLE_CODE, 1.0f);
		table.setPageLength(Math.min(15, dataProvider.size()));
		table.sort();

		return table;
	}

	public void refreshTables() {
		tabSheet.removeAllComponents();

		for (ExternalLinkSource source : presenter.getSources()) {
			for (Entry<String, List<String>> tab : source.getTabs()) {
				if (presenter.hasResults(tab.getValue())) {
					tabSheet.addTab(buildTable(presenter.getDataProvider(tab.getValue())), tab.getKey());
				}
			}
		}
	}
}
