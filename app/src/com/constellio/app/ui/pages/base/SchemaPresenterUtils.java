/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
import com.constellio.model.services.users.UserServices;

public class SchemaPresenterUtils extends BasePresenterUtils {

	private static final String VERSION_INPUT_STREAM_NAME = "SchemaPresenterUtils-VersionInputStream";

	private static Logger LOGGER = LoggerFactory.getLogger(SchemaPresenterUtils.class);

	protected String schemaCode;

	public SchemaPresenterUtils(String schemaCode, ConstellioFactories constellioFactories, SessionContext sessionContext) {
		super(constellioFactories, sessionContext);
		this.schemaCode = schemaCode;
	}

	public final String getSchemaCode() {
		return schemaCode;
	}

	public final void setSchemaCode(String schemaCode) {
		this.schemaCode = schemaCode;
	}

	public final Record newRecord() {
		return recordServices().newRecordWithSchema(schema(schemaCode));
	}

	public final Record getRecord(String id) {
		return recordServices().getDocumentById(id);
	}

	public final Metadata getMetadata(String code) {
		return types().getSchema(schemaCode).getMetadata(code);
	}

	public final List<BatchProcess> addOrUpdate(Record record) {
		Transaction createTransaction = new Transaction();
		createTransaction.setUser(getCurrentUser());
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

	public final void delete(Record record, String reason, boolean physically) {
		User currentUser = getCurrentUser();
		recordServices().logicallyDelete(record, currentUser);
		modelLayerFactory().newLoggingServices().logDeleteRecordWithJustification(record, currentUser, reason);
		if (physically) {
			recordServices().physicallyDelete(record, currentUser);
		}
	}

	@SuppressWarnings("unchecked")
	public final Record toRecord(RecordVO recordVO) {
		Record record;
		try {
			record = recordServices().getDocumentById(recordVO.getId());
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			record = newRecord();
		}
		MetadataSchema schema = schema();
		for (MetadataValueVO metadataValueVO : recordVO.getMetadataValues()) {
			MetadataVO metadataVO = metadataValueVO.getMetadata();
			String metadataCode = metadataVO.getCode();

			Metadata metadata;
			try {
				metadata = schema.getMetadata(metadataCode);
			} catch (NoSuchMetadata e) {
				continue;
			}

			if (metadata.getDataEntry().getType() == DataEntryType.MANUAL) {

				Object metadataValue = record.get(metadata);
				Object metadataVOValue = metadataValueVO.getValue();
				if (metadataVOValue instanceof RecordVO) {
					metadataVOValue = ((RecordVO) metadataVOValue).getId();
				} else if (metadataVOValue instanceof ContentVersionVO) {
					ContentVersionVO contentVersionVO = (ContentVersionVO) metadataVOValue;
					Content content = toContent(contentVersionVO);
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
							Content content = toContent(contentVersionVO);
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

				record.set(metadata, metadataVOValue);
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

		ContentVersionDataSummary contentVersionDataSummary;
		if (hash == null) {
			InputStream inputStream = null;
			try {
				inputStream = inputStreamProvider.getInputStream(VERSION_INPUT_STREAM_NAME);
				contentVersionDataSummary = contentManager.upload(inputStream);
			} finally {
				IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
				ioServices.closeQuietly(inputStream);
			}
			hash = contentVersionDataSummary.getHash();
			//			contentVersionVO.setId(id);
		} else {
			contentVersionDataSummary = contentManager.getContentVersionSummary(hash);
		}
		if (newContent) {
			if (majorVersion == null) {
				// TODO Use the right kind of exception
				throw new RuntimeException("Must specify if the version is minor or major");
			} else if (majorVersion) {
				content = contentManager.createMajor(currentUser, fileName, contentVersionDataSummary);
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

	public final MetadataSchema schema() {
		return schema(schemaCode);
	}

}
