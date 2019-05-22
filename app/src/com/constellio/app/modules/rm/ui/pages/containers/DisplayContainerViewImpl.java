package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.ui.breadcrumb.ContainerByAdministrativeUnitBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.ReportButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.LabelButtonV2;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.data.utils.Factory;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayContainerViewImpl extends BaseViewImpl implements DisplayContainerView {
	private final DisplayContainerPresenter presenter;
	private Label borrowedLabel;
	private boolean popup = false;
	private MetadataSchemaToVOBuilder schemaVOBuilder = new MetadataSchemaToVOBuilder();
	public DisplayContainerViewImpl() {
		this(null, false);
	}


	public DisplayContainerViewImpl(RecordVO recordVO, boolean popup) {
		presenter = new DisplayContainerPresenter(this, recordVO, popup);
		this.popup = popup;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (event != null) {
			presenter.forParams(event.getParameters());
		}
	}

	public DisplayContainerPresenter getPresenter() {
		return this.presenter;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		setBorrowedMessage(presenter.getBorrowMessageState(presenter.getContainer()));
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setWidth("100%");
		layout.setSpacing(true);

		final RecordVO recordVO = presenter.getContainer();
		borrowedLabel = new Label();
		borrowedLabel.setVisible(false);
		borrowedLabel.addStyleName(ValoTheme.LABEL_COLORED);
		borrowedLabel.addStyleName(ValoTheme.LABEL_BOLD);
		MetadataDisplayFactory metadataDisplayFactory = new MetadataDisplayFactory() {
			@Override
			public Component build(RecordVO recordVO, MetadataValueVO metadataValue) {
				if (metadataValue.getMetadata().getLocalCode().equals(ContainerRecord.FILL_RATIO_ENTRED)) {
					return buildSingleValue(recordVO, metadataValue.getMetadata(), metadataValue.getValue());
				} else {
					return super.build(recordVO, metadataValue);
				}
			}

			@Override
			public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
				if (metadata.getLocalCode().equals(ContainerRecord.FILL_RATIO_ENTRED)) {
					try {
						Double fillRatio = presenter.getFillRatio(recordVO);
						return new Label(fillRatio.toString());
					} catch (ContainerWithoutCapacityException e) {
						return new Label($("ContainerWithoutCapacityException"));
					} catch (RecordInContainerWithoutLinearMeasure e) {
						return new Label($("RecordInContainerWithoutLinearMeasure"));
					}
				} else {
					return super.buildSingleValue(recordVO, metadata, displayValue);
				}
			}
		};
		layout.addComponents(borrowedLabel, new RecordDisplay(recordVO, metadataDisplayFactory) {
			@Override
			protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
				super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
			}
		});

		layout.addComponent(buildFoldersTable(presenter.getFolders()));

		return layout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		if (!popup) {
			return new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					presenter.backButtonClicked();
				}
			};
		} else {
			return null;
		}
	}

	private Component buildFoldersTable(final RecordVODataProvider provider) {
		RecordVOLazyContainer folders = new RecordVOLazyContainer(provider);
		ButtonsContainer<RecordVOLazyContainer> container = new ButtonsContainer<>(folders, "buttons");
		container.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						RecordVO entity = provider.getRecordVO(index);
						presenter.displayFolderButtonClicked(entity);
					}
				};
			}
		});

		RecordVOTable table = new RecordVOTable($("DisplayContainerView.foldersTableTitle"), container);
		if (popup) {
			table.setWidth("80%");
		} else {
			table.setWidth("100%");
		}

		table.setColumnHeader("buttons", "");

		return table;
	}

	@Override
	protected String getTitle() {
		return $("DisplayContainerView.viewTitle");
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> actionMenuButtons = super.buildActionMenuButtons(event);
		Button edit = new EditButton($("DisplayContainerView.edit")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.editContainer();
			}
		};
		edit.setVisible(presenter.isEditButtonVisible());
		actionMenuButtons.add(edit);

		Button slip = new ReportButton(new ReportWithCaptionVO("Reports.ContainerRecordReport", $("Reports.ContainerRecordReport")), presenter) {
			@Override
			protected Component buildWindowContent() {
				presenter.saveIfFirstTimeReportCreated();
				return super.buildWindowContent();
			}
		};
		slip.setCaption($("DisplayContainerView.slip"));
		slip.setStyleName(ValoTheme.BUTTON_LINK);
		slip.setEnabled(presenter.canPrintReports());
		actionMenuButtons.add(slip);
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
		Button labels = new LabelButtonV2($("SearchView.labels"), $("SearchView.printLabels"), customLabelTemplatesFactory,
				defaultLabelTemplatesFactory, getConstellioFactories().getAppLayerFactory(),
				getSessionContext().getCurrentCollection(),getSessionContext().getCurrentUser(), presenter.getContainer());
		labels.setEnabled(presenter.canPrintReports());
		actionMenuButtons.add(labels);
		WindowButton addToCartButton = buildAddToCartButton();
		Button addToCartMyCartButton = buildAddToMyCartButton();

		if (presenter.hasCurrentUserPermissionToUseCartGroup()) {
			actionMenuButtons.add(addToCartButton);
		} else if (presenter.hasCurrentUserPermissionToUseMyCart()){
			actionMenuButtons.add(addToCartMyCartButton);
		}

		actionMenuButtons.add(addToCartButton);
		Button empty = new ConfirmDialogButton($("DisplayContainerView.empty")) {
			@Override
			protected String getConfirmDialogMessage() {
				return $("DisplayContainerView.confirmEmpty");
			}

			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.emptyButtonClicked();
			}
		};
		ComponentState state = presenter.getEmptyButtonState();
		empty.setVisible(state.isVisible());
		empty.setEnabled(state.isEnabled());
		actionMenuButtons.add(empty);

		Button delete = new DeleteButton($("DisplayContainerView.delete")) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				presenter.deleteButtonClicked();
			}
		};
		delete.setVisible(presenter.canDelete());
		delete.setEnabled(presenter.canDelete());
		actionMenuButtons.add(delete);

		return actionMenuButtons;
	}

	private Button buildAddToMyCartButton(){
		Button button = new BaseButton($("DisplayFolderView.addToCart")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addToDefaultFavorite();
			}
		};

		return button;
	}

	// TODO: Quick hack to make printing container labels work...
	//	public static class ContainerLabelsButton extends WindowButton {
	//		private final RecordSelector selector;
	//		private final List<LabelTemplate> labelTemplates;
	//		private NewReportWriterFactory<LabelsReportParameters> labelsReportFactory;
	//
	//		@PropertyId("startPosition") private ComboBox startPosition;
	//		@PropertyId("numberOfCopies") private TextField copies;
	//		@PropertyId("labelConfiguration") private ComboBox labelConfiguration;
	//
	//		public ContainerLabelsButton(String caption, String windowCaption, RecordSelector selector,
	//				List<LabelTemplate> labelTemplates, final NewReportWriterFactory<LabelsReportParameters> labelsReportFactory) {
	//			super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
	//			this.selector = selector;
	//			this.labelTemplates = labelTemplates;
	//			this.labelsReportFactory = labelsReportFactory;
	//		}
	//
	//		@Override
	//		protected Component buildWindowContent() {
	//			startPosition = new ComboBox($("LabelsButton.startPosition"));
	//			if (labelTemplates.size() > 0) {
	//				int size = labelTemplates.get(0).getLabelsReportLayout().getNumberOfLabelsPerPage();
	//				startPosition.clear();
	//				for (int i = 1; i <= size; i++) {
	//					startPosition.addItem(i);
	//				}
	//			}
	//			for (int i = 1; i <= 10; i++) {
	//				startPosition.addItem(i);
	//			}
	//			startPosition.setNullSelectionAllowed(false);
	//
	//			labelConfiguration = new ComboBox($("LabelsButton.labelFormat"));
	//			for (LabelTemplate labelTemplate : labelTemplates) {
	//				labelConfiguration.addItem(labelTemplate);
	//				labelConfiguration.setItemCaption(labelTemplate, $(labelTemplate.getName()));
	//			}
	//			labelConfiguration.setNullSelectionAllowed(false);
	//			labelConfiguration.setImmediate(true);
	//			labelConfiguration.addValueChangeListener(new ValueChangeListener() {
	//				@Override
	//				public void valueChange(ValueChangeEvent event) {
	//					LabelTemplate labelTemplate = (LabelTemplate) event.getProperty().getValue();
	//					int size = labelTemplate.getLabelsReportLayout().getNumberOfLabelsPerPage();
	//					startPosition.clear();
	//					startPosition.removeAllItems();
	//					for (int i = 1; i <= size; i++) {
	//
	//						startPosition.addItem(i);
	//					}
	//				}
	//			});
	//
	//			copies = new TextField($("LabelsButton.numberOfCopies"));
	//			copies.setConverter(Integer.class);
	//
	//			return new BaseForm<LabelParametersVO>(
	//					new LabelParametersVO(labelTemplates.get(0)), this, labelConfiguration, startPosition,
	//					copies) {
	//				@Override
	//				protected void saveButtonClick(LabelParametersVO parameters)
	//						throws ValidationException {
	//					LabelsReportParameters labelsReportParameters = new LabelsReportParameters(
	//							selector.getSelectedRecordIds(),
	//							parameters.getLabelConfiguration(),
	//							parameters.getStartPosition(),
	//							parameters.getNumberOfCopies());
	//
	//					getWindow().setContent(new ReportViewer(labelsReportFactory.getReportBuilder(labelsReportParameters),
	//							labelsReportFactory.getFilename(null)));
	//				}
	//
	//				@Override
	//				protected void cancelButtonClick(LabelParametersVO parameters) {
	//					getWindow().close();
	//				}
	//			};
	//		}
	//	}

	@Override
	public void setBorrowedMessage(String borrowedMessage) {
		if (borrowedMessage != null) {
			borrowedLabel.setVisible(true);
			borrowedLabel.setValue($(borrowedMessage));
		} else {
			borrowedLabel.setVisible(false);
			borrowedLabel.setValue(null);
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		String searchId = getUIContext().getAttribute(DecommissioningBuilderViewImpl.SAVE_SEARCH_DECOMMISSIONING);

		if(presenter.getAdministrativeUnitId() != null && presenter.getTabName() != null) {
			return new ContainerByAdministrativeUnitBreadcrumbTrail(presenter.getContainerId(), presenter.getAdministrativeUnitId(), this, presenter.getTabName()) {
				@Override
				public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
					return Arrays.asList(BreadcrumbTrailUtil.containterByAdministrativeUnit(presenter.getTabName()));
				}
			};
		}

		String regularSearchId = getUIContext().getAttribute(BaseBreadcrumbTrail.SEARCH_ID);
		Boolean advancedSearch = getUIContext().getAttribute(BaseBreadcrumbTrail.ADVANCED_SEARCH);

		String favGroupIdKey = null;

		if(presenter.getParams() != null) {
			favGroupIdKey = presenter.getParams().get(RMViews.FAV_GROUP_ID_KEY);
		}

		if(favGroupIdKey != null) {
			return new FolderDocumentContainerBreadcrumbTrail(null, null,presenter.getContainer().getId(), favGroupIdKey, this);
		} else if (searchId == null && regularSearchId != null && advancedSearch) {
			return new FolderDocumentContainerBreadcrumbTrail(null, null, presenter.getContainer().getId(), this);
		} else {
			return new FolderDocumentContainerBreadcrumbTrail(null, null, presenter.getContainer().getId(), this) {
				@Override
				public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
					return Arrays.asList(BreadcrumbTrailUtil.getArchiveManagementIntermediateBreadcrumb());
				}
			};
		}
	}

	private WindowButton buildAddToCartButton() {
		WindowButton.WindowConfiguration configuration = new WindowButton.WindowConfiguration(true, true, "50%", "750px");
		return new WindowButton($("DisplayContainerView.addToCart"), $("DisplayContainerView.selectCart"), configuration) {
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
				Table ownedCartsTable = buildOwnedFavoritesTable(getWindow());

				final RecordVOLazyContainer sharedCartsContainer = new RecordVOLazyContainer(
						presenter.getSharedCartsDataProvider());
				RecordVOTable sharedCartsTable = new RecordVOTable($("CartView.sharedCarts"), sharedCartsContainer);
				sharedCartsTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
					@Override
					public void itemClick(ItemClickEvent event) {
						presenter.addToCartRequested(sharedCartsContainer.getRecordVO((int) event.getItemId()));
						getWindow().close();
					}
				});

				sharedCartsTable.setWidth("100%");
				tabSheet.addTab(ownedCartsTable);
				tabSheet.addTab(sharedCartsTable);
				layout.addComponents(newCartLayout, tabSheet);
				layout.setExpandRatio(tabSheet, 1);
				return layout;
			}
		};
	}

	private DefaultFavoritesTable buildOwnedFavoritesTable(final Window window) {
		List<DefaultFavoritesTable.CartItem> cartItems = new ArrayList<>();
		if(presenter.hasCurrentUserPermissionToUseMyCart()) {
			cartItems.add(new DefaultFavoritesTable.CartItem($("CartView.defaultFavorites")));
		}

		for (Cart cart : presenter.getOwnedCarts()) {
			cartItems.add(new DefaultFavoritesTable.CartItem(cart, cart.getTitle()));
		}
		final DefaultFavoritesTable.FavoritesContainer container = new DefaultFavoritesTable.FavoritesContainer(DefaultFavoritesTable.CartItem.class, cartItems);
		DefaultFavoritesTable defaultFavoritesTable = new DefaultFavoritesTable("favoritesTableContainerDisplay", container, presenter.getSchema());
		defaultFavoritesTable.setCaption($("CartView.ownedCarts"));
		defaultFavoritesTable.addItemClickListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Cart cart = container.getCart((DefaultFavoritesTable.CartItem) event.getItemId());
				if (cart == null) {
					presenter.addToDefaultFavorite();
				} else {
					presenter.addToCartRequested(cart);
				}
				window.close();
			}
		});
		container.removeContainerProperty(DefaultFavoritesTable.CartItem.DISPLAY_BUTTON);
		defaultFavoritesTable.setWidth("100%");
		return defaultFavoritesTable;
	}

	public String getRecordId() {
		return presenter.getContainerId();
	}

}
