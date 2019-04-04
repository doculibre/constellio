package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.TaskCompleteWindowButton;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.menuBar.ConfirmDialogMenuBarItemCommand;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ContainerAdapter;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TaskTable extends VerticalLayout {

	public static final String PREFIX = "images/icons/task/";
	public static final ThemeResource COMPLETE_ICON = new ThemeResource(PREFIX + "task.png");
	public static final ThemeResource CLOSE_ICON = new ThemeResource(PREFIX + "task_complete.png");

	private I18NHorizontalLayout controlsLayout;

	private RecordVO selectedTask;

	private Button filterButton;

	private ComboBox sortField;

	private TaskRecordVOTable table;

	private final TaskPresenter presenter;

	public TaskTable(RecordVODataProvider provider, TaskPresenter presenter) {
		this.presenter = presenter;

		setWidth("100%");
		addStyleName("task-table-layout");

		table = new TaskRecordVOTable($("TaskTable.caption", provider.size()));
		table.setContainerDataSource(buildContainer(provider));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 200);
		table.setCellStyleGenerator(new TaskStyleGenerator());
		table.setPageLength(Math.min(15, provider.size()));
		table.setWidth("100%");
		table.addStyleName("task-table");
		addDisplayOnClickListener();

		controlsLayout = new I18NHorizontalLayout();
		controlsLayout.setWidth("100%");
		controlsLayout.setSpacing(true);
		controlsLayout.setDefaultComponentAlignment(Alignment.TOP_RIGHT);

		filterButton = new BaseButton($("filter"), FontAwesome.FILTER) {
			@Override
			protected void buttonClick(ClickEvent event) {

			}
		};
		filterButton.addStyleName(ValoTheme.BUTTON_LINK);
		filterButton.addStyleName("task-table-filter");

		sortField = new BaseComboBox($("sortBy"));
		sortField.setWidth("250px");
		sortField.addStyleName("task-table-sort");
		sortField.addStyleName(ValoTheme.COMBOBOX_BORDERLESS);
		sortField.setTextInputAllowed(false);
		sortField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
//				sortField.setCaption($("sortBy") + " " + value);
			}
		});
		Object sortPropertyId = table.getSortContainerPropertyId();
		for (Object sortablePropertyId : table.getSortableContainerPropertyIds()) {
			if (sortablePropertyId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) sortablePropertyId;
				sortField.addItem(metadataVO);
				sortField.setItemCaption(metadataVO, metadataVO.getLabel());

				if (metadataVO.equals(sortPropertyId)) {
					sortField.setValue(metadataVO);
				}
			}
		}

		controlsLayout.addComponents(filterButton, sortField);

		addComponents(controlsLayout, table);
	}

	protected void addDisplayOnClickListener() {
		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
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
					displayTask(item, recordVO);
				}
			}
		});
		table.removeStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
	}

	private Container buildContainer(RecordVODataProvider provider) {
		return addButtons(new RecordVOLazyContainer(provider));
	}

	private Container addButtons(final RecordVOLazyContainer records) {
		String columnId = "menuBar";
		table.addGeneratedColumn(columnId, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final RecordVO recordVO = records.getRecordVO((int) itemId);
				MenuBar menuBar = new BaseMenuBar();
				menuBar.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

				MenuItem rootItem = menuBar.addItem("", FontAwesome.ELLIPSIS_V, null);

				rootItem.addItem($("display"), DisplayButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						displayTask(null, recordVO);
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
		table.setColumnHeader(columnId, "");
		return records;
	}

	private void editTask(RecordVO recordVO) {
		presenter.setReadByUser(recordVO, true);
		presenter.registerPreviousSelectedTab();
		presenter.editButtonClicked(recordVO);
	}

	private void displayTask(Object itemId, RecordVO recordVO) {
		presenter.setReadByUser(recordVO, true);
		presenter.registerPreviousSelectedTab();

		if (itemId == null) {
			// FIXME
			presenter.displayButtonClicked(recordVO);
		} else {
			// Deselecting
			if (selectedTask != null && selectedTask.equals(recordVO)) {
				table.hideSelectedTaskDetails(itemId);
				selectedTask = null;
			} else if (recordVO != null) {
				selectedTask = recordVO;
				table.showSelectedTaskDetails(itemId);
			}
		}
	}

	private static class TaskDetailsPanel extends VerticalLayout {

		private RecordVO taskVO;

		private TaskDetailsPanel(RecordVO taskVO) {
			setSizeFull();
			setSpacing(true);

			RecordDisplay taskDisplay = new RecordDisplay(taskVO);
			addComponent(taskDisplay);
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

		void callAssignationExtension();
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

	public void resort() {
		table.sort();
		table.resetPageBuffer();
		table.enableContentRefreshing(true);
		table.refreshRenderedCells();
	}

	protected String getTitleColumnStyle(RecordVO recordVO) {
		return table.getInheritedTitleColumnStyle(recordVO);
	}

	protected TableColumnsManager newColumnsManager() {
		return new RecordVOTableColumnsManager() {
			@Override
			protected String toColumnId(Object propertyId) {
				if (propertyId instanceof MetadataVO) {
					if (Task.STARRED_BY_USERS.equals(((MetadataVO) propertyId).getLocalCode())) {
						table.setColumnHeader(propertyId, "");
						table.setColumnWidth(propertyId, 60);
					}
				}
				return super.toColumnId(propertyId);
			}
		};
	}

	private class TaskRecordVOTable extends RecordVOTable {

		private List<Object> selectedIds = new ArrayList<>();

		public TaskRecordVOTable(String caption) {
			super(caption);
			addStyleName(ValoTheme.TABLE_BORDERLESS);
			addStyleName(ValoTheme.TABLE_NO_HEADER);
			addStyleName(ValoTheme.TABLE_NO_STRIPES);
			addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
		}

		@Override
		protected Property<?> loadContainerProperty(Object itemId, Object propertyId) {
			Property<?> property;
			if (!selectedIds.isEmpty() && propertyId instanceof MetadataVO && ((MetadataVO) propertyId).codeMatches(Schemas.TITLE_CODE)) {
				Label testLabel = new Label(
						"Lorem ipsum dolor sit amet, <br/> "
						+ "consectetur adipiscing elit, <br/> "
						+ "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. <br/> "
						+ "Ut enim ad minim veniam, <br/> "
						+ "quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. <br/> "
						+ "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. <br/> "
						+ "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
				testLabel.setContentMode(ContentMode.HTML);
				testLabel.setSizeFull();
				property = new ObjectProperty(testLabel);
			} else {
				property = super.loadContainerProperty(itemId, propertyId);
			}
			return property;
		}

		public void showSelectedTaskDetails(Object itemId) {
			selectedIds.add(itemId);
//			fireItemSetChange();
			containerItemSetChange(new ItemSetChangeEvent() {
				@Override
				public Container getContainer() {
					return getContainerDataSource();
				}
			});
		}

		public void hideSelectedTaskDetails(Object itemId) {
			selectedIds.remove(itemId);
//			fireItemSetChange();
			containerItemSetChange(new ItemSetChangeEvent() {
				@Override
				public Container getContainer() {
					return getContainerDataSource();
				}
			});
		}

		@Override
		protected void refreshRenderedCells() {
			super.refreshRenderedCells();
		}

		@Override
		protected void enableContentRefreshing(boolean refreshContent) {
			super.enableContentRefreshing(refreshContent);
		}

		@Override
		protected void resetPageBuffer() {
			super.resetPageBuffer();
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

		protected String getInheritedTitleColumnStyle(RecordVO recordVO) {
			return super.getTitleColumnStyle(recordVO);
		}

		@Override
		public String getTitleColumnStyle(RecordVO recordVO) {
			return TaskTable.this.getTitleColumnStyle(recordVO);
		}

		@Override
		protected TableColumnsManager newColumnsManager() {
			return TaskTable.this.newColumnsManager();
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
	}
}
