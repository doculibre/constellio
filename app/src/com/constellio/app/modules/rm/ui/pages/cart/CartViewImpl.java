package com.constellio.app.modules.rm.ui.pages.cart;

import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.buttons.*;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.components.table.columns.TableColumnsManager;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.data.utils.Factory;
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
import org.vaadin.dialogs.ConfirmDialog;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		Factory<List<LabelTemplate>> labelTemplatesFactory = new Factory<List<LabelTemplate>>() {
			@Override
			public List<LabelTemplate> get() {
				return presenter.getTemplates(schemaType);
			}
		};
		LabelsButton labelsButton = new LabelsButton(
				$("SearchView.labels"),
				$("SearchView.printLabels"),
				getConstellioFactories().getAppLayerFactory(),
				getSessionContext().getCurrentCollection(),
				Folder.SCHEMA_TYPE,
				presenter.getRecordsIds(Folder.SCHEMA_TYPE),
				getSessionContext().getCurrentUser().getUsername()
		);
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
		BatchProcessingButton button = new BatchProcessingButton(presenter, new BatchProcessingViewImpl(schemaType)) {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.setBatchProcessSchemaType(schemaType);
				super.buttonClick(event);
			}
		};
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

				final ListAddRemoveRecordLookupField lookup = new ListAddRemoveRecordLookupField(User.SCHEMA_TYPE);
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

		private Button buildDecommissionButton() {
			WindowButton windowButton = new WindowButton($("CartView.decommissioningList"), $("CartView.createDecommissioningList")) {

				@Override
				public void buttonClick(ClickEvent event) {
					if (presenter.getCommonAdministrativeUnit(presenter.getCartFolders()) == null) {
						showErrorMessage($("CartView.foldersFromDifferentAdminUnits"));
					} else if (presenter.getCommonDecommissioningListTypes(presenter.getCartFolders()).isEmpty()) {
						showErrorMessage($("CartView.foldersShareNoCommonDecommisioningTypes"));
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
		TabSheet tabSheet = new TabSheet();
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
		} else {
			mainLayout.addComponent(tabSheet);
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
	public void startDownload(final InputStream stream) {
		Resource resource = new DownloadStreamResource(new StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, "cart.eml");
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
		Button button = new DeleteButton(false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deletionRequested();
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
				dataProvider, Arrays.asList(CommonMetadataBuilder.TITLE));
		ButtonsContainer<RecordVOWithDistinctSchemaTypesLazyContainer> container = new ButtonsContainer<>(records);
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId) {
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
			return presenter.getRecordsIds(schemaType);
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

	private class LabelsRecordSelectorImpl implements LabelsButton.RecordSelector {
		private final String schemaType;

		public LabelsRecordSelectorImpl(String schemaType) {
			this.schemaType = schemaType;
		}

		@Override
		public List<String> getSelectedRecordIds() {
			return presenter.getRecordsIds(schemaType);
		}
	}
	
}
