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
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextArea;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
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
        int l = table.getPageLength();
        int index = table.indexOfId(itemId);
        int indexToSelectAbove = index - (l/2);
        if( indexToSelectAbove<0 ) indexToSelectAbove=0;
        table.setCurrentPageFirstItemIndex(indexToSelectAbove);
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
		
		private TaskMenuBar(final RecordVO recordVO) {
			addStyleName(ValoTheme.MENUBAR_BORDERLESS);

			MenuItem rootItem = addItem("", FontAwesome.ELLIPSIS_V, null);

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
		}
	}

	private class TaskDetailsComponent extends VerticalLayout {
		
		private boolean expanded;
		
		private VerticalLayout expandLayout;
		
		private String dropFolderId;
		
		private TaskDetailsComponent(Object itemId, final RecordVO taskVO, boolean expanded) {
			this.expanded = expanded;
			setSizeFull();
			setSpacing(true);
			addStyleName("task-details");
			
			String createdById = taskVO.get(Schemas.CREATED_BY);
			LocalDateTime createdOnDate = taskVO.get(Schemas.CREATED_ON);
			String title = taskVO.getTitle();
			String description = taskVO.get(Task.DESCRIPTION);
			LocalDate dueDate = taskVO.get(Task.DUE_DATE);
			String assigneeId = taskVO.get(Task.ASSIGNEE);
			List<Comment> comments = taskVO.get(Task.COMMENTS);
			List<String> linkedFolderIds = taskVO.get(Task.LINKED_FOLDERS);
			List<String> linkedDocumentIds = taskVO.get(Task.LINKED_DOCUMENTS);
			List<String> linkedContainerIds = taskVO.get(Task.LINKED_CONTAINERS);
			List<ContentVersionVO> contents = taskVO.get(Task.CONTENTS);
			
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
			addComponent(taskDetailsTopLayout);
			
			Label titleLabel = new Label(title);
			titleLabel.addStyleName("task-details-title");
			addComponent(titleLabel);

			if (dueDate != null) {
				Label dueDateLabel = new Label($("TaskTable.details.dueDate", taskVO.getMetadata(Task.DUE_DATE).getLabel(), dueDate.toString()));
				dueDateLabel.addStyleName("task-details-due-date");
				addComponent(dueDateLabel);
			}
			
			expandLayout = new VerticalLayout();
			expandLayout.setSpacing(true);
			expandLayout.setWidth("100%");
			if (expanded) {
				addComponent(expandLayout);
			}
			
			if (assigneeId != null) {
				Component assigneeComponent = new UserDisplay(assigneeId);
				assigneeComponent.addStyleName("task-details-assignee");
				assigneeComponent.setCaption(taskVO.getMetadata(Task.ASSIGNEE).getLabel());
				expandLayout.addComponent(assigneeComponent);
			}
			
			if (StringUtils.isNotBlank(description)) {
				description = StringUtils.replace(description, "overflow:hidden", ""); // Ugly CSS Bugfix
				Label descriptionLabel = new Label(description, ContentMode.HTML);
				descriptionLabel.addStyleName("task-details-description");
				descriptionLabel.setWidth("100%");
				expandLayout.addComponent(descriptionLabel);
			}
			
			if (!contents.isEmpty()) {
				VerticalLayout contentsLayout = new VerticalLayout();
				contentsLayout.setCaption(taskVO.getMetadata(Task.CONTENTS).getLabel());
				contentsLayout.setWidth("100%");
				contentsLayout.setSpacing(true);
				contentsLayout.addStyleName("task-details-contents");
				expandLayout.addComponent(contentsLayout);
				
				for (ContentVersionVO contentVersionVO : contents) {
					String filename = contentVersionVO.getFileName();
					DownloadContentVersionLink downloadContentLink = new DownloadContentVersionLink(contentVersionVO, filename);
					contentsLayout.addComponent(downloadContentLink);
				}
			}
			
			if (!linkedDocumentIds.isEmpty()) {
				VerticalLayout linkedDocumentsLayout = new VerticalLayout();
				linkedDocumentsLayout.setCaption(taskVO.getMetadata(Task.LINKED_DOCUMENTS).getLabel());
				linkedDocumentsLayout.setWidth("100%");
				linkedDocumentsLayout.setSpacing(true);
				linkedDocumentsLayout.addStyleName("task-details-linked-documents");
				expandLayout.addComponent(linkedDocumentsLayout);
				
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
			}
			
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
				
				expandLayout.addComponent(taskFoldersTree);
			}
			
			if (!linkedContainerIds.isEmpty()) {
				VerticalLayout linkedContainersLayout = new VerticalLayout();
				linkedContainersLayout.setCaption(taskVO.getMetadata(Task.LINKED_CONTAINERS).getLabel());
				linkedContainersLayout.setWidth("100%");
				linkedContainersLayout.setSpacing(true);
				linkedContainersLayout.addStyleName("task-details-linked-containers");
				expandLayout.addComponent(linkedContainersLayout);
				
				for (String linkedContainerId : linkedContainerIds) {
					ReferenceDisplay referenceDisplay = new ReferenceDisplay(linkedContainerId);
					linkedContainersLayout.addComponent(referenceDisplay);
				}
			}
			
			final VerticalLayout commentsLayout = new VerticalLayout();
			commentsLayout.setCaption(taskVO.getMetadata(Task.COMMENTS).getLabel());
			commentsLayout.setWidth("100%");
			commentsLayout.setSpacing(true);
			commentsLayout.addStyleName("task-details-comments");
			
			final Label noCommentLabel = new Label($("TaskTable.details.noComment"));
			noCommentLabel.addStyleName("task-details-no-comment");
			if (comments.isEmpty()) {
				commentsLayout.addComponent(noCommentLabel);
			}
			
			WindowButton addCommentButton = new WindowButton($("TaskTable.details.addComment"), $("TaskTable.details.addComment"), WindowConfiguration.modalDialog("400px", "280px")) {
				@Override
				protected Component buildWindowContent() {
					Comment newComment = new Comment();
					BaseTextArea commentField = new BaseTextArea();
					commentField.setWidth("100%");
					FieldAndPropertyId commentFieldAndPropertyId = new FieldAndPropertyId(commentField, "message"); 
					BaseForm<Comment> commentForm = new BaseForm<Comment>(newComment, Arrays.asList(commentFieldAndPropertyId)) {
						@Override
						protected void saveButtonClick(Comment newComment) throws ValidationException {
							if (presenter.taskCommentAdded(taskVO, newComment)) {
								addComment(newComment, commentsLayout);
							}
							getWindow().close();
						}

						@Override
						protected void cancelButtonClick(Comment newComment) {
							getWindow().close();
						}
					};	
					return commentForm;
				}
			};
			addCommentButton.setIcon(FontAwesome.PLUS);
			addCommentButton.addStyleName(ValoTheme.BUTTON_LINK);
			addCommentButton.addStyleName("task-details-add-comment-button");
			expandLayout.addComponent(addCommentButton);
			expandLayout.setComponentAlignment(addCommentButton, Alignment.TOP_RIGHT);

			expandLayout.addComponent(commentsLayout);
			
			for (Comment comment : comments) {
				addComment(comment, commentsLayout);
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
		
		private void addComment(Comment comment, VerticalLayout commentsLayout) {
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
			Label messageLabel = new Label(message);
			messageLabel.addStyleName("task-details-comment-message");
			
			commentsLayout.addComponents(userTimeLayout, messageLabel);
		}
	}
	
	private void addSelectClickListener(AbstractOrderedLayout layout, final Object itemId) {
		layout.addLayoutClickListener(new LayoutClickListener() {
			@Override
			public void layoutClick(LayoutClickEvent event) {
				Component clickedComponent = event.getClickedComponent();
				if (!(clickedComponent instanceof Button) && !(clickedComponent instanceof Link) && !(clickedComponent instanceof Tree) && !(clickedComponent instanceof ReferenceDisplay)) {
					toggleSelection(itemId);
				}
			}
		});
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

		RecordVO getDocumentVO(String linkedDocumentId);

		boolean taskCommentAdded(RecordVO taskVO, Comment newComment);

		boolean taskFolderOrDocumentClicked(RecordVO taskVO, String recordId);

		BaseRecordTreeDataProvider getTaskFoldersTreeDataProvider(RecordVO taskVO);
		
		void contentVersionUploaded(ContentVersionVO uploadedContentVO, String folderId, LazyTreeDataProvider<String> treeDataProvider);
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
				metadataComponent = new TaskDetailsComponent(itemId, recordVO, isSelected(itemId));
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
				if (!(visibleColumn instanceof MetadataVO) || !((MetadataVO) visibleColumn).codeMatches(Schemas.CODE.getLocalCode())) {
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
