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

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class TitlePanel extends Panel {
	//public static final String STYLE_NAME = "degradSection";
	public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/retour.gif");

	public TitlePanel(String title){
		this(title, false);
	}

	public TitlePanel(String title, boolean withBackButton){
		VerticalLayout vLayout = new VerticalLayout();
		HorizontalLayout titleWithReturn = new HorizontalLayout();
		titleWithReturn.setSizeFull();
		Label titleLabel = new Label(title);
		titleLabel.setStyleName(ValoTheme.LABEL_H1);
		titleWithReturn.addComponent(titleLabel);
		titleWithReturn.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);
		if (withBackButton){
			BaseButton buckButton = new IconButton(ICON_RESOURCE, $("back"), false) {
				@Override
				protected void buttonClick(ClickEvent event) {
					buckButtonClick();
				}
			};
			//buckButton.addStyleName(ValoTheme.BUTTON_HUGE);
			titleWithReturn.addComponent(buckButton);
			titleWithReturn.setComponentAlignment(buckButton, Alignment.MIDDLE_RIGHT);
		}
		vLayout.addComponent(titleWithReturn);
		Label separator = new Label("&nbsp;", ContentMode.HTML);
		//separator.addStyleName(STYLE_NAME);
		vLayout.addComponent(separator);
		setContent(vLayout);
		addStyleName(ValoTheme.PANEL_BORDERLESS);
	}

	protected void buckButtonClick() {
	}

}
