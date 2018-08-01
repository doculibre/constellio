package com.constellio.app.ui.pages.management.ExcelReport;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
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

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListExcelReportViewImpl extends BaseViewImpl implements ListExcelReportView {
	public static final String TYPE_TABLE = "types";

	public Map<String, String> POSSIBLE_TAB;
	private ListExcelReportPresenter presenter;
	private String currentSchema;
	private TabSheet tabSheet;

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
		POSSIBLE_TAB = presenter.initPossibleTab();
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		tabSheet = new TabSheet();
		Iterator<Map.Entry<String, String>> possibleTabIterator = POSSIBLE_TAB.entrySet().iterator();
		while (possibleTabIterator.hasNext()) {
			Map.Entry<String, String> entry = possibleTabIterator.next();
			tabSheet.addTab(generateTab(entry.getValue()), entry.getKey());
			if (currentSchema == null) {
				currentSchema = entry.getValue();
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
				return new ListExcelReportViewImpl.ExcelDisplayButton(itemId, schemaType);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new ListExcelReportViewImpl.ExcelEditButton(itemId, schemaType);
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new ListExcelReportViewImpl.ExcelDeleteButton(itemId, schemaType);
			}
		});
		container = buttonsContainerForFolder;
		Table table = new RecordVOTable($("ListSchemaTypeView.tableTitle"), buttonsContainerForFolder);
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
		private String itemId;
		private String currentSchema;

		public ExcelEditButton(Object itemId, String currentSchema) {
			RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
			if (item != null) {
				this.itemId = item.getId();
				this.currentSchema = currentSchema;
			}
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.editButtonClicked(itemId, currentSchema);
		}

		@Override
		public boolean isVisible() {
			RecordVO ret = presenter.getRecordsWithIndex(currentSchema, itemId);
			return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
		}
	}

	@Override
	protected String getTitle() {
		return $("ListExcelReportViewImpl.title");
	}

	private class ExcelDisplayButton extends DisplayButton {
		private String itemId;
		private String currentSchema;

		public ExcelDisplayButton(Object itemId, String currentSchema) {
			RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
			if (item != null) {
				this.itemId = item.getId();
				this.currentSchema = currentSchema;
			}
		}

		@Override
		protected void buttonClick(ClickEvent event) {
			presenter.displayButtonClicked(itemId, currentSchema);
		}
	}

	private class ExcelDeleteButton extends DeleteButton {
		private String itemId;
		private String currentSchema;

		public ExcelDeleteButton(Object itemId, String currentSchema) {
			RecordVO item = presenter.getRecordsWithIndex(currentSchema, itemId + "");
			if (item != null) {
				this.itemId = item.getId();
				this.currentSchema = currentSchema;
			}
		}

		@Override
		protected void confirmButtonClick(ConfirmDialog dialog) {
			presenter.removeRecord(itemId, currentSchema);
		}

		@Override
		public boolean isVisible() {
			RecordVO ret = presenter.getRecordsWithIndex(currentSchema, itemId);
			return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
		}
	}

	private class TabsChangeListener implements TabSheet.SelectedTabChangeListener {

		@Override
		public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
			TabSheet eventSource = (TabSheet) event.getSource();
			TabSheet.Tab tab = eventSource.getTab(eventSource.getSelectedTab());
			if (POSSIBLE_TAB.containsKey(tab.getCaption())) {
				currentSchema = POSSIBLE_TAB.get(tab.getCaption());
			}
		}
	}
}
