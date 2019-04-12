package com.constellio.app.ui.pages.base;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadata;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.schemas.PutSchemaRecordsInTrashEvent;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;

public class SchemaPresenterUtils extends BasePresenterUtils {

	private static final String VERSION_INPUT_STREAM_NAME = "SchemaPresenterUtils-VersionInputStream";

	private static Logger LOGGER = LoggerFactory.getLogger(SchemaPresenterUtils.class);

	protected String schemaCode;

	RMConfigs rmConfigs;

	public SchemaPresenterUtils(String schemaCode, ConstellioFactories constellioFactories,
								SessionContext sessionContext) {
		super(constellioFactories, sessionContext);
		this.schemaCode = schemaCode;
		this.rmConfigs = new RMConfigs(modelLayerFactory().getSystemConfigurationsManager());
	}

	@Deprecated
	//Keep the schema code outside of this class
	public final String getSchemaCode() {
		return schemaCode;
	}

	@Deprecated
	//Keep the schema code outside of this class
	public final void setSchemaCode(String schemaCode) {
		this.schemaCode = schemaCode;
	}

	public final Record newRecord() {
		return recordServices().newRecordWithSchema(schema(schemaCode));
	}

	private Record newRecord(String id) {
		return recordServices().newRecordWithSchema(schema(schemaCode), id);
	}

	public final Record getRecord(String id) {
		return recordServices().getDocumentById(id);
	}

	public final Metadata getMetadata(String code) {
		return types().getSchema(schemaCode).getMetadata(code);
	}

	public final List<BatchProcess> addOrUpdate(Record record) {
		return addOrUpdate(record, getCurrentUser(), RecordsFlushing.NOW());
	}

	public final List<BatchProcess> addOrUpdate(Record record, RecordUpdateOptions updateOptions) {
		return addOrUpdate(record, getCurrentUser(), RecordsFlushing.NOW(), updateOptions);
	}

	public final List<BatchProcess> addOrUpdateWithoutUser(Record record) {
		return addOrUpdate(record, (User) null);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user) {
		return addOrUpdate(record, user, RecordsFlushing.NOW());
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user, RecordsFlushing recordFlushing) {
		return addOrUpdate(record, user, recordFlushing, null);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user, RecordsFlushing recordFlushing,
												RecordUpdateOptions updateOptions) {
		Transaction createTransaction = new Transaction();
		createTransaction.setUser(user);
		createTransaction.setToReindexAll();
		createTransaction.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
		if (updateOptions != null) {
			createTransaction.setOptions(updateOptions);
		}
		createTransaction.addUpdate(record);
		//		if (!modelLayerFactory().getRecordsCaches().isCached(record.getId())
		//				|| !modelLayerFactory().getRecordsCaches().getCache(getCollection()).isCached(record.getId())) {
		createTransaction.setRecordFlushing(recordFlushing);
		//		}
		try {
			return recordServices().executeHandlingImpactsAsync(createTransaction);
		} catch (RecordServicesException e) {
			Exception nestedException;
			if (e instanceof RecordServicesException.ValidationException) {
				LOGGER.error(e.getMessage(), e);
				nestedException = new ValidationException(((RecordServicesException.ValidationException) e).getErrors());
			} else {
				nestedException = e;
			}
			throw new RuntimeException(nestedException);
		}
	}

	public final void delete(Record record, String reason) {
		delete(record, reason, true);
	}

	public final void delete(Record record, String reason, User user) {
		delete(record, reason, true, user);
	}

	public final void delete(Record record, String reason, boolean physically) {
		delete(record, reason, physically, getCurrentUser());
	}

	public final void delete(Record record, String reason, boolean physically, User user) {
		delete(record, reason, physically, user, 0);
	}

	public final void delete(Record record, String reason, boolean physically, int waitInSeconds) {
		delete(record, reason, physically, getCurrentUser(), waitInSeconds);
	}

