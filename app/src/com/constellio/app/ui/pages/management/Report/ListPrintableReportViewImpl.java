package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.PrintableReportVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.GetXmlButtonV2;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ListPrintableReportViewImpl extends BaseViewImpl implements ListPrintableReportView {

	public static final String TYPE_TABLE = "types";

	private static final PrintableReportListPossibleType[] SCHEMA_INDEX_ARRAY = new PrintableReportListPossibleType[]{PrintableReportListPossibleType.FOLDER, PrintableReportListPossibleType.DOCUMENT, PrintableReportListPossibleType.TASK};

	private List<PrintableReportVO> printableReportVOS;

	private TabSheet tabSheet;
	private PrintableReportListPossibleType currentSchema;
	private ListPrintableReportPresenter presenter = new ListPrintableReportPresenter(this);
	private VerticalLayout mainLayout;
	private Button addLabelButton, downloadTemplateButton;
	private GetXmlButtonV2 getXmlButtonV2;
	List<TabWithTable> tabs = new ArrayList<>();

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		this.currentSchema = PrintableReportListPossibleType.FOLDER;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ViewGroup.PrintableViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().viewReport();
					}
				});
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		mainLayout = new VerticalLayout();

		tabSheet = new TabSheet();
		TabWithTable folderTab = new TabWithTable(PrintableReportListPossibleType.FOLDER) {
			@Override
			public Table buildTable() {
				return createFolderTable();
			}
		};
		TabWithTable documentTab = new TabWithTable(PrintableReportListPossibleType.DOCUMENT) {
			@Override
			public Table buildTable() {
				return createDocumentTable();
			}
		};
		TabWithTable taskTab = new TabWithTable(PrintableReportListPossibleType.TASK) {
			@Override
			public Table buildTable() {
				return createTaskTable();
			}
		};

		tabs.addAll(asList(folderTab, documentTab, taskTab));
		tabSheet.addTab(folderTab.getTabLayout(), $("PrintableReport.tabs.folderTitle"));
		tabSheet.addTab(documentTab.getTabLayout(), $("PrintableReport.tabs.documentTitle"));
		tabSheet.addTab(taskTab.getTabLayout(), $("PrintableReport.tabs.taskTitle"));
		tabSheet.addSelectedTabChangeListener(new TabsChangeListener());
		mainLayout.addComponent(tabSheet);
		return mainLayout;
	}

	private Table createFolderTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportFolderDataProvider());
		ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container, "buttons");
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDisplayButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportEditButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDeleteButton(report);
			}
		});
		//		container = buttonsContainerForFolder;
		Table tableFolder = new RecordVOTable(buttonsContainerForFolder);
		tableFolder = setTableProperty(tableFolder, buttonsContainerForFolder.size());
		return tableFolder;
		//return new PrintableReportColumnGenerator(presenter, PrintableReportListPossibleType.FOLDER).withRecordSchema().withRecordType().attachTo((BaseTable) tableFolder);
	}

	private Table createDocumentTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportDocumentDataProvider());
		ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container, "buttons");
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDisplayButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportEditButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDeleteButton(report);
			}
		});
		//		container = buttonsContainerForFolder;
		Table tableFolder = new RecordVOTable(buttonsContainerForFolder);
		tableFolder = setTableProperty(tableFolder, buttonsContainerForFolder.size());
		return tableFolder;
		//return new PrintableReportColumnGenerator(presenter, PrintableReportListPossibleType.DOCUMENT).withRecordSchema().withRecordType().attachTo((BaseTable) tableFolder);
	}

	private Table createTaskTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportTaskDataProvider());
		ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container, "buttons");
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDisplayButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportEditButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDeleteButton(report);
			}
		});
		//		container = buttonsContainerForFolder;
		//		HorizontalLayout buttonLayout = new HorizontalLayout();
		//		buttonLayout.addComponents();
		Table tableFolder = new RecordVOTable(buttonsContainerForFolder);
		tableFolder = setTableProperty(tableFolder, buttonsContainerForFolder.size());
		return tableFolder;
		//return new PrintableReportColumnGenerator(presenter, PrintableReportListPossibleType.TASK).withRecordSchema().withRecordType().withButtonContainer().attachTo((BaseTable) tableFolder);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		addLabelButton = new AddButton($("PrintableReport.menu.addNewReport")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addLabelButtonClicked();
			}
		};

		getXmlButtonV2 = new GetXmlButtonV2($("DisplayLabelViewImpl.menu.getXMLButton"), $("DisplayLabelViewImpl.menu.getXMLButton"), getConstellioFactories().getAppLayerFactory(), getCollection(), this, currentSchema, true);

		downloadTemplateButton = new Button($("DisplayLabelViewImpl.menu.getTemplate"));
		StreamResource zip = presenter.createResource();
		FileDownloader fileDownloader = new FileDownloader(zip);
		fileDownloader.extend(downloadTemplateButton);


		actionMenuButtons.add(addLabelButton);
		actionMenuButtons.add(getXmlButtonV2);
		actionMenuButtons.add(downloadTemplateButton);
		return actionMenuButtons;
	}

	@Override
	protected String getTitle() {
		return $("PrintableReport.title");
	}

	private class PrintableReportEditButton extends EditButton {
		private RecordVO report;

		public PrintableReportEditButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.editButtonClicked(report);
		}

		@Override
		public boolean isVisible() {
			if (report == null) {
				return false;
			} else {
				return report.get(Printable.ISDELETABLE).equals(true);
			}
		}
	}

	private class PrintableReportDisplayButton extends DisplayButton {
		private RecordVO report;

		public PrintableReportDisplayButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.displayButtonClicked(report);
		}
	}

	private class PrintableReportDeleteButton extends DeleteButton {
		private RecordVO report;

		public PrintableReportDeleteButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void confirmButtonClick(ConfirmDialog dialog) {
			presenter.removeRecord(report);
		}

		@Override
		public boolean isVisible() {
			if (report == null) {
				return false;
			} else {
				return report.get(Printable.ISDELETABLE).equals(true);
			}
		}
	}

	private class TabsChangeListener implements TabSheet.SelectedTabChangeListener {

		@Override
		public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
			for (TabWithTable tab : tabs) {
				if (tab.getTabLayout().equals(event.getTabSheet().getSelectedTab())) {
					tab.refreshTable();
					currentSchema = (PrintableReportListPossibleType) tab.getId();
					break;
				}
			}
			ListPrintableReportViewImpl.this.getXmlButtonV2.setCurrentSchema(currentSchema);
		}
	}
}
