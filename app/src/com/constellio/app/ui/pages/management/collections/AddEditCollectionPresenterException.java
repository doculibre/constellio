package com.constellio.app.ui.pages.management.collections;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditCollectionPresenterException extends Exception {

	public AddEditCollectionPresenterException(String message) {
		super(message);
	}

	public static class AddEditCollectionPresenterException_MustSelectAtLeastOneModule
			extends AddEditCollectionPresenterException {
		public AddEditCollectionPresenterException_MustSelectAtLeastOneModule() {
			super($("AddEditCollectionPresenter.mustSelectAtLeastOneModule"));
		}
	}

	public static class AddEditCollectionPresenterException_CodeShouldNotContainDash
			extends AddEditCollectionPresenterException {
		public AddEditCollectionPresenterException_CodeShouldNotContainDash() {
			super($("AddEditCollectionPresenter.invalidCode"));
		}
	}

	public static class AddEditCollectionPresenterException_CodeUnAvailable
			extends AddEditCollectionPresenterException {
		public AddEditCollectionPresenterException_CodeUnAvailable() {
			super($("AddEditCollectionPresenter.codeNonAvailable"));
		}
	}

	public static class AddEditCollectionPresenterException_CodeCodeChangeForbidden
			extends AddEditCollectionPresenterException {
		public AddEditCollectionPresenterException_CodeCodeChangeForbidden() {
			super($("AddEditCollectionPresenter.codeChangeForbidden"));
		}
	}

}
