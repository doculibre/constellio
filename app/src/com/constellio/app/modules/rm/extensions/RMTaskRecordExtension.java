package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;

public class RMTaskRecordExtension extends RecordExtension {

	private final RMSchemasRecordsServices rmSchema;
	private final RecordServices recordServices;
	private final AuthorizationsServices authorizationsServices;

	private final String collection;
	private final ModelLayerFactory modelLayerFactory;

	public RMTaskRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		rmSchema = new RMSchemasRecordsServices(collection, appLayerFactory);
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		authorizationsServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {
		if (!event.isSchemaType(RMTask.SCHEMA_TYPE)) {
			return;
		}

		RMTask task = rmSchema.wrapRMTask(event.getRecord());

		List<String> recordIds = new ArrayList<>();
		recordIds.addAll(task.getLinkedFolders());
		recordIds.addAll(task.getLinkedDocuments());
		createMissingAuthorizations(task, recordIds, getUser(task));
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		if (!event.isSchemaType(RMTask.SCHEMA_TYPE)) {
			return;
		}

		RMTask task = rmSchema.wrapRMTask(event.getRecord());

		if (event.hasModifiedMetadata(Task.ASSIGNEE)) {
			User previousUser = getUser((String) event.getPreviousValue(Task.ASSIGNEE));
			User currentUser = getUser(task.getAssignee());

			onAssigneeModificationEvent(task, previousUser, currentUser);
		}

		if (event.hasModifiedMetadata(Task.LINKED_DOCUMENTS)) {
			List<String> previousDocuments = event.getPreviousValue(Task.LINKED_DOCUMENTS);
			List<String> currentDocuments = task.getLinkedDocuments();

			List<String> addedDocuments = ListUtils.subtract(currentDocuments, previousDocuments);
			List<String> removedDocuments = ListUtils.subtract(previousDocuments, currentDocuments);

			onLinkedDocumentsModificationEvent(task, addedDocuments, removedDocuments);
		}

		if (event.hasModifiedMetadata(Task.LINKED_FOLDERS)) {
			List<String> previousFolders = event.getPreviousValue(Task.LINKED_FOLDERS);
			List<String> currentFolders = task.getLinkedFolders();

			List<String> addedFolders = ListUtils.subtract(currentFolders, previousFolders);
			List<String> removedFolders = ListUtils.subtract(previousFolders, currentFolders);

			onLinkedFoldersModificationEvent(task, addedFolders, removedFolders);
		}

		if (event.hasModifiedMetadata(Task.STATUS)) {
			if (task.getStatusType().isFinishedOrClosed()) {
				onTaskFinishedOrClosedEvent(task);
			}
		}
	}

	private void onAssigneeModificationEvent(RMTask task, User previousUser, User currentUser) {
		deleteAllCreatedAuthorizations(task);

		List<String> recordIds = new ArrayList<>();
		recordIds.addAll(task.getLinkedFolders());
		recordIds.addAll(task.getLinkedDocuments());
		createMissingAuthorizations(task, recordIds, currentUser);
	}

	private void onLinkedDocumentsModificationEvent(RMTask task, List<String> addedDocuments,
													List<String> removedDocuments) {
		if (!removedDocuments.isEmpty()) {
			deleteCreatedAuthorizations(task, removedDocuments);
		}
		if (!addedDocuments.isEmpty()) {
			createMissingAuthorizations(task, addedDocuments, getUser(task));
		}
	}

	private void onLinkedFoldersModificationEvent(RMTask task, List<String> addedFolders,
												  List<String> removedFolders) {
		if (!removedFolders.isEmpty()) {
			deleteCreatedAuthorizations(task, removedFolders);
		}
		if (!addedFolders.isEmpty()) {
			createMissingAuthorizations(task, addedFolders, getUser(task));
		}
	}

	private void onTaskFinishedOrClosedEvent(RMTask task) {
		deleteAllCreatedAuthorizations(task);
	}

	private User getUser(RMTask task) {
		return getUser(task.getAssignee());
	}

	private User getUser(String userId) {
		if (userId == null) {
			return null;
		}
		return rmSchema.wrapUser(recordServices.getDocumentById(userId));
	}

	private void createMissingAuthorizations(RMTask task, List<String> recordIds, User user) {
		if (user == null || !isCreateMissingAuthorizationsForTaskEnabled()) {
			return;
		}

		List<String> addedAuthorizationIds = new ArrayList<>();
		for (String recordId : recordIds) {
			Record record = recordServices.getDocumentById(recordId);
			if (!user.hasReadAccess().on(record)) {
				AuthorizationAddRequest request = authorizationForUsers(user).on(record).givingReadAccess();
				addedAuthorizationIds.add(authorizationsServices.add(request));
			}
		}

		if (!addedAuthorizationIds.isEmpty()) {
			task.addCreatedAuthorizations(addedAuthorizationIds);
			try {
				recordServices.update(task);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void deleteAllCreatedAuthorizations(RMTask task) {
		List<String> authorizationIds = task.getCreatedAuthorizations();
		for (String authorizationId : authorizationIds) {
			try {
				authorizationsServices.execute(authorizationDeleteRequest(authorizationId, collection));
			} catch (AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId ignored) {
			}
		}

		try {
			recordServices.update(task.setCreatedAuthorizations(null));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}


	}

	private void deleteCreatedAuthorizations(RMTask task, List<String> recordIds) {
		List<String> deletedAuthorizationIds = new ArrayList<>();

		for (String authorizationId : task.getCreatedAuthorizations()) {
			try {
				Authorization authorization = authorizationsServices.getAuthorization(collection, authorizationId);
				if (recordIds.contains(authorization.getTarget())) {
					authorizationsServices.execute(authorizationDeleteRequest(authorizationId, collection));
					deletedAuthorizationIds.add(authorizationId);
				}
			} catch (AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId e) {
				// record not found, we remove it from metadata
				deletedAuthorizationIds.add(authorizationId);
			}
		}

		if (!deletedAuthorizationIds.isEmpty()) {
			try {
				recordServices.update(task.removeCreatedAuthorizations(deletedAuthorizationIds));
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private boolean isCreateMissingAuthorizationsForTaskEnabled() {
		return new RMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isCreateMissingAuthorizationsForTask();
	}

}
