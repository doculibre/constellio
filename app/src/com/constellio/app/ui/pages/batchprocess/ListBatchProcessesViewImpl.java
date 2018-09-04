package com.constellio.app.ui.pages.batchprocess;

import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.BatchProcessContainer;
import com.constellio.app.ui.framework.data.BatchProcessDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListBatchProcessesViewImpl extends BaseViewImpl implements ListBatchProcessesView {

	private BatchProcessDataProvider userBatchProcessDataProvider;
	private BatchProcessDataProvider systemBatchProcessDataProvider;

	private ListBatchProcessesPresenter presenter;

	public ListBatchProcessesViewImpl() {
		this.presenter = new ListBatchProcessesPresenter(this);
	}

	@Override
	public void setUserBatchProcesses(BatchProcessDataProvider dataProvider) {
		this.userBatchProcessDataProvider = dataProvider;
	}

	@Override
	public void setSystemBatchProcesses(BatchProcessDataProvider dataProvider) {
		this.systemBatchProcessDataProvider = dataProvider;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("batch-processes");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		if (systemBatchProcessDataProvider != null) {
			BaseTable userBatchProcessTable = newTable(false, null);
			BaseTable systemBatchProcessTable = newTable(true, null);

			TabSheet tabSheet = new TabSheet();
			tabSheet.addTab(userBatchProcessTable, $("ListBatchProcessesView.userBatchProcesses"));
			tabSheet.addTab(systemBatchProcessTable, $("ListBatchProcessesView.systemBatchProcesses"));

			mainLayout.addComponent(tabSheet);
		} else {
			BaseTable userBatchProcessTable = newTable(false, $("ListBatchProcessesView.userBatchProcesses"));
			mainLayout.addComponent(userBatchProcessTable);
		}

		return mainLayout;
	}

	private BaseTable newTable(boolean systemBatchProcesses, String caption) {
		BatchProcessContainer container;
		BaseTable batchProcessTable;
		if (systemBatchProcesses) {
			container = new BatchProcessContainer(systemBatchProcessDataProvider, systemBatchProcesses, presenter);
			batchProcessTable = new BaseTable("ListBatchProcessesView.systemBatchProcesses", caption);
		} else {
			container = new BatchProcessContainer(userBatchProcessDataProvider, systemBatchProcesses, presenter);
			batchProcessTable = new BaseTable("ListBatchProcessesView.userBatchProcesses", $("ListBatchProcessesView.userBatchProcesses"));
		}
		batchProcessTable.setContainerDataSource(container);

		batchProcessTable.setColumnHeader("rank", $("ListBatchProcessesView.rank"));
		batchProcessTable.setColumnHeader("title", $("ListBatchProcessesView.title"));
		batchProcessTable.setColumnHeader("status", $("ListBatchProcessesView.status"));
		batchProcessTable.setColumnHeader("requestDateTime", $("ListBatchProcessesView.requestDateTime"));
		batchProcessTable.setColumnHeader("startDateTime", $("ListBatchProcessesView.startDateTime"));
		batchProcessTable.setColumnHeader("handledRecordsCount", $("ListBatchProcessesView.handledRecordsCount"));
		batchProcessTable.setColumnHeader("totalRecordsCount", $("ListBatchProcessesView.totalRecordsCount"));
		batchProcessTable.setColumnHeader("progress", $("ListBatchProcessesView.progress"));
		batchProcessTable.setColumnHeader("username", $("ListBatchProcessesView.username"));
		batchProcessTable.setColumnHeader("collection", $("ListBatchProcessesView.collection"));
		batchProcessTable.setColumnHeader("report", $("ListBatchProcessesView.report"));

		batchProcessTable.setWidth("98%");

		return batchProcessTable;
	}

	@Override
	public boolean isBackgroundViewMonitor() {
		return true;
	}

	@Override
	protected void onBackgroundViewMonitor() {
		presenter.backgroundViewMonitor();
	}

	@Override
	protected String getTitle() {
		return $("ListBatchProcessesView.viewTitle");
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

}
