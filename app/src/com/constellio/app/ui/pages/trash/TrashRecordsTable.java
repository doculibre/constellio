package com.constellio.app.ui.pages.trash;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.SelectionTableAdapter;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class TrashRecordsTable extends SelectionTableAdapter {

	private static final Resource RELATED_RESOURCE = new ThemeResource("images/commun/warning.png");
	public static final String TRASH_BUTTONS = "buttons";

	private final TrashPresenter presenter;
	private final RecordVODataProvider dataProvider;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public TrashRecordsTable(RecordVODataProvider dataProvider, final TrashPresenter presenter) {
		super();
		this.presenter = presenter;
		this.dataProvider = dataProvider;

		RecordVOTable table = new RecordVOTable() {
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

			@Override
			public boolean isSelectColumn() {
				return true;
			}

			@Override
			public boolean addGeneratedSelectColumn() {
				return false;
			}

			@Override
			public boolean isButtonsColumn() {
				return true;
			}

			@Override
			public boolean isContextMenuPossible() {
				return false;
			}
		};

		ButtonsContainer<?> dataSource = new ButtonsContainer(new RecordVOLazyContainer(dataProvider), TRASH_BUTTONS);
		table.setColumnCollapsingAllowed(true);
		table.setContainerDataSource(dataSource);

		dataSource.addButton(new ContainerButton() {
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
		dataSource.addButton(new ContainerButton() {
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

		table.setPageLength(Math.min(15, dataProvider.size()));
		table.setSizeFull();

		table.setColumnHeader(TRASH_BUTTONS, "");
		table.setCellStyleGenerator(new TrashStyleGenerator());
		setTable(table);
	}

	public class TrashStyleGenerator implements Table.CellStyleGenerator {
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

	@Override
	public boolean isAllItemsSelected() {
		return presenter.isAllItemsSelected();
	}

	@Override
	public boolean isAllItemsDeselected() {
		return presenter.isAllItemsDeselected();
	}

	@Override
	public void selectAll() {
		presenter.selectAllClicked();
	}

	@Override
	public void deselectAll() {
		presenter.deselectAllClicked();
	}

	@Override
	public boolean isSelected(Object itemId) {
		return presenter.isRecordSelected(dataProvider.getRecordVO((Integer) itemId));
	}

	@Override
	public void setSelected(Object itemId, boolean selected) {
		Integer index = (Integer) itemId;
		RecordVO entity = dataProvider.getRecordVO(index);
		presenter.recordSelectionChanged(entity, selected);
	}

}
