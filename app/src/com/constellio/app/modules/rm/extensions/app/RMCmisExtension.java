package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.cmis.builders.object.AllowableActionsBuilder;
import com.constellio.app.api.cmis.builders.object.PropertiesBuilder;
import com.constellio.app.extensions.api.cmis.CmisExtension;
import com.constellio.app.extensions.api.cmis.params.BuildAllowableActionsParams;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.extensions.api.cmis.params.CheckInParams;
import com.constellio.app.extensions.api.cmis.params.CheckOutParams;
import com.constellio.app.extensions.api.cmis.params.GetObjectParams;
import com.constellio.app.extensions.api.cmis.params.IsSchemaTypeSupportedParams;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.joda.time.LocalDateTime;

import static com.constellio.app.api.cmis.utils.CmisRecordUtils.toGregorianCalendar;
import static org.apache.chemistry.opencmis.commons.PropertyIds.CREATION_DATE;
import static org.apache.chemistry.opencmis.commons.PropertyIds.LAST_MODIFICATION_DATE;

public class RMCmisExtension extends CmisExtension {

	RMSchemasRecordsServices rm;
	LoggingServices loggingServices;
	TaxonomiesManager taxonomiesManager;
	RMConfigs configs;

	public RMCmisExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.loggingServices = new LoggingServices(appLayerFactory.getModelLayerFactory());
		this.taxonomiesManager = appLayerFactory.getModelLayerFactory().getTaxonomiesManager();
		configs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	@Override
	public void buildCMISObjectFromConstellioRecord(BuildCmisObjectFromConstellioRecordParams params) {
		Record record = params.getRecord();
		PropertiesBuilder propertiesBuilder = params.getPropertiesBuilder();

		if (record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE) || record.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			RMObject rmObject = rm.wrapRMObject(record);
			if (rmObject.getFormCreatedOn() != null) {
				propertiesBuilder.addPropertyDateTime(CREATION_DATE, toGregorianCalendar(rmObject.getFormCreatedOn()));
			}
			if (rmObject.getFormModifiedOn() != null) {
				propertiesBuilder.addPropertyDateTime(LAST_MODIFICATION_DATE, toGregorianCalendar(rmObject.getFormModifiedOn()));
			}
		}

	}

	@Override
	public void buildConstellioRecordFromCmisObject(BuildConstellioRecordFromCmisObjectParams params) {
		Record record = params.getRecord();

		LocalDateTime now = TimeProvider.getLocalDateTime();
		if (record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE) || record.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			RMObject rmObject = rm.wrapRMObject(record);
			if (rmObject.getFormCreatedOn() == null) {
				rmObject.setCreatedOn(now);
				rmObject.setFormCreatedOn(now);
			}
			if (rmObject.getFormModifiedOn() == null) {
				rmObject.setModifiedOn(now);
				rmObject.setFormModifiedOn(now);
			}
		}
	}

	@Override
	public void buildAllowableActions(BuildAllowableActionsParams params) {
		User user = params.getUser();
		Record record = params.getRecord();

		if (user.hasWriteAccess().on(record) && user.hasDeleteAccess().on(record)
			&& user.has(RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS).on(record)
			&& record.getTypeCode().equals(Folder.SCHEMA_TYPE)) {
			params.getActions().addAll(AllowableActionsBuilder.MANAGE_SECURITY_ACTIONS);
		}

		if (user.hasWriteAccess().on(record) && user.hasDeleteAccess().on(record)
			&& user.has(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS).on(record)
			&& record.getTypeCode().equals(Document.SCHEMA_TYPE)) {
			params.getActions().addAll(AllowableActionsBuilder.MANAGE_SECURITY_ACTIONS);
		}

	}

	@Override
	public void onGetObject(GetObjectParams params) {
		if (configs.isLoggingFolderDocumentAccessWithCMISEnable() && (params.isOfType(Folder.SCHEMA_TYPE) || params.isOfType(Document.SCHEMA_TYPE))) {
			loggingServices.logRecordView(params.getRecord(), params.getUser());
		}
	}
	//
	//	@Override
	//	public void onCreateCMISFolder(CreateFolderParams params) {
	//
	//	}
	//
	//	@Override
	//	public void onCreateCMISDocument(CreateDocumentParams params) {
	//		super.onCreateCMISDocument(params);
	//	}
	//
	//	@Override
	//	public void onUpdateCMISFolder(UpdateFolderParams params) {
	//		super.onUpdateCMISFolder(params);
	//	}
	//
	//	@Override
	//	public void onUpdateCMISDocument(UpdateDocumentParams params) {
	//		super.onUpdateCMISDocument(params);
	//	}

	@Override
	public void onCheckIn(CheckInParams params) {
		if (params.isOfType(Document.SCHEMA_TYPE)) {
			loggingServices.borrowRecord(params.getRecord(), params.getUser());
		}
	}

	@Override
	public void onCheckOut(CheckOutParams params) {
		if (params.isOfType(Document.SCHEMA_TYPE)) {
			loggingServices.returnRecord(params.getRecord(), params.getUser());
		}
	}

	//	@Override
	//	public void onDeleteContent(DeleteContentParams params) {
	//
	//	}

	@Override
	public ExtensionBooleanResult isSchemaTypeSupported(IsSchemaTypeSupportedParams params) {
		String schemaType = params.getSchemaType().getCode();

		if (Folder.SCHEMA_TYPE.equals(schemaType)
			|| Document.SCHEMA_TYPE.equals(schemaType)
			|| ContainerRecord.SCHEMA_TYPE.equals(schemaType)) {
			return ExtensionBooleanResult.FORCE_TRUE;
		} else {
			return ExtensionBooleanResult.NOT_APPLICABLE;
		}
	}

}
