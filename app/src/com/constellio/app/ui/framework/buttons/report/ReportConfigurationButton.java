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

import com.constellio.app.ui.framework.buttons.LabelsButton.RecordSelector;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class ReportConfigurationButton extends WindowButton {
    private final RecordSelector selector;
    private final String caption;
    private final String collection;
    protected ReportConfigurationPresenter presenter;

    public ReportConfigurationButton(String caption, String windowCaption, RecordSelector recordSelector, ReportConfigurationPresenter presenter, String collection) {
        super(caption, windowCaption, WindowConfiguration.modalDialog("75%", "75%"));
        this.caption = caption;
        this.selector = recordSelector;
        this.presenter = presenter;
        this.collection = collection;
    }

    @Override
    protected Component buildWindowContent() {
        Panel reportConfigPanel = new ReportConfigurationPanel(caption, this.presenter, collection);
        return reportConfigPanel;
    }

}