package com.constellio.app.modules.rm.services.actions;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.utils.CartUtil;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CartActionsServices {
	private RMSchemasRecordsServices rm;
	private RMModuleExtensions rmModuleExtensions;
	private ModelLayerFactory modelLayerFactory;
	private CartUtil cartUtil;

	public CartActionsServices(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		this.cartUtil = new CartUtil(collection, appLayerFactory);
	}

	public boolean isRenameActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return user.has(RMPermissionsTo.USE_GROUP_CART).globally()
			   && rmModuleExtensions.isRenameActionPossibleOnCart(cart, user)
			   && canRenameFavoriteGroup(cart, user);
	}

	public boolean isPrepareEmailActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return cartUtil.cartHasRecords(cart.getId()) && cartUtil.cartContainerIsEmpty(cart.getId())
			   && hasCartPermission(cart.getId(), user)
			   && rmModuleExtensions.isPrepareEmailActionPossibleOnCart(cart, user);
	}

	public boolean isBatchDuplicateActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return cartHasOnlyFolders(cart.getId()) && canDuplicateFolders(user, cart.getId())
			   && hasCartPermission(cart.getId(), user)
			   && rmModuleExtensions.isBatchDuplicateActionPossibleOnCart(cart, user);
	}

	private boolean cartHasOnlyFolders(String cartId) {
		return !cartUtil.cartFoldersIsEmpty(cartId)
			   && cartUtil.cartDocumentsIsEmpty(cartId)
			   && cartUtil.cartContainerIsEmpty(cartId);
	}

	private boolean canDuplicateFolders(User user, String cartId) {
		for (Folder folder : cartUtil.getCartFolders(cartId)) {
			RecordWrapper parent = folder.getParentFolder() != null ?
								   rm.getFolder(folder.getParentFolder()) :
								   rm.getAdministrativeUnit(folder.getAdministrativeUnitEntered());
			if (!user.hasWriteAccess().on(parent)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DUPLICATE_SEMIACTIVE_FOLDER).on(folder)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DUPLICATE_INACTIVE_FOLDER).on(folder)) {
						return false;
					}
					break;
			}
		}
		return true;
	}

	public boolean isDocumentBatchProcessingActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = Document.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return isBatchProcessingButtonVisible(schemaTypeCode, user, cart)
				   && hasCartPermission(cart.getId(), user)
				   && rmModuleExtensions.isDocumentBatchProcessingActionPossibleOnCart(cart, user);
		} else {
			return false;
		}	
	}

	public boolean isFolderBatchProcessingActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = Folder.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return isBatchProcessingButtonVisible(schemaTypeCode, user, cart)
				   && hasCartPermission(cart.getId(), user)
				   && rmModuleExtensions.isFolderBatchProcessingActionPossibleOnCart(cart, user);
		} else {
			return false;
		}
	}

	public boolean isContainerBatchProcessingActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = ContainerRecord.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return isBatchProcessingButtonVisible(schemaTypeCode, user, cart)
				   && hasCartPermission(cart.getId(), user)
				   && rmModuleExtensions.isContainerRecordBatchProcessingActionPossibleOnCart(cart, user);
		} else {
			return false;
		}	
	}

	public boolean isFoldersLabelsActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = Folder.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return hasCartPermission(cart.getId(), user)
				   && isLabelsButtonVisible(schemaTypeCode, cart.getId())
				   && rmModuleExtensions.isFoldersLabelsActionPossibleOnCart(cart, user);
		} else {
			return false;
		}	
	}

	public boolean isDocumentLabelsActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = Document.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return hasCartPermission(cart.getId(), user)
				   && isLabelsButtonVisible(schemaTypeCode, cart.getId())
				   && rmModuleExtensions.isDocumentLabelsActionPossibleOnCart(cart, user);
		} else {
			return false;
		}
	}

	@NotNull
	private boolean areSchemaTypeRecordPresent(Record record, String schemaType, User user) {
		List<? extends RecordWrapper> recordWithPossibleDeleted;

		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
			recordWithPossibleDeleted = cartUtil.getCartContainers(record.getId());
		} else if (Folder.SCHEMA_TYPE.equals(schemaType)) {
			recordWithPossibleDeleted = cartUtil.getCartFolders(record.getId());
		} else if (Document.SCHEMA_TYPE.equals(schemaType)) {
			recordWithPossibleDeleted = cartUtil.getCartDocuments(record.getId());
		} else {
			throw new IllegalArgumentException("SchemaType not supported : " + schemaType);
		}

		return getNonDeletedRecordsIds(recordWithPossibleDeleted, user).size() > 0;
	}

	public boolean isContainersLabelsActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = ContainerRecord.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return hasCartPermission(cart.getId(), user)
				   && isLabelsButtonVisible(schemaTypeCode, cart.getId())
				   && rmModuleExtensions.isContainerLabelsActionPossibleOnCart(cart, user);
		} else {
			return false;
		}	
	}

	public boolean isLabelsButtonVisible(String schemaType, String cartId) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				return !cartUtil.cartFoldersIsEmpty(cartId);
			case ContainerRecord.SCHEMA_TYPE:
				return !cartUtil.cartContainerIsEmpty(cartId);
			case Document.SCHEMA_TYPE:
				return !cartUtil.cartDocumentsIsEmpty(cartId);

			default:
				throw new RuntimeException("No labels for type : " + schemaType);
		}
	}

	public boolean isBatchDeleteActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && canDeleteContainers(user, cart.getId())
			   && canDeleteFolders(user, cart.getId())
			   && canDeleteDocuments(user, cart.getId())
			   && hasCartBatchDeletePermission(user)
			   && rmModuleExtensions.isBatchDeleteActionPossibleOnCart(cart, user);
	}

	private boolean canDeleteFolders(User user, String cartId) {
		for (Folder folder : cartUtil.getCartFolders(cartId)) {
			if (!user.hasDeleteAccess().on(folder)) {
				return false;
			}
			switch (folder.getPermissionStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_FOLDERS).on(folder)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DELETE_INACTIVE_FOLDERS).on(folder)) {
						return false;
					}
					break;
			}
		}
		return true;
	}

	private boolean canDeleteContainers(User user, String cartId) {
		for (ContainerRecord container : cartUtil.getCartContainers(cartId)) {
			if (!user.has(RMPermissionsTo.DELETE_CONTAINERS).on(container)) {
				return false;
			}
		}
		return true;
	}

	private boolean canDeleteDocuments(User user, String cartId) {
		for (Document document : cartUtil.getCartDocuments(cartId)) {
			if (!user.hasDeleteAccess().on(document)) {
				return false;
			}
			switch (document.getArchivisticStatus()) {
				case SEMI_ACTIVE:
					if (!user.has(RMPermissionsTo.DELETE_SEMIACTIVE_DOCUMENT).on(document)) {
						return false;
					}
					break;
				case INACTIVE_DEPOSITED:
				case INACTIVE_DESTROYED:
					if (!user.has(RMPermissionsTo.DELETE_INACTIVE_DOCUMENT).on(document)) {
						return false;
					}
			}
			if (document.isPublished() && !user.has(RMPermissionsTo.DELETE_PUBLISHED_DOCUMENT).on(document)) {
				return false;
			}
			if (getCurrentBorrowerOf(document) != null && !user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT)
					.on(document)) {
				return false;
			}
		}
		return true;
	}

	private String getCurrentBorrowerOf(Document document) {
		return document.getContent() == null ? null : document.getContent().getCheckoutUserId();
	}

	public boolean hasCartBatchDeletePermission(User user) {
		return user.has(RMPermissionsTo.CART_BATCH_DELETE).globally();
	}

	public boolean isEmptyActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && rmModuleExtensions.isEmptyActionPossibleOnCart(cart, user);
	}

	public boolean isShareActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return !isDefaultCart(cart.getId(), user)
			   && hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && rmModuleExtensions.isShareActionPossibleOnCart(cart, user);
	}

	public boolean isDecommissionActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && canCurrentUserBuildDecommissioningList(user)
			   && !cartUtil.cartFoldersIsEmpty(cart.getId())
			   && rmModuleExtensions.isDecommissionActionPossibleOnCart(cart, user);
	}

	public boolean canCurrentUserBuildDecommissioningList(User user) {
		return user.has(RMPermissionsTo.CREATE_DECOMMISSIONING_LIST).onSomething() ||
			   user.has(RMPermissionsTo.CREATE_TRANSFER_DECOMMISSIONING_LIST).onSomething();
	}

	public boolean isPrintMetadataReportActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && rmModuleExtensions.isPrintMetadataReportActionPossibleOnCart(cart, user);
	}

	public boolean isCreateSIPArchvesActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && rmModuleExtensions.isCreateSIPArchvesActionPossibleOnCart(cart, user);
	}

	public boolean isPrntConsolidatedPdfActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);

		return hasCartPermission(cart.getId(), user)
			   && cartUtil.cartHasRecords(cart.getId())
			   && rmModuleExtensions.isConsolidatedPdfActionPossibleOnCart(cart, user);
	}

	private boolean isBatchProcessingButtonVisible(String schemaType, User user, Cart cart) {
		if (ContainerRecord.SCHEMA_TYPE.equals(schemaType) && !user.has(RMPermissionsTo.MANAGE_CONTAINERS)
				.onSomething()) {
			return false;
		}

		if (!user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).onSomething()) {
			return false;
		}

		return getNotDeletedRecordsIds(schemaType, user, cart.getId()).size() != 0;
	}

	public List<String> getNotDeletedRecordsIds(String schemaType, User user, String cartId) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				List<String> folders = cartUtil.getCartFolderIds(cartId);
				return getNonDeletedRecordsIds(rm.getFolders(folders), user);
			case Document.SCHEMA_TYPE:
				List<String> documents = cartUtil.getCartDocumentIds(cartId);
				return getNonDeletedRecordsIds(rm.getDocuments(documents), user);
			case ContainerRecord.SCHEMA_TYPE:
				List<String> containers = cartUtil.getCartContainersIds(cartId);
				return getNonDeletedRecordsIds(rm.getContainerRecords(containers), user);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}


	private List<String> getNonDeletedRecordsIds(List<? extends RecordWrapper> records, User currentUser) {
		ArrayList<String> ids = new ArrayList<>();
		for (RecordWrapper record : records) {
			if (!record.isLogicallyDeletedStatus() && currentUser.hasReadAccess().on(record)) {
				ids.add(record.getId());
			}
		}
		return ids;
	}

	public boolean hasCartPermission(String cartId, User user) {
		if (cartId.equals(user.getId())) {
			return user.has(RMPermissionsTo.USE_MY_CART).globally();
		} else {
			return user.has(RMPermissionsTo.USE_GROUP_CART).globally();
		}
	}


	public boolean canRenameFavoriteGroup(Cart cart, User user) {
		if (isDefaultCart(cart.getId(), user)) {
			return false;
		}

		List<String> sharedWithUsers = cart.getSharedWithUsers();
		return (sharedWithUsers == null || sharedWithUsers.size() <= 0);
	}


	private boolean isDefaultCart(String cartId, User user) {
		return user.getId().equals(cartId);
	}

	public boolean isFolderBorrowActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = Folder.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return hasCartPermission(cart.getId(), user)
				   && isBorrowButtonVisible(schemaTypeCode, cart.getId())
				   && rmModuleExtensions.isFolderBorrowActionPossibleOnCart(cart, user);
		} else {
			return false;
		}
	}

	public boolean isContainersBorrowActionPossible(Record record, User user) {
		Cart cart = rm.wrapCart(record);
		String schemaTypeCode = ContainerRecord.SCHEMA_TYPE;
		if (areSchemaTypeRecordPresent(record, schemaTypeCode, user)) {
			return hasCartPermission(cart.getId(), user)
				   && isBorrowButtonVisible(schemaTypeCode, cart.getId())
				   && rmModuleExtensions.isContainerBorrowActionPossibleOnCart(cart, user);
		} else {
			return false;
		}
	}

	public boolean isBorrowButtonVisible(String schemaType, String cartId) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				return !cartUtil.cartFoldersIsEmpty(cartId);
			case ContainerRecord.SCHEMA_TYPE:
				return !cartUtil.cartContainerIsEmpty(cartId);

			default:
				throw new RuntimeException("Cannot borrow type : " + schemaType);
		}
	}
}
