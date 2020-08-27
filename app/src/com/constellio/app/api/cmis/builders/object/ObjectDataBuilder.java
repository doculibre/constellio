package com.constellio.app.api.cmis.builders.object;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_Runtime;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.global.ConstellioCmisContextParameters;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.app.api.cmis.utils.CmisRecordUtils.toGregorianCalendar;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.CMIS_NEVER_RETURN_ACL;
import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_GET_ACL;

public class ObjectDataBuilder {

	private static final String USER_UNKNOWN = "<unknown>";

	private final ConstellioCollectionRepository repository;
	private final AppLayerFactory appLayerFactory;
	private final ModelLayerFactory modelLayerFactory;
	private final SchemasRecordsServices schemas;
	private final CallContext context;
	private final String taxonomyPath = null;
	private final AllowableActionsBuilder allowableActionsBuilder;
	private final User user;

	public ObjectDataBuilder(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
							 CallContext context) {
		this.repository = repository;
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.context = context;
		this.schemas = new SchemasRecordsServices(repository.getCollection(), modelLayerFactory);
		this.allowableActionsBuilder = new AllowableActionsBuilder(repository, appLayerFactory, context);
		this.user = (User) context.get(ConstellioCmisContextParameters.USER);
	}

	public ObjectData build(Record record, Set<String> filter, boolean includeAllowableActions,
							boolean includeAcl, ObjectInfoHandler objectInfos) {
		ObjectDataImpl result = new ObjectDataImpl();
		ObjectInfoImpl objectInfo = new ObjectInfoImpl();

		String typeId = record.getSchemaCode();
		Set<String> filterSet = (filter == null ? null : new HashSet<String>(filter));
		PropertiesBuilder propertiesBuilder = new PropertiesBuilder(repository, typeId, filterSet);

		if (result != null) {
			result.setProperties(compileProperties(record, objectInfo, propertiesBuilder, typeId));
		}

		if (includeAllowableActions) {
			result.setAllowableActions(allowableActionsBuilder.build(record));
		}

		callExtensions(result, propertiesBuilder, record);

		boolean neverReturlACL = modelLayerFactory.getSystemConfigurationsManager().getValue(CMIS_NEVER_RETURN_ACL);
		if (includeAcl && !neverReturlACL && allowableActionsBuilder.build(record).getAllowableActions().contains(CAN_GET_ACL)) {
			result.setAcl(new AclBuilder(repository, modelLayerFactory).build(record));
			result.setIsExactAcl(true);
		}

		if (context.isObjectInfoRequired()) {
			objectInfo.setObject(result);
			objectInfos.addObjectInfo(objectInfo);
		}

		return result;
	}

	private void callExtensions(ObjectDataImpl result, PropertiesBuilder propertiesBuilder, Record record) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollectionOf(record);
		extensions.buildCMISObjectFromConstellioRecord(
				new BuildCmisObjectFromConstellioRecordParams(result, propertiesBuilder, record));
	}

	private Properties compileProperties(Record record, ObjectInfoImpl objectInfo,
										 PropertiesBuilder propertiesBuilder, String typeId) {
		if (record == null) {
			throw new IllegalArgumentException("Record must not be null!");
		}

		boolean readAccess = user.hasReadAccess().on(record);

		setupObjectInfo(objectInfo, typeId);

		try {

			String id = record.getId();
			propertiesBuilder.addPropertyId(PropertyIds.OBJECT_ID, id);
			objectInfo.setId(id);

			String name = record.get(Schemas.TITLE);
			if (name == null && record.getSchemaCode().startsWith("collection_")) {
				name = schemas.wrapCollection(record).getName();
			}
			propertiesBuilder.addPropertyString(PropertyIds.NAME, name);
			objectInfo.setName(name);

			propertiesBuilder.addPropertyString(PropertyIds.CREATED_BY,
					(String) record.get(Schemas.CREATED_BY));
			propertiesBuilder.addPropertyString(PropertyIds.LAST_MODIFIED_BY,
					(String) record.get(Schemas.MODIFIED_BY));
			objectInfo.setCreatedBy(USER_UNKNOWN);

			GregorianCalendar creationDate = toGregorianCalendar(record.get(Schemas.CREATED_ON));
			GregorianCalendar lastModified = toGregorianCalendar(record.get(Schemas.MODIFIED_ON));
			propertiesBuilder.addPropertyDateTime(PropertyIds.CREATION_DATE, creationDate);
			propertiesBuilder.addPropertyDateTime(PropertyIds.LAST_MODIFICATION_DATE, lastModified);
			objectInfo.setCreationDate(creationDate);
			objectInfo.setLastModificationDate(lastModified);

			propertiesBuilder.addPropertyString(PropertyIds.CHANGE_TOKEN,
					Long.toString(record.getVersion()));

			propertiesBuilder.addPropertyId(PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
			propertiesBuilder.addPropertyId(PropertyIds.OBJECT_TYPE_ID, typeId);

			// The principal path is always used for now
			String path = record.get(Schemas.PRINCIPAL_PATH);
			List<String> paths = record.getList(Schemas.PATH);
			if (path == null && !paths.isEmpty()) {
				path = paths.get(0);
			}

			if (path != null) {
				if ("collection_default".equals(typeId)) {
					propertiesBuilder.addPropertyString(PropertyIds.PATH, "/");
				} else {
					path = "/taxo_" + path.substring(1);
					propertiesBuilder.addPropertyString(PropertyIds.PATH, path);
				}
			}

			MetadataSchema schema = schemas.getSchemaOf(record);
			if (record.getParentId(schema) != null) {
				propertiesBuilder.addPropertyString(PropertyIds.PARENT_ID, record.getParentId(schema));
			} else if (path != null) {
				// The principal path is used here, also.
				propertiesBuilder.addPropertyString(PropertyIds.PARENT_ID,
						path.split("/")[path.split("/").length - 2]);
			}
			if (readAccess) {
				addPropertiesForMetadatas(record, propertiesBuilder);
			}

			propertiesBuilder.addPropertyIdList(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);

			return propertiesBuilder.getBuiltProperties();
		} catch (CmisBaseException cbe) {
			throw cbe;
		} catch (Exception e) {
			throw new CmisExceptions_Runtime(e.getMessage(), e);
		}
	}

	private void addPropertiesForMetadatas(Record record, PropertiesBuilder propertiesBuilder) {
		SchemaUtils schemaUtils = new SchemaUtils();
		for (Metadata metadata : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection())
				.getSchema(record.getSchemaCode()).getMetadatas()) {
			Object value = record.get(metadata);
			String propertyCode = schemaUtils.getLocalCodeFromMetadataCode(metadata.getCode());
			if (value != null) {
				if (!metadata.isMultivalue()) {
					propertiesBuilder.addPropertyForSingleValueMetadata(metadata, value, propertyCode);
				} else {
					propertiesBuilder.addPropertyForMultiValueMetadata(metadata, value, propertyCode);
				}
			}
		}
	}

	private void setupObjectInfo(ObjectInfoImpl objectInfo, String typeId) {
		MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(repository.getCollection())
				.getSchemaType(new SchemaUtils().getSchemaTypeCode(typeId));

		objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
		objectInfo.setTypeId(typeId);
		objectInfo.setContentType(null);
		objectInfo.setFileName(null);
		objectInfo.setHasAcl(type.hasSecurity());
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

		if ("collection_default".equals(typeId)) {
			objectInfo.setHasParent(false);
		}
	}

}
