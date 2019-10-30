package com.constellio.app.ui.pages.management.TemporaryRecord;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.TitlePanel;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.utils.FileLengthUtils;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.getLanguage;

public class ListTemporaryRecordViewImpl extends BaseViewImpl implements ListTemporaryRecordView {

	private ListTemporaryRecordPresenter presenter;

	private Map<String, String> tabsSchemasAndLabel = new HashMap<>();

	private TabSheet tabSheet;

	private String currentSchema;

	private Map<TabSheet.Tab, String> tabs;

	public ListTemporaryRecordViewImpl() {
		presenter = new ListTemporaryRecordPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("ListTemporaryRecordViewImpl.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("batch-processes");
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		initTabWithDefaultValues();
		tabSheet = new TabSheet();
		tabs = new HashMap<>();
		for (Map.Entry<String, String> currentTabs : tabsSchemasAndLabel.entrySet()) {
			RecordVODataProvider provider = presenter.getDataProviderFromType(currentTabs.getKey());
			if (provider.size() > 0) {
				tabs.put(tabSheet.addTab(buildTable(provider), currentTabs.getValue()), currentTabs.getKey());
				if (currentSchema == null) {
					currentSchema = currentTabs.getKey();
				}
				tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
					@Override
					public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
						currentSchema = ((RecordVOTable) event.getTabSheet().getSelectedTab()).getSchemas().get(0).getCode();
					}
				});
			}
		}
		if (tabSheet.getComponentCount() > 0) {
			mainLayout.addComponent(tabSheet);
		} else {
			mainLayout.addComponent(new TitlePanel($("ListTemporaryRecordViewImpl.noTemporaryReportAvailable")));
		}
		return mainLayout;
	}

	private BaseTable buildTable(RecordVODataProvider provider) {
		Container container = new RecordVOLazyContainer(provider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(container, "buttons");
		buttonsContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.deleteButtonClick(itemId + "", currentSchema);
					}

					@Override
					public boolean isVisible() {
						return presenter.isVisible(itemId + "", currentSchema);
					}
				};
			}
		});

		RecordVOTable importTable = new RecordVOTable("", buttonsContainer) {
			@Override
			protected Component buildMetadataComponent(Object itemId, final MetadataValueVO metadataValue, final RecordVO recordVO) {
				final Component defaultComponent = super.buildMetadataComponent(itemId, metadataValue, recordVO);
				if (metadataValue.getMetadata().getLocalCode().equals(ImportAudit.ERRORS) && metadataValue.getValue() != null && metadataValue.getValue() instanceof String) {
					final String value = metadataValue.getValue();
					if (!value.isEmpty()) {
						int maxLength = (value.length() < 40) ? value.length() : 40;
						String caption = maxLength == 40 ? value.substring(0, maxLength) + "..." : value.substring(0, maxLength);
						WindowButton windowButton = new WindowButton(caption, metadataValue.getMetadata().getLabel()) {
							@Override
							protected Component buildWindowContent() {
								if (defaultComponent != null) {
									VerticalLayout mainLayout = new VerticalLayout();
									mainLayout.setSizeFull();
									TextArea textArea = new TextArea();
									textArea.setValue(value);
									textArea.setSizeFull();
									mainLayout.addComponents(textArea);
									return mainLayout;
								} else {
									return defaultComponent;
								}
							}
						};
						windowButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
						return windowButton;
					} else {
						return defaultComponent;
					}
				} else {
					return defaultComponent;
				}
			}
		};
		importTable.setWidth("98%");
		importTable.setCellStyleGenerator(newImportStyleGenerator());
		importTable.setColumnHeader("buttons", "");
		importTable.setColumnWidth("buttons", 50);
		return importTable;
	}

	private Container buildContainer(Table table, RecordVODataProvider provider) {
		return addContentLength(table, new RecordVOLazyContainer(provider));
	}

	private Container addContentLength(Table table, final RecordVOLazyContainer records) {
		String columnId = "menuBar";
		table.addGeneratedColumn(columnId, new Table.ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId, Object columnId) {
				final RecordVO recordVO = records.getRecordVO((int) itemId);
				ContentVersionVO content = recordVO.get(TemporaryRecord.CONTENT);
				return new Label(FileLengthUtils.readableFileSize((content != null ? content.getLength() : 0)));
			}
		});
		table.setColumnHeader(columnId, $("ListTemporaryRecordViewImpl.contentLength"));
		return records;
	}

	private Table.CellStyleGenerator newImportStyleGenerator() {
		return new Table.CellStyleGenerator() {

			@Override
			public String getStyle(Table source, Object itemId, Object propertyId) {
				try {
					RecordVOItem item = (RecordVOItem) source.getItem(itemId);
					RecordVO record = item.getRecord();
					MetadataVO errors = record.getMetadata(ImportAudit.ERRORS);
					if (errors != null && record.getMetadataValue(errors).getValue() != null && !((String) record.getMetadataValue(errors).getValue()).isEmpty()) {
						return "error";
					}
				} catch (Exception e) {

				}
				return null;
			}
		};
	}

	private void initTabWithDefaultValues() {
		MetadataSchemasManager manager = getConstellioFactories().getModelLayerFactory().getMetadataSchemasManager();
		for (MetadataSchema schema : manager.getSchemaTypes(getCollection()).getSchemaType(TemporaryRecord.SCHEMA_TYPE).getCustomSchemas()) {
			tabsSchemasAndLabel.put(schema.getCode(), schema.getLabel(getLanguage()));
		}
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return true;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return (ClickListener) event -> presenter.backButtonClicked();
	}
}
