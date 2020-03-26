package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType;
import com.constellio.app.modules.rm.ui.breadcrumb.ContainerByAdministrativeUnitBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerPresenterParam;
import com.constellio.app.modules.rm.ui.pages.cart.DefaultFavoritesTable;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.constellio.app.ui.util.MessageUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayContainerViewImpl extends BaseViewImpl implements DisplayContainerView {
	private final DisplayContainerPresenter presenter;
	private Label borrowedLabel;
	private boolean popup = false;
	private boolean isNested = false;
	private Button consultButton = null;
	private Button editButton = null;
	private RMModuleExtensions rmModuleExtensions;
	public DisplayContainerViewImpl() {
		this(null, false, false);
	}


	public DisplayContainerViewImpl(RecordVO recordVO, boolean popup, boolean isNested) {
		presenter = new DisplayContainerPresenter(this, recordVO, popup);
		this.isNested = isNested;
		this.popup = popup;
		rmModuleExtensions = getConstellioFactories().getAppLayerFactory()
				.getExtensions().forCollection(getCollection()).forModule(ConstellioRMModule.ID);
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

		if (isNested) {
			Label titleLabel = new Label(getTitleCaption());
			titleLabel.addStyleName(ValoTheme.LABEL_H1);
			layout.addComponent(titleLabel);
		}

		layout.addComponents(borrowedLabel, new RecordDisplay(recordVO, metadataDisplayFactory));

		layout.addComponent(buildFoldersTable(presenter.getFolders()));
		return layout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		if (!popup && !isNested) {
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

	private String getTitleCaption() {
		return $("DisplayContainerView.viewTitle");
	}

	@Override
	protected String getTitle() {
		if (!isNested) {
			return getTitleCaption();
		} else {
			return null;
		}
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> buttonList = new RecordVOActionButtonFactory(presenter.getContainer(),
				rmModuleExtensions.getFilteredActionsForContainers()).build();

		consultButton = getConsultButton(buttonList);

		if (consultButton != null) {
			buttonList.remove(consultButton);
		}

		editButton = getEditButton(buttonList);

		if (!isNested && editButton != null) {
			buttonList.remove(editButton);
		}

		return buttonList;
	}

	private Button getConsultButton(List<Button> buttons) {
		return getButtonById(buttons, ContainerRecordMenuItemActionType.CONTAINER_CONSULT.name());
	}

	private Button getEditButton(List<Button> buttons) {
		return getButtonById(buttons, ContainerRecordMenuItemActionType.CONTAINER_EDIT.name());
	}

	private Button getButtonById(List<Button> buttons, String id) {
		for (Button button : buttons) {
			if (button.getId().equals(id)) {
				return button;
			}
		}

		return null;
	}

	@Override
	protected List<Button> getQuickActionMenuButtons() {
		if (consultButton != null && isNested) {
			return Arrays.asList(consultButton);
		} else if (editButton != null && !isNested) {
			return Arrays.asList(editButton);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected boolean isActionMenuBar() {
		return true;
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
	protected boolean isBreadcrumbsVisible() {
		return !isNested;
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

			return new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(null, null, presenter.getContainer().getId(), favGroupIdKey, this));
		} else if (searchId == null && regularSearchId != null && advancedSearch) {

			return new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(null, null, presenter.getContainer().getId(), null, this));
		} else {
			return new FolderDocumentContainerBreadcrumbTrail(new FolderDocumentContainerPresenterParam(null, null, presenter.getContainer().getId(), null, this)) {
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
