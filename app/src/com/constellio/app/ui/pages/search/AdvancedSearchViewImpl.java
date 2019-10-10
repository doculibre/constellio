package com.constellio.app.ui.pages.search;

import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.components.SearchResultTable;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.search.batchProcessing.BatchProcessingView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.util.MessageUtils;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.constellio.app.ui.i18n.i18n.$;

public class AdvancedSearchViewImpl extends SearchViewImpl<AdvancedSearchPresenter>
		implements AdvancedSearchView, BatchProcessingView, Observer {

	public static final String BATCH_PROCESS_BUTTONSTYLE = "searchBatchProcessButton";
	public static final String LABELS_BUTTONSTYLE = "searchLabelsButton";

	private final ConstellioHeader header;
	//	private WindowButton batchProcessingButton;
	//	private ReportTabButton reportButton;

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
		//		batchProcessingButton.getWindow().close();
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
		return results.createSummary(Collections.emptyList(), Collections.emptyList());
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

	//	private WindowButton newBatchProcessingButton() {
	//		BatchProcessingMode mode = presenter.getBatchProcessingMode();
	//		if (mode.equals(ALL_METADATA_OF_SCHEMA)) {
	//			return new BatchProcessingButton(presenter, this).hasResultSelected(!getSelectedRecordIds().isEmpty());
	//		} else if (mode.equals(ONE_METADATA)) {
	//			return new BatchProcessingModifyingOneMetadataButton(presenter, this)
	//					.hasResultSelected(!getSelectedRecordIds().isEmpty());
	//		} else {
	//			throw new RuntimeException("Unsupported mode " + mode);
	//		}
	//	}

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
		//		if (reportButton != null) {
		//			reportButton.addRecordToVoList((RecordVO) arg);
		//		}
	}

	@Override
	public void fireSomeRecordsSelected() {
		//		if (batchProcessingButton != null) {
		//			if (batchProcessingButton instanceof BatchProcessingButton) {
		//				((BatchProcessingButton) batchProcessingButton).hasResultSelected(true);
		//			} else if (batchProcessingButton instanceof BatchProcessingModifyingOneMetadataButton) {
		//				((BatchProcessingModifyingOneMetadataButton) batchProcessingButton).hasResultSelected(true);
		//			}
		//		}
	}

	@Override
	public void fireNoRecordSelected() {
		//		if (batchProcessingButton != null) {
		//			if (batchProcessingButton instanceof BatchProcessingButton) {
		//				((BatchProcessingButton) batchProcessingButton).hasResultSelected(false);
		//			} else if (batchProcessingButton instanceof BatchProcessingModifyingOneMetadataButton) {
		//				((BatchProcessingModifyingOneMetadataButton) batchProcessingButton).hasResultSelected(false);
		//			}
		//		}
	}

	@Override
	public NewReportPresenter getPresenter() {
		return presenter;
	}
}
