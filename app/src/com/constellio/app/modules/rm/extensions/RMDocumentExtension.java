package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.RecordSetCategoryEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RMDocumentExtension extends RecordExtension {
	private final ModelLayerFactory modelLayerFactory;
	private final RMSchemasRecordsServices rmSchema;

	private static String OUTLOOK_MSG_MIMETYPE = "application/vnd.ms-outlook";

	private String collection;
	private AppLayerFactory appLayerFactory;
	private List<String> removedCartsIds;

	public RMDocumentExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		rmSchema = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		removedCartsIds = new ArrayList<>();
	}

	@Override
	public void setRecordCategory(RecordSetCategoryEvent event) {

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				ConstellioFactories.getInstance().getAppLayerFactory());

		if (event.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(event.getRecord());
			DocumentType type = null;

			if ("default".equals(document.getSchema().getLocalCode())) {
				Content content = document.getContent();

				if (event.getCategory() != null) {
					type = rm.getDocumentTypeByCode(event.getCategory());
				}
				if (type == null && content != null) {
					String mimetype = content.getCurrentVersion().getMimetype();
					if (OUTLOOK_MSG_MIMETYPE.equals(mimetype)) {
						type = rm.getDocumentTypeByCode(DocumentType.EMAIL_DOCUMENT_TYPE);
					}
				}
				if (type != null) {
					document.setType(type);
					if (type.getLinkedSchema() != null) {
						document.changeSchemaTo(type.getLinkedSchema());
					}
				}
			}
		}

	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				ConstellioFactories.getInstance().getAppLayerFactory());

		if (event.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(event.getRecord());
			if (LangUtils.isTrueOrNull(document.get(Schemas.MARKED_FOR_PREVIEW_CONVERSION))) {
				Content content = document.getContent();
				boolean requireConversion =
						content != null && isFilePreviewSupportedFor(content.getCurrentVersion().getFilename());
				document.setMarkedForPreviewConversion(requireConversion ? true : null);
			}
		}
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				ConstellioFactories.getInstance().getAppLayerFactory());

		if (event.isSchemaType(Document.SCHEMA_TYPE) && event.hasModifiedMetadata(Document.CONTENT)) {
			Document document = rm.wrapDocument(event.getRecord());
			Content content = document.getContent();
			boolean requireConversion = content != null && isFilePreviewSupportedFor(content.getCurrentVersion().getFilename());
			document.setMarkedForPreviewConversion(requireConversion ? true : null);
		}
	}

	@Override
	public ExtensionBooleanResult isRecordModifiableBy(IsRecordModifiableByParams params) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				ConstellioFactories.getInstance().getAppLayerFactory());

		User user = params.getUser();
		if (params.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(params.getRecord());

			if (user.hasWriteAccess().on(document)) {
				if (document.getArchivisticStatus().isInactive()) {
					Folder parentFolder = rm.getFolder(document.getFolder());
					if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
						return ExtensionBooleanResult
								.trueIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_BORROWED_FOLDER).on(parentFolder)
										&& user.has(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT).on(document));
					}
					return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MODIFY_INACTIVE_DOCUMENT).on(document));
				}
				if (document.getArchivisticStatus().isSemiActive()) {
					Folder parentFolder = rm.getFolder(document.getFolder());
					if (parentFolder.getBorrowed() != null && parentFolder.getBorrowed()) {
						return ExtensionBooleanResult
								.trueIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_BORROWED_FOLDER).on(parentFolder)
										&& user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT).on(document));
					}
					return ExtensionBooleanResult.trueIf(user.has(RMPermissionsTo.MODIFY_SEMIACTIVE_DOCUMENT).on(document));
				}
				return ExtensionBooleanResult.TRUE;
			}

			return ExtensionBooleanResult.FALSE;
		}
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	private boolean isFilePreviewSupportedFor(String filename) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(filename));
		return modelLayerFactory.getDataLayerFactory().getConversionManager().isSupportedExtension(extension)
			   || extension.equalsIgnoreCase("pdf");
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
	}

	@Override
	public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
			RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
		if (event.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rmSchema.wrapDocument(event.getRecord());
			deleteNonExistentFavoritesIds(document);
		}
	}

	@Override
	public ValidationErrors validateLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection,
				ConstellioFactories.getInstance().getAppLayerFactory());

		if (event.isSchemaType(Document.SCHEMA_TYPE)) {
			Document document = rm.wrapDocument(event.getRecord());
			User user = event.getUser();

			Content content = document.getContent();
			String checkoutUserId = content != null ? content.getCheckoutUserId() : null;

			SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
			TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
			boolean usedInTasks = false;
			List<Record> tasks = new ArrayList<>();
			if (!event.isThenPhysicallyDeleted()) {
				usedInTasks = searchServices.getResultsCount(new LogicalSearchQuery(from(rm.userTask.schemaType())
						.where(rm.userTask.linkedDocuments()).isEqualTo(event.getRecord().getId())
						.andWhere(taskSchemas.userTask.status()).isNotIn(taskSchemas.getFinishedOrClosedStatuses())
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())) > 0;
			}
			if ((checkoutUserId != null && (user == null || !user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT).on(document)))
				|| usedInTasks) {
				ValidationErrors validationErrors = new ValidationErrors();
				if (usedInTasks) {
					Map<String, Object> parameter = new HashMap<>();
					parameter.put("records", tasks);
					validationErrors.add(RMDocumentExtension.class, "documentUsedInTasks", parameter);
				}
				if ((checkoutUserId != null && (user == null || !user.has(RMPermissionsTo.DELETE_BORROWED_DOCUMENT).on(document)))) {
					validationErrors.add(RMDocumentExtension.class, "userDoesNotHavePremissionToDeleteBorrowedDocument");
				}
				return validationErrors;
			}
		}
		return super.validateLogicallyDeletable(event);
	}

	private void deleteNonExistentFavoritesIds(Document document) {
		List<String> removedIds = new ArrayList<>();
		RecordsCaches recordsCaches = modelLayerFactory.getRecordsCaches();
		for (String cartId : document.getFavorites()) {
			if (!removedCartsIds.contains(cartId)) {
				if (recordsCaches.getRecord(cartId) == null) {
					removedIds.add(cartId);
					removedCartsIds.add(cartId);
				}
			} else {
				removedIds.add(cartId);
			}
		}
		if (!removedIds.isEmpty()) {
			document.removeFavorites(removedIds);
		}
	}

}
