package com.constellio.app.modules.rm.extensions.app;

import static com.constellio.app.api.cmis.utils.CmisRecordUtils.toGregorianCalendar;
import static org.apache.chemistry.opencmis.commons.PropertyIds.CREATION_DATE;
import static org.apache.chemistry.opencmis.commons.PropertyIds.LAST_MODIFICATION_DATE;

import org.joda.time.LocalDateTime;

import com.constellio.app.api.cmis.builders.object.PropertiesBuilder;
import com.constellio.app.extensions.api.cmis.CmisExtension;
import com.constellio.app.extensions.api.cmis.params.BuildCmisObjectFromConstellioRecordParams;
import com.constellio.app.extensions.api.cmis.params.BuildConstellioRecordFromCmisObjectParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;

public class RMCmisExtension extends CmisExtension {

	RMSchemasRecordsServices rm;

	public RMCmisExtension(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory());
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
}
