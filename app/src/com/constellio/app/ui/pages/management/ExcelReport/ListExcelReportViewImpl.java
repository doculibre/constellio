package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListExcelReportViewImpl extends BaseViewImpl implements ListExcelReportView {
	public static final String TYPE_TABLE = "types";

	private ListExcelReportPresenter presenter;
	private String currentSchema;
	private TabSheet tabSheet;
	List<TabWithTable> tabs = new ArrayList<>();

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
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		MetadataSchemasManager metadataSchemasManager = getConstellioFactories().getAppLayerFactory().getModelLayerFactory()
				.getMetadataSchemasManager();
		presenter = new ListExcelReportPresenter(this);
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		tabSheet = new TabSheet();
		Iterator<Map.Entry<String, String>> possibleTabIterator = presenter.initPossibleTab().entrySet().iterator();
		while (possibleTabIterator.hasNext()) {
			final Map.Entry<String, String> entry = possibleTabIterator.next();
			TabWithTable tab = new TabWithTable(entry.getKey()) {
				@Override
				public Table buildTable() {
					return generateTab(entry.getKey());
				}
			};
			tabs.add(tab);
			tabSheet.addTab(tab.getTabLayout(), entry.getValue());
			if (currentSchema == null) {
				currentSchema = entry.getKey();
			}
		}
		tabSheet.addSelectedTabChangeListener(new TabsChangeListener());
		return tabSheet;
	}

	private Table generateTab(final String schemaType) {
		Container container = new RecordVOLazyContainer(presenter.getDataProviderForSchemaType(schemaType));
		ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(container, "buttons");
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new ListExcelReportViewImpl.ExcelDisplayButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new ListExcelReportViewImpl.ExcelEditButton(report);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				RecordVO report = ((RecordVOItem) container.getItem(itemId)).getRecord();
				return new ListExcelReportViewImpl.ExcelDeleteButton(report);
			}
		});
		container = buttonsContainerForFolder;
		Table table = new RecordVOTable(buttonsContainerForFolder);
		table.setSizeFull();
		table.setPageLength(Math.min(15, container.size()));
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
		table.setColumnExpandRatio("caption", 1);
		table.addStyleName(TYPE_TABLE);
		return table;
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
		return Collections.singletonList((Button) new AddButton() {
			@Override
			protected void buttonClick(ClickEvent event) {
				Map<String, String> paramsMap = new HashMap<>();
				paramsMap.put("schemaTypeCode", currentSchema);
				String params = ParamUtils.addParams(NavigatorConfigurationService.REPORT_DISPLAY_FORM, paramsMap);
				navigate().to().reportDisplayForm(params);
			}
		});
	}

	private class ExcelEditButton extends EditButton {
		private RecordVO report;

		public ExcelEditButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.editButtonClicked(report.getId(), currentSchema);
		}

		@Override
		public boolean isVisible() {
			return true;
		}
	}

	@Override
	protected String getTitle() {
		return $("ListExcelReportViewImpl.title");
	}

	private class ExcelDisplayButton extends DisplayButton {
		private RecordVO report;

		public ExcelDisplayButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.displayButtonClicked(report.getId(), currentSchema);
		}
	}

	private class ExcelDeleteButton extends DeleteButton {
		private RecordVO report;

		public ExcelDeleteButton(RecordVO report) {
			this.report = report;
		}

		@Override
		protected void confirmButtonClick(ConfirmDialog dialog) {
			presenter.removeRecord(report.getId(), currentSchema);
		}

		@Override
		public boolean isVisible() {
			return true;
		}
	}

	private class TabsChangeListener implements TabSheet.SelectedTabChangeListener {

		@Override
		public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
			for (TabWithTable tab : tabs) {
				if (tab.getTabLayout().equals(event.getTabSheet().getSelectedTab())) {
					tab.refreshTable();
					currentSchema = (String) tab.getId();
					break;
				}
			}
		}
	}
}
