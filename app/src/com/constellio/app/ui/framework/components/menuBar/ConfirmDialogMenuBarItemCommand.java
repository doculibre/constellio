package com.constellio.app.ui.framework.components.menuBar;

import static com.constellio.app.ui.i18n.i18n.$;

import org.vaadin.dialogs.ConfirmDialog;

import com.constellio.app.ui.framework.buttons.ConfirmDialogButton;
import com.constellio.app.ui.framework.buttons.ConfirmDialogButton.DialogMode;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public abstract class ConfirmDialogMenuBarItemCommand implements Command {
	
	private DialogMode dialogMode;
	
	public ConfirmDialogMenuBarItemCommand() {
		this(DialogMode.TEXT);
	}
	
	public ConfirmDialogMenuBarItemCommand(DialogMode dialogMode) {
		this.dialogMode = dialogMode;
	}

	@Override
	public void menuSelected(MenuItem selectedItem) {
		ConfirmDialogButton.showDialog(
				dialogMode, 
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
