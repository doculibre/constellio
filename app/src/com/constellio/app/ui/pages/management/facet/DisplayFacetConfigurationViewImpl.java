package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.MetadataDisplayFactory;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.constellio.model.entities.records.wrappers.Facet;
import com.constellio.model.entities.structures.MapStringStringStructure;
import com.vaadin.data.Item;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class DisplayFacetConfigurationViewImpl extends BaseViewImpl implements DisplayFacetConfigurationView {
	private DisplayFacetConfigurationPresenter presenter;

	public DisplayFacetConfigurationViewImpl() {
		presenter = new DisplayFacetConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("DisplayFacetConfigurationView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		RecordDisplay display = new RecordDisplay(presenter.getDisplayRecordVO(), new FacetMetadataDisplayFactory());

		mainLayout.addComponent(display);
		mainLayout.addComponent(createValuesTable());
		return mainLayout;
	}

	@Override
	protected ClickListener getBackButtonClickListener() {
		return new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.backButtonClicked();
			}
		};
	}

	@Override
	public List<Button> buildActionMenuButtons(ViewChangeEvent event) {
		List<Button> result = super.buildActionMenuButtons(event);

		Button edit = new Button($("DisplayFacetConfiguration.edit"));
		edit.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.editButtonClicked();
			}
		});

		Button delete = new Button($("DisplayFacetConfiguration.delete"));
		delete.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.deleteButtonClicked();
			}
		});

		Button activate = new Button($("DisplayFacetConfiguration.activate"));
		activate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.activate();
			}
		});
		activate.setVisible(!presenter.isActive());

		Button deactivate = new Button($("DisplayFacetConfiguration.deactivate"));
		deactivate.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.deactivate();
			}
		});
		deactivate.setVisible(presenter.isActive());

		result.add(edit);
		result.add(delete);
		result.add(activate);
		result.add(deactivate);
		return result;
	}

	private Component createValuesTable() {
		Table table = new BaseTable(getClass().getName(), $("DisplayFacetConfiguration.tableTitle"));
		table.addContainerProperty("label", String.class, null);
		table.addContainerProperty("value", String.class, null);
		table.setColumnHeader("label", $("DisplayFacetConfiguration.values.label." + presenter.getTypePostfix()));
		table.setColumnHeader("value", $("DisplayFacetConfiguration.values.value." + presenter.getTypePostfix()));
		table.setWidth("100%");
		table.setPageLength(table.getItemIds().size());

		MapStringStringStructure values = presenter.getValues();
		if (values != null) {
			for (String key : values.keySet()) {
				Item row1 = table.addItem(key);
				row1.getItemProperty("value").setValue(key);
				row1.getItemProperty("label").setValue(values.get(key));
			}
		}

		return table;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		if (!event.getParameters().isEmpty()) {
			presenter.setDisplayRecordVO(event.getParameters());
		}
	}

	public class FacetMetadataDisplayFactory extends MetadataDisplayFactory {
		@Override
		public Component buildSingleValue(RecordVO recordVO, MetadataVO metadata, Object displayValue) {
			if (metadata.getCode().endsWith(Facet.ORDER)) {
				return null;
			} else {
				return super.buildSingleValue(recordVO, metadata, displayValue);
			}
		}
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getSearchConfigurationBreadCrumbTrail(this, getTitle());
	}
}
