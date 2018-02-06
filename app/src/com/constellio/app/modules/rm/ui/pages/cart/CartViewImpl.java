package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.ui.entities.FolderVO;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPButtonImpl;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupFieldWithIgnoreOneRecord;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;
import static java.util.Arrays.asList;

public class CartViewImpl extends BaseViewImpl implements CartView {
	private final CartPresenter presenter;
	private VerticalLayout folderLayout;
	private VerticalLayout documentLayout;
	private VerticalLayout containerLayout;
	private Table folderTable;
	private Table documentTable;
	private Table containerTable;
	private BaseTextField folderFilterField;
	private BaseTextField documentFilterField;
	private BaseTextField containerFilterField;
	private String currentSchemaType;
	private ReportSelector reportSelector;
	private VerticalLayout mainLayout;

	public CartViewImpl() {
		presenter = new CartPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		presenter.forParams(event.getParameters());
		currentSchemaType = Folder.SCHEMA_TYPE;
	}

	@Override
	protected String getTitle() {
		return $("CartView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttons = super.buildActionMenuButtons(event);
		buttons.add(buildPrepareEmailButton());
		buttons.add(buildBatchDuplicateButton());
		buttons.add(buildDocumentsBatchProcessingButton());
		buttons.add(buildFoldersBatchProcessingButton());
		buttons.add(buildContainersBatchProcessingButton());
		buttons.add(buildFoldersLabelsButton());
		buttons.add(buildContainersLabelsButton());
		buttons.add(buildBatchDeleteButton());
		buttons.add(buildEmptyButton());
		buttons.add(buildShareButton());
		buttons.add(buildDecommissionButton());
		buttons.add(buildPrintMetadataReportButton());
		buttons.add(buildCreateSIPArchivesButton());
		return buttons;
	}

	private Button buildFoldersLabelsButton() {
		Button button = buildLabelsButton(Folder.SCHEMA_TYPE);
		button.setCaption($("CartView.foldersLabelsButton"));
		return button;
	}

	private Button buildContainersLabelsButton() {
		Button button = buildLabelsButton(ContainerRecord.SCHEMA_TYPE);
		button.setCaption($("CartView.containersLabelsButton"));
		return button;
	}

	private Button buildLabelsButton(final String schemaType) {
		Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getCustomTemplates(schemaType);
			}
		};
		Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getDefaultTemplates(schemaType);
			}
		};
		LabelButtonV2 labelsButton = new LabelButtonV2(
				$("SearchView.labels"),
				$("SearchView.printLabels"),
				customLabelTemplatesFactory,
				defaultLabelTemplatesFactory,
				getConstellioFactories().getAppLayerFactory(),
				getSessionContext().getCurrentCollection()
		);
		labelsButton.setElementsWithIds(presenter.getNotDeletedRecordsIds(schemaType), schemaType, getSessionContext());
		labelsButton.setEnabled(presenter.isLabelsButtonVisible(schemaType));
		labelsButton.setVisible(presenter.isLabelsButtonVisible(schemaType));
		return labelsButton;
	}

	private Button buildContainersBatchProcessingButton() {
		Button batchProcessingButton = buildBatchProcessingButton(ContainerRecord.SCHEMA_TYPE);
		batchProcessingButton.setCaption($("CartView.containersBatchProcessingButton"));
		return batchProcessingButton;
	}

	private Button buildBatchProcessingButton(final String schemaType) {
		BatchProcessingMode mode = presenter.getBatchProcessingMode();
		WindowButton button;
		if (mode.equals(ALL_METADATA_OF_SCHEMA)) {
			button = new BatchProcessingButton(presenter, new BatchProcessingViewImpl(schemaType));
		} else if (mode.equals(ONE_METADATA)) {
			button = new BatchProcessingModifyingOneMetadataButton(presenter, new BatchProcessingViewImpl(schemaType));
		} else {
			throw new RuntimeException("Unsupported mode " + mode);
		}
		button.setEnabled(presenter.isBatchProcessingButtonVisible(schemaType));
		button.setVisible(presenter.isBatchProcessingButtonVisible(schemaType));
		return button;
	}

	private Button buildDocumentsBatchProcessingButton() {
		Button button = buildBatchProcessingButton(Document.SCHEMA_TYPE);
		button.setCaption($("CartView.documentsBatchProcessingButton"));
		return button;
	}

	private Button buildFoldersBatchProcessingButton() {
		Button button = buildBatchProcessingButton(Folder.SCHEMA_TYPE);
		button.setCaption($("CartView.foldersBatchProcessingButton"));
		return button;
	}

	private Button buildShareButton() {
		return new WindowButton($("CartView.share"),$("CartView.shareWindow")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSpacing(true);

				final ListAddRemoveRecordLookupFieldWithIgnoreOneRecord lookup = new ListAddRemoveRecordLookupFieldWithIgnoreOneRecord(User.SCHEMA_TYPE, getSessionContext().getCurrentUser().getId());
				lookup.setValue(presenter.cart().getSharedWithUsers());

				layout.addComponent(lookup);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.shareWithUsersRequested(lookup.getValue());
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(saveButton);
				return layout;
			}
		};
	}

	private HorizontalLayout buildFolderFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		folderFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.folderFilterButtonClicked();
			}
		};
		filterComponent.addComponents(folderFilterField, filterButton);
		return filterComponent;
	}

	private HorizontalLayout buildDocumentFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		documentFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.documentFilterButtonClicked();
			}
		};
		filterComponent.addComponents(documentFilterField, filterButton);
		return filterComponent;
	}

	private HorizontalLayout buildContainerFilterComponent() {
		HorizontalLayout filterComponent = new HorizontalLayout();
		filterComponent.setSpacing(true);
		containerFilterField = new BaseTextField();
		BaseButton filterButton = new BaseButton($("ConnectorReportView.filterButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.containerFilterButtonClicked();
			}
		};
		filterComponent.addComponents(containerFilterField, filterButton);
		return filterComponent;
	}

	private Button buildPrintMetadataReportButton() {
		ReportTabButton reportGeneratorButton = new ReportTabButton($("ReportGeneratorButton.buttonText"), $("ReportGeneratorButton.windowText"),
				this.getConstellioFactories().getAppLayerFactory(), getCollection(), false ,false, this.presenter, getSessionContext()) {
			@Override
			public void buttonClick(ClickEvent event) {
				List<RecordVO> allRecords = new ArrayList<>();
				allRecords.addAll(presenter.getNotDeletedCartFoldersVO());
				allRecords.addAll(presenter.getNotDeletedCartDocumentVO());
				setRecordVoList(allRecords.toArray(new RecordVO[0]));
				super.buttonClick(event);
			}
		};

		return reportGeneratorButton;
	}

	private Button buildDecommissionButton() {
		WindowButton windowButton = new WindowButton($("CartView.decommissioningList"), $("CartView.createDecommissioningList")) {

			@Override
			public void buttonClick(ClickEvent event) {
				if (presenter.getCommonAdministrativeUnit(presenter.getCartFolders()) == null) {
					showErrorMessage($("CartView.foldersFromDifferentAdminUnits"));
				} else if (presenter.getCommonDecommissioningListTypes(presenter.getCartFolders()).isEmpty()) {
					showErrorMessage($("CartView.foldersShareNoCommonDecommisioningTypes"));
				} else if (presenter.isAnyFolderBorrowed()) {
					showErrorMessage($("CartView.aFolderIsBorrowed"));
				} else if (presenter.isAnyFolderInDecommissioningList()) {
					showErrorMessage($("CartView.aFolderIsInADecommissioningList"));
				} else {
					super.buttonClick(event);
				}
			}

			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();

				final BaseTextField titleField = new BaseTextField($("title"));
				layout.addComponent(titleField);

				final EnumWithSmallCodeComboBox<DecommissioningListType> decomTypeField = new EnumWithSmallCodeComboBox<>(DecommissioningListType.class);
				decomTypeField.removeAllItems();
				decomTypeField.addItems(presenter.getCommonDecommissioningListTypes(presenter.getCartFolders()));
				decomTypeField.setCaption($("CartView.decommissioningTypeField"));
				layout.addComponent(decomTypeField);

				BaseButton saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						if(StringUtils.isBlank(titleField.getValue())) {
							showErrorMessage($("CartView.decommissioningListIsMissingTitle"));
							return;
						}
						if(decomTypeField.getValue() == null) {
							showErrorMessage($("CartView.decommissioningListIsMissingType"));
							return;
						}
						presenter.buildDecommissioningListRequested(titleField.getValue(), (DecommissioningListType) decomTypeField.getValue());
						getWindow().close();
					}
				};
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
				layout.addComponent(saveButton);
				layout.setSpacing(true);
				return layout;
			}
		};
		windowButton.setEnabled(!presenter.getCartFolders().isEmpty());
		windowButton.setVisible(!presenter.getCartFolders().isEmpty());
		return windowButton;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		FireableTabSheet tabSheet = new FireableTabSheet();
		folderTable = buildTable("CartView.folders", presenter.getFolderRecords());
		documentTable = buildTable("CartView.documents", presenter.getDocumentRecords());
		containerTable = buildTable("CartView.containers", presenter.getContainerRecords());
		TabSheet.Tab folderTab = tabSheet.addTab(folderLayout = new VerticalLayout(buildFolderFilterComponent(),folderTable));
		folderTab.setCaption($("CartView.foldersTab"));
		folderLayout.setDescription(Folder.SCHEMA_TYPE);
		folderTab.setVisible(!folderTable.getContainerDataSource().getItemIds().isEmpty());
		TabSheet.Tab documentTab = tabSheet.addTab(documentLayout = new VerticalLayout(buildDocumentFilterComponent(),documentTable));
		documentTab.setCaption($("CartView.documentsTab"));
		documentLayout.setDescription(Document.SCHEMA_TYPE);
		documentTab.setVisible(!documentTable.getContainerDataSource().getItemIds().isEmpty());
		TabSheet.Tab containerTab = tabSheet.addTab(containerLayout = new VerticalLayout(buildContainerFilterComponent(),containerTable));
		containerTab.setCaption($("CartView.containersTab"));
		containerLayout.setDescription(ContainerRecord.SCHEMA_TYPE);
		containerTab.setVisible(!containerTable.getContainerDataSource().getItemIds().isEmpty());
		mainLayout = new VerticalLayout(reportSelector = new ReportSelector(presenter));
		mainLayout.setSizeFull();
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				Component selectedTab = event.getTabSheet().getSelectedTab();
				currentSchemaType = selectedTab.getDescription();
				ReportSelector newReportSelector = new ReportSelector(presenter);
				mainLayout.replaceComponent(reportSelector, newReportSelector);
				reportSelector = newReportSelector;
			}
		});
		if(!folderTab.isVisible() && !documentTab.isVisible() && !containerTab.isVisible()) {
			mainLayout.addComponent(new Label($("CartView.emptyCart")));
			reportSelector.setVisible(false);
		} else {
			mainLayout.addComponent(tabSheet);
			tabSheet.fireTabSelectionChanged();
		}
		return mainLayout;
	}

	private Table buildTable(final String tableId, final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		final Container container = buildContainer(dataProvider);
		Table table = new RecordVOTable($("CartView.records", container.size()), container) {
			@Override
			protected String getTableId() {
				return tableId;
			}

			@Override
			protected TableColumnsManager newColumnsManager() {
				return new TableColumnsManager();
			}
			
		};
		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				int itemId = (int) event.getItemId();
				presenter.displayRecordRequested(dataProvider.getRecordVO(itemId));
			}
		});
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("init.allTypes.allSchemas.title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();
		return table;
	}

	@Override
	public void startDownload(final InputStream stream, String filename) {
		Resource resource = new DownloadStreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, filename);
		Page.getCurrent().open(resource, null, false);
	}

	@Override
	public void filterFolderTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if(folderFilterField.isEmpty()) {
			dataProvider = presenter.getFolderRecords();
		} else {
			dataProvider = presenter.getFilteredFolderRecords(folderFilterField.getValue());
		}
		Table newTable = buildTable("CartView.folders", dataProvider);
		folderLayout.replaceComponent(folderTable,newTable);
		folderTable = newTable;
	}

	@Override
	public void filterDocumentTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if(documentFilterField.isEmpty()) {
			dataProvider = presenter.getDocumentRecords();
		} else {
			dataProvider = presenter.getFilteredDocumentRecords(documentFilterField.getValue());
		}
		Table newTable = buildTable("CartView.documents", dataProvider);
		documentLayout.replaceComponent(documentTable,newTable);
		documentTable = newTable;
	}

	@Override
	public void filterContainerTable() {
		final RecordVOWithDistinctSchemasDataProvider dataProvider;
		if(containerFilterField.isEmpty()) {
			dataProvider = presenter.getContainerRecords();
		} else {
			dataProvider = presenter.getFilteredContainerRecords(containerFilterField.getValue());
		}
		Table newTable = buildTable("CartView.containers", dataProvider);
		containerLayout.replaceComponent(containerTable,newTable);
		containerTable = newTable;
	}

	private Button buildPrepareEmailButton() {
		Button button = new LinkButton($("CartView.prepareEmail")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.emailPreparationRequested();
			}
		};
		button.setEnabled(presenter.canPrepareEmail());
		button.setVisible(presenter.canPrepareEmail());
		return button;
	}

	private Button buildBatchDuplicateButton() {
		Button button = new LinkButton($("CartView.batchDuplicate")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.duplicationRequested();
			}
		};
		button.setEnabled(presenter.canDuplicate());
		button.setVisible(presenter.canDuplicate());
		return button;
	}

	private Button buildBatchDeleteButton() {
		Button button = new DeleteWithJustificationButton(false) {
			@Override
			protected void deletionConfirmed(String reason) {
				presenter.deletionRequested(reason);
			}

			@Override
			protected String getConfirmDialogMessage() {
				List<String> cartFolderIds = presenter.getCartFolderIds();
				List<String> cartDocumentIds = presenter.getCartDocumentIds();

				StringBuilder stringBuilder = new StringBuilder();
				String prefix = "";
				if(cartFolderIds != null && !cartFolderIds.isEmpty()) {
					stringBuilder.append(prefix + cartFolderIds.size() + " " + $("CartView.folders"));
					prefix = " " + $("CartView.andAll") + " ";
				}
				if(cartDocumentIds != null && !cartDocumentIds.isEmpty()) {
					stringBuilder.append(prefix + cartDocumentIds.size() + " " + $("CartView.documents"));
				}
				return $("CartView.deleteConfirmationMessage", stringBuilder.toString());
			}
		};
		button.setEnabled(presenter.canDelete());
		button.setVisible(presenter.canDelete());
		return button;
	}

	private Button buildEmptyButton() {
		Button button = new ConfirmDialogButton($("CartView.empty")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("CartView.emptyCartConfirmation");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.cartEmptyingRequested();
			}
		};
		button.setEnabled(presenter.canEmptyCart());
		button.setVisible(presenter.canEmptyCart());
		return button;
	}

	private Container buildContainer(final RecordVOWithDistinctSchemasDataProvider dataProvider) {
		RecordVOWithDistinctSchemaTypesLazyContainer records = new RecordVOWithDistinctSchemaTypesLazyContainer(
				dataProvider, asList(CommonMetadataBuilder.TITLE));
		ButtonsContainer<RecordVOWithDistinctSchemaTypesLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						int index = (int) itemId;
						presenter.itemRemovalRequested(dataProvider.getRecordVO(index));
					}
				};
			}
		});
		return container;
	}

	private Button buildCreateSIPArchivesButton(){
		SIPButtonImpl siPbutton = new SIPButtonImpl($("SIPButton.caption"), $("SIPButton.caption"), ConstellioUI.getCurrent().getHeader(), true) {
			@Override
			public void buttonClick(ClickEvent event) {
				setAllObject(presenter.getNotDeletedCartFoldersVO().toArray(new FolderVO[0]));
				super.buttonClick(event);
			}
		};
		return siPbutton;
	}

	@Override
	public String getCurrentSchemaType() {
		return currentSchemaType;
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

	private class BatchProcessingViewImpl implements BatchProcessingView {
		private final String schemaType;

		public BatchProcessingViewImpl(String schemaType) {
			this.schemaType = schemaType;
		}

		@Override
		public List<String> getSelectedRecordIds() {
			return presenter.getNotDeletedRecordsIds(schemaType);
		}

		@Override
		public List<String> getUnselectedRecordIds() {
			return null;
		}

		@Override
		public String getSchemaType() {
			return schemaType;
		}

		@Override
		public SessionContext getSessionContext() {
			return CartViewImpl.this.getSessionContext();
		}

		@Override
		public void showErrorMessage(String error) {
			CartViewImpl.this.showErrorMessage(error);
		}

		@Override
		public void showMessage(String message) {
			CartViewImpl.this.showMessage(message);
		}
	}

	private class FireableTabSheet extends TabSheet {
		public void fireTabSelectionChanged() {
			fireSelectedTabChange();
		}
	}
}
