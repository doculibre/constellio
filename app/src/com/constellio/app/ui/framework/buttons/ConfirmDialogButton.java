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
package com.constellio.app.ui.framework.buttons;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.server.Resource;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public abstract class ConfirmDialogButton extends IconButton {

	private static ConfirmDialog.Factory factory = new DefaultConfirmDialogFactory();

	static {
		ConfirmDialog.setFactory(new ConfirmDialog.Factory() {
			@Override
			public ConfirmDialog create(String windowCaption, String message, String okTitle, String cancelTitle,
					String notOKCaption) {
				ConfirmDialog confirmDialog = factory.create(windowCaption, message, okTitle, cancelTitle, notOKCaption);
				confirmDialog.addAttachListener(new AttachListener() {
					@Override
					public void attach(AttachEvent event) {
						BaseWindow.executeZIndexAdjustJavascript(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX + 1);
					}
				});
				return confirmDialog;
			}
		});
	}

	public ConfirmDialogButton(String caption) {
		this(null, caption, false);
	}

	public ConfirmDialogButton(Resource iconResource, String caption) {
		super(iconResource, caption);
	}

	public ConfirmDialogButton(Resource iconResource, String caption, boolean iconOnly) {
		super(iconResource, caption, iconOnly);
	}

	@Override
	protected final void buttonClick(ClickEvent event) {
		ConfirmDialog.show(
				UI.getCurrent(),
				getConfirmDialogTitle(),
				getConfirmDialogMessage(),
				getConfirmDialogOKCaption(),
				getConfirmDialogCancelCaption(),
				new ConfirmDialog.Listener() {
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							confirmButtonClick(dialog);
						} else {
							dialogClosedWitoutConfirm(dialog);
						}
					}
				});
	}

	protected String getConfirmDialogTitle() {
		return $("ConfirmDialog.title");
	}

	protected String getConfirmDialogOKCaption() {
		return $("ConfirmDialog.yes");
	}

	protected String getConfirmDialogCancelCaption() {
		return $("ConfirmDialog.no");
	}

	protected void dialogClosedWitoutConfirm(ConfirmDialog dialog) {
	}

	protected abstract String getConfirmDialogMessage();

	protected abstract void confirmButtonClick(ConfirmDialog dialog);

}
