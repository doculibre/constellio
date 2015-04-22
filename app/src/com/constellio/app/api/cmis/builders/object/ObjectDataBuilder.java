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
package com.constellio.app.api.cmis.builders.object;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_Runtime;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ObjectDataBuilder {

	private static final String USER_UNKNOWN = "<unknown>";

	private final ConstellioCollectionRepository repository;
	private final ModelLayerFactory modelLayerFactory;

	public ObjectDataBuilder(ConstellioCollectionRepository repository, ModelLayerFactory modelLayerFactory) {
		this.repository = repository;
		this.modelLayerFactory = modelLayerFactory;
	}

	public ObjectData build(CallContext context, Record record, Set<String> filter, boolean includeAllowableActions,
			boolean includeAcl, ObjectInfoHandler objectInfos) {
		ObjectDataImpl result = new ObjectDataImpl();
		ObjectInfoImpl objectInfo = new ObjectInfoImpl();

		result.setProperties(compileProperties(record, filter, objectInfo));

		if (includeAllowableActions) {
			result.setAllowableActions(new AllowableActionsBuilder(repository, record).build());
		}

		//		if (includeAcl) {
		//			result.setAcl(new AclBuilder(repository).build(file));
		//			result.setIsExactAcl(true);
		//		}

		if (context.isObjectInfoRequired()) {
			objectInfo.setObject(result);
			objectInfos.addObjectInfo(objectInfo);
		}

		return result;
	}

	private Properties compileProperties(Record record, Set<String> orgfilter,
			ObjectInfoImpl objectInfo) {
		if (record == null) {
			throw new IllegalArgumentException("File must not be null!");
		}

		Set<String> filter = (orgfilter == null ? null : new HashSet<String>(orgfilter));
		String typeId = record.getSchemaCode();
		setupObjectInfo(objectInfo, typeId);

		try {
			PropertiesImpl result = new PropertiesImpl();

			String id = record.getId();
			addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
			objectInfo.setId(id);

			String name = record.get(Schemas.TITLE);
			addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
			objectInfo.setName(name);

			addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, (String) record.get(Schemas.CREATED_BY));
			addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, (String) record.get(Schemas.MODIFIED_BY));
			objectInfo.setCreatedBy(USER_UNKNOWN);

			GregorianCalendar creationDate = getGregorianCalendar(record.get(Schemas.CREATED_ON));
			GregorianCalendar lastModified = getGregorianCalendar(record.get(Schemas.MODIFIED_ON));
			addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, creationDate);
			addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
			objectInfo.setCreationDate(creationDate);
			objectInfo.setLastModificationDate(lastModified);

			addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, Long.toString(record.getVersion()));

			addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
			addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId);

			// The principal path is always used for now
			String path = record.get(Schemas.PRINCIPAL_PATH);
			if ("collection_default".equals(typeId)) {
				addPropertyString(result, typeId, filter, PropertyIds.PATH, "/");
			} else {
				addPropertyString(result, typeId, filter, PropertyIds.PATH, path);
			}

			if (record.getParentId() != null) {
				addPropertyString(result, typeId, filter, PropertyIds.PARENT_ID, record.getParentId());
			} else if (path != null) {
				// The principal path is used here, also.
				addPropertyString(result, typeId, filter, PropertyIds.PARENT_ID, path.split("/")[path.split("/").length - 2]);
			}
			addPropertiesForMetadatas(record, orgfilter, typeId, result);

			addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);

			return result;
		} catch (CmisBaseException cbe) {
			throw cbe;
		} catch (Exception e) {
			throw new CmisExceptions_Runtime(e.getMessage(), e);
		}
	}

	private void addPropertiesForMetadatas(Record record, Set<String> orgfilter, String typeId, PropertiesImpl result) {
		for (Metadata metadata : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection())
				.getSchema(record.getSchemaCode()).getMetadatas()) {
			Object value = record.get(metadata);
			String propertyCode = metadata.getCode();
			if (value != null) {
				if (!metadata.isMultivalue()) {
					addPropertyForSingleValueMetadata(orgfilter, typeId, result, metadata, value, propertyCode);
				} else {
					addPropertyForMultiValueMetadata(orgfilter, typeId, result, metadata, value, propertyCode);
				}
			}
		}
	}

	private void setupObjectInfo(ObjectInfoImpl objectInfo, String typeId) {
		objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
		objectInfo.setTypeId(typeId);
		objectInfo.setContentType(null);
		objectInfo.setFileName(null);
		objectInfo.setHasAcl(false);
		objectInfo.setHasContent(false);
		objectInfo.setVersionSeriesId(null);
		objectInfo.setIsCurrentVersion(true);
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

	private void addPropertyForSingleValueMetadata(Set<String> orgfilter, String typeId, PropertiesImpl result, Metadata metadata,
			Object value, String propertyCode) {
		if (metadata.getType().isStringOrText()) {
			addPropertyString(result, typeId, orgfilter, propertyCode, (String) value);
		} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
			addPropertyBoolean(result, typeId, orgfilter, propertyCode, (Boolean) value);
		} else if (metadata.getType() == MetadataValueType.NUMBER) {
			addPropertyDouble(result, typeId, orgfilter, propertyCode, (Double) value);
		} else if (metadata.getType() == MetadataValueType.DATE_TIME) {
			GregorianCalendar calendarValue = getGregorianCalendar(value);
			addPropertyDateTime(result, typeId, orgfilter, propertyCode, calendarValue);
		} else if (metadata.getType() == MetadataValueType.CONTENT) {
			addPropertyString(result, typeId, orgfilter, propertyCode, ((Content) value).getId());
		}
	}

	private GregorianCalendar getGregorianCalendar(Object value) {
		if (value != null) {
			return ((LocalDateTime) value).toDateTime().toGregorianCalendar();
		} else {
			return null;
		}
	}

	private void addPropertyForMultiValueMetadata(Set<String> orgfilter, String typeId, PropertiesImpl result, Metadata metadata,
			Object value, String propertyCode) {
		if (!((List) value).isEmpty()) {
			if (metadata.getType().isStringOrText()) {
				addPropertyListString(result, typeId, orgfilter, propertyCode, (List<String>) value);
			} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
				addPropertyListBoolean(result, typeId, orgfilter, propertyCode, (List<Boolean>) value);
			} else if (metadata.getType() == MetadataValueType.NUMBER) {
				addPropertyListInteger(result, typeId, orgfilter, propertyCode, (List<Long>) value);
			} else if (metadata.getType() == MetadataValueType.DATE_TIME) {
				List<GregorianCalendar> calendarValues = new ArrayList<>();
				for (DateTime date : (List<DateTime>) value) {
					calendarValues.add(date.toGregorianCalendar());
				}
				addPropertyListDateTime(result, typeId, orgfilter, propertyCode, calendarValues);
			}
		}
	}

	private void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyIdList(PropertiesImpl props, String typeId, Set<String> filter, String id, List<String> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyStringImpl(id, value));
	}

	private void addPropertyListString(PropertiesImpl props, String typeId, Set<String> filter, String id, List<String> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyStringImpl(id, value));
	}

	private void addPropertyDouble(PropertiesImpl props, String typeId, Set<String> filter, String id, Double value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}
		BigDecimal bigDecimalValue = new BigDecimal(value, MathContext.DECIMAL64);
		props.addProperty(new PropertyDecimalImpl(id, bigDecimalValue));
	}

	private void addPropertyListInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, List<Long> values) {
		List<BigInteger> convertedValues = new ArrayList<>();
		for (Long value : values) {
			convertedValues.add(BigInteger.valueOf(value));
		}

		addPropertyListBigInteger(props, typeId, filter, id, convertedValues);
	}

	private void addPropertyListBigInteger(PropertiesImpl props, String typeId, Set<String> filter, String id,
			List<BigInteger> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIntegerImpl(id, value));
	}

	private void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyBooleanImpl(id, value));
	}

	private void addPropertyListBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, List<Boolean> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyBooleanImpl(id, value));
	}

	private void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
			GregorianCalendar value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyDateTimeImpl(id, value));
	}

	private void addPropertyListDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
			List<GregorianCalendar> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyDateTimeImpl(id, value));
	}

	private boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
		if ((properties == null) || (properties.getProperties() == null)) {
			throw new IllegalArgumentException("Properties must not be null!");
		}

		if (id == null) {
			throw new IllegalArgumentException("Id must not be null!");
		}

		TypeDefinition type = repository.getTypeDefinitionsManager().getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new IllegalArgumentException("Unknown type: " + typeId);
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
