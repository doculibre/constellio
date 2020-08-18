package com.constellio.app.ui.framework.components.dialogs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Builder
public class ConfirmDialogProperties {
	public static final Consumer<ConfirmDialogResults> ON_CLOSE_LISTENER_DEFAULT = confirmDialogResult -> {
	};

	@Getter
	@Setter
	private String title;

	@Getter
	@Setter
	private String message;

	@Getter
	@Setter
	private String okCaption;

	@Getter
	@Setter
	private String cancelCaption;

	@Getter
	@Setter
	private String notOkCaption;

	@Getter
	private Consumer<ConfirmDialogResults> onCloseListener = ON_CLOSE_LISTENER_DEFAULT;

	@Getter
	@Setter
	private boolean isModal = true;

	public void setOnCloseListener(
			Consumer<ConfirmDialogResults> onCloseListener) {

		this.onCloseListener = onCloseListener;

		if (this.onCloseListener == null) {
			this.onCloseListener = ON_CLOSE_LISTENER_DEFAULT;
		}
	}
}
