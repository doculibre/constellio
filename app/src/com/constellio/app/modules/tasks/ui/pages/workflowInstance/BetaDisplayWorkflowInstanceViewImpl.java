package com.constellio.app.modules.tasks.ui.pages.workflowInstance;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowInstanceVO;
import com.constellio.app.modules.tasks.ui.entities.BetaWorkflowTaskProgressionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.converters.EnumWithSmallCodeToCaptionConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateToStringConverter;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.NoDragAndDrop;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;

public class BetaDisplayWorkflowInstanceViewImpl extends BaseViewImpl implements BetaDisplayWorkflowInstanceView, NoDragAndDrop {

	private BetaWorkflowInstanceVO workflowInstanceVO;

	private List<BetaWorkflowTaskProgressionVO> workflowTaskProgressionVOs = new ArrayList<>();

	private VerticalLayout mainLayout;

	private RecordDisplay workflowInstanceDisplay;

	private TreeTable workflowTaskVOTable;

	private BetaDisplayWorkflowInstancePresenter presenter;

	private JodaDateToStringConverter jodaDateToStringConverter = new JodaDateToStringConverter();

	private EnumWithSmallCodeToCaptionConverter taskStatusToCaptionConverter = new EnumWithSmallCodeToCaptionConverter(
			TaskStatusType.class);

	public BetaDisplayWorkflowInstanceViewImpl() {
		presenter = new BetaDisplayWorkflowInstancePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setWorkflowInstanceVO(BetaWorkflowInstanceVO workflowInstanceVO) {
		this.workflowInstanceVO = workflowInstanceVO;
	}

	@Override
	public void setWorkflowTaskProgressionVOs(List<BetaWorkflowTaskProgressionVO> workflowTaskProgressionVOs) {
		this.workflowTaskProgressionVOs = workflowTaskProgressionVOs;
	}

	@Override
	protected String getTitle() {
		return $("DisplayWorkflowInstanceView.viewTitle");
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
		buildTasksTable();

		mainLayout.addComponents(workflowInstanceDisplay, workflowTaskVOTable);
		mainLayout.setExpandRatio(workflowTaskVOTable, 1);

		return mainLayout;
	}

	private void buildWorkflowDisplay() {
		workflowInstanceDisplay = new RecordDisplay(workflowInstanceVO);
	}

	private void buildTasksTable() {
		workflowTaskVOTable = new TreeTable($("DisplayWorkflowInstanceView.tableTitle"));
		workflowTaskVOTable.setDragMode(TableDragMode.ROW);
		workflowTaskVOTable.setWidth("100%");

		final HierarchicalContainer container = new HierarchicalContainer();
		container.addContainerProperty("title", String.class, "");
		container.addContainerProperty("status", String.class, "");
		container.addContainerProperty("decision", String.class, "");
		container.addContainerProperty("dueDate", String.class, "");

		for (BetaWorkflowTaskProgressionVO workflowTaskProgressionVO : workflowTaskProgressionVOs) {
			addToTable(workflowTaskProgressionVO, container);
		}

		workflowTaskVOTable.setColumnHeader("title", $("DisplayWorkflowInstanceView.table.title"));
		workflowTaskVOTable.setColumnHeader("status", $("DisplayWorkflowInstanceView.table.status"));
		workflowTaskVOTable.setColumnHeader("decision", $("DisplayWorkflowInstanceView.table.decision"));
		workflowTaskVOTable.setColumnHeader("dueDate", $("DisplayWorkflowInstanceView.table.dueDate"));

		workflowTaskVOTable.setColumnExpandRatio("title", 1);
		workflowTaskVOTable.setContainerDataSource(container);

		workflowTaskVOTable.setCellStyleGenerator(new TaskStyleGenerator());
	}

	@SuppressWarnings("unchecked")
	private void addToTable(BetaWorkflowTaskProgressionVO workflowTaskProgressionVO, HierarchicalContainer container) {
		Item item = container.addItem(workflowTaskProgressionVO);
		String title = workflowTaskProgressionVO.getTitle();
		TaskStatusType status = workflowTaskProgressionVO.getStatus();
		String decision = workflowTaskProgressionVO.getDecision();
		LocalDate dueDate = workflowTaskProgressionVO.getDueDate();

		Locale locale = getLocale();
		String statusStr =
				status != null ? taskStatusToCaptionConverter.convertToPresentation(status.getCode(), String.class, locale) : "";
		String dueDateStr = jodaDateToStringConverter.convertToPresentation(dueDate, String.class, locale);

		item.getItemProperty("title").setValue(title);
		item.getItemProperty("status").setValue(statusStr);
		item.getItemProperty("decision").setValue(decision);
		item.getItemProperty("dueDate").setValue(dueDateStr);

		List<BetaWorkflowTaskProgressionVO> children = presenter.getChildren(workflowTaskProgressionVO);
		for (BetaWorkflowTaskProgressionVO child : children) {
			// Recursive call
			addToTable(child, container);
			container.setParent(child, workflowTaskProgressionVO);
		}
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
				BetaWorkflowTaskProgressionVO workflowTaskProgressionVO = (BetaWorkflowTaskProgressionVO) itemId;
				if (presenter.isFinished(workflowTaskProgressionVO)) {
					style = FINISHED_TASK_STYLE;
				} else if (presenter.isTaskOverDue(workflowTaskProgressionVO)) {
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
