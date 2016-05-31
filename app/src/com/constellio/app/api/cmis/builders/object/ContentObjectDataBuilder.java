package com.constellio.app.api.cmis.builders.object;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.joda.time.LocalDateTime;

import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;

public class ContentObjectDataBuilder {

	private static String DOCUMENT_TYPE_ID = BaseTypeId.CMIS_DOCUMENT.value();
	private final ConstellioCollectionRepository repository;

	public ContentObjectDataBuilder(ConstellioCollectionRepository repository) {
		this.repository = repository;
	}

	public ObjectData build(AppLayerFactory appLayerFactory, CallContext context, ContentCmisDocument contentCmisDocument,
			Set<String> filter,
			boolean includeAllowableActions, boolean includeAcl, ObjectInfoHandler objectInfos) {
		ObjectDataImpl result = new ObjectDataImpl();
		ObjectInfoImpl objectInfo = new ObjectInfoImpl();

		result.setProperties(compileProperties(contentCmisDocument, filter, objectInfo));

		if (includeAllowableActions) {
			result.setAllowableActions(
					new AllowableActionsBuilder(appLayerFactory, repository, contentCmisDocument.getRecord()).build());
		}

		if (context.isObjectInfoRequired()) {
			objectInfo.setObject(result);
			objectInfos.addObjectInfo(objectInfo);
		}

		return result;
	}

	private Properties compileProperties(ContentCmisDocument content, Set<String> receivedFilter,
			ObjectInfoImpl objectInfo) {
		if (content == null) {
			throw new IllegalArgumentException("Content must not be null!");
		}

		Record contentRecord = content.getRecord();
		ContentVersion contentVersion;
		contentVersion = content.getContentVersion();
		Set<String> filter = (receivedFilter == null ? null : new HashSet<>(receivedFilter));

		PropertiesImpl result = new PropertiesImpl();

		String id = content.getContentVersionId();
		addPropertyId(result, filter, PropertyIds.OBJECT_ID, id);
		objectInfo.setId(id);

		addPropertyString(result, filter, PropertyIds.NAME, id);
		objectInfo.setName(id);

		addPropertyString(result, filter, PropertyIds.CREATED_BY, contentVersion.getModifiedBy());
		addPropertyString(result, filter, PropertyIds.LAST_MODIFIED_BY, contentVersion.getModifiedBy());
		objectInfo.setCreatedBy(contentVersion.getModifiedBy());

		GregorianCalendar lastModified = getGregorianCalendar(contentVersion.getLastModificationDateTime());
		addPropertyDateTime(result, filter, PropertyIds.CREATION_DATE, lastModified);
		addPropertyDateTime(result, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
		objectInfo.setCreationDate(lastModified);
		objectInfo.setLastModificationDate(lastModified);

		addPropertyString(result, filter, PropertyIds.CHANGE_TOKEN, Long.toString(contentRecord.getVersion()));

		addPropertyId(result, filter, PropertyIds.BASE_TYPE_ID, DOCUMENT_TYPE_ID);
		addPropertyId(result, filter, PropertyIds.OBJECT_TYPE_ID, DOCUMENT_TYPE_ID);
		addPropertyString(result, filter, PropertyIds.VERSION_LABEL, content.getVersionLabel());
		addPropertyString(result, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, content.getContent().getCheckoutUserId());
		if (content.isCheckedOut()) {
			addPropertyString(result, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
					content.getPrivateWorkingCopyVersionId());
		}
		addPropertyString(result, filter, PropertyIds.VERSION_SERIES_ID, content.getVersionSeriesId());
		addPropertyBoolean(result, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, content.isLatestMajor());
		addPropertyBoolean(result, filter, PropertyIds.IS_LATEST_VERSION, content.isLatest());
		addPropertyBoolean(result, filter, PropertyIds.IS_MAJOR_VERSION, content.isMajor());
		addPropertyBigInteger(result, filter, PropertyIds.CONTENT_STREAM_LENGTH, BigInteger.valueOf(contentVersion.getLength()));
		addPropertyId(result, filter, PropertyIds.CONTENT_STREAM_ID, contentVersion.getHash());
		addPropertyString(result, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, contentVersion.getFilename());
		addPropertyString(result, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, contentVersion.getMimetype());
		//addPropertyBoolean(builtProperties, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, content.isPrivateWorkingCopy());
		addPropertyBoolean(result, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, content.isCheckedOut());
		addPropertyString(result, filter, "metadata", content.getMetadataLocalCode());

		addPropertyString(result, filter, PropertyIds.PARENT_ID, contentRecord.getId());

		setupObjectInfo(objectInfo, content, contentVersion);

		return result;
	}

	private void setupObjectInfo(ObjectInfoImpl objectInfo, ContentCmisDocument content, ContentVersion contentVersion) {
		objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
		objectInfo.setTypeId(DOCUMENT_TYPE_ID);
		objectInfo.setContentType(contentVersion.getMimetype());
		objectInfo.setFileName(contentVersion.getFilename());
		objectInfo.setHasAcl(false);
		objectInfo.setHasContent(true);
		objectInfo.setVersionSeriesId(content.getVersionSeriesId());
		objectInfo.setIsCurrentVersion(content.isLatest());
		objectInfo.setRelationshipSourceIds(null);
		objectInfo.setRelationshipTargetIds(null);
		objectInfo.setRenditionInfos(null);
		objectInfo.setSupportsDescendants(true);
		objectInfo.setSupportsFolderTree(true);
		objectInfo.setSupportsPolicies(false);
		objectInfo.setSupportsRelationships(false);
		objectInfo.setWorkingCopyId(null);
		objectInfo.setWorkingCopyOriginalId(null);
	}

	private GregorianCalendar getGregorianCalendar(Object value) {
		if (value != null) {
			return ((LocalDateTime) value).toDateTime().toGregorianCalendar();
		} else {
			return null;
		}
	}

	private void addPropertyId(PropertiesImpl props, Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyString(PropertiesImpl props, Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, filter, id)) {
			return;
		}

		props.addProperty(new PropertyStringImpl(id, value));
	}

