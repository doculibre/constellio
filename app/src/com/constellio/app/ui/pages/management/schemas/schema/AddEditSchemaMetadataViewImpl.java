package com.constellio.app.ui.pages.management.schemas.schema;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.Map;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.AddButton;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.app.ui.framework.buttons.EditButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.framework.containers.ButtonsContainer;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.constellio.app.ui.framework.containers.MetadataVOLazyContainer;
import com.constellio.app.ui.framework.data.MetadataVODataProvider;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.params.ParamUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class AddEditSchemaMetadataViewImpl extends BaseViewImpl implements AddEditSchemaMetadataView, ClickListener {

	AddEditSchemaMetadataPresenter presenter;
	private final int batchSize = 100;

	public AddEditSchemaMetadataViewImpl() {
		this.presenter = new AddEditSchemaMetadataPresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		Map<String, String> params = ParamUtils.getParamsMap(event.getParameters());
		presenter.setSchemaCode(params.get("schemaCode"));
		presenter.setParameters(params);
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	@Override
	protected String getTitle() {
		return $("AddEditSchemaMetadataView.viewTitle",
				presenter.getSchemaVO().getLabel(getSessionContext().getCurrentLocale().getLanguage()));
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

		Button addButton = new AddButton() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addButtonClicked();
			}
		};

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(addButton, buildTables());
		viewLayout.setComponentAlignment(addButton, Alignment.TOP_RIGHT);
		return viewLayout;
	}

	private Component buildTables() {
		TabSheet tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		
		Map<String, MetadataVODataProvider> dataProviders = presenter.getDataProviders();
		for (String tabCaption : dataProviders.keySet()) {
			final MetadataVODataProvider dataProvider = dataProviders.get(tabCaption);

			MetadataVOLazyContainer recordsContainer = new MetadataVOLazyContainer(dataProvider, batchSize);
			ButtonsContainer<MetadataVOLazyContainer> buttonsContainer = new ButtonsContainer<>(recordsContainer, "buttons");
			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new EditButton() {
						@Override
						protected void buttonClick(ClickEvent event) {
							Integer index = (Integer) itemId;
							MetadataVO entity = dataProvider.getMetadataVO(index);
							presenter.editButtonClicked(entity);
						}
					};
				}
			});

			buttonsContainer.addButton(new ContainerButton() {
				@Override
				protected Button newButtonInstance(final Object itemId, ButtonsContainer<?> container) {
					return new DeleteButton() {
						@Override
						protected void confirmButtonClick(ConfirmDialog dialog) {
							Integer index = (Integer) itemId;
							MetadataVO entity = dataProvider.getMetadataVO(index);
							presenter.deleteButtonClicked(entity);
						}
					};
				}
			});

			Table table = new BaseTable(getClass().getName(), $("AddEditSchemaMetadataView.tableTitle", recordsContainer.size()), buttonsContainer);
			table.setSizeFull();
			table.setPageLength(Math.min(12, dataProvider.size()));
			table.setColumnHeader("localCode", $("AddEditSchemaMetadataView.code"));
			table.setColumnHeader("caption", $("AddEditSchemaMetadataView.caption"));
			table.setColumnHeader("enabledCaption", $("AddEditSchemaMetadataView.enabledCaption"));
			table.setColumnHeader("valueCaption", $("AddEditSchemaMetadataView.valueCaption"));
			table.setColumnHeader("inputCaption", $("AddEditSchemaMetadataView.inputCaption"));
			table.setColumnHeader("requiredCaption", $("AddEditSchemaMetadataView.requiredCaption"));
			table.setColumnHeader("buttons", "");
			table.setColumnWidth("buttons", 80);
			
			tabSheet.addTab(table, tabCaption);
		}

		return tabSheet;
	}
}
