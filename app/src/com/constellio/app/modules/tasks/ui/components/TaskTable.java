package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.TaskCompleteWindowButton;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.menuBar.ConfirmDialogMenuBarItemCommand;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseView;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		addStyleName("task-table");

		addDisplayOnClickListener();
	}

	protected void addDisplayOnClickListener() {
		this.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Item item = event.getItem();
				RecordVO recordVO = null;
				if (item instanceof RecordVO) {
					recordVO = (RecordVO) item;
				} else if (item instanceof RecordVOItem) {
					recordVO = ((RecordVOItem) item).getRecord();
				}

				if (recordVO != null) {
					displayTask(recordVO);
				}
			}
		});
	}

	@Override
	protected String getTitleColumnStyle(RecordVO recordVO) {
		return super.getTitleColumnStyle(recordVO);
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
				MenuBar menuBar = new BaseMenuBar();
				MenuItem rootItem = menuBar.addItem("", FontAwesome.BARS, null);

				rootItem.addItem($("display"), DisplayButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						displayTask(recordVO);
					}
				});

				if (presenter.isEditButtonEnabled(recordVO)) {
					rootItem.addItem($("edit"), EditButton.ICON_RESOURCE, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							editTask(recordVO);
						}
					});
				}

				if (presenter.isReadByUser(recordVO)) {
					rootItem.addItem($("TaskTable.markAsUnread"), EditButton.ICON_RESOURCE, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.setReadByUser(recordVO, false);
						}
					});
				} else {
					rootItem.addItem($("TaskTable.markAsRead"), EditButton.ICON_RESOURCE, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.setReadByUser(recordVO, true);
						}
					});
				}

				if (presenter.isCompleteButtonEnabled(recordVO)) {
					rootItem.addItem($("TaskTable.complete"), COMPLETE_ICON, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							TaskCompleteWindowButton completeTask = new TaskCompleteWindowButton(presenter.getTask(recordVO),
									$("DisplayTaskView.completeTask"),
									presenter.getView().getConstellioFactories().getAppLayerFactory(), presenter) {
								@Override
								protected String getConfirmDialogMessage() {
									if (presenter.isSubTaskPresentAndHaveCertainStatus(recordVO)) {
										return $("DisplayTaskView.subTaskPresentComplete");
									}

									return $("DisplayTaskView.completeTaskDialogMessage");
								}
							};

							completeTask.click();
						}
					});
				}

				if (presenter.isAutoAssignButtonEnabled(recordVO)) {
					rootItem.addItem($("TaskTable.autoAssignTask"), FontAwesome.HAND_O_RIGHT, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.autoAssignButtonClicked(recordVO);
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
							if (presenter.isSubTaskPresentAndHaveCertainStatus(recordVO)) {
								return $("DisplayTaskView.subTaskPresentWarning");
							} else {
								return $("ConfirmDialog.confirmDelete");
							}
						}

						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							presenter.deleteButtonClicked(recordVO);
						}
					}).setEnabled(presenter.isDeleteButtonEnabled(recordVO));
				}

				if (presenter.isMetadataReportAllowed(recordVO)) {
					rootItem.addItem($("TaskTable.reportMetadata"), FontAwesome.LIST_ALT, new Command() {
						@Override
						public void menuSelected(MenuItem selectedItem) {
							presenter.generateReportButtonClicked(recordVO);
						}
					});
				}
				return menuBar;
			}
		});
		setColumnHeader(columnId, "");
		return records;
	}

	private void editTask(RecordVO recordVO) {
		presenter.setReadByUser(recordVO, true);
		presenter.registerPreviousSelectedTab();
		presenter.editButtonClicked(recordVO);
	}

	private void displayTask(RecordVO recordVO) {
		presenter.setReadByUser(recordVO, true);
		presenter.registerPreviousSelectedTab();
		presenter.displayButtonClicked(recordVO);
	}

	@Override
	protected Component buildMetadataComponent(MetadataValueVO metadataValue, RecordVO recordVO) {
		if (Task.STARRED_BY_USERS.equals(metadataValue.getMetadata().getLocalCode())) {
			return new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(), presenter.getCurrentUserId()) {
				@Override
				public void updateTaskStarred(boolean isStarred, String taskId) {
					presenter.updateTaskStarred(isStarred, taskId);
				}
			};
		} else {
			return super.buildMetadataComponent(metadataValue, recordVO);
		}
	}


	public interface TaskPresenter {
		boolean isSubTaskPresentAndHaveCertainStatus(RecordVO recordVO);

		void displayButtonClicked(RecordVO record);

		void editButtonClicked(RecordVO record);

		void deleteButtonClicked(RecordVO record);

		void closeButtonClicked(RecordVO record);

		void generateReportButtonClicked(RecordVO recordVO);

		boolean isTaskOverdue(TaskVO taskVO);

		boolean isFinished(TaskVO taskVO);

		void autoAssignButtonClicked(RecordVO recordVO);

		boolean isAutoAssignButtonEnabled(RecordVO recordVO);

		boolean isEditButtonEnabled(RecordVO recordVO);

		boolean isReadByUser(RecordVO recordVO);

		void setReadByUser(RecordVO recordVO, boolean readByUser);

		boolean isCompleteButtonEnabled(RecordVO recordVO);

		boolean isCloseButtonEnabled(RecordVO recordVO);

		boolean isDeleteButtonEnabled(RecordVO recordVO);

		boolean isDeleteButtonVisible(RecordVO recordVO);

		boolean isMetadataReportAllowed(RecordVO recordVO);

		BaseView getView();

		void reloadTaskModified(Task task);

		String getCurrentUserId();

		void updateTaskStarred(boolean isStarred, String taskId);

		void registerPreviousSelectedTab();

		Task getTask(RecordVO recordVO);

		void afterCompletionActions();

		void beforeCompletionActions(Task task);
	}

	public class TaskStyleGenerator implements CellStyleGenerator {
		private static final String OVER_DUE_TASK_STYLE = "error";
		private static final String UNREAD_TASK_STYLE = "important";
		private static final String FINISHED_TASK_STYLE = "disabled";

		@Override
		public String getStyle(Table source, Object itemId, Object propertyId) {
			String style;
			if (isTitleColumn(propertyId)) {
				RecordVOItem item = (RecordVOItem) source.getItem(itemId);
				if (!presenter.isReadByUser(item.getRecord())) {
					// TODO Rendre gras le texte plutôt que le fond
					style = UNREAD_TASK_STYLE;
				} else {
					style = null;
				}
			} else if (!isDueDateColumn(propertyId)) {
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

		private boolean isTitleColumn(Object propertyId) {
			if (!(propertyId instanceof MetadataVO)) {
				return false;
			}
			MetadataVO metadata = (MetadataVO) propertyId;
			return Task.TITLE.equals(MetadataVO.getCodeWithoutPrefix(metadata.getCode()));
		}

		private boolean isDueDateColumn(Object propertyId) {
			if (!(propertyId instanceof MetadataVO)) {
				return false;
			}
			MetadataVO metadata = (MetadataVO) propertyId;
			return Task.DUE_DATE.equals(MetadataVO.getCodeWithoutPrefix(metadata.getCode()));
		}
	}

	@Override
	protected TableColumnsManager newColumnsManager() {
		return new RecordVOTableColumnsManager() {
			@Override
			protected String toColumnId(Object propertyId) {
				if (propertyId instanceof MetadataVO) {
					if (Task.STARRED_BY_USERS.equals(((MetadataVO) propertyId).getLocalCode())) {
						setColumnHeader(propertyId, "");
						setColumnWidth(propertyId, 60);
					}
				}
				return super.toColumnId(propertyId);
			}
		};
	}

	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		Collection<?> sortableContainerPropertyIds = super.getSortableContainerPropertyIds();
		Iterator<?> iterator = sortableContainerPropertyIds.iterator();
		while (iterator.hasNext()) {
			Object property = iterator.next();
			if (property != null && property instanceof MetadataVO && Task.STARRED_BY_USERS.equals(((MetadataVO) property).getLocalCode())) {
				iterator.remove();
			}
		}
		return sortableContainerPropertyIds;
	}

	public void resort() {
		sort();
		resetPageBuffer();
		enableContentRefreshing(true);
		refreshRenderedCells();
	}
}