	private void addPropertyBigInteger(PropertiesImpl props, Set<String> filter, String id, BigInteger value) {
		if (!checkAddProperty(props, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIntegerImpl(id, value));
	}

	private void addPropertyBoolean(PropertiesImpl props, Set<String> filter, String id, boolean value) {
		if (!checkAddProperty(props, filter, id)) {
			return;
		}

		props.addProperty(new PropertyBooleanImpl(id, value));
	}

	private void addPropertyDateTime(PropertiesImpl props, Set<String> filter, String id,
			GregorianCalendar value) {
		if (!checkAddProperty(props, filter, id)) {
			return;
		}

		props.addProperty(new PropertyDateTimeImpl(id, value));
	}

	private boolean checkAddProperty(Properties properties, Set<String> filter, String id) {
		if ((properties == null) || (properties.getProperties() == null)) {
			throw new IllegalArgumentException("Properties must not be null!");
		}

		if (id == null) {
			throw new IllegalArgumentException("Id must not be null!");
		}

		TypeDefinition type = repository.getTypeDefinitionsManager().getInternalTypeDefinition(DOCUMENT_TYPE_ID);
		if (type == null) {
			throw new IllegalArgumentException("Unknown type: " + DOCUMENT_TYPE_ID);
		}
		if (!type.getPropertyDefinitions().containsKey(id)) {
			throw new IllegalArgumentException("Unknown property: " + id);
		}

		String queryName = type.getPropertyDefinitions().get(id).getQueryName();

		if ((queryName != null) && (filter != null)) {
			if (!filter.contains(queryName)) {
				return false;
			} else {
				filter.remove(queryName);
			}
		}

		return true;
	}

}
