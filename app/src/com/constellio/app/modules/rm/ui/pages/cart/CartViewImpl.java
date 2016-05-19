package com.constellio.app.modules.rm.ui.pages.cart;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.buttons.LabelsButton;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.data.utils.Factory;
import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOWithDistinctSchemaTypesLazyContainer;
import com.constellio.app.ui.framework.data.RecordVOWithDistinctSchemasDataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class CartViewImpl extends BaseViewImpl implements CartView {
	private final CartPresenter presenter;

	public CartViewImpl() {
		presenter = new CartPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		super.initBeforeCreateComponents(event);
		presenter.forParams(event.getParameters());
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
		buttons.add(buildFoldersBatchProcessingButton());
		buttons.add(buildDocumentsBatchProcessingButton());
		buttons.add(buildContainersBatchProcessingButton());
		buttons.add(buildFoldersLabelsButton());
		buttons.add(buildContainersLabelsButton());
		buttons.add(buildBatchDeleteButton());
		buttons.add(buildEmptyButton());
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
		LabelsButton labelsButton = new LabelsButton($("SearchView.labels"), $("SearchView.printLabels"), new LabelsRecordSelectorImpl(schemaType),
				labelTemplatesFactory);
		labelsButton.setEnabled(presenter.isLabelsButtonVisible(schemaType));
		return labelsButton;
	}

	private Button buildContainersBatchProcessingButton() {
		Button batchProcessingButton = buildBatchProcessingButton(Folder.SCHEMA_TYPE);
		batchProcessingButton.setCaption($("CartView.containersBatchProcessingButton"));
		return batchProcessingButton;
	}

	private Button buildBatchProcessingButton(String schemaType) {
		BatchProcessingButton button = new BatchProcessingButton(presenter, new BatchProcessingViewImpl(schemaType));
		button.setEnabled(presenter.isBatchProcessingButtonVisible(schemaType));
		return button;
	}

	private Button buildDocumentsBatchProcessingButton() {
		Button button = buildBatchProcessingButton(Document.SCHEMA_TYPE);
		button.setCaption($("CartView.documentsBatchProcessingButton"));
		return button;
	}

	private Button buildFoldersBatchProcessingButton() {
		Button button = buildBatchProcessingButton(ContainerRecord.SCHEMA_TYPE);
		button.setCaption($("CartView.foldersBatchProcessingButton"));
		return button;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Container container = buildContainer(presenter.getRecords());
		Table table = new RecordVOTable($("CartView.records", container.size()), container);
		table.setColumnHeader(CommonMetadataBuilder.TITLE, $("init.allTypes.allSchemas.title"));
		table.setColumnHeader(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, "");
		table.setColumnWidth(ButtonsContainer.DEFAULT_BUTTONS_PROPERTY_ID, 50);
		table.setPageLength(Math.min(15, container.size()));
		table.setSizeFull();

		VerticalLayout layout = new VerticalLayout(table);
		layout.setSizeFull();
		return layout;
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

	private Button buildPrepareEmailButton() {
		Button button = new LinkButton($("CartView.prepareEmail")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.emailPreparationRequested();
			}
		};
		button.setEnabled(presenter.canPrepareEmail());
		return button;
	}

	private Button buildBatchDuplicateButton() {
		Button button = new LinkButton($("CartView.batchDuplicate")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.duplicationRequested();
			}
		};
		button.setEnabled(presenter.canDelete());
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
		return button;
	}

	private Button buildEmptyButton() {
		Button button = new LinkButton($("CartView.empty")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.cartEmptyingRequested();
			}
		};
		button.setEnabled(presenter.canEmptyCart());
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

	private class BatchProcessingViewImpl implements BatchProcessingView{
		private final String schemaType;
		public BatchProcessingViewImpl(String schemaType) {
			this.schemaType = schemaType;
		}

		@Override
		public List<String> getSelectedRecordIds() {
			return presenter.getRecordsIds(schemaType);
		}

		@Override
		public String getSchemaType() {
			return schemaType;
		}

		@Override
		public SessionContext getSessionContext() {
			return CartViewImpl.this.getSessionContext();
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
