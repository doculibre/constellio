package com.constellio.app.ui.framework.buttons.report;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.vaadin.tepi.listbuilder.ListBuilder;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.FormMetadataVO;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ReportConfigurationPanel extends Panel {
	protected ReportConfigurationPanelPresenter presenter;
	private String collection;

	public ReportConfigurationPanel(String caption, ReportConfigurationPresenter presenter, String collection) {
		super(caption);
		this.presenter = new ReportConfigurationPanelPresenter(presenter);
		this.collection = collection;
		init();
	}

	private void init() {
		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.addComponents(buildTables());
	}

	private Component buildTables() {
		List<FormMetadataVO> metadataVOs = presenter.getMetadata();//collection
		List<FormMetadataVO> valueMetadataVOs = presenter.getValueMetadatas(collection);

		final ListBuilder select = new ListBuilder();
		select.setColumns(30);
		select.setRightColumnCaption($("SearchDisplayConfigView.rightColumn"));
		select.setLeftColumnCaption($("SearchDisplayConfigView.leftColumn"));

		for (FormMetadataVO form : metadataVOs) {
			select.addItem(form);
			select.setItemCaption(form, form.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage()));
		}

		select.setValue(valueMetadataVOs);

		Button saveButton = new Button($("save"));
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				List<FormMetadataVO> values = (List) select.getValue();
				presenter.saveButtonClicked(values);
			}
		});

		Button cancelButton = new Button($("cancel"));
		cancelButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		});

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponent(saveButton);
		buttonsLayout.addComponent(cancelButton);

		VerticalLayout viewLayout = new VerticalLayout();
		viewLayout.setSizeFull();
		viewLayout.setSpacing(true);
		viewLayout.addComponent(select);
		viewLayout.addComponent(buttonsLayout);

		return viewLayout;
	}
}
