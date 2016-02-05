package com.constellio.app.ui.pages.base;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.ContentVersionVO.InputStreamProvider;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchMetadata;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;

public class SchemaPresenterUtils extends BasePresenterUtils {

	private static final String VERSION_INPUT_STREAM_NAME = "SchemaPresenterUtils-VersionInputStream";

	private static Logger LOGGER = LoggerFactory.getLogger(SchemaPresenterUtils.class);

	protected String schemaCode;

	public SchemaPresenterUtils(String schemaCode, ConstellioFactories constellioFactories, SessionContext sessionContext) {
		super(constellioFactories, sessionContext);
		this.schemaCode = schemaCode;
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
		return addOrUpdate(record, getCurrentUser());
	}

	public final List<BatchProcess> addOrUpdateWithoutUser(Record record) {
		return addOrUpdate(record, null);
	}

	public final List<BatchProcess> addOrUpdate(Record record, User user) {
		Transaction createTransaction = new Transaction();
		createTransaction.setUser(user);
		createTransaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		createTransaction.addUpdate(record);
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
		recordServices().logicallyDelete(record, user);
		modelLayerFactory().newLoggingServices().logDeleteRecordWithJustification(record, user, reason);
		if (physically) {
			recordServices().physicallyDelete(record, user);
		}
	}

	@SuppressWarnings("unchecked")
	public final Record toRecord(RecordVO recordVO) {
		return toRecord(recordVO, false);
	}

	@SuppressWarnings("unchecked")
	public final Record toRecord(RecordVO recordVO, boolean newMinorEmpty) {
		Record record;
		try {
			record = recordServices().getDocumentById(recordVO.getId());
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			record = newRecord(recordVO.getId());
		}
		String recordSchemaCode = record.getSchemaCode();
		MetadataSchema currentSchema = schema(recordSchemaCode);
		MetadataSchema targetSchema = schema(schemaCode);
		if (!recordSchemaCode.equals(schemaCode)) {
			record.changeSchema(currentSchema, targetSchema);
		}
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

			boolean systemReserved = metadata.isSystemReserved();
			if (!systemReserved && metadata.isEnabled() && metadata.getDataEntry().getType() == DataEntryType.MANUAL) {
				Object metadataValue = record.get(metadata);
				Object metadataVOValue = metadataValueVO.getValue();
				if (metadataVOValue instanceof RecordVO) {
					metadataVOValue = ((RecordVO) metadataVOValue).getId();
				} else if (metadataVOValue instanceof ContentVersionVO) {
					ContentVersionVO contentVersionVO = (ContentVersionVO) metadataVOValue;
					Content content = toContent(contentVersionVO, newMinorEmpty);
					if (content != null) {
						metadataVOValue = content;
					} else {
						// Same value
						metadataVOValue = metadataValue;
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
							Content content = toContent(contentVersionVO, newMinorEmpty);
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
					record.set(metadata, metadataVOValue);
				}
			}
		}
		return record;
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

	public Content toContent(ContentVersionVO contentVersionVO) {
		return toContent(contentVersionVO, false);
	}

	public Content toContent(ContentVersionVO contentVersionVO, boolean newMinorEmpty) {
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

		boolean newContent = hash == null;

		if (newContent) {
			InputStream inputStream = null;
			ContentVersionDataSummary contentVersionDataSummary;
			try {
				inputStream = inputStreamProvider.getInputStream(VERSION_INPUT_STREAM_NAME);
				contentVersionDataSummary = contentManager.upload(inputStream);
			} finally {
				IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
				ioServices.closeQuietly(inputStream);
			}
			hash = contentVersionDataSummary.getHash();
			//			contentVersionVO.setId(id);
			if (majorVersion == null) {
				// TODO Use the right kind of exception
				throw new RuntimeException("Must specify if the version is minor or major");
			} else if (majorVersion) {
				content = contentManager.createMajor(currentUser, fileName, contentVersionDataSummary);
			} else if (newMinorEmpty) {
				content = contentManager.createEmptyMinor(currentUser, fileName, contentVersionDataSummary);
			} else {
				content = contentManager.createMinor(currentUser, fileName, contentVersionDataSummary);
			}
		} else {
			content = null;
		}
		if (content != null) {
			contentVersionVO.setContentId(content.getId());
		}
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

}
