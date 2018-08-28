package com.constellio.app.ui.pages.management.schemas.type;

import com.constellio.app.api.extensions.params.ListSchemaExtraCommandReturnParams;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.menuBar.BaseMenuBar;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.data.SchemaVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.breadcrumb.BreadcrumbTrailUtil;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ListSchemaViewImpl extends BaseViewImpl implements ListSchemaView, ClickListener {
	ListSchemaPresenter presenter;
	public static final String OPTIONS_COL = "options";

	public ListSchemaViewImpl() {
		this.presenter = new ListSchemaPresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return this;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		presenter.backButtonClicked();
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		String parameters = event.getParameters();
		Map<String, String> paramsMap = ParamUtils.getParamsMap(parameters);
		presenter.setSchemaTypeCode(paramsMap.get("schemaTypeCode"));
		presenter.setParameters(paramsMap);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		viewLayout.addComponents(addButton, buildSchemasTables());
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private TabSheet buildSchemasTables() {
		TabSheet tabSheet = new TabSheet();
		Table activeRecordsTable = buildTables(true);
		Table inactiveRecordsTable = buildTables(false);

		VerticalLayout activeLayout = new VerticalLayout();
		activeLayout.addComponent(activeRecordsTable);

		VerticalLayout inactiveLayout = new VerticalLayout();
		inactiveLayout.addComponent(inactiveRecordsTable);

		tabSheet.addTab(activeLayout, $("ListValueDomainRecordsViewImpl.actives", activeRecordsTable.size()));
		tabSheet.addTab(inactiveLayout, $("ListValueDomainRecordsViewImpl.inactives", inactiveRecordsTable.size()));

		return tabSheet;
	}

	public void addSchemaToTable(final MetadataSchemaVO metadataSchemaVO, final boolean active,
								 Container indexedContainer) {
		MenuBar menuBar = new BaseMenuBar();
		MenuBar.MenuItem rootItem = menuBar.addItem("", FontAwesome.BARS, null);

		final MenuBar.Command editListener = new MenuBar.Command() {
			@Override
			public void menuSelected(MenuBar.MenuItem selectedItem) {
				presenter.editButtonClicked(metadataSchemaVO);
			}
		};

		rootItem.addItem($("ListSchemaViewImpl.menu.edit"), EditButton.ICON_RESOURCE, editListener);

		if (active) {
			final MenuBar.Command disableListener = new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					presenter.disableButtonClick(metadataSchemaVO.getCode());
				}
			};
			rootItem.addItem($("ListSchemaViewImpl.menu.disable"),
					new ThemeResource("images/commun/desactiverRouge.gif"), disableListener);
		} else {
			final MenuBar.Command enableListener = new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					presenter.enableButtonClick(metadataSchemaVO.getCode());
				}
			};
			rootItem.addItem($("ListSchemaViewImpl.menu.enable"),
					new ThemeResource("images/commun/reactiver.gif"), enableListener);
		}

		if (super.isVisible()) {
			final MenuBar.Command deleteListener = new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					presenter.deleteButtonClicked(metadataSchemaVO.getCode());
				}
			};
			rootItem.addItem($("ListSchemaViewImpl.menu.delete"), new ThemeResource("images/icons/actions/delete.png"),
					deleteListener);
		}

		final MenuBar.Command formOrderListener = new MenuBar.Command() {
			@Override
			public void menuSelected(MenuBar.MenuItem selectedItem) {
				presenter.formOrderButtonClicked(metadataSchemaVO);
			}
		};

		rootItem.addItem($("ListSchemaViewImpl.menu.formConfiguration"),
				new ThemeResource("images/icons/config/display-config-form.png"), formOrderListener);

		final MenuBar.Command formListener = new MenuBar.Command() {
			@Override
			public void menuSelected(MenuBar.MenuItem selectedItem) {
				presenter.formButtonClicked(metadataSchemaVO);
			}
		};

		rootItem.addItem($("ListSchemaViewImpl.menu.display"),
				new ThemeResource("images/icons/config/display-config-display.png"), formListener);

		final MenuBar.Command searchListener = new MenuBar.Command() {
			@Override
			public void menuSelected(MenuBar.MenuItem selectedItem) {
				presenter.searchButtonClicked(metadataSchemaVO);
			}
		};

		rootItem.addItem($("ListSchemaViewImpl.menu.searchResult"),
				new ThemeResource("images/icons/config/display-config-search.png"), searchListener);

		if (!(metadataSchemaVO == null || metadataSchemaVO.getCode() == null || !metadataSchemaVO.getCode()
				.endsWith("default"))) {
			final MenuBar.Command tableListener = new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					presenter.tableButtonClicked();
				}
			};
			rootItem.addItem($("ListSchemaViewImpl.menu.table"),
					new ThemeResource("images/icons/config/display-config-table.png"), tableListener);
		}

		for (ListSchemaExtraCommandReturnParams schemaExtraCommandReturnParams : presenter
				.getExtensionMenuBar(metadataSchemaVO)) {
			rootItem.addItem($(schemaExtraCommandReturnParams.getCaption()), schemaExtraCommandReturnParams.getRessource(),
					schemaExtraCommandReturnParams.getCommand());
		}

		HorizontalLayout buttonVerticalLayout = new HorizontalLayout();

		Button metadataButton = new Button($("ListSchemaView.button.metadata"));
		metadataButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.editMetadataButtonClicked(metadataSchemaVO);
			}
		});

		buttonVerticalLayout.addComponent(metadataButton);
		buttonVerticalLayout.addComponent(menuBar);
		buttonVerticalLayout.setSpacing(true);

		indexedContainer.addItem(metadataSchemaVO);

		indexedContainer.getContainerProperty(metadataSchemaVO, "title").setValue(metadataSchemaVO.getLabel());
		indexedContainer.getContainerProperty(metadataSchemaVO, "localCode").setValue(metadataSchemaVO.getLocalCode());
		indexedContainer.getContainerProperty(metadataSchemaVO, OPTIONS_COL).setValue(buttonVerticalLayout);
	}

	private Table buildTables(boolean active) {
		final SchemaVODataProvider dataProvider = presenter.getDataProvider(active);

		Container indexContainer = new IndexedContainer();
		indexContainer.addContainerProperty("localCode", String.class, "");
		indexContainer.addContainerProperty("title", String.class, "");
		indexContainer.addContainerProperty(OPTIONS_COL, HorizontalLayout.class, null);

		for (Integer integer : dataProvider.list()) {
			MetadataSchemaVO metadataSchemaVO = dataProvider.getSchemaVO(integer);
			addSchemaToTable(metadataSchemaVO, active, indexContainer);
		}

		Table table = new BaseTable(getClass().getName(), $("ListSchemaView.tableTitle", indexContainer.size()));
		table.setSizeFull();
		table.setContainerDataSource(indexContainer);
		table.setPageLength(Math.min(15, indexContainer.size()));
		table.setColumnHeader("localCode", $("ListSchemaView.localCode"));
		table.setColumnHeader("title", $("ListSchemaView.caption"));
		table.setColumnHeader(OPTIONS_COL, "");
		table.setColumnExpandRatio("title", 1);

		return table;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return asList(BreadcrumbTrailUtil.listSchemaTypeIntermediateBreadcrumb());
			}
		};
	}
}
