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
package com.constellio.app.modules.rm.ui.components.userDocuments;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.containers.ButtonsContainer.ContainerButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class DeclareUserDocumentContainerButton extends ContainerButton {

	@Override
	protected Button newButtonInstance(final Object itemId) {
		Button declareUserDocumentButton = new BaseButton($("ListUserDocumentsView.declareDocument")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				UserDocumentVO userDocumentVO = (UserDocumentVO) itemId;
				String userDocumentId = userDocumentVO.getId();
				ConstellioUI.getCurrent().navigateTo().declareUserDocument(userDocumentId);
				for (Window window : ConstellioUI.getCurrent().getWindows()) {
					window.close();
				}
			}
		};
		declareUserDocumentButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		return declareUserDocumentButton;
	}

}
