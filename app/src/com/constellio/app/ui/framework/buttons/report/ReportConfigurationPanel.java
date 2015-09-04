/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.ui.entities.FormMetadataVO;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.tepi.listbuilder.ListBuilder;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ReportConfigurationPanel extends Panel{
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
            select.setItemCaption(form, form.getLabel());
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
