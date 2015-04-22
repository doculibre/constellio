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
package com.constellio.app.ui.framework.components;

import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.pages.events.EventCategory;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ValoTheme;

public abstract class EventReportGenerationPanel extends Panel {
	private EventCategory eventCategory;

	public EventReportGenerationPanel(EventCategory eventCategory) {
		this.eventCategory = eventCategory;
		Layout hLayout = new HorizontalLayout();
		hLayout.setSizeFull();

		NativeSelect selectReport = new NativeSelect();
		selectReport.addItem(getReportTitle(eventCategory));
		selectReport.setNullSelectionAllowed(false);

		hLayout.addComponent(selectReport);
		Label separator1 = new Label("&nbsp;", ContentMode.HTML);
		hLayout.addComponent(separator1);

		Button viewReportButton = new IconButton(new ThemeResource("images/commun/loupe_carnet.gif"), "Generate report") {
			@Override
			protected void buttonClick(ClickEvent event) {
				//TODO
			}
		};
		hLayout.addComponent(viewReportButton);
		Label separator2 = new Label("&nbsp;", ContentMode.HTML);
		hLayout.addComponent(separator2);

		Button generateReportButton = new IconButton(new ThemeResource("images/commun/disket.gif"), "Save report") {
			@Override
			protected void buttonClick(ClickEvent event) {
				//TODO
			}
		};
		hLayout.addComponent(generateReportButton);

		setContent(hLayout);
		addStyleName(ValoTheme.PANEL_BORDERLESS);
	}

	protected abstract String getReportTitle(EventCategory eventCategory);
}
