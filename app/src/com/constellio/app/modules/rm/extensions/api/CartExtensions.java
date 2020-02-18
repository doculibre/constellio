package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class CartExtensions {
	public ExtensionBooleanResult isRenameActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPrepareEmailActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isBatchDuplicateActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDocumentBatchProcessingActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isFolderBatchProcessingActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isContainerRecordBatchProcessingActionPossible(
			CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isFoldersLabelsActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDocumentLabelsActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isContainersLabelsActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isBatchDeleteActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isShareActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isDecommissionActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isPrintMetadataReportActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isCreateSIPArchvesActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isConsolidatedPdfActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isEmptyActionPossible(CartExtensionActionPossibleParams params) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public ExtensionBooleanResult isRecordBorrowActionPossible(
			CartExtensionActionPossibleParams cartExtensionActionPossibleParams) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	public static class CartExtensionActionPossibleParams {
		private Cart cart;
		private User user;

		public CartExtensionActionPossibleParams(Cart cart, User user) {
			this.cart = cart;
			this.user = user;
		}

		public Record getRecord() {
			return cart.getWrappedRecord();
		}

		public Cart getCart() {
			return cart;
		}

		public User getUser() {
			return user;
		}
	}
}
