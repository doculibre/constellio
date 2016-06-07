package com.constellio.app.ui.pages.trash;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
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
	private static final Resource RELATED_RESOURCE = new ThemeResource("images/commun/relation.gif");
	private final TrashPresenter presenter;
	private final RecordVODataProvider dataProvider;

	public TrashRecordsTable(RecordVODataProvider dataProvider, final TrashPresenter presenter) {
		super("", new ButtonsContainer(new RecordVOLazyContainer(dataProvider), "buttons"));
		this.presenter = presenter;
		this.dataProvider = presenter.getTrashRecords();

		ButtonsContainer withButtons = (ButtonsContainer) getContainerDataSource();
		withButtons.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
			protected Button newButtonInstance(Object itemId) {
				final Integer index = (Integer) itemId;
				return new WindowButton(RELATED_RESOURCE, "related records", true,
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

	public class TrashStyleGenerator implements CellStyleGenerator {
		private static final String ERROR_STYLE = "redText";

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
