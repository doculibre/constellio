package com.constellio.app.modules.tasks.ui.components;

import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.modules.tasks.extensions.api.TaskModuleExtensions;
import com.constellio.app.modules.tasks.extensions.ui.TaskTableExtension.TaskTableColumnsExtensionParams;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable.TaskDetailsComponentFactory;
import com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable.TaskMenuBar;
import com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable.TaskPresenter;
import com.constellio.app.modules.tasks.ui.components.fields.StarredFieldImpl;
import com.constellio.app.modules.tasks.ui.entities.TaskVO;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsDisplayImpl;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.RecordVOTableColumnsManager;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.constellio.app.modules.tasks.ui.components.ExpandableTaskTable.MENUBAR_COLUMN_ID;
import static com.constellio.app.ui.i18n.i18n.$;

public class LegacyTaskTable extends RecordVOTable implements TaskTable {
	public static final String PREFIX = "images/icons/task/";
	public static final ThemeResource COMPLETE_ICON = new ThemeResource(PREFIX + "task.png");
	public static final ThemeResource CLOSE_ICON = new ThemeResource(PREFIX + "task_complete.png");

	private final TaskPresenter presenter;
	private TaskDetailsComponentFactory taskDetailsComponentFactory;

	public LegacyTaskTable(RecordVODataProvider provider, TaskPresenter presenter) {
		super($("TaskTable.caption", provider.size()));
		this.presenter = presenter;
		setContainerDataSource(buildContainer(provider));
		setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 200);
		setCellStyleGenerator(new TaskStyleGenerator());
		setPageLength(Math.min(15, provider.size()));
		setWidth("100%");

		addExtraGeneratedColumns();

		addDisplayOnClickListener();
	}

	private void addExtraGeneratedColumns() {
		AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		if (sessionContext != null) {
			String collection = sessionContext.getCurrentCollection();
			TaskModuleExtensions extensions = appLayerFactory.getExtensions().forCollection(collection).forModule(TaskModule.ID);
			if (extensions != null) {
				extensions.addTaskTableExtraColumns(new TaskTableColumnsExtensionParams(this::addGeneratedColumn, this::setColumnHeader, this::setColumnWidth));
			}
		}
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
					displayTask(null, recordVO);
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
		addGeneratedColumn(MENUBAR_COLUMN_ID, new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				RecordVO recordVO = records.getRecordVO((int) itemId);
				return new TaskMenuBar(recordVO, presenter, LegacyTaskTable.this);
			}
		});
		setColumnHeader(MENUBAR_COLUMN_ID, "");
		setColumnWidth(MENUBAR_COLUMN_ID, 40);

		return records;
	}

	@Override
	public void addCompleteWindowCommentField(RecordVO taskVO, Field commentField, VerticalLayout fieldLayout) {
		fieldLayout.addComponent(commentField);
		if (taskDetailsComponentFactory != null) {
			taskDetailsComponentFactory.decorateCompleteWindowCommentField(taskVO, commentField, fieldLayout);
		}
	}

	public void setTaskDetailsComponentFactory(TaskDetailsComponentFactory taskDetailsComponentFactory) {
		this.taskDetailsComponentFactory = taskDetailsComponentFactory;
	}

	@Override
	public void displayTask(Object itemId, RecordVO taskVO) {
		presenter.setReadByUser(taskVO, true);
		presenter.registerPreviousSelectedTab();
		presenter.displayButtonClicked(taskVO);
	}

	@Override
	public void editTask(RecordVO recordVO) {
		presenter.setReadByUser(recordVO, true);
		presenter.registerPreviousSelectedTab();
		presenter.editButtonClicked(recordVO);
	}

	@Override
	protected Component buildMetadataComponent(Object itemId, MetadataValueVO metadataValue, RecordVO recordVO) {
		if (Task.STARRED_BY_USERS.equals(metadataValue.getMetadata().getLocalCode())) {
			return new StarredFieldImpl(recordVO.getId(), (List<String>) metadataValue.getValue(), presenter.getCurrentUserId()) {
				@Override
				public void updateTaskStarred(boolean isStarred, String taskId) {
					presenter.updateTaskStarred(isStarred, taskId);
				}
			};
		} else {
			Component component = super.buildMetadataComponent(itemId, metadataValue, recordVO);

			if (component instanceof RecordCommentsDisplayImpl) {


				RecordCommentsDisplayImpl recordCommentsDisplayImpl = (RecordCommentsDisplayImpl) component;
				recordCommentsDisplayImpl.setForcedReadOnly(true);

				if (taskDetailsComponentFactory != null) {

					List<Comment> consolidatedComments = taskDetailsComponentFactory.getAdditionalCommentsProvider().apply(recordVO);

					if (consolidatedComments != null) {
						recordCommentsDisplayImpl.setComments(consolidatedComments);
					}
				}

			}

			return component;
		}
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
