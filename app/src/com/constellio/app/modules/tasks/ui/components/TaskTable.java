package com.constellio.app.modules.tasks.ui.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.tepi.filtertable.FilterGenerator;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.components.folder.fields.LookupFolderField;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.modules.tasks.ui.pages.tasks.TaskCompleteWindowButton;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.WindowButton.WindowConfiguration;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseForm.FieldAndPropertyId;
import com.constellio.app.ui.framework.components.BaseUpdatableContentVersionPresenter;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.menuBar.ConfirmDialogMenuBarItemCommand;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.components.tree.RecordLazyTree;
import com.constellio.app.ui.framework.components.tree.TreeItemClickListener;
import com.constellio.app.ui.framework.components.user.UserDisplay;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.BaseRecordTreeDataProvider;
import com.constellio.app.ui.framework.data.LazyTreeDataProvider;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class TaskTable extends VerticalLayout {

	public static final String PREFIX = "images/icons/task/";
	public static final ThemeResource COMPLETE_ICON = new ThemeResource(PREFIX + "task.png");
	public static final ThemeResource CLOSE_ICON = new ThemeResource(PREFIX + "task_complete.png");

	public static final String EXPAND_BUTTON_COLUMN_ID = "expandButton";
	public static final String MENUBAR_COLUMN_ID = "menuBar";

	private JodaDateTimeToStringConverter dateTimeConverter = new JodaDateTimeToStringConverter();

	private I18NHorizontalLayout controlsLayout;

	private RecordVO selectedTask;

	private Button filterButton;

	private ComboBox sortField;

	private Button sortAscButton;

	private TaskRecordVOTable table;

	private FilterGenerator filterGenerator;

	private TaskDetailsComponentFactory taskDetailsComponentFactory;

	private final TaskPresenter presenter;

	public TaskTable(RecordVODataProvider provider, TaskPresenter presenter) {
		this.presenter = presenter;

		setWidth("100%");
		addStyleName("task-table-layout");

		//		table = new TaskRecordVOTable($("TaskTable.caption", provider.size()));
		table = new TaskRecordVOTable("");
		table.setContainerDataSource(buildContainer(provider));
		table.setPageLength(Math.min(5, provider.size()));

		controlsLayout = new I18NHorizontalLayout();
		controlsLayout.addStyleName("task-table-controls");
		controlsLayout.setSpacing(true);
		controlsLayout.setDefaultComponentAlignment(Alignment.TOP_RIGHT);

		filterButton = new WindowButton($("TaskTable.filter"), $("TaskTable.filter"), WindowConfiguration.modalDialog("90%", "90%")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout mainLayout = new VerticalLayout();
				mainLayout.setWidth("100%");

				return mainLayout;
			}
		};
		filterButton.setIcon(FontAwesome.FILTER);
		filterButton.addStyleName(ValoTheme.BUTTON_LINK);
		filterButton.addStyleName("task-table-filter");
		filterButton.setVisible(filterGenerator != null);

		sortField = new BaseComboBox($("TaskTable.sortBy"));
		sortField.addItem("_NULL_");
		sortField.setNullSelectionItemId("_NULL_");
		sortField.setItemCaption("_NULL_", "");
		sortField.setWidth("250px");
		sortField.addStyleName("task-table-sort");
		sortField.addStyleName(ValoTheme.COMBOBOX_BORDERLESS);
		sortField.setTextInputAllowed(false);
		sortField.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				table.setSortContainerPropertyId(value);
				table.setSortAscending(true);
				if (value != null) {
					sortAscButton.setCaption($("TaskTable.sort.asc"));
				} else {
					sortAscButton.setCaption($("TaskTable.sort.none"));
				}
			}
		});

		Object sortPropertyId = table.getSortContainerPropertyId();
		for (Object sortablePropertyId : table.getSortableContainerPropertyIds()) {
			if (sortablePropertyId instanceof MetadataVO) {
				MetadataVO metadataVO = (MetadataVO) sortablePropertyId;
				boolean sortable = metadataVO.isSortable();
				MetadataValueType type = metadataVO.getType();
				if (!sortable && (
						type == MetadataValueType.STRING ||
						type == MetadataValueType.TEXT ||
						type == MetadataValueType.DATE ||
						type == MetadataValueType.DATE_TIME ||
						type == MetadataValueType.INTEGER ||
						type == MetadataValueType.NUMBER ||
						type == MetadataValueType.BOOLEAN)) {
					sortable = true;
				}
				if (sortable) {
					sortField.addItem(metadataVO);
					sortField.setItemCaption(metadataVO, metadataVO.getLabel());

					if (metadataVO.equals(sortPropertyId)) {
						sortField.setValue(metadataVO);
					}
				}
			}
		}

		sortAscButton = new BaseButton($("TaskTable.sort.none")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				if (table.getSortContainerPropertyId() != null) {
					if (table.isSortAscending()) {
						sortAscButton.setCaption($("TaskTable.sort.desc"));
						table.setSortAscending(false);
					} else {
						sortAscButton.setCaption($("TaskTable.sort.asc"));
						table.setSortAscending(true);
					}
				} else {
					sortAscButton.setCaption($("TaskTable.sort.none"));
				}
			}
		};
		sortAscButton.addStyleName(ValoTheme.BUTTON_LINK);
		sortAscButton.addStyleName("task-table-sort-asc-button");

		controlsLayout.addComponents(filterButton, sortField, sortAscButton);
		controlsLayout.setComponentAlignment(sortAscButton, Alignment.TOP_LEFT);
		controlsLayout.setExpandRatio(sortField, 0);

		addComponents(controlsLayout, table);
		setComponentAlignment(controlsLayout, Alignment.BOTTOM_RIGHT);

		addTableSelectOnClickListener();
	}

	protected void addTableSelectOnClickListener() {
		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				if (table.isSelected(itemId)) {
					table.deselect(itemId);
				} else {
					table.select(itemId);
				}
				table.setCurrentPageFirstItemId(itemId);
			}
		});
		table.removeStyleName(RecordVOTable.CLICKABLE_ROW_STYLE_NAME);
	}

	private Container buildContainer(RecordVODataProvider provider) {
		return addButtons(new RecordVOLazyContainer(provider));
	}

	private Container addButtons(final RecordVOLazyContainer records) {
		table.addGeneratedColumn(EXPAND_BUTTON_COLUMN_ID, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final TaskExpandButton taskExpandButton = new TaskExpandButton(itemId);
				table.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						taskExpandButton.refresh();
					}
				});
				return taskExpandButton;
			}
		});
		table.setColumnHeader(EXPAND_BUTTON_COLUMN_ID, "");

		table.addGeneratedColumn(MENUBAR_COLUMN_ID, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				RecordVO recordVO = records.getRecordVO((int) itemId);
				return new TaskMenuBar(recordVO);
			}
		});
		table.setColumnHeader(MENUBAR_COLUMN_ID, "");
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

	private void toggleSelection(Object itemId) {
		if (table.isSelected(itemId)) {
			table.deselect(itemId);
		} else {
			table.select(itemId);
		}
		ensureHeight(itemId);
	}

	private void ensureHeight(Object itemId) {
		int l = table.getPageLength();
		int index = table.indexOfId(itemId);
		int indexToSelectAbove = index - (l/2);
		if( indexToSelectAbove<0 ) indexToSelectAbove=0;
		table.setCurrentPageFirstItemIndex(indexToSelectAbove);
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

	private class TaskExpandButton extends BaseButton {

		private Object itemId;

		public TaskExpandButton(Object itemId) {
			super("");
			this.itemId = itemId;
			setIcon(computeIcon());
			addStyleName(ValoTheme.BUTTON_LINK);
		}

		private Resource computeIcon() {
			Resource icon;
			if (table.isSelected(itemId)) {
				icon = FontAwesome.ANGLE_UP;
			} else {
				icon = FontAwesome.ANGLE_DOWN;
			}
			return icon;
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			toggleSelection(itemId);
		}

		private void refresh() {
			setIcon(computeIcon());
		}

	}

	private class TaskMenuBar extends BaseMenuBar {

		private TaskMenuBar(final RecordVO taskVO) {
			addStyleName(ValoTheme.MENUBAR_BORDERLESS);

			MenuItem rootItem = addItem("", FontAwesome.ELLIPSIS_V, null);

			rootItem.addItem($("display"), DisplayButton.ICON_RESOURCE, new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
					displayTask(null, taskVO);
				}
			});

			if (presenter.isEditButtonEnabled(taskVO)) {
				rootItem.addItem($("edit"), EditButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						editTask(taskVO);
					}
				});
			}

			if (presenter.isReadByUser(taskVO)) {
				rootItem.addItem($("TaskTable.markAsUnread"), EditButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.setReadByUser(taskVO, false);
					}
				});
			} else {
				rootItem.addItem($("TaskTable.markAsRead"), EditButton.ICON_RESOURCE, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.setReadByUser(taskVO, true);
					}
				});
			}

			if (presenter.isCompleteButtonEnabled(taskVO)) {
				rootItem.addItem($("TaskTable.complete"), COMPLETE_ICON, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						TaskCompleteWindowButton completeTaskButton = new TaskCompleteWindowButton(presenter.getTask(taskVO),
								$("DisplayTaskView.completeTask"),
								presenter.getView().getConstellioFactories().getAppLayerFactory(), presenter) {
							@Override
							protected String getConfirmDialogMessage() {
								if (presenter.isSubTaskPresentAndHaveCertainStatus(taskVO)) {
									return $("DisplayTaskView.subTaskPresentComplete");
								}
								return $("DisplayTaskView.completeTaskDialogMessage");
							}

							@Override
							protected void addCommentField(RecordVO taskVO, Field commentField, VerticalLayout fieldLayout) {
								TaskTable.this.addCompleteWindowCommentField(taskVO, commentField, fieldLayout);
							}
						};
						completeTaskButton.click();
					}
				});
			}

			if (presenter.isCloseButtonEnabled(taskVO)) {
				rootItem.addItem($("TaskTable.close"), CLOSE_ICON, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.closeButtonClicked(taskVO);
					}
				});
			}

			if (presenter.isDeleteButtonVisible(taskVO)) {
				rootItem.addItem($("delete"), DeleteButton.ICON_RESOURCE, new ConfirmDialogMenuBarItemCommand() {
					@Override
					protected String getConfirmDialogMessage() {
						if (presenter.isSubTaskPresentAndHaveCertainStatus(taskVO)) {
							return $("DisplayTaskView.subTaskPresentWarning");
						} else {
							return $("ConfirmDialog.confirmDelete");
						}
					}

					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClicked(taskVO);
					}
				}).setEnabled(presenter.isDeleteButtonEnabled(taskVO));
			}

			if (presenter.isMetadataReportAllowed(taskVO)) {
				rootItem.addItem($("TaskTable.reportMetadata"), FontAwesome.LIST_ALT, new Command() {
					@Override
					public void menuSelected(MenuItem selectedItem) {
						presenter.generateReportButtonClicked(taskVO);
					}
				});
			}
		}
	}

	public TaskDetailsComponentFactory getTaskDetailsComponentFactory() {
		return taskDetailsComponentFactory;
	}

	protected void addCompleteWindowCommentField(RecordVO taskVO, Field commentField, VerticalLayout fieldLayout) {
		fieldLayout.addComponent(commentField);
		if (taskDetailsComponentFactory != null) {
			taskDetailsComponentFactory.decorateCompleteWindowCommentField(taskVO, commentField, fieldLayout);
		}
	}

	public void setTaskDetailsComponentFactory(TaskDetailsComponentFactory taskDetailsComponentFactory) {
		this.taskDetailsComponentFactory = taskDetailsComponentFactory;
	}

	public static interface TaskDetailsComponentFactory {

		void decorateCompleteWindowCommentField(RecordVO taskVO, Field commentField, VerticalLayout fieldLayout);

		Component newTaskDetailsComponent(TaskTable taskTable, Object itemId, RecordVO taskVO, boolean expanded);

	}

	public class TaskDetailsComponent extends VerticalLayout {

		private Object itemId;

		private RecordVO taskVO;

		private boolean expanded;

		private Component taskDetailsTopComponent;

		private Component titleComponent;

		private Component subTitleComponent;

		private Component assigneeComponent;

		private VerticalLayout expandLayout;

		private Component descriptionComponent;

		private Component linkedContentComponent;

		private Button addDocumentsButton;

		private Component contentsComponent;

		private Component linkedDocumentsComponent;

		private Component linkedFoldersComponent;

		private Component linkedContainersComponent;

		private VerticalLayout commentsLayout;

		public TaskDetailsComponent(Object itemId, final RecordVO taskVO, boolean expanded) {
			this.itemId = itemId;
			this.taskVO = taskVO;
			this.expanded = expanded;
			init();
		}

		protected void reloadTask() {
			taskVO = presenter.reloadRequested(taskVO);
		}

		protected void reloadComments() {
			expandLayout.removeComponent(commentsLayout);
			reloadTask();
			commentsLayout = newCommentsLayout();
			expandLayout.addComponent(commentsLayout);
			ensureHeight(itemId);
		}

		protected void reloadLinkedContents() {
			int index = expandLayout.getComponentIndex(linkedContentComponent);
			expandLayout.removeComponent(linkedContentComponent);
			reloadTask();
			linkedContentComponent = newLinkedContentComponent();
			expandLayout.addComponent(linkedContentComponent, index);
			ensureHeight(itemId);
		}

		protected Component newInvisibleComponent() {
			Label label = new Label();
			label.setVisible(false);
			return label;
		}

		protected Component newTaskDetailsTopComponent() {
			List<String> linkedFolderIds = taskVO.get(Task.LINKED_FOLDERS);
			List<String> linkedDocumentIds = taskVO.get(Task.LINKED_DOCUMENTS);
			List<ContentVersionVO> contents = taskVO.get(Task.CONTENTS);

			String createdById = taskVO.get(Schemas.CREATED_BY);
			LocalDateTime createdOnDate = taskVO.get(Schemas.CREATED_ON);

			Component createdByComponent = new UserDisplay(createdById);
			createdByComponent.addStyleName("task-details-created-by");

			Label createdOnLabel = new Label(dateTimeConverter.convertToPresentation(createdOnDate, String.class, getLocale()));
			createdOnLabel.addStyleName("task-details-created-on");

			Label contentsImage = new Label("");
			contentsImage.setIcon(FontAwesome.PAPERCLIP);
			contentsImage.addStyleName("task-details-contents-info");
			contentsImage.setVisible(!contents.isEmpty() || !linkedDocumentIds.isEmpty() || !linkedFolderIds.isEmpty());

			I18NHorizontalLayout taskDetailsTopLayout = new I18NHorizontalLayout(createdByComponent, createdOnLabel, contentsImage);
			taskDetailsTopLayout.addStyleName("task-details-top");
			taskDetailsTopLayout.setSpacing(true);
			return taskDetailsTopLayout;
		}

		protected Component newTitleComponent() {
			String title = taskVO.getTitle();
			Label titleLabel = new Label(title);
			titleLabel.addStyleName("task-details-title");
			return titleLabel;
		}

		protected Component newSubTitleComponent() {
			Component dueDateLabel;
			LocalDate dueDate = taskVO.get(Task.DUE_DATE);
			if (dueDate != null) {
				dueDateLabel = new Label($("TaskTable.details.dueDate", taskVO.getMetadata(Task.DUE_DATE).getLabel(), dueDate.toString()));
				dueDateLabel.addStyleName("task-details-due-date");
				addComponent(dueDateLabel);
			} else {
				dueDateLabel = newInvisibleComponent();
			}
			return dueDateLabel;
		}

		protected Component newAssigneeComponent() {
			Component assigneeComponent;
			String assigneeId = taskVO.get(Task.ASSIGNEE);
			if (assigneeId != null) {
				assigneeComponent = new UserDisplay(assigneeId);
				assigneeComponent.addStyleName("task-details-assignee");
				assigneeComponent.setCaption(taskVO.getMetadata(Task.ASSIGNEE).getLabel());
			} else {
				assigneeComponent = newInvisibleComponent();
			}
			return assigneeComponent;
		}

		protected Component newDescriptionComponent() {
			Component descriptionComponent;

			String description = taskVO.get(Task.DESCRIPTION);
			if (StringUtils.isNotBlank(description)) {
				description = StringUtils.replace(description, "overflow:hidden", ""); // Ugly CSS Bugfix
				descriptionComponent = new Label(description, ContentMode.HTML);
				descriptionComponent.addStyleName("task-details-description");
				descriptionComponent.setWidth("100%");
			} else {
				descriptionComponent = newInvisibleComponent();
			}
			return descriptionComponent;
		}

		protected String getDefaultFolderId() {
			String defaultFolderId = null;
			List<String> linkedFolderIds = taskVO.get(Task.LINKED_FOLDERS);
			if (!linkedFolderIds.isEmpty()) {
				defaultFolderId = linkedFolderIds.get(0);
			}
			return defaultFolderId;
		}

		protected List<String> addDocumentsButtonClicked(RecordVO taskVO, List<ContentVersionVO> contentVersionVOs, String folderId) {
			return presenter.addDocumentsButtonClicked(taskVO, contentVersionVOs, folderId);
		}

		protected Button newAddDocumentsButton() {
			Button addDocumentsButton = new WindowButton($("TaskTable.details.addDocuments"), $("TaskTable.details.addDocuments"), WindowConfiguration.modalDialog("90%", "450px")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout formLayout = new VerticalLayout();
					formLayout.addStyleName("no-scroll");
					formLayout.setSpacing(true);

					final ContentVersionUploadField uploadField = new ContentVersionUploadField(true);
					uploadField.setCaption($("TaskTable.details.addDocuments.files"));
					uploadField.setMajorVersionFieldVisible(false);

					final LookupFolderField folderField = new LookupFolderField(true);
					folderField.setCaption($("TaskTable.details.addDocuments.folder"));
					folderField.focus();
					folderField.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
					folderField.setValue(getDefaultFolderId());

					formLayout.addComponents(uploadField, folderField);

					BaseButton saveButton = new BaseButton($("save")) {
						@SuppressWarnings("unchecked")
						@Override
						protected void buttonClick(ClickEvent event) {
							String folderId = folderField.getValue();
							List<ContentVersionVO> contentVersionVOs = (List<ContentVersionVO>) uploadField.getValue();
							if (contentVersionVOs != null && !contentVersionVOs.isEmpty()) {
								try {
									addDocumentsButtonClicked(taskVO, contentVersionVOs, folderId);
									reloadLinkedContents();
								} catch (Throwable e) {
									//                            LOGGER.warn("error when trying to modify folder parent to " + parentId, e);
									//                            showErrorMessage("DisplayFolderView.parentFolderException");
									e.printStackTrace();
								}
								getWindow().close();
							}
						}
					};
					saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

					Button cancelButton = new BaseButton($("cancel")) {
						@Override
						protected void buttonClick(ClickEvent event) {
							getWindow().close();
						}
					};

					I18NHorizontalLayout buttonsLayout = new I18NHorizontalLayout();
					buttonsLayout.setSpacing(true);
					buttonsLayout.addComponents(saveButton, cancelButton);
					formLayout.addComponent(buttonsLayout);
					formLayout.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_CENTER);

					Panel panel = new Panel(formLayout);
					panel.setSizeFull();
					return panel;
				}

			};
			addDocumentsButton.addStyleName(ValoTheme.BUTTON_LINK);
			addDocumentsButton.addStyleName("task-details-add-documents-button");
			addDocumentsButton.setIcon(FontAwesome.PLUS);
			return addDocumentsButton;
		}

		protected Component newContentsComponent() {
			Component contentsComponent;

			List<ContentVersionVO> contents = taskVO.get(Task.CONTENTS);
			if (!contents.isEmpty()) {
				VerticalLayout contentsLayout = new VerticalLayout();
				contentsLayout.setCaption(taskVO.getMetadata(Task.CONTENTS).getLabel());
				contentsLayout.setWidth("100%");
				contentsLayout.setSpacing(true);
				contentsLayout.addStyleName("task-details-contents");

				for (ContentVersionVO contentVersionVO : contents) {
					String filename = contentVersionVO.getFileName();
					DownloadContentVersionLink downloadContentLink = new DownloadContentVersionLink(contentVersionVO, filename);
					contentsLayout.addComponent(downloadContentLink);
				}
				contentsComponent = contentsLayout;
			} else {
				contentsComponent = newInvisibleComponent();
			}
			return contentsComponent;
		}

		protected Component newLinkedDocumentsComponent() {
			Component linkedDocumentsComponent;
			List<String> linkedDocumentIds = taskVO.get(Task.LINKED_DOCUMENTS);
			//			if (!linkedDocumentIds.isEmpty()) {
			VerticalLayout linkedDocumentsLayout = new VerticalLayout();
			linkedDocumentsLayout.setCaption(taskVO.getMetadata(Task.LINKED_DOCUMENTS).getLabel());
			linkedDocumentsLayout.setWidth("100%");
			linkedDocumentsLayout.setSpacing(true);
			linkedDocumentsLayout.addStyleName("task-details-linked-documents");

			for (String linkedDocumentId : linkedDocumentIds) {
				RecordVO documentVO = presenter.getDocumentVO(linkedDocumentId);
				ContentVersionVO contentVersionVO = documentVO.get(Document.CONTENT);
				String agentURL = ConstellioAgentUtils.getAgentURL(documentVO, contentVersionVO);
				Component linkComponent;
				if (agentURL != null) {
					linkComponent = new ConstellioAgentLink(agentURL, documentVO, contentVersionVO, documentVO.getTitle(), false, new BaseUpdatableContentVersionPresenter());
					((ConstellioAgentLink) linkComponent).addVisitedClickListener(documentVO.getId());
				} else {
					linkComponent = new ReferenceDisplay(documentVO);
				}
				linkedDocumentsLayout.addComponent(linkComponent);
			}
			linkedDocumentsComponent = linkedDocumentsLayout;
			//			} else {
			//				linkedDocumentsComponent = newInvisibleComponent();
			//			}
			return linkedDocumentsComponent;
		}

		protected Component newLinkedFoldersComponent() {
			Component linkedFoldersComponent;
			List<String> linkedFolderIds = taskVO.get(Task.LINKED_FOLDERS);
			if (!linkedFolderIds.isEmpty()) {
				final LazyTreeDataProvider<String> taskFoldersTreeDataProvider = presenter.getTaskFoldersTreeDataProvider(taskVO);
				final RecordLazyTree taskFoldersTree = new RecordLazyTree(taskFoldersTreeDataProvider);
				taskFoldersTree.addItemClickListener(new TreeItemClickListener() {
					boolean clickNavigating;

					@Override
					public boolean shouldExpandOrCollapse(ItemClickEvent event) {
						return !clickNavigating;
					}

					@Override
					public void itemClick(ItemClickEvent event) {
						if (event.getButton() == MouseButton.LEFT) {
							String recordId = (String) event.getItemId();
							clickNavigating = presenter.taskFolderOrDocumentClicked(taskVO, recordId);
						} else {
							clickNavigating = true;
						}
					}
				});
				taskFoldersTree.setCaption(taskVO.getMetadata(Task.LINKED_FOLDERS).getLabel());
				taskFoldersTree.addStyleName("task-details-linked-folders");

				linkedFoldersComponent = taskFoldersTree;
			} else {
				linkedFoldersComponent = newInvisibleComponent();
			}
			return linkedFoldersComponent;
		}

		protected Component newLinkedContainersComponent() {
			Component linkedContainersComponent;

			List<String> linkedContainerIds = taskVO.get(Task.LINKED_CONTAINERS);
			if (!linkedContainerIds.isEmpty()) {
				VerticalLayout linkedContainersLayout = new VerticalLayout();
				linkedContainersLayout.setCaption(taskVO.getMetadata(Task.LINKED_CONTAINERS).getLabel());
				linkedContainersLayout.setWidth("100%");
				linkedContainersLayout.setSpacing(true);
				linkedContainersLayout.addStyleName("task-details-linked-containers");

				for (String linkedContainerId : linkedContainerIds) {
					ReferenceDisplay referenceDisplay = new ReferenceDisplay(linkedContainerId);
					linkedContainersLayout.addComponent(referenceDisplay);
				}
				linkedContainersComponent = linkedContainersLayout;
			} else {
				linkedContainersComponent = newInvisibleComponent();
			}
			return linkedContainersComponent;
		}

		protected VerticalLayout newLinkedContentComponent() {
			VerticalLayout linkedContentLayout = new VerticalLayout();
			linkedContentLayout.addStyleName("task-details-linked-content");
			linkedContentLayout.setWidth("100%");
			linkedContentLayout.setSpacing(true);

			addDocumentsButton = newAddDocumentsButton();
			contentsComponent = newContentsComponent();
			linkedDocumentsComponent = newLinkedDocumentsComponent();
			linkedFoldersComponent = newLinkedFoldersComponent();
			linkedContainersComponent = newLinkedContainersComponent();

			linkedContentLayout.addComponent(addDocumentsButton);
			linkedContentLayout.addComponent(contentsComponent);
			linkedContentLayout.addComponent(linkedDocumentsComponent);
			linkedContentLayout.addComponent(linkedFoldersComponent);
			linkedContentLayout.addComponent(linkedContainersComponent);
			linkedContentLayout.setComponentAlignment(addDocumentsButton, Alignment.TOP_RIGHT);
			return linkedContentLayout;
		}

		protected Component newCommentForm(final Comment newComment, final Window window, final VerticalLayout commentsLayout) {
			BaseTextArea commentField = new BaseTextArea();
			commentField.setWidth("100%");
			FieldAndPropertyId commentFieldAndPropertyId = new FieldAndPropertyId(commentField, "message");
			BaseForm<Comment> commentForm = new BaseForm<Comment>(newComment, Arrays.asList(commentFieldAndPropertyId)) {
				@Override
				protected void saveButtonClick(Comment newComment) throws ValidationException {
					if (taskCommentAdded(taskVO, newComment)) {
						reloadComments();
					}
					window.close();
					ensureHeight(itemId);
				}

				@Override
				protected void cancelButtonClick(Comment newComment) {
					window.close();
				}
			};
			return commentForm;
		}

		protected boolean taskCommentAdded(RecordVO taskVO, Comment newComment) {
			return presenter.taskCommentAdded(taskVO, newComment);
		}

		protected void addComment(Comment comment, VerticalLayout commentsLayout) {
			String userId = comment.getUserId();
			LocalDateTime commentDateTime = comment.getDateTime();
			String commentDateTimeStr = dateTimeConverter.convertToPresentation(commentDateTime, String.class, getLocale());

			Component commentUserComponent = new UserDisplay(userId);
			commentUserComponent.addStyleName("task-details-comment-user");

			Label commentDateTimeLabel = new Label(commentDateTimeStr);
			commentDateTimeLabel.addStyleName("task-details-comment-date-time");

			I18NHorizontalLayout userTimeLayout = new I18NHorizontalLayout(commentUserComponent, commentDateTimeLabel);
			userTimeLayout.addStyleName("task-details-user-date-time");
			userTimeLayout.setSpacing(true);
			String message = comment.getMessage();
			message = StringUtils.replace(message, "\n", "<br/>");
			Label messageLabel = new Label(message, ContentMode.HTML);
			messageLabel.addStyleName("task-details-comment-message");

			commentsLayout.addComponents(userTimeLayout, messageLabel);
		}

		protected Component newAddCommentComponent(final VerticalLayout commentsLayout) {
			WindowButton addCommentButton = new WindowButton($("TaskTable.details.addComment"), $("TaskTable.details.addComment"), WindowConfiguration.modalDialog("400px", "280px")) {
				@Override
				protected Component buildWindowContent() {
					Comment newComment = new Comment();
					return newCommentForm(newComment, getWindow(), commentsLayout);
				}
			};
			addCommentButton.setIcon(FontAwesome.PLUS);
			addCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
			addCommentButton.addStyleName("task-details-add-comment-button");
			return addCommentButton;
		}

		protected VerticalLayout newCommentsLayout() {
			List<Comment> comments = taskVO.get(Task.COMMENTS);

			final VerticalLayout commentsLayout = new VerticalLayout();
			commentsLayout.setCaption(taskVO.getMetadata(Task.COMMENTS).getLabel());
			commentsLayout.setWidth("100%");
			commentsLayout.setSpacing(true);
			commentsLayout.addStyleName("task-details-comments");

			Component addCommentsComponent = newAddCommentComponent(commentsLayout);
			commentsLayout.addComponent(addCommentsComponent);
			commentsLayout.setComponentAlignment(addCommentsComponent, Alignment.TOP_RIGHT);

			final Label noCommentLabel = new Label($("TaskTable.details.noComment"));
			noCommentLabel.addStyleName("task-details-no-comment");
			if (comments.isEmpty()) {
				commentsLayout.addComponent(noCommentLabel);
			}

			for (Comment comment : comments) {
				addComment(comment, commentsLayout);
			}
			return commentsLayout;
		}

		protected VerticalLayout newExpandLayout() {
			VerticalLayout expandLayout = new VerticalLayout();
			expandLayout.addStyleName("task-details-expanded");
			expandLayout.setSpacing(true);
			expandLayout.setWidth("100%");

			assigneeComponent = newAssigneeComponent();
			descriptionComponent = newDescriptionComponent();
			linkedContentComponent = newLinkedContentComponent();
			commentsLayout = newCommentsLayout();
			expandLayout.addComponent(assigneeComponent);
			expandLayout.addComponent(descriptionComponent);
			expandLayout.addComponent(linkedContentComponent);
			expandLayout.addComponent(commentsLayout);

			return expandLayout;
		}

		private void init() {
			setSizeFull();
			setSpacing(true);
			addStyleName("task-details");

			taskDetailsTopComponent = newTaskDetailsTopComponent();
			titleComponent = newTitleComponent();
			subTitleComponent = newSubTitleComponent();
			addComponent(taskDetailsTopComponent);
			addComponent(titleComponent);
			addComponent(subTitleComponent);

			expandLayout = newExpandLayout();
			if (expanded) {
				addComponent(expandLayout);
			}
			addSelectClickListener(this, itemId);
		}

		private void setExpanded(boolean expanded) {
			if (this.expanded && !expanded) {
				removeComponent(expandLayout);
			} else if (!this.expanded && expanded) {
				addComponent(expandLayout);
			}
			this.expanded = expanded;
		}
	}

	private void addSelectClickListener(AbstractOrderedLayout layout, final Object itemId) {
		layout.addLayoutClickListener(new LayoutClickListener() {
			@Override
			public void layoutClick(LayoutClickEvent event) {
				Component clickedComponent = event.getClickedComponent();
				if (!(clickedComponent instanceof Button) && !(clickedComponent instanceof Link) && !(clickedComponent instanceof Tree) && !(clickedComponent instanceof ReferenceDisplay) && !(clickedComponent instanceof Field)) {
					toggleSelection(itemId);
				}
			}
		});
	}

	public interface TaskPresenter {

		RecordVO reloadRequested(RecordVO recordVO);

		boolean isSubTaskPresentAndHaveCertainStatus(RecordVO recordVO);

		List<String> addDocumentsButtonClicked(RecordVO taskVO, List<ContentVersionVO> contentVersionVOs, String folderId);

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

		RecordVO getDocumentVO(String linkedDocumentId);

		boolean taskCommentAdded(RecordVO taskVO, Comment newComment);

		boolean taskFolderOrDocumentClicked(RecordVO taskVO, String recordId);

		BaseRecordTreeDataProvider getTaskFoldersTreeDataProvider(RecordVO taskVO);
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

	private class TaskRecordVOTable extends RecordVOTable {

		private Set<Object> selectedIds = new HashSet<>();

		private String id;

		public TaskRecordVOTable(String caption) {
			super(caption);

			id = UUID.randomUUID().toString();
			setId(id);

			setWidth("100%");
			addStyleName("task-table");

			setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
			setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 200);
			setCellStyleGenerator(new TaskStyleGenerator());

			setMultiSelect(true);
			setMultiSelectMode(MultiSelectMode.SIMPLE);
			addStyleName(ValoTheme.TABLE_BORDERLESS);
			addStyleName(ValoTheme.TABLE_NO_HEADER);
			addStyleName(ValoTheme.TABLE_NO_STRIPES);
			addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);

			addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					Set<Object> newValue = new HashSet<Object>((Set<?>) event.getProperty().getValue());
					for (Iterator<Object> it = selectedIds.iterator(); it.hasNext();) {
						Object selectedId = it.next();
						if (!newValue.contains(selectedId)) {
							it.remove();
							hideSelectedTaskDetails(selectedId);
						}
					}
					for (Object newSelectedId : newValue) {
						if (!selectedIds.contains(newSelectedId)) {
							selectedIds.add(newSelectedId);
							showSelectedTaskDetails(newSelectedId);
						}
					}

					// Missing scrollbar bug workaround
					if (getPageLength() <= size()) {
						refreshRowCache();
					}
				}
			});
		}

		@Override
		public int indexOfId(Object itemId) {
			return super.indexOfId(itemId);
		}

		@Override
		public void select(Object itemId) {
			super.select(itemId);
		}

		public void deselect(Object itemId) {
			Set<Object> selectedValues = new HashSet<Object>((Set<?>) getValue());
			selectedValues.remove(itemId);
			setValue(selectedValues);
		}

		private TaskDetailsComponent getTaskDetailsComponent(Object itemId) {
			return (TaskDetailsComponent) table.getMetadataProperty(itemId, Schemas.TITLE_CODE).getValue();
		}

		public void showSelectedTaskDetails(Object itemId) {
			TaskDetailsComponent taskDetailsComponent = getTaskDetailsComponent(itemId);
			taskDetailsComponent.setExpanded(true);
			//			containerItemSetChange(new ItemSetChangeEvent() {
			//				@Override
			//				public Container getContainer() {
			//					return getContainerDataSource();
			//				}
			//			});
		}

		public void hideSelectedTaskDetails(Object itemId) {
			TaskDetailsComponent taskDetailsComponent = getTaskDetailsComponent(itemId);
			taskDetailsComponent.setExpanded(false);
			//			containerItemSetChange(new ItemSetChangeEvent() {
			//				@Override
			//				public Container getContainer() {
			//					return getContainerDataSource();
			//				}
			//			});
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

		@SuppressWarnings("unchecked")
		@Override
		protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue, RecordVO recordVO) {
			Component metadataComponent;
			String metadataCode = metadataValue.getMetadata().getLocalCode();
			if (Task.TITLE.equals(metadataCode)) {
				boolean expanded = isSelected(itemId);
				metadataComponent = taskDetailsComponentFactory != null ? taskDetailsComponentFactory.newTaskDetailsComponent(TaskTable.this, itemId, recordVO, expanded) : new TaskDetailsComponent(itemId, recordVO, expanded);
			} else if (Task.STARRED_BY_USERS.equals(metadataCode)) {
				metadataComponent = new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(), presenter.getCurrentUserId()) {
					@Override
					public void updateTaskStarred(boolean isStarred, String taskId) {
						presenter.updateTaskStarred(isStarred, taskId);
					}
				};
			} else {
				metadataComponent = super.buildMetadataComponent(itemId, metadataValue, recordVO);
			}
			return metadataComponent;
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
		public void setContainerDataSource(Container newDataSource) {
			super.setContainerDataSource(newDataSource);
			Object[] visibleColumns = getVisibleColumns();
			List<Object> newVisibleColumns = new ArrayList<>();
			for (Object visibleColumn : visibleColumns) {
				if ((visibleColumn instanceof MetadataVO) && ((MetadataVO) visibleColumn).codeMatches(Task.STARRED_BY_USERS)) {
					newVisibleColumns.add(0, visibleColumn);
				} else if (!(visibleColumn instanceof MetadataVO) || !((MetadataVO) visibleColumn).codeMatches(Schemas.CODE.getLocalCode())) {
					newVisibleColumns.add(visibleColumn);
				}
			}
			setVisibleColumns(newVisibleColumns.toArray(new Object[0]));
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

	public FilterGenerator getFilterGenerator() {
		return filterGenerator;
	}

	public void setFilterGenerator(FilterGenerator filterGenerator) {
		this.filterGenerator = filterGenerator;
	}

}
