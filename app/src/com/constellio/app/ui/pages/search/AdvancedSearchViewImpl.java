package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LabelsButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.ReportSelector;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultSimpleTable;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter> implements AdvancedSearchView, BatchProcessingView {
	
	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;
	private WindowButton batchProcessingButton;

	public AdvancedSearchViewImpl() {
		presenter = new AdvancedSearchPresenter(this);
		presenter.resetFacetAndOrder();
		header = ConstellioUI.getCurrent().getHeader();
	}

	@Override
	public List<Criterion> getSearchCriteria() {
		return header.getAdvancedSearchCriteria();
	}

	@Override
	public void setSearchCriteria(List<Criterion> criteria) {
		header.setAdvancedSearchCriteria(criteria);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void downloadBatchProcessingResults(final InputStream stream) {
		Resource resource = new DownloadStreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return stream;
			}
		}, "results.xls");
		Page.getCurrent().open(resource, null, false);
	}

	@Override
	public void closeBatchProcessingWindow() {
		batchProcessingButton.getWindow().close();
	}

	@Override
	public String getSchemaType() {
		return header.getAdvancedSearchSchemaType();
	}

	@Override
	public void setSchemaType(String schemaTypeCode) {
		header.selectAdvancedSearchSchemaType(schemaTypeCode);
	}

	@Override
	public String getSearchExpression() {
		return header.getSearchExpression();
	}

	@Override
	protected Component buildSearchUI() {
		return new VerticalLayout();
	}

	@Override
	protected Component buildSummary(SearchResultTable results) {
		// TODO: Create an extension for this

		String schemaType = getSchemaType();
		List<Component> selectionActions = new ArrayList<>();

		batchProcessingButton = newBatchProcessingButton();
		batchProcessingButton.addStyleName(ValoTheme.BUTTON_LINK);
		batchProcessingButton.addStyleName(BATCH_PROCESS_BUTTONSTYLE);
		selectionActions.add(batchProcessingButton);

		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(ContainerRecord.SCHEMA_TYPE)) {
			Factory<List<LabelTemplate>> labelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getTemplates();
				}
			};
			final LabelsButton labelsButton = new LabelsButton($("SearchView.labels"),
					$("SearchView.printLabels"),
					getConstellioFactories().getAppLayerFactory(),
					getSessionContext().getCurrentCollection(),
					schemaType, getSelectedRecordIds(), getSessionContext().getCurrentUser().getUsername());
			labelsButton.addStyleName(ValoTheme.BUTTON_LINK);
			labelsButton.addStyleName(LABELS_BUTTONSTYLE);
			labelsButton.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					labelsButton.setIds(getSelectedRecordIds());
				}
			});
			selectionActions.add(labelsButton);
		}

		if (schemaType.equals(Document.SCHEMA_TYPE)) {
			Component zipButton = new Link($("ReportViewer.download", "(zip)"),
					new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
			zipButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectionActions.add(zipButton);
		}

		if (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE) ||
				schemaType.equals(ContainerRecord.SCHEMA_TYPE)) {
			if (presenter.hasCurrentUserPermissionToUseCart()) {
				Button addToCart = buildAddToCartButton();
				selectionActions.add(addToCart);
			}
		}

		Button switchViewMode = buildSwitchViewMode();

		// TODO Build SelectAllButton properly for table mode
//		List<Component> actions = Arrays.asList(
//				buildSelectAllButton(), buildSavedSearchButton(), (Component) new ReportSelector(presenter));
		List<Component> actions = Arrays.asList(
				buildSelectAllButton(), buildAddToSelectionButton(), buildSavedSearchButton(), (Component) new ReportSelector(presenter),switchViewMode);

		return results.createSummary(actions, selectionActions);
	}
	
	private String getSwitchViewModeCaption() {
		String caption;
		if (presenter.getResultsViewMode().equals(SearchResultsViewMode.DETAILED)) {
			caption = $("AdvancedSearchView.switchToTable");
		} else {
			caption = $("AdvancedSearchView.switchToList");
		}
		return caption;
	}

	private Button buildSwitchViewMode() {
		final Button switchViewModeButton = new Button(getSwitchViewModeCaption());
		switchViewModeButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				if (presenter.getResultsViewMode().equals(SearchResultsViewMode.DETAILED)) {
					presenter.switchToTableView();
				} else if(presenter.getResultsViewMode().equals(SearchResultsViewMode.TABLE)) {
					presenter.switchToDetailedView();
				}
				switchViewModeButton.setCaption(getSwitchViewModeCaption());
			}
		});
		switchViewModeButton.addStyleName(ValoTheme.BUTTON_LINK);
		return switchViewModeButton;
	}

	@Override
	protected SearchResultTable buildResultTable() {
		if (presenter.getResultsViewMode().equals(SearchResultsViewMode.TABLE)) {
			return buildSimpleResultsTable();
		} else {
			return buildDetailedResultsTable();
		}
	}

	private SearchResultTable buildSimpleResultsTable() {
		int maxSelectableResults = presenter.getMaxSelectableResults();
		final RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSearchResultsAsRecordVOs());
		SearchResultSimpleTable table = new SearchResultSimpleTable(container, maxSelectableResults, presenter);
		table.setWidth("100%");
		return table;
	}

	private WindowButton buildAddToCartButton() {
		WindowButton windowButton = new WindowButton($("SearchView.addToCart"), $("SearchView.selectCart")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();

				HorizontalLayout newCartLayout = new HorizontalLayout();
				newCartLayout.setSpacing(true);
				newCartLayout.addComponent(new Label($("CartView.newCart")));
				final BaseTextField newCartTitleField;
				newCartLayout.addComponent(newCartTitleField = new BaseTextField());
				BaseButton saveButton;
				newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.createNewCartAndAddToItRequested(newCartTitleField.getValue());
						getWindow().close();
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				final RecordVOLazyContainer ownedCartsContainer = new RecordVOLazyContainer(presenter.getOwnedCartsDataProvider());
				RecordVOTable ownedCartsTable = new RecordVOTable($("CartView.ownedCarts"), ownedCartsContainer);
				ownedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(getSelectedRecordIds(), ownedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(getSelectedRecordIds(), sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				ownedCartsTable.setPageLength(Math.min(15, ownedCartsContainer.size()));
				ownedCartsTable.setWidth("100%");
				sharedCartsTable.setPageLength(Math.min(15, ownedCartsContainer.size()));
				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout,tabSheet);
				return layout;
			}
		};
		windowButton.addStyleName(ValoTheme.BUTTON_LINK);
		return windowButton;
	}

	private WindowButton newBatchProcessingButton() {
		BatchProcessingMode mode = presenter.getBatchProcessingMode();
		if (mode.equals(ALL_METADATA_OF_SCHEMA)) {
			return new BatchProcessingButton(presenter, this);
		} else if (mode.equals(ONE_METADATA)) {
			return new BatchProcessingModifyingOneMetadataButton(presenter, this);
		} else {
			throw new RuntimeException("Unsupported mode " + mode);
		}
	}

	@Override
	public Boolean computeStatistics() {
		return presenter.computeStatistics();
	}

	@Override
	protected String getTitle() {
		return $("searchResults");
	}

}
