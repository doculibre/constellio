package com.constellio.app.ui.pages.management.schemas;

import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.buttons.ListMetadataGroupButton;
import com.constellio.app.ui.framework.components.TabWithTable;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.SchemaTypeVOLazyContainer;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.Container;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.api.extensions.GenericRecordPageExtension.DDV_TAB;
import static com.constellio.app.api.extensions.GenericRecordPageExtension.OTHERS_TAB;
import static com.constellio.app.api.extensions.GenericRecordPageExtension.TAXONOMY_TAB;
import static com.constellio.app.services.menu.MenuItemServices.BATCH_ACTIONS_FAKE_SCHEMA_TYPE;
import static com.constellio.app.ui.i18n.i18n.$;

public class ListSchemaTypeViewImpl extends BaseViewImpl implements ListSchemaTypeView, ClickListener {
	ListSchemaTypePresenter presenter;
	public static final String TYPE_TABLE = "types";
	private TabSheet sheet = new TabSheet();
	private List<TabWithTable> tabs = new ArrayList<>();

	public static final Resource ICON_EXCEL_RESOURCE = new ThemeResource("images/icons/excel-metadata-generate.png");


	public ListSchemaTypeViewImpl() {
		this.presenter = new ListSchemaTypePresenter(this);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("ListSchemaTypeView.viewTitle");
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return this;
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		addTab(TAXONOMY_TAB, $("ListSchemaTypeView.taxonomyTabCaption"));
		addTab(DDV_TAB, $("ListSchemaTypeView.ddvTabCaption"));
		addTab(OTHERS_TAB, $("ListSchemaTypeView.othersTabCaption"));


		Button batchActionMenuEditor = buildBatchActionMenuConfigButton();
		viewLayout.addComponent(batchActionMenuEditor, 0);
		viewLayout.setComponentAlignment(batchActionMenuEditor, Alignment.BOTTOM_RIGHT);

		viewLayout.addComponents(sheet);
		return viewLayout;
	}

	public TabWithTable addTab(final String id, String caption) {
		TabWithTable returnedTab = null;

		for (TabWithTable tab : tabs) {
			if (tab.getId().equals(id)) {
				returnedTab = tab;
				break;
			}
		}

		if (returnedTab == null) {
			TabWithTable tab = new TabWithTable(id) {
				@Override
				public Table buildTable() {
					return ListSchemaTypeViewImpl.this.buildTable(presenter.getDataProvider(id));
				}
			};
			tabs.add(tab);
			sheet.addTab(tab.getTabLayout(), caption);

			returnedTab = tab;
		}

		return returnedTab;
	}

	private Table buildTable(final SchemaTypeVODataProvider dataProvider) {
		//		final SchemaTypeVODataProvider dataProvider = presenter.getDataProvider();

		Container typeContainer = new SchemaTypeVOLazyContainer(dataProvider);
		ButtonsContainer buttonsContainer = new ButtonsContainer(typeContainer, "buttons");
		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaTypeVO entity = dataProvider.getSchemaTypeVO(index);
						presenter.editButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new ListMetadataGroupButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaTypeVO entity = dataProvider.getSchemaTypeVO(index);
						presenter.listGroupButtonClicked(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new IconButton(ICON_EXCEL_RESOURCE, $("ListSchemaTypeView.generateExcelFileForSchemaType")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						Integer index = (Integer) itemId;
						MetadataSchemaTypeVO entity = dataProvider.getSchemaTypeVO(index);
						presenter.generateExcelWithMetadataInfo(entity);
					}
				};
			}
		});

		buttonsContainer.addButton(new ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				Integer index = (Integer) itemId;
				MetadataSchemaTypeVO entity = dataProvider.getSchemaTypeVO(index);
				IconButton button = new IconButton(new ThemeResource("images/icons/config/display-config-summary-column.png"),
						$("MenuDisplayConfigViewImpl.displaySchemaType.menu.caption")) {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editActionMenuButtonClicked(entity);
					}
				};

				button.setVisible(presenter.editActionMenuButtonIsVisible(entity));
				return button;
			}
		});

		typeContainer = buttonsContainer;

		Table table = new BaseTable(getClass().getName(), $("ListSchemaTypeView.tableTitle", typeContainer.size()), typeContainer);
		table.setSizeFull();
		table.setPageLength(Math.min(15, typeContainer.size()));
		table.setColumnHeader("buttons", "");
		table.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
		table.setColumnExpandRatio("caption", 1);
		table.addStyleName(TYPE_TABLE);
		table.addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				Integer index = (Integer) event.getItemId();
				MetadataSchemaTypeVO entity = dataProvider.getSchemaTypeVO(index);
				presenter.editButtonClicked(entity);
			}
		});
		table.setSortContainerPropertyId(SchemaTypeVOLazyContainer.LABEL);

		return table;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		presenter.backButtonClicked();
	}

	@Override
	public void startDownload(String filename, final InputStream inputStream, String mimeType) {
		StreamSource streamSource = new StreamSource() {
			@Override
			public InputStream getStream() {
				return inputStream;
			}
		};
		StreamResource resource = new StreamResource(streamSource, filename);
		resource.setMIMEType(mimeType);
		Page.getCurrent().open(resource, "_blank", false);
	}

	private Button buildBatchActionMenuConfigButton() {
		Button batchActionMenuEditor = new Button($("MenuDisplayConfigViewImpl.displaySchemaType.bulkAction.caption"), event -> {
			navigateTo().menuDisplayForm(BATCH_ACTIONS_FAKE_SCHEMA_TYPE);
		});
		batchActionMenuEditor.addStyleName(ValoTheme.BUTTON_LINK);

		return batchActionMenuEditor;
	}
}
