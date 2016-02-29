package com.constellio.app.modules.tasks.ui.pages.workflow;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableFooterEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableHeaderEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuOpenedOnTableRowEvent;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.ui.entities.WorkflowTaskVO;
import com.constellio.app.modules.tasks.ui.entities.WorkflowVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.contextmenu.BaseContextMenuTableListener;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveTextField;
import com.constellio.app.ui.framework.components.fields.record.RecordComboBox;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.NoDragAndDrop;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.Table.TableTransferable;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class DisplayWorkflowViewImpl extends BaseViewImpl implements DisplayWorkflowView, NoDragAndDrop {

	private WorkflowVO workflowVO;

	private List<WorkflowTaskVO> workflowTaskVOs = new ArrayList<>();

	private RecordVODataProvider taskTypeDataProvider;

	private WorkflowTaskVO selectedWorkflowTaskVO;

	private VerticalLayout mainLayout;

	private RecordDisplay workflowDisplay;

	private Button addTaskButton;

	private TreeTable workflowTaskVOTable;

	private Button editButton;

	private Button deleteButton;

	private Window addTaskWindow;

	private DisplayWorkflowPresenter presenter;

	private static ConfirmDialog.Factory factory = new DefaultConfirmDialogFactory();

	static {
		ConfirmDialog.setFactory(new ConfirmDialog.Factory() {
			@Override
			public ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle,
					String notOKCaption) {
				ConfirmDialog confirmDialog = factory.create(windowCaption, message, okTitle, cancelTitle, notOKCaption);
				confirmDialog.addAttachListener(new AttachListener() {
					@Override
					public void attach(AttachEvent event) {
						BaseWindow.executeZIndexAdjustJavascript(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
					}
				});
				return confirmDialog;
			}
		});
	}

	public DisplayWorkflowViewImpl() {
		presenter = new DisplayWorkflowPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setTaskTypeDataProvider(RecordVODataProvider taskTypeDataProvider) {
		this.taskTypeDataProvider = taskTypeDataProvider;
	}

	@Override
	public void setWorkflowVO(WorkflowVO workflowVO) {
		this.workflowVO = workflowVO;
	}

	@Override
	public void setWorkflowTaskVOs(List<WorkflowTaskVO> workflowTaskVOs) {
		this.workflowTaskVOs = workflowTaskVOs;
	}

	@Override
	public void remove(WorkflowTaskVO workflowTaskVO) {
		List<WorkflowTaskVO> children = presenter.getChildren(workflowTaskVO);
		for (WorkflowTaskVO child : children) {
			// Recursive call
			remove(child);
		}
		workflowTaskVOTable.removeItem(workflowTaskVO);
	}

	@Override
	protected String getTitle() {
		return $("DisplayWorkflowView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		buildWorkflowDisplay();
		buildAddTaskButton();
		workflowTaskVOTable = buildTasksTable();
		buildContextMenu(workflowTaskVOTable);
		buildAddTaskWindow();

		mainLayout.addComponents(workflowDisplay, addTaskButton, workflowTaskVOTable);
		mainLayout.setExpandRatio(workflowTaskVOTable, 1);
		mainLayout.setComponentAlignment(addTaskButton, Alignment.TOP_RIGHT);

		return mainLayout;
	}

	private void buildWorkflowDisplay() {
		workflowDisplay = new RecordDisplay(workflowVO);
	}

	private void buildAddTaskButton() {
		addTaskButton = new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addTaskButtonClicked();
			}
		};
	}

	private TreeTable buildTasksTable() {
		TreeTable newWorkflowTaskVOTable = new TreeTable($("DisplayWorkflowView.tableTitle"));
		newWorkflowTaskVOTable.setDragMode(TableDragMode.ROW);
		newWorkflowTaskVOTable.setWidth("100%");

		final HierarchicalContainer container = new HierarchicalContainer();
		container.addContainerProperty("title", String.class, "");

		newWorkflowTaskVOTable.setDropHandler(new DropHandler() {
			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}

			@Override
			public void drop(DragAndDropEvent event) {
				TableTransferable transferable = (TableTransferable) event.getTransferable();
				AbstractSelectTargetDetails targetDetails = (AbstractSelectTargetDetails) event.getTargetDetails();

				WorkflowTaskVO droppedItemId = (WorkflowTaskVO) transferable.getItemId();
				WorkflowTaskVO targetItemId = (WorkflowTaskVO) targetDetails.getItemIdOver();

				Object droppedItemParent = container.getParent(droppedItemId);
				Object targetItemParent = container.getParent(targetItemId);

				boolean accept;
				if (droppedItemParent == null && targetItemParent == null) {
					accept = true;
				} else if (ObjectUtils.equals(droppedItemParent, targetItemParent)) {
					accept = true;
				} else {
					accept = false;
				}

				if (accept && presenter.moveAfter(droppedItemId, targetItemId)) {
					container.moveAfterSibling(droppedItemId, targetItemId);
				}
			}
		});

		for (WorkflowTaskVO workflowTaskVO : workflowTaskVOs) {
			addToTable(workflowTaskVO, container, null);
		}

		newWorkflowTaskVOTable.setColumnHeader("title", $("DisplayWorkflowView.table.title"));
		newWorkflowTaskVOTable.setColumnExpandRatio("title", 1);
		newWorkflowTaskVOTable.setContainerDataSource(container);

		return newWorkflowTaskVOTable;
	}

	@SuppressWarnings("unchecked")
	private void addToTable(WorkflowTaskVO workflowTaskVO, HierarchicalContainer container, WorkflowTaskVO parentWorkflowTaskVO) {
		if (container.getItem(workflowTaskVO) != null) {
			WorkflowTaskVO newWorkflowTaskVO = new WorkflowTaskVO();
			String title = $("DisplayWorkflowView.redirect", workflowTaskVO.getTitle());
			newWorkflowTaskVO.setTitle(title);
			Item item = container.addItem(newWorkflowTaskVO);
			item.getItemProperty("title").setValue(title);

			if (parentWorkflowTaskVO != null) {
				container.setParent(newWorkflowTaskVO, parentWorkflowTaskVO);
			}
		} else {
			Item item = container.addItem(workflowTaskVO);
			String title = workflowTaskVO.getTitle();
			item.getItemProperty("title").setValue(title);

			List<WorkflowTaskVO> children = presenter.getChildren(workflowTaskVO);
			for (WorkflowTaskVO child : children) {
				boolean setParent = true;
				if (container.getItem(child) != null) {
					setParent = false;
				}
				// Recursive call
				addToTable(child, container, workflowTaskVO);
				if (setParent) {
					container.setParent(child, workflowTaskVO);
				} else {
					break;
				}
			}
		}
	}

	private void buildContextMenu(TreeTable workflowTaskVOTable) {
		ContextMenu contextMenu = new ContextMenu();

		ContextMenuItem createTaskAfterItem = contextMenu.addItem($("DisplayWorkflowView.menu.createTaskAfter"));
		createTaskAfterItem.addItemClickListener(new ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				presenter.createTaskSelected(selectedWorkflowTaskVO);
			}
		});

		ContextMenuItem editTaskItem = contextMenu.addItem($("DisplayWorkflowView.menu.editTask"));
		editTaskItem.addItemClickListener(new ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				presenter.editTaskSelected(selectedWorkflowTaskVO);
			}
		});

		ContextMenuItem deleteTaskItem = contextMenu.addItem($("DisplayWorkflowView.menu.deleteTask"));
		deleteTaskItem.addItemClickListener(new ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				ConfirmDialog.show(
						UI.getCurrent(),
						$("ConfirmDialog.title"),
						$("ConfirmDialog.confirmDelete"),
						$("ConfirmDialog.yes"),
						$("ConfirmDialog.no"),
						new ConfirmDialog.Listener() {
							public void onClose(ConfirmDialog dialog) {
								if (dialog.isConfirmed()) {
									presenter.deleteTaskSelected(selectedWorkflowTaskVO);
								}
							}
						});
			}
		});

		if (Toggle.ADD_EXISTING_TASK.isEnabled()) {
			ContextMenuItem createExistingTaskAfterItem = contextMenu
					.addItem($("DisplayWorkflowView.menu.createExistingTaskAfter"));
			createExistingTaskAfterItem.addItemClickListener(new ContextMenuItemClickListener() {
				@Override
				public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
					//TODO Thiago if reference, create after Reference ...create reference
					if (selectedWorkflowTaskVO.getTaskVO() != null) {
						presenter.createExistingTaskSelected(selectedWorkflowTaskVO);
					}
				}
			});
		}

		contextMenu.setAsTableContextMenu(workflowTaskVOTable);
		BaseContextMenuTableListener contextMenuTableListener = new BaseContextMenuTableListener() {
			@Override
			public void onContextMenuOpenFromFooter(ContextMenuOpenedOnTableFooterEvent event) {
			}

			@Override
			public void onContextMenuOpenFromHeader(ContextMenuOpenedOnTableHeaderEvent event) {
			}

			@Override
			public void onContextMenuOpenFromRow(ContextMenuOpenedOnTableRowEvent event) {
				selectedWorkflowTaskVO = (WorkflowTaskVO) event.getItemId();
			}
		};
		contextMenu.addContextMenuTableListener(contextMenuTableListener);
	}

	private void buildAddTaskWindow() {
		addTaskWindow = new BaseWindow($("DisplayWorkflowView.addTaskWindow.windowTitle"));
		addTaskWindow.center();
		addTaskWindow.setModal(true);
		addTaskWindow.setWidth("80%");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		editButton = new EditButton(false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}
		};

		deleteButton = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}
		};
		return Arrays.asList(editButton, deleteButton);
	}

	@Override
	public void openAddTaskWindow(WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs) {
		addTaskWindow.setContent(new AddTaskWindowContent(workflowTaskVO, availableTaskVOs));
		UI.getCurrent().addWindow(addTaskWindow);
	}

	@Override
	public void openExistingTasksWindow(WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs) {
		addTaskWindow.setContent(buildExistingTasksContent(workflowTaskVO, availableTaskVOs));
		UI.getCurrent().addWindow(addTaskWindow);
	}

	@Override
	public void closeAddTaskWindow() {
		addTaskWindow.close();
	}

	public class AddTaskWindowContent extends Panel {

		private WorkflowTaskVO workflowTaskVO;

		private String taskType;

		private String taskTitle;

		private List<String> decisions = new ArrayList<>();

		private BaseForm<AddTaskWindowContent> baseForm;

		@PropertyId("taskType")
		private RecordComboBox typeField;

		@PropertyId("taskTitle")
		private TextField taskTitleField;

		@PropertyId("workflowTaskVO")
		private ComboBox workflowTaskVOField;

		@PropertyId("decisions")
		private ListAddRemoveTextField decisionsField;

		public AddTaskWindowContent(WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs) {
			this.workflowTaskVO = workflowTaskVO;

			typeField = new RecordComboBox(TaskType.DEFAULT_SCHEMA);
			typeField.setCaption($("DisplayWorkflowView.addTaskWindow.taskType"));
			typeField.setDataProvider(taskTypeDataProvider);

			taskTitleField = new BaseTextField($("DisplayWorkflowView.addTaskWindow.taskTitle"));

			workflowTaskVOField = new BaseComboBox();
			workflowTaskVOField.setCaption($("DisplayWorkflowView.addTaskWindow.workflowTaskVO"));
			for (WorkflowTaskVO availableTaskVO : availableTaskVOs) {
				workflowTaskVOField.addItem(availableTaskVO);
				workflowTaskVOField.setItemCaption(availableTaskVO, availableTaskVO.getTitle());
			}
			workflowTaskVOField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					boolean decisionsFieldVisible;
					WorkflowTaskVO newValue = (WorkflowTaskVO) event.getProperty().getValue();
					if (newValue != null) {
						decisionsFieldVisible = presenter.isDecisionField(newValue);
					} else {
						decisionsFieldVisible = false;
					}
					decisionsField.setVisible(decisionsFieldVisible);
				}
			});

			decisionsField = new ListAddRemoveTextField();
			decisionsField.setCaption($("DisplayWorkflowView.addTaskWindow.decisions"));
			if (workflowTaskVO != null && !presenter.isDecisionField(workflowTaskVO)) {
				decisionsField.setVisible(false);
			}

			baseForm = new BaseForm<AddTaskWindowContent>(this, this, typeField, taskTitleField, workflowTaskVOField,
					decisionsField) {
				@Override
				protected void saveButtonClick(AddTaskWindowContent viewObject)
						throws ValidationException {
					presenter.saveNewTaskButtonClicked(taskType, taskTitle, decisions, AddTaskWindowContent.this.workflowTaskVO);
				}

				@Override
				protected void cancelButtonClick(AddTaskWindowContent viewObject) {
					presenter.cancelNewTaskButtonClicked();
				}
			};
			setContent(baseForm);
		}

		public WorkflowTaskVO getWorkflowTaskVO() {
			return workflowTaskVO;
		}

		public void setWorkflowTaskVO(WorkflowTaskVO workflowTaskVO) {
			this.workflowTaskVO = workflowTaskVO;
		}

		public String getTaskType() {
			return taskType;
		}

		public void setTaskType(String taskType) {
			this.taskType = taskType;
		}

		public String getTaskTitle() {
			return taskTitle;
		}

		public void setTaskTitle(String taskTitle) {
			this.taskTitle = taskTitle;
		}

		public List<String> getDecisions() {
			return decisions;
		}

		public void setDecisions(List<String> decisions) {
			this.decisions = decisions;
		}

	}

	protected Component buildExistingTasksContent(final WorkflowTaskVO workflowTaskVO, List<WorkflowTaskVO> availableTaskVOs) {

		final ComboBox workflowTaskVOField = new BaseComboBox();
		workflowTaskVOField.setCaption($("DisplayWorkflowView.addTaskWindow.availableTasks"));
		for (WorkflowTaskVO availableTaskVO : availableTaskVOs) {
			workflowTaskVOField.addItem(availableTaskVO);
			workflowTaskVOField.setItemCaption(availableTaskVO, availableTaskVO.getTitle());
		}

		BaseButton saveButton = new BaseButton($("save")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				WorkflowTaskVO selectedWorkflowTaskVO = (WorkflowTaskVO) workflowTaskVOField.getValue();
				if (selectedWorkflowTaskVO != null) {
					//TODO Thiago
					presenter.addExistingTaskAfter(selectedWorkflowTaskVO, workflowTaskVO);
				}
			}
		};
		saveButton.addStyleName(BaseForm.SAVE_BUTTON);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		BaseButton cancelButton = new BaseButton($("cancel")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				addTaskWindow.close();
			}
		};
		saveButton.addStyleName(BaseForm.CANCEL_BUTTON);
		cancelButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.addComponents(saveButton, cancelButton);

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout
				.addComponents(workflowTaskVOField, horizontalLayout);
		verticalLayout.setSpacing(true);

		return verticalLayout;
	}

}
