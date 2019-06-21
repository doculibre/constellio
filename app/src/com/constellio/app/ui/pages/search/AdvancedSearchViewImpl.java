package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.pdf.ConsolidatedPdfButton;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.BaseLink;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPButtonImpl;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.ReportTabButton;
import com.constellio.app.ui.framework.components.ReportViewer.DownloadStreamResource;
import com.constellio.app.ui.framework.components.SearchResultSimpleTable;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.SearchResultVODataProvider;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingModifyingOneMetadataButton;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.enums.BatchProcessingMode;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.enums.BatchProcessingMode.ALL_METADATA_OF_SCHEMA;
import static com.constellio.model.entities.enums.BatchProcessingMode.ONE_METADATA;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter>
		implements AdvancedSearchView, BatchProcessingView, Observer {

	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;
	private WindowButton batchProcessingButton;
	private ReportTabButton reportButton;
	private SIPButtonImpl sipButton;

	public AdvancedSearchViewImpl() {
		presenter = new AdvancedSearchPresenter(this);
		presenter.addObserver(this);
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
		return null;
	}

	@Override
	protected Component buildSummary(final SearchResultTable results) {
		// TODO: Create an extension for this

		final String schemaType = getSchemaType();
		List<Component> selectionActions = new ArrayList<>();
		selectionActions.add(buildAddToSelectionButton());
		selectionActions.add(buildZipButton());

		batchProcessingButton = newBatchProcessingButton();
		batchProcessingButton.addStyleName(ValoTheme.BUTTON_LINK);
		batchProcessingButton.addStyleName(BATCH_PROCESS_BUTTONSTYLE);
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
			batchProcessingButton.setVisible(presenter.getUser().has(RMPermissionsTo.MANAGE_CONTAINERS).onSomething());
		} else if (StorageSpace.SCHEMA_TYPE.equals(schemaType)) {
			batchProcessingButton.setVisible(presenter.getUser().has(RMPermissionsTo.MANAGE_STORAGE_SPACES).globally());
		}

		if (!presenter.hasBatchProcessPermission()) {
			batchProcessingButton.setVisible(false);
		}

		selectionActions.add(batchProcessingButton);

		if (Folder.SCHEMA_TYPE.equals(schemaType) || ContainerRecord.SCHEMA_TYPE.equals(schemaType) || Document.SCHEMA_TYPE.equals(schemaType)) {
			Factory<List<LabelTemplate>> customLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getCustomTemplates();
				}
			};
			Factory<List<LabelTemplate>> defaultLabelTemplatesFactory = new Factory<List<LabelTemplate>>() {
				@Override
				public List<LabelTemplate> get() {
					return presenter.getDefaultTemplates();
				}
			};
			final LabelButtonV2 labelsButton = new LabelButtonV2($("SearchView.labels"),
					$("SearchView.printLabels"),
					customLabelTemplatesFactory,
					defaultLabelTemplatesFactory,
					getConstellioFactories().getAppLayerFactory(),
					getSessionContext().getCurrentCollection(),
					getSessionContext().getCurrentUser());
			labelsButton.setSchemaType(schemaType);
			labelsButton.addStyleName(ValoTheme.BUTTON_LINK);
			labelsButton.addStyleName(LABELS_BUTTONSTYLE);
			labelsButton.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					labelsButton.setElementsWithIds(getSelectedRecordIds(), schemaType, getSessionContext());
				}
			});
			selectionActions.add(labelsButton);
		}

		if (Document.SCHEMA_TYPE.equals(schemaType)) {
			Component zipButton = new BaseLink($("ReportViewer.download", "(zip)"),
					new DownloadStreamResource(presenter.getZippedContents(), presenter.getZippedContentsFilename()));
			zipButton.setIcon(FontAwesome.FILE_ARCHIVE_O);
			zipButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectionActions.add(zipButton);

			Button consolidatedPdfButton = new ConsolidatedPdfButton() {
				@Override
				public void buttonClick(ClickEvent event) {
					List<String> selectedDocumentIds = getSelectedRecordIds();
					if (presenter.isPdfGenerationActionPossible(selectedDocumentIds)) {
						setRecordIds(selectedDocumentIds);
						super.buttonClick(event);
					}
				}
			};
			consolidatedPdfButton.addStyleName(ValoTheme.BUTTON_LINK);
			selectionActions.add(consolidatedPdfButton);
		}


		reportButton = new ReportTabButton($("SearchView.metadataReportTitle"), $("SearchView.metadataReportTitle"), this,
				!presenter.getListSearchableMetadataSchemaType().contains(schemaType),
				!(Folder.SCHEMA_TYPE.equals(schemaType) || Document.SCHEMA_TYPE.equals(schemaType) || Task.SCHEMA_TYPE.equals(schemaType))) {

			BaseWindow querySelectionWindow;

			@Override
			public void buttonClick(ClickEvent event) {
				if (querySelectionWindow == null || !(ConstellioUI.getCurrent().getWindows() != null && ConstellioUI.getCurrent().getWindows().contains(querySelectionWindow))) {
					querySelectionWindow = new BaseWindow($("com.constellio.app.extensions.WorkflowPageExtension_confirmationTitle"));
					querySelectionWindow.setWidth("50%");
					querySelectionWindow.setHeight("220px");
					querySelectionWindow.center();
					querySelectionWindow.setModal(true);
					querySelectionWindow.setContent(buildQuerySelectionWindow());
					ConstellioUI.getCurrent().addWindow(querySelectionWindow);
					querySelectionWindow.focus();
				}
			}

			@Override
			protected LogicalSearchQuery getLogicalSearchQuery(String selectedSchemaFilter) {
				LogicalSearchQuery query = AdvancedSearchViewImpl.this.presenter.buildReportLogicalSearchQuery();
				if (selectedSchemaFilter != null) {
					query.setCondition(query.getCondition().andWhere(Schemas.SCHEMA).isEqualTo(selectedSchemaFilter));
				}
				return query;
			}

			private Component buildQuerySelectionWindow() {
				Panel panel = new Panel();
				VerticalLayout vLayout = new VerticalLayout();
				vLayout.setSpacing(true);

				Label questionLabel = new Label($("AdvancedSearch.reportRecordSelection"));

				BaseButton allSearchResultsButton = new BaseButton($("AdvancedSearchView.allSearchResults")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.allSearchResultsButtonClicked();
						querySelectionWindow.close();
						proceedToReportSelection(event);
					}
				};

				BaseButton selectedSearchResultsButton = new BaseButton($("AdvancedSearchView.selectedSearchResults")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.selectedSearchResultsButtonClicked();
						querySelectionWindow.close();
						proceedToReportSelection(event);
					}
				};

				if (getSelectedRecordIds() == null || getSelectedRecordIds().isEmpty()) {
					selectedSearchResultsButton.setEnabled(false);
				}

				vLayout.addComponents(questionLabel, allSearchResultsButton, selectedSearchResultsButton);

				panel.setContent(vLayout);
				panel.setSizeFull();
				return panel;
			}

			private void proceedToReportSelection(ClickEvent event) {
				super.buttonClick(event);
			}

			@Override
			public boolean isVisible() {
				return super.isVisible();
			}

			@Override
			public void setVisible(boolean visible) {
				super.setVisible(visible);
			}

			@Override
			public boolean isEnabled() {
				return super.isEnabled();
			}

			@Override
			public void setEnabled(boolean enabled) {
				super.setEnabled(enabled);
			}
		};
		reportButton.addStyleName(ValoTheme.BUTTON_LINK);
		reportButton.setVisible(presenter.hasAnyReportForSchemaType(schemaType));
		reportButton.setEnabled(presenter.hasAnyReportForSchemaType(schemaType));
		selectionActions.add(reportButton);


		if (Folder.SCHEMA_TYPE.equals(schemaType) || Document.SCHEMA_TYPE.equals(schemaType)) {
			if (presenter.hasCurrentUserPermissionToUseCart()) {
				Button addToCart = buildAddToCartButton();
				selectionActions.add(addToCart);
			}

			UserServices userServices = header.getConstellioFactories().getModelLayerFactory().newUserServices();
			boolean hasAccessToSIP = userServices.getUserInCollection(header.getSessionContext().getCurrentUser().getUsername(), getCollection())
					.has(RMPermissionsTo.GENERATE_SIP_ARCHIVES).globally();
			sipButton = new SIPButtonImpl($("SIPButton.caption"), $("SIPButton.caption"), ConstellioUI.getCurrent().getHeader(), true) {
				@Override
				public void buttonClick(ClickEvent event) {
					RecordVO[] recordVOS = presenter.getRecordVOList(results.getSelectedRecordIds())
							.toArray(new RecordVO[0]);
					sipButton.setAllObject(recordVOS);
					super.buttonClick(event);
				}
			};
			sipButton.addStyleName(ValoTheme.BUTTON_LINK);
			sipButton.setVisible(hasAccessToSIP);
			sipButton.setEnabled(hasAccessToSIP);
			selectionActions.add(sipButton);
		}

		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
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
				/*buildSelectAllButton(), buildAddToSelectionButton(),*/ buildSavedSearchButton()/*, (Component) switchViewMode*/);

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
				} else if (presenter.getResultsViewMode().equals(SearchResultsViewMode.TABLE)) {
					presenter.switchToDetailedView();
				}
				switchViewModeButton.setCaption(getSwitchViewModeCaption());
			}
		});
		switchViewModeButton.addStyleName(ValoTheme.BUTTON_LINK);
		return switchViewModeButton;
	}

    @Override
    protected SearchResultTable buildSimpleResultsTable(SearchResultVODataProvider dataProvider) {
         SearchResultTable table;
    	if (!Toggle.SEARCH_RESULTS_VIEWER.isEnabled()) {
        //Fixme : use dataProvider instead
        final RecordVOLazyContainer container = new RecordVOLazyContainer(presenter.getSearchResultsAsRecordVOs());
         table = new SearchResultSimpleTable(container, presenter);
        table.setWidth("100%");
        ((SearchResultSimpleTable)table).getTable().addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Object itemId = event.getItemId();
				RecordVOItem item = (RecordVOItem) container.getItem(itemId);
				RecordVO recordVO = item.getRecord();
				((AdvancedSearchPresenter) presenter).searchResultClicked(recordVO, (Integer) itemId);
			}});
    	} else {
    		table = super.buildSimpleResultsTable(dataProvider);
		}
		return table;
	}

	private WindowButton buildAddToCartButton() {
		WindowButton windowButton = new WindowButton($("SearchView.addToCart"), $("SearchView.selectCart")) {
			@Override
			protected Component buildWindowContent() {
				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();

				HorizontalLayout newCartLayout = new HorizontalLayout();
				newCartLayout.setSpacing(true);
				newCartLayout.addComponent(new Label($("CartView.newCart")));
				final BaseTextField newCartTitleField;
				newCartLayout.addComponent(newCartTitleField = new BaseTextField());
				newCartTitleField.setRequired(true);
				BaseButton saveButton;
				newCartLayout.addComponent(saveButton = new BaseButton($("save")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						try {
							presenter.createNewCartAndAddToItRequested(newCartTitleField.getValue());
							getWindow().close();
						} catch (Exception e) {
							showErrorMessage(MessageUtils.toMessage(e));
						}
					}
				});
				saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);

				TabSheet tabSheet = new TabSheet();
				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(
						presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(getSelectedRecordIds(),
								sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow());
				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout, tabSheet);
				layout.setExpandRatio(tabSheet, 1);
				return layout;
			}
		};
		windowButton.addStyleName(ValoTheme.BUTTON_LINK);
		return windowButton;
	}

	private Table buildOwnedFavoritesTable(final Window window) {
		List<DefaultFavoritesTable.CartItem> cartItems = new ArrayList<>();
		cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTableAdvancedSearch", container, presenter.getSchema());
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					presenter.addToDefaultFavorite(getSelectedRecordIds());
				} else {
					presenter.addToCartRequested(getSelectedRecordIds(), cart);
				}
				window.close();
			}
		});
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	private WindowButton newBatchProcessingButton() {
		BatchProcessingMode mode = presenter.getBatchProcessingMode();
		if (mode.equals(ALL_METADATA_OF_SCHEMA)) {
			return new BatchProcessingButton(presenter, this).hasResultSelected(!getSelectedRecordIds().isEmpty());
		} else if (mode.equals(ONE_METADATA)) {
			return new BatchProcessingModifyingOneMetadataButton(presenter, this)
					.hasResultSelected(!getSelectedRecordIds().isEmpty());
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

	@Override
	public void update(Observable o, Object arg) {
		if (reportButton != null) {
			reportButton.addRecordToVoList((RecordVO) arg);
		}
	}

	@Override
	public void fireSomeRecordsSelected() {
		if (batchProcessingButton != null) {
			if (batchProcessingButton instanceof BatchProcessingButton) {
				((BatchProcessingButton) batchProcessingButton).hasResultSelected(true);
			} else if (batchProcessingButton instanceof BatchProcessingModifyingOneMetadataButton) {
				((BatchProcessingModifyingOneMetadataButton) batchProcessingButton).hasResultSelected(true);
			}
		}
	}

	@Override
	public void fireNoRecordSelected() {
		if (batchProcessingButton != null) {
			if (batchProcessingButton instanceof BatchProcessingButton) {
				((BatchProcessingButton) batchProcessingButton).hasResultSelected(false);
			} else if (batchProcessingButton instanceof BatchProcessingModifyingOneMetadataButton) {
				((BatchProcessingModifyingOneMetadataButton) batchProcessingButton).hasResultSelected(false);
			}
		}
	}

	@Override
	public NewReportPresenter getPresenter() {
		return presenter;
	}
}
