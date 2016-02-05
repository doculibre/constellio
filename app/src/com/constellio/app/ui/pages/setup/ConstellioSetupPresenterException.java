package com.constellio.app.ui.pages.setup;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConstellioSetupPresenterException extends Exception {

	public ConstellioSetupPresenterException(String message) {
		super(message);
	}

	public static class ConstellioSetupPresenterException_MustSelectAtLeastOneModule
			extends ConstellioSetupPresenterException {
		public ConstellioSetupPresenterException_MustSelectAtLeastOneModule() {
			super($("ConstellioSetupPresenter.mustSelectAtLeastOneModule"));
		}
	}

	public static class ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule
			extends ConstellioSetupPresenterException {
		public ConstellioSetupPresenterException_TasksCannotBeTheOnlySelectedModule() {
			super($("ConstellioSetupPresenter.tasksCannotBeTheOnlySelectedModule"));
		}
	}

	public static class ConstellioSetupPresenterException_CannotLoadSaveState
			extends ConstellioSetupPresenterException {
		public ConstellioSetupPresenterException_CannotLoadSaveState() {
			super($("ConstellioSetupPresenter.cannotLoadSaveState"));
		}
	}

}
