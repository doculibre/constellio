package com.constellio.app.ui.pages.trash;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Property;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class TrashRecordsTable extends RecordVOTable {
	
	private static final Resource RELATED_RESOURCE = new ThemeResource("images/commun/warning.png");
	private final TrashPresenter presenter;
	private final RecordVODataProvider dataProvider;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TrashRecordsTable(RecordVODataProvider dataProvider, final TrashPresenter presenter) {
		super("");
		this.presenter = presenter;
		this.dataProvider = dataProvider;

		setColumnCollapsingAllowed(true);
		setContextMenuPossible(false);
		setContainerDataSource(new ButtonsContainer(new RecordVOLazyContainer(dataProvider), "buttons"));

		ButtonsContainer<?> withButtons = (ButtonsContainer<?>) getContainerDataSource();
		withButtons.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = TrashRecordsTable.this.dataProvider.getRecordVO(index);
						TrashRecordsTable.this.presenter.displayButtonClicked(entity);
					}
				};
			}
		});
		withButtons.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(Object itemId, ButtonsContainer<?> container) {
				final Integer index = (Integer) itemId;
				WindowButton relatedRecordsWindow = new WindowButton(RELATED_RESOURCE, $("TrashRecordsTable.relatedRecords"), true,
						WindowConfiguration.modalDialog("50%", "50%")) {
					@Override
					protected Component buildWindowContent() {
						VerticalLayout verticalLayout = new VerticalLayout();
						Label label = new Label(TrashRecordsTable.this.presenter
								.getRelatedRecordsMessage(TrashRecordsTable.this.dataProvider.getRecordVO(index)));
						verticalLayout.addComponent(label);
						return verticalLayout;
					}
				};
				relatedRecordsWindow.setEnabled(TrashRecordsTable.this.presenter
						.physicalDeleteFailed(TrashRecordsTable.this.dataProvider.getRecordVO(index)));
				return relatedRecordsWindow;
			}
		});

		addGeneratedColumn("checkbox", new Table.ColumnGenerator() {
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final CheckBox cb = new CheckBox();
				cb.setValue(TrashRecordsTable.this.presenter
						.isRecordSelected(TrashRecordsTable.this.dataProvider.getRecordVO((Integer) itemId)));
				cb.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(Property.ValueChangeEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = TrashRecordsTable.this.dataProvider.getRecordVO(index);
						TrashRecordsTable.this.presenter.recordToggled(entity);
					}
				});
				return cb;
			}
		});

		setPageLength(Math.min(15, dataProvider.size()));
		setSizeFull();
		List<Object> checkBoxAsFirstColumn = new ArrayList<>();
		checkBoxAsFirstColumn.add("checkbox");
		for (Object visibleItem : getVisibleColumns()) {
			if (!visibleItem.equals("checkbox")) {
				checkBoxAsFirstColumn.add(visibleItem);
			}
		}
		setVisibleColumns(checkBoxAsFirstColumn.toArray());
		setColumnHeader("checkbox", "");
		setColumnHeader("buttons", "");
		setCellStyleGenerator(new TrashStyleGenerator());
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		return new RecordVOTableColumnsManager() {
			@Override
			public void manage(Table table, String tableId) {
				super.manage(table, tableId);
				setColumnCollapsed(Schemas.MODIFIED_ON.getLocalCode(), true);
				setColumnCollapsed(Schemas.LOGICALLY_DELETED_ON.getLocalCode(), false);
			}
		};
	}

	public class TrashStyleGenerator implements CellStyleGenerator {
		private static final String ERROR_STYLE = "textRed";

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			String style;
			RecordVOItem item = (RecordVOItem) source.getItem(itemId);
			RecordVO recordVO = item.getRecord();
			if (presenter.physicalDeleteFailed(recordVO)) {
				style = ERROR_STYLE;
			} else {
				style = null;
			}
			return style;
		}
	}
}
