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
package com.constellio.app.modules.rm.ui.pages.containers;

import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.framework.components.BaseDisplay;
import com.constellio.app.ui.framework.components.RecordDisplay;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContainerRatioPanel extends Panel {
    public ContainerRatioPanel(Double ratio) {
        this(ratio.toString());
    }

    public ContainerRatioPanel(String ratioAsStringOrMessage) {
        addStyleName(ValoTheme.PANEL_BORDERLESS);
        addStyleName(BaseDisplay.STYLE_NAME);
        addStyleName(RecordDisplay.STYLE_NAME);
        HorizontalLayout layout = new HorizontalLayout();
        Label ratioCaption = new Label($("ContainerRatioPanel.containerRatio"));
        ratioCaption.addStyleName(RecordDisplay.STYLE_CAPTION);
        ratioCaption.setSizeFull();
        layout.addComponent(ratioCaption);
        Label ratio = new Label(ratioAsStringOrMessage);
        ratio.setSizeFull();
        ratio.addStyleName(RecordDisplay.STYLE_VALUE);
        layout.addComponent(ratio);
        setContent(layout);
    }
}
