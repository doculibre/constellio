package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.List;

public abstract class DecommissioningListPresenterExtension {

	public abstract void validateProcessable(ValidateDecommissioningListProcessableParams params);

	public abstract void importExternalLinks(ImportExternalLinksParams params);

	public static class ImportExternalLinksParams {
		List<String> foldersIds;
		String username;

		public ImportExternalLinksParams(List<String> foldersIds, String username, AppLayerFactory appLayerFactory,
										 String collection) {
			this.foldersIds = foldersIds;
			this.username = username;
		}

		public List<String> getFoldersIds() {
			return foldersIds;
		}

		public String getUsername() {
			return username;
		}
	}

	public static class ValidateDecommissioningListProcessableParams {

		private DecommissioningList decommissioningList;
		private ValidationErrors validationErrors = new ValidationErrors();

		public ValidateDecommissioningListProcessableParams(DecommissioningList decommissioningList) {
			this.decommissioningList = decommissioningList;
		}

		public DecommissioningList getDecommissioningList() {
			return decommissioningList;
		}

		public ValidationErrors getValidationErrors() {
			return validationErrors;
		}

	}
}
