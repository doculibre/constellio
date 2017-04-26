package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.menuBar.ConfirmDialogMenuBarItemCommand;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.vaadin.data.Container;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;

public class TaskTable extends RecordVOTable {
	public static final String PREFIX = "images/icons/task/";
	public static final ThemeResource COMPLETE_ICON = new ThemeResource(PREFIX + "task.png");
	public static final ThemeResource CLOSE_ICON = new ThemeResource(PREFIX + "task_complete.png");

	private final TaskPresenter presenter;

	public TaskTable(RecordVODataProvider provider, TaskPresenter presenter) {
		super($("TaskTable.caption", provider.size()));
		this.presenter = presenter;
		setContainerDataSource(buildContainer(provider));
		setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 200);
		setCellStyleGenerator(new TaskStyleGenerator());
		setPageLength(Math.min(15, provider.size()));
		setWidth("100%");
	}

	private Container buildContainer(RecordVODataProvider provider) {
		return addButtons(new RecordVOLazyContainer(provider));
	}

	private Container addButtons(final RecordVOLazyContainer records) {
		String columnId = "menuBar";
		addGeneratedColumn(columnId, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final RecordVO recordVO = records.getRecordVO((int) itemId);
				MenuBar menuBar = new MenuBar();
				MenuItem rootItem = menuBar.addItem("", FontAwesome.BARS, null);
				
				rootItem.addItem($("display"), DisplayButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.displayButtonClicked(recordVO);
					}
				});
				
				if (presenter.isEditButtonEnabled(recordVO)) {
					rootItem.addItem($("edit"), EditButton.ICON_RESOURCE, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.editButtonClicked(recordVO);
						}
					});
				}
				
				if (presenter.isCompleteButtonEnabled(recordVO)) {
					rootItem.addItem($("TaskTable.complete"), COMPLETE_ICON, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.completeButtonClicked(recordVO);
						}
					});
				}
				
				if (presenter.isCloseButtonEnabled(recordVO)) {
					rootItem.addItem($("TaskTable.close"), CLOSE_ICON, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.closeButtonClicked(recordVO);
						}
					});
				}
				
				if (presenter.isDeleteButtonVisible(recordVO)) {
					rootItem.addItem($("delete"), DeleteButton.ICON_RESOURCE, new ConfirmDialogMenuBarItemCommand() {
						@Override
						protected String getConfirmDialogMessage() {
							return $("ConfirmDialog.confirmDelete");
						}

						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							presenter.deleteButtonClicked(recordVO);
						}
					}).setEnabled(presenter.isDeleteButtonEnabled(recordVO));
				}
				
				return menuBar;
			}
		});
		setColumnHeader(columnId, "");
		return records;
	}

	public interface TaskPresenter {
		
		void displayButtonClicked(RecordVO record);

		void editButtonClicked(RecordVO record);

		void deleteButtonClicked(RecordVO record);

		void completeButtonClicked(RecordVO record);

		void closeButtonClicked(RecordVO record);

		boolean isTaskOverdue(TaskVO taskVO);

		boolean isFinished(TaskVO taskVO);

		void autoAssignButtonClicked(RecordVO recordVO);

		boolean isAutoAssignButtonEnabled(RecordVO recordVO);

		boolean isEditButtonEnabled(RecordVO recordVO);

		boolean isCompleteButtonEnabled(RecordVO recordVO);

		boolean isCloseButtonEnabled(RecordVO recordVO);

		boolean isDeleteButtonEnabled(RecordVO recordVO);
		
		boolean isDeleteButtonVisible(RecordVO recordVO);
		
	}

	public class TaskStyleGenerator implements CellStyleGenerator {
		private static final String OVER_DUE_TASK_STYLE = "error";
		private static final String FINISHED_TASK_STYLE = "disabled";

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			String style;
			if (!isDueDateColumn(propertyId)) {
				style = null;
			} else {
				RecordVOItem item = (RecordVOItem) source.getItem(itemId);
				TaskVO taskVO = new TaskVO(item.getRecord());
				if (presenter.isFinished(taskVO)) {
					style = FINISHED_TASK_STYLE;
				} else if (presenter.isTaskOverdue(taskVO)) {
					style = OVER_DUE_TASK_STYLE;
				} else {
					style = null;
				}
			}
			return style;
		}

		private boolean isDueDateColumn(Object propertyId) {
			if (!(propertyId instanceof MetadataVO)) {
				return false;
			}
			MetadataVO metadata = (MetadataVO) propertyId;
			return Task.DUE_DATE.equals(MetadataVO.getCodeWithoutPrefix(metadata.getCode()));
		}
	}
}
