package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.restapi.core.util.ListUtils;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.navigation.RMViews;
import com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType;
import com.constellio.app.modules.rm.ui.breadcrumb.ContainerByAdministrativeUnitBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerBreadcrumbTrail;
import com.constellio.app.modules.rm.ui.components.breadcrumb.FolderDocumentContainerPresenterParam;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.actionDisplayManager.MenuDisplayList;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemFactory.MenuItemRecordProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.buttons.RecordVOActionButtonFactory;
import com.constellio.app.ui.framework.components.menuBar.ActionMenuDisplay;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

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

		buildActionMenuButtons();
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
	protected List<MenuItemAction> buildMenuItemActions(ViewChangeEvent event) {
		List<String> excludedActionTypes = new ArrayList<>();

		if (!isNested) {
			excludedActionTypes.add(ContainerRecordMenuItemActionType.CONTAINER_CONSULT.name());
		}

		List<MenuItemAction> menuItemActions = buildRecordVOActionButtonFactory(excludedActionTypes).buildMenuItemActions();

		if (consultButton != null) {
			menuItemActions.stream()
					.filter(menuItemAction -> menuItemAction.getType().equals(ContainerRecordMenuItemActionType.CONTAINER_CONSULT))
					.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, consultButton));
		}

		if (editButton != null) {
			menuItemActions.stream()
					.filter(menuItemAction -> menuItemAction.getType().equals(ContainerRecordMenuItemActionType.CONTAINER_EDIT))
					.forEach(menuItemAction -> updateMenuActionBasedOnButton(menuItemAction, editButton));
		}

		return ListUtils.flatMapFilteringNull(
				super.buildMenuItemActions(event),
				menuItemActions
		);
	}

	@Override
	protected ActionMenuDisplay buildActionMenuDisplay(ActionMenuDisplay defaultActionMenuDisplay) {

		ActionMenuDisplay actionMenuDisplay = new ActionMenuDisplay(defaultActionMenuDisplay) {
			@Override
			public Supplier<String> getSchemaTypeCodeSupplier() {
				return presenter.getSchema()::getTypeCode;
			}

			@Override
			public Supplier<MenuItemRecordProvider> getMenuItemRecordProviderSupplier() {
				return buildRecordVOActionButtonFactory()::buildMenuItemRecordProvider;
			}

			@Override
			public int getQuickActionCount() {
				return isNested ? 2 : MenuDisplayList.QUICK_ACTION_COUNT_DEFAULT;
			}
		};

		return actionMenuDisplay;
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory() {
		return buildRecordVOActionButtonFactory(Collections.emptyList());
	}

	private RecordVOActionButtonFactory buildRecordVOActionButtonFactory(List<String> excludedActionTypes) {
		excludedActionTypes.addAll(rmModuleExtensions.getFilteredActionsForContainers());
		return new RecordVOActionButtonFactory(presenter.getContainer(), excludedActionTypes);
	}

	protected void buildActionMenuButtons() {
		List<Button> buttonList = new RecordVOActionButtonFactory(presenter.getContainer(),
				rmModuleExtensions.getFilteredActionsForContainers()).build();

		consultButton = getConsultButton(buttonList);
		editButton = getEditButton(buttonList);
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
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getActionMenuBarCaption() {
		return null;
	}

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

	public String getRecordId() {
		return presenter.getContainerId();
	}

}