	public final void delete(Record record, String reason, boolean physically, User user, int waitInSeconds) {
		boolean putFirstInTrash = putFirstInTrash(record);
		if (recordServices().validateLogicallyThenPhysicallyDeletable(record, user).isEmpty() || putFirstInTrash) {

			RecordLogicalDeleteOptions options = new RecordLogicalDeleteOptions();
			//Validations are already done
			options.setSkipValidations(true);
			if (waitInSeconds > 0) {
				options.setRecordsFlushing(RecordsFlushing.WITHIN_SECONDS(waitInSeconds));
			}

			recordServices().logicallyDelete(record, user, options);

			if (waitInSeconds > 0) {
				try {
					Thread.sleep(1000 * waitInSeconds);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}

			modelLayerFactory().newLoggingServices().logDeleteRecordWithJustification(record, user, reason);
			if (physically && !putFirstInTrash) {
				recordServices().physicallyDeleteNoMatterTheStatus(record, user, new RecordPhysicalDeleteOptions());
			}
		}
	}

	private boolean putFirstInTrash(Record record) {
		ModelLayerExtensions ext = modelLayerFactory().getExtensions();
		if (ext == null) {
			return false;
		}
		ModelLayerCollectionExtensions extensions = ext.forCollection(record.getCollection());
		PutSchemaRecordsInTrashEvent event = new PutSchemaRecordsInTrashEvent(record.getSchemaCode(), null);
		return extensions.isPutInTrashBeforePhysicalDelete(event);
	}

	@SuppressWarnings("unchecked")
	public final Record toRecord(RecordVO recordVO) {
		return toRecord(recordVO, false);
	}

	@SuppressWarnings("unchecked")
	public final Record toNewRecord(RecordVO recordVO) {
		return toNewRecord(recordVO, false);
	}

	@SuppressWarnings("unchecked")
	public final Record toRecord(RecordVO recordVO, boolean newMinorEmpty) {
		Record record;
		try {
			record = recordServices().getDocumentById(recordVO.getId()).getCopyOfOriginalRecord();
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			record = newRecord(recordVO.getId());
		}
		fillRecordUsingRecordVO(record, recordVO, newMinorEmpty);
		return record;
	}

	@SuppressWarnings("unchecked")
	public final Record toNewRecord(RecordVO recordVO, boolean newMinorEmpty) {
		Record record = newRecord(recordVO.getId());
		fillRecordUsingRecordVO(record, recordVO, newMinorEmpty);
		return record;
	}

	@SuppressWarnings("unchecked")
	public final void fillRecordUsingRecordVO(Record record, RecordVO recordVO, boolean newMinorEmpty) {
		String recordSchemaCode = record.getSchemaCode();
		MetadataSchema currentSchema = schema(recordSchemaCode);
		MetadataSchema targetSchema = schema(schemaCode);
		if (!recordSchemaCode.equals(schemaCode)) {
			record.changeSchema(currentSchema, targetSchema);
		}

		boolean newRecord = !record.isSaved();

		for (MetadataValueVO metadataValueVO : recordVO.getMetadataValues()) {
			MetadataVO metadataVO = metadataValueVO.getMetadata();
			String metadataCode = metadataVO.getCode();
			String localMetadataCode = new SchemaUtils().getLocalCodeFromMetadataCode(metadataCode);

			Metadata metadata;
			try {
				metadata = targetSchema.getMetadata(localMetadataCode);
			} catch (NoSuchMetadata e) {
				continue;
			}

			boolean systemReserved = metadata.isSystemReserved() && !metadata.hasSameCode(Schemas.LEGACY_ID);
			if (!systemReserved && metadata.isEnabled() && isMetadataManuallyFilled(metadata)) {
				Object metadataValue;
				if (metadataVO.isMultiLingual() && metadataVO.getLocale() != null) {
					metadataValue = record.get(metadata, metadataVO.getLocale());
				} else {
					metadataValue = record.get(metadata);
				}
				Object metadataVOValue = metadataValueVO.getValue();
				if (metadataVOValue instanceof RecordVO) {
					metadataVOValue = ((RecordVO) metadataVOValue).getId();
				} else if (metadataVOValue instanceof ContentVersionVO) {
					ContentVersionVO contentVersionVO = (ContentVersionVO) metadataVOValue;
					Content content = toContent(recordVO, metadataVO, contentVersionVO, newMinorEmpty);
					if (!newRecord) {
						Content recordContent = (Content) metadataValue;
						String recordContentVersionHash = recordContent.getCurrentVersion().getHash();
						if (recordContentVersionHash.equals(contentVersionVO.getHash())) {
							// No change to content
							metadataVOValue = recordContent;
						} else {
							metadataVOValue = content;
						}
					} else {
						metadataVOValue = content;
					}
				} else if (metadataVOValue instanceof Collection) {
					List<Object> replacementValue = new ArrayList<Object>();
					Collection<Object> collectionMetadataValue = (Collection<Object>) metadataVOValue;
					boolean contentMetadata = false;
					for (Iterator<Object> it = collectionMetadataValue.iterator(); it.hasNext(); ) {
						Object element = it.next();
						if (element instanceof RecordVO) {
							replacementValue.add(((RecordVO) metadataVOValue).getId());
						} else if (element instanceof ContentVersionVO) {
							contentMetadata = true;
							ContentVersionVO contentVersionVO = (ContentVersionVO) element;
							Content content = toContent(recordVO, metadataVO, contentVersionVO, newMinorEmpty);
							if (content == null) {
								content = getContent(contentVersionVO.getHash(), (List<Content>) metadataValue);
							}
							replacementValue.add(content);
						}
					}
					if (!replacementValue.isEmpty()) {
						metadataVOValue = replacementValue;
					} else if (contentMetadata) {
						// No changes were made
						if (collectionMetadataValue.isEmpty()) {
							// Set an empty list, as a content may have been removed from the collection
							metadataVOValue = new ArrayList<Content>();
						} else {
							metadataVOValue = metadataValue;
						}
					}
				}
				boolean valueDifferent;
				if ((metadataValue != null && metadataVOValue == null) || (metadataValue == null && metadataVOValue != null)) {
					valueDifferent = true;
				} else if (metadataVOValue == null && metadataValue == null) {
					valueDifferent = false;
				} else {
					valueDifferent = !metadataValueVO.equals(metadataValue);
				}
				if (valueDifferent) {
					if (metadataVO.isMultiLingual() && metadataVO.getLocale() != null) {
						metadataValue = record.set(metadata, metadataVO.getLocale(), metadataVOValue);
					} else {
						metadataValue = record.set(metadata, metadataVOValue);
					}
				}
			}
		}
	}

	private Content getContent(String id, List<Content> contentList) {
		Content match = null;
		if (contentList != null) {
			for (Content content : contentList) {
				ContentVersion currentVersion = content.getCurrentVersion();
				String hash = currentVersion.getHash();
				if (id.equals(hash)) {
					match = content;
					break;
				}
			}
		}
		return match;
	}

	public Content toContent(RecordVO recordVO, MetadataVO metadataVO, ContentVersionVO contentVersionVO) {
		return toContent(recordVO, metadataVO, contentVersionVO, false);
	}

	public Content toContent(RecordVO recordVO, MetadataVO metadataVO, ContentVersionVO contentVersionVO,
							 boolean newMinorEmpty) {
		Content content;
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		UserServices userServices = modelLayerFactory.newUserServices();

		String collection = sessionContext.getCurrentCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String username = currentUserVO.getUsername();
		User currentUser = userServices.getUserInCollection(username, collection);

		String hash = contentVersionVO.getHash();
		String fileName = contentVersionVO.getFileName();
		Boolean majorVersion = contentVersionVO.isMajorVersion();
		InputStreamProvider inputStreamProvider = contentVersionVO.getInputStreamProvider();

		InputStream inputStream = null;
		ContentVersionDataSummary contentVersionDataSummary;
		try {
			inputStream = inputStreamProvider.getInputStream(VERSION_INPUT_STREAM_NAME);
			UploadOptions options = new UploadOptions().setFileName(fileName);
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = uploadContent(inputStream, options);
			contentVersionDataSummary = uploadResponse.getContentVersionDataSummary();
			contentVersionVO.setHasFoundDuplicate(uploadResponse.hasFoundDuplicate())
					.setDuplicatedHash(contentVersionDataSummary.getHash());
		} finally {
			IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
			ioServices.closeQuietly(inputStream);
		}
		if (majorVersion == null) {
			boolean versioning;
			if (metadataVO != null) {
				MetadataInputType inputType = metadataVO.getMetadataInputType();
				versioning = inputType == MetadataInputType.CONTENT_CHECK_IN_CHECK_OUT;
			} else {
				versioning = false;
			}
			if (versioning) {
				// TODO Use the right kind of exception
				throw new RuntimeException("Must specify if the version is minor or major");
			} else {
				content = contentManager.createMajor(currentUser, fileName, contentVersionDataSummary);
			}
		} else if (majorVersion) {
			content = contentManager.createMajor(currentUser, fileName, contentVersionDataSummary);
		} else if (newMinorEmpty && rmConfigs.isMajorVersionForNewFile()) {
			content = contentManager.createEmptyMajor(currentUser, fileName, contentVersionDataSummary);
		} else if (newMinorEmpty) {
			content = contentManager.createEmptyMinor(currentUser, fileName, contentVersionDataSummary);
		} else {
			content = contentManager.createMinor(currentUser, fileName, contentVersionDataSummary);
		}
		contentVersionVO.setContentId(content.getId());
		return content;
	}

	@Deprecated
	//Use schema(code) instead
	public final MetadataSchema schema() {
		return schema(schemaCode);
	}

	public final MetadataSchema defaultSchema() {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return schemaType(schemaTypeCode).getDefaultSchema();
	}

	public boolean isMetadataManuallyFilled(Metadata metadata) {
		return metadata.getDataEntry().getType() == MANUAL ||
			   (metadata.getDataEntry().getType() == CALCULATED &&
				((CalculatedDataEntry) metadata.getDataEntry()).getCalculator().hasEvaluator());
	}

}
