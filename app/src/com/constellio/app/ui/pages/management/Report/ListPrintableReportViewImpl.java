package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.ui.application.Navigation;
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

	private TabSheet tabSheet;
	private PrintableReportListPossibleType currentSchema;
	private ListPrintableReportPresenter presenter = new ListPrintableReportPresenter(this);
	private VerticalLayout mainLayout;
	private Button addLabelButton;
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
		TabWithTable categoryTab = new TabWithTable(PrintableReportListPossibleType.CATEGORY) {
			@Override
			public Table buildTable() {
				return createCategoryTable();
			}
		};
		TabWithTable retentionRuleTab = new TabWithTable(PrintableReportListPossibleType.RETENTION_RULE) {
			@Override
			public Table buildTable() {
				return createRetentionRuleTable();
			}
		};
		TabWithTable administrativeUnitTab = new TabWithTable(PrintableReportListPossibleType.ADMINISTRATIVE_UNIT) {
			@Override
			public Table buildTable() {
				return createAdministrativeUnitTable();
			}
		};
		TabWithTable legalRequirementTab = new TabWithTable(PrintableReportListPossibleType.LEGAL_REQUIREMENT) {
			@Override
			public Table buildTable() {
				return createLegalRequirementTabTable();
			}
		};
		tabs.addAll(asList(folderTab, documentTab, taskTab, categoryTab, retentionRuleTab, administrativeUnitTab, legalRequirementTab));
		tabSheet.addTab(folderTab.getTabLayout(), $("PrintableReport.tabs.folderTitle"));
		tabSheet.addTab(documentTab.getTabLayout(), $("PrintableReport.tabs.documentTitle"));
		tabSheet.addTab(taskTab.getTabLayout(), $("PrintableReport.tabs.taskTitle"));
		tabSheet.addTab(categoryTab.getTabLayout(), $("PrintableReport.tabs.category"));
		tabSheet.addTab(retentionRuleTab.getTabLayout(), $("PrintableReport.tabs.retentionRule"));
		tabSheet.addTab(administrativeUnitTab.getTabLayout(), $("PrintableReport.tabs.administrativeUnit"));
		tabSheet.addTab(legalRequirementTab.getTabLayout(), $("PrintableReport.tabs.legalRequirement"));
		tabSheet.addSelectedTabChangeListener(new TabsChangeListener());
		mainLayout.addComponent(tabSheet);
		return mainLayout;
	}

	private Table createFolderTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportFolderDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createDocumentTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportDocumentDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createTaskTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportTaskDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createCategoryTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportCategoryDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createRetentionRuleTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportRetentionRuleDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createAdministrativeUnitTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportAdministrativeUnitDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createLegalRequirementTabTable() {
		Container container = new RecordVOLazyContainer(presenter.getPrintableReportLegalRequirementDataProvider());
		return createPrintableReportTable(container);
	}

	private Table createPrintableReportTable(Container container) {
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, "buttons");
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDisplayButton(report);
			}
		});
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportEditButton(report);
			}
		});
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new PrintableReportDeleteButton(report);
			}
		});
		Table printableReportTab = new RecordVOTable(buttonsContainer);
		printableReportTab = setTableProperty(printableReportTab, buttonsContainer.size());
		return printableReportTab;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		addLabelButton = new AddButton($("PrintableReport.menu.addNewReport")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addLabelButtonClicked(currentSchema.getSchemaType());
			}
		};

		getXmlButtonV2 = new GetXmlButtonV2($("DisplayLabelViewImpl.menu.getXMLButton"),
				$("DisplayLabelViewImpl.menu.getXMLButton"), getConstellioFactories().getAppLayerFactory(),
				getCollection(), this, currentSchema, true);

		actionMenuButtons.add(addLabelButton);
		actionMenuButtons.add(getXmlButtonV2);
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
			return report != null;
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
