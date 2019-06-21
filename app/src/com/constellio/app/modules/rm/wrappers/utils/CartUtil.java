package com.constellio.app.modules.rm.wrappers.utils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CartUtil {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private RMSchemasRecordsServices rm;
	private SearchServices searchServices;

	public CartUtil(String collecton, AppLayerFactory appLayerFactory) {
		this.collection = collecton;
		this.appLayerFactory = appLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public String cartOwner(User user, Cart cart) {
		return isDefaultCart(user, cart.getId()) ? user.getId() : cart.getOwner();
	}

	public boolean isDefaultCart(User user, String cartId) {
		return user.getId().equals(cartId);
	}


	public List<Document> getCartDocuments(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartDocumentsLogicalSearchQuery(cartId);
		return rm.searchDocuments(logicalSearchQuery);
	}

	public List<Folder> getCartFolders(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery(cartId);
		return rm.searchFolders(logicalSearchQuery);
	}

	private List<ContainerRecord> getCartContainers(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartContainersLogicalSearchQuery(cartId);
		return rm.searchContainerRecords(logicalSearchQuery);
	}

	public boolean cartHasRecords(String cartId) {
		return !(cartFoldersIsEmpty(cartId) && cartDocumentsIsEmpty(cartId) && cartContainerIsEmpty(cartId));
	}

	public boolean cartFoldersIsEmpty(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery(cartId);
		return searchServices.getResultsCount(logicalSearchQuery) == 0;
	}

	public boolean cartDocumentsIsEmpty(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartDocumentsLogicalSearchQuery(cartId);
		return searchServices.getResultsCount(logicalSearchQuery) == 0;
	}

	public boolean cartContainerIsEmpty(String cartId) {
		LogicalSearchQuery logicalSearchQuery = getCartContainersLogicalSearchQuery(cartId);
		return searchServices.getResultsCount(logicalSearchQuery) == 0;
	}

	public List<String> getCartFolderIds(String cartId) {
		List<Folder> folders = getCartFolders(cartId);
		List<String> foldersIds = new ArrayList<>();
		for (Folder folder : folders) {
			foldersIds.add(folder.getId());
		}
		return foldersIds;
	}

	public List<String> getCartDocumentIds(String cartId) {
		List<Document> documents = getCartDocuments(cartId);
		List<String> documentsIds = new ArrayList<>();
		for (Document document : documents) {
			documentsIds.add(document.getId());
		}
		return documentsIds;
	}

	public List<String> getCartContainersIds(String cartId) {
		List<ContainerRecord> containers = getCartContainers(cartId);
		List<String> containersIds = new ArrayList<>();
		for (ContainerRecord container : containers) {
			containersIds.add(container.getId());
		}
		return containersIds;
	}

	@NotNull
	public LogicalSearchQuery getCartFoldersLogicalSearchQuery(String cartId) {
		return new LogicalSearchQuery(from(rm.folder.schemaType()).where(rm.folder.favorites()).isEqualTo(cartId));
	}

	@NotNull
	public LogicalSearchQuery getCartDocumentsLogicalSearchQuery(String cartId) {
		return new LogicalSearchQuery(from(rm.document.schemaType()).where(rm.document.favorites()).isEqualTo(cartId));
	}

	@NotNull
	public LogicalSearchQuery getCartContainersLogicalSearchQuery(String cartId) {
		return new LogicalSearchQuery(
				from(rm.containerRecord.schemaType()).where(rm.containerRecord.favorites()).isEqualTo(cartId));
	}


	public List<String> getNotDeletedRecordsIds(String schemaType, User currentUser, String cartId) {
		switch (schemaType) {
			case Folder.SCHEMA_TYPE:
				List<String> folders = getCartFolderIds(cartId);
				return getNonDeletedRecordsIds(rm.getFolders(folders), currentUser);
			case Document.SCHEMA_TYPE:
				List<String> documents = getCartDocumentIds(cartId);
				return getNonDeletedRecordsIds(rm.getDocuments(documents), currentUser);
			case ContainerRecord.SCHEMA_TYPE:
				List<String> containers = getCartContainersIds(cartId);
				return getNonDeletedRecordsIds(rm.getContainerRecords(containers), currentUser);
			default:
				throw new RuntimeException("Unsupported type : " + schemaType);
		}
	}

	public List<String> getNonDeletedRecordsIds(List<? extends RecordWrapper> records, User currentUser) {
		ArrayList<String> ids = new ArrayList<>();
		for (RecordWrapper record : records) {
			if (!record.isLogicallyDeletedStatus() && currentUser.hasReadAccess().on(record)) {
				ids.add(record.getId());
			}
		}
		return ids;
	}

	public List<Record> getCartRecords(String cartId) {
		List<Record> records = new ArrayList<>();
		LogicalSearchQuery logicalSearchQuery = getCartFoldersLogicalSearchQuery(cartId);
		SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		records.addAll(searchServices.search(logicalSearchQuery));
		logicalSearchQuery = getCartDocumentsLogicalSearchQuery(cartId);
		records.addAll((searchServices.search(logicalSearchQuery)));
		logicalSearchQuery = getCartContainersLogicalSearchQuery(cartId);
		records.addAll((searchServices.search(logicalSearchQuery)));
		return records;
	}
}
