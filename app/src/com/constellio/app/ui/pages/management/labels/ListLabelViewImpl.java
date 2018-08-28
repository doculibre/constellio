package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.LabelVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.DisplayButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.buttons.GetXMLButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.breadcrumb.IntermediateBreadCrumbTailItem;
import com.constellio.app.ui.framework.components.breadcrumb.TitleBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.RecordVOTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.RecordVOLazyContainer;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.framework.data.SchemaTypeVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.schemas.Schemas;
import com.vaadin.data.Container;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.io.FileUtils;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ListLabelViewImpl extends BaseViewImpl implements AddEditLabelView {
	private List<LabelVO> listLabel;
	private TabSheet tabSheet;
	private AddEditLabelPresenter presenter;
	private Component folderDisplay, containerDisplay;
	private VerticalLayout mainLayout;
	private Button addLabelButton, downloadTemplateButton;
	private String currentSchema;
	final private GetXMLButton getXMLButton = new GetXMLButton($("DisplayLabelViewImpl.menu.getXMLButton"), $("DisplayLabelViewImpl.menu.getXMLButton"), getConstellioFactories().getAppLayerFactory(), getSessionContext().getCurrentCollection(), this, true);
	public static final String TYPE_TABLE = "types";

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return new TitleBreadcrumbTrail(this, getTitle()) {
			@Override
			public List<? extends IntermediateBreadCrumbTailItem> getIntermediateItems() {
				return Collections.singletonList(new IntermediateBreadCrumbTailItem() {
					@Override
					public boolean isEnabled() {
						return true;
					}

					@Override
					public String getTitle() {
						return $("ViewGroup.PrintableViewGroup");
					}

					@Override
					public void activate(Navigation navigate) {
						navigate.to().viewReport();
					}
				});
			}
		};
	}

	@Override
	protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
		mainLayout = new VerticalLayout();
		presenter = new AddEditLabelPresenter(this);
		folderDisplay = new CustomComponent();
		containerDisplay = new CustomComponent();
		this.currentSchema = Folder.SCHEMA_TYPE;

		//        final RecordVOLazyContainer FolderLabel = new RecordVOLazyContainer(presenter.getLabelFolderDataProvider());
		//        final RecordVOTable FolderLabelTable = new RecordVOTable($("DisplayLabelViewImpl.tabs.table.header"), FolderLabel);
		//        FolderLabelTable.setWidth("100%");
		//
		//        final RecordVOLazyContainer ContainerLabel = new RecordVOLazyContainer(presenter.getLabelContainerDataProvider());
		//        RecordVOTable ContainerLabelTable = new RecordVOTable($("DisplayLabelViewImpl.tabs.table.header"), ContainerLabel);
		//        ContainerLabelTable.setWidth("100%");

		//        folderDisplay.setWidth("100%");
		//        containerDisplay.setWidth("100%");
		final SchemaTypeVODataProvider dataProvider = presenter.getDataProvider();
		Container folderContainer = new RecordVOLazyContainer(presenter.getLabelFolderDataProvider());
		Container conteneurContainer = new RecordVOLazyContainer(presenter.getLabelContainerDataProvider());
		ButtonsContainer buttonsContainerForContainer = new ButtonsContainer(conteneurContainer, "buttons");
		ButtonsContainer buttonsContainerForFolder = new ButtonsContainer(folderContainer, "buttons");
		buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), ContainerRecord.SCHEMA_TYPE);
					}
				};
			}
		});

		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DisplayButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.displayButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), Folder.SCHEMA_TYPE);
					}
				};
			}
		});
		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), Folder.SCHEMA_TYPE);
					}

					@Override
					public boolean isVisible() {
						RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
						return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
					}
				};
			}
		});

		buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {
			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new EditButton() {
					@Override
					protected void buttonClick(ClickEvent event) {
						presenter.editButtonClicked(presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + ""), ContainerRecord.SCHEMA_TYPE);
					}

					@Override
					public boolean isVisible() {
						RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
						return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
					}
				};
			}
		});

		buttonsContainerForContainer.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.removeRecord(itemId + "", ListLabelViewImpl.this.currentSchema);
					}

					@Override
					public boolean isVisible() {
						RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
						return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
					}
				};
			}
		});

		buttonsContainerForFolder.addButton(new ButtonsContainer.ContainerButton() {

			@Override
			protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
				return new DeleteButton() {
					@Override
					protected void confirmButtonClick(ConfirmDialog dialog) {
						presenter.removeRecord(itemId + "", ListLabelViewImpl.this.currentSchema);
					}

					@Override
					public boolean isVisible() {
						RecordVO ret = presenter.getRecordsWithIndex(ListLabelViewImpl.this.currentSchema, itemId + "");
						return !(super.isVisible() && ret != null) || ret.get(Printable.ISDELETABLE).equals(true);
					}
				};
			}
		});

		folderContainer = buttonsContainerForFolder;
		conteneurContainer = buttonsContainerForContainer;

		final Table tableFolder = new RecordVOTable($("ListSchemaTypeView.tableTitle"), folderContainer);
		tableFolder.setSizeFull();
		tableFolder.setPageLength(Math.min(15, folderContainer.size()));
		tableFolder.setColumnHeader("buttons", "");
		tableFolder.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
		tableFolder.setColumnExpandRatio("caption", 1);
		tableFolder.addStyleName(TYPE_TABLE);
		setDefaultOrderBy(Schemas.TITLE.getLocalCode(), presenter.getLabelFolderDataProvider(), tableFolder);

		Table tableContainer = new RecordVOTable($("ListSchemaTypeView.tableTitle"), conteneurContainer);
		tableContainer.setSizeFull();
		tableContainer.setPageLength(Math.min(15, conteneurContainer.size()));
		tableContainer.setColumnHeader("buttons", "");
		tableContainer.setColumnHeader("caption", $("ListSchemaTypeView.caption"));
		tableContainer.setColumnExpandRatio("caption", 1);
		tableContainer.addStyleName(TYPE_TABLE);
		setDefaultOrderBy(Schemas.TITLE.getLocalCode(), presenter.getLabelContainerDataProvider(), tableContainer);

		tabSheet = new TabSheet();
		tabSheet.addTab(tableFolder, $("DisplayLabelViewImpl.tabs.folder"));
		tabSheet.addTab(tableContainer, $("DisplayLabelViewImpl.tabs.container"));
		System.out.println(tabSheet.getTabIndex());
		tabSheet.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				getXMLButton.setCurrentSchema(tabSheet.getSelectedTab().equals(tableFolder) ? Folder.SCHEMA_TYPE : ContainerRecord.SCHEMA_TYPE);
				ListLabelViewImpl.this.currentSchema = getXMLButton.getCurrentSchema();
				System.out.println(ListLabelViewImpl.this.currentSchema);
			}
		});


		mainLayout.addComponent(tabSheet);
		return mainLayout;
	}

	private void setDefaultOrderBy(String localCode, RecordVODataProvider dataProvider, Table table) {
		Object[] properties = {dataProvider.getSchema().getMetadata(localCode)};
		boolean[] ordering = {true};
		table.sort(properties, ordering);
	}

	@Override
	protected List<Button> buildActionMenuButtons(ViewChangeListener.ViewChangeEvent event) {
		List<Button> actionMenuButtons = new ArrayList<Button>();

		addLabelButton = new AddButton($("DisplayLabelViewImpl.menu.addLabelButton")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				presenter.addLabelButtonClicked();
			}
		};

		downloadTemplateButton = new Button($("DisplayLabelViewImpl.menu.getTemplate"));
		StreamResource zip = createResource();
		FileDownloader fileDownloader = new FileDownloader(zip);
		fileDownloader.extend(downloadTemplateButton);


		actionMenuButtons.add(addLabelButton);
		actionMenuButtons.add(getXMLButton);
		actionMenuButtons.add(downloadTemplateButton);
		return actionMenuButtons;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {

	}

	@Override
	public void setLabels(List<LabelVO> list) {
		this.listLabel = list;
	}

	@Override
	public void addLabels(LabelVO... items) {
		this.listLabel.addAll(Arrays.asList(items));
	}

	@Override
	protected String getTitle() {
		return $("LabelViewImpl.title");
	}

	private StreamResource createResource() {
		return new StreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				InputStream stream = null;
				try {
					File extensionFile = getConstellioFactories().getAppLayerFactory().getExtensions().forCollection(getCollection()).changeDownloadableTemplate();
					File file = extensionFile != null ? extensionFile : new File(new FoldersLocator().getModuleResourcesFolder("rm"), "Template_Etiquette.zip");
					stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
				return stream;
			}
		}, "templates.zip");
	}
}
