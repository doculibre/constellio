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
package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class RMCheckInAlertsRecordExtension extends RecordExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(RMCheckInAlertsRecordExtension.class);

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rmSchemasRecordsServices;

	SearchServices searchServices;

	MetadataSchemasManager metadataSchemasManager;

	RecordServices recordServices;

	public RMCheckInAlertsRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE) && event.hasModifiedMetadata(Folder.BORROWED)) {
			alertUsers(Folder.SCHEMA_TYPE, event.getRecord());
		} else if (event.isSchemaType(Document.SCHEMA_TYPE) && event.hasModifiedMetadata(Document.CONTENT)) {
			alertUsers(Document.SCHEMA_TYPE, event.getRecord());
		}
		super.recordModified(event);
	}

	private void alertUsers(String schemaType, Record record) {
		try {
			RMObject rmObject;
			if (schemaType.equals(Folder.SCHEMA_TYPE)) {
				rmObject = rmSchemasRecordsServices.wrapFolder(record);
			} else if (schemaType.equals(Document.SCHEMA_TYPE)) {
				rmObject = rmSchemasRecordsServices.wrapDocument(record);
			} else {
				throw new UnsupportedOperationException("Invalid schemaType");
			}
			if (rmObject.getAlertUsersWhenAvailable().isEmpty()) {
				return;
			} else {
				EmailToSend emailToSend = newEmailToSend();
				List<EmailAddress> emailAddressesTo = new ArrayList<>();

				for (String userId : rmObject.getAlertUsersWhenAvailable()) {
					User user = rmSchemasRecordsServices.getUser(userId);
					EmailAddress toAddress = new EmailAddress(user.getTitle(), user.getEmail());
					emailAddressesTo.add(toAddress);
				}
				LocalDateTime returnDate = TimeProvider.getLocalDateTime();
				emailToSend.setTo(emailAddressesTo);
				emailToSend.setSendOn(returnDate);
				emailToSend.setSubject($("RMObject.alertWhenAvailableSubject", schemaType) + " " + rmObject.getTitle());
				emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
				List<String> parameters = new ArrayList<>();
				parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + returnDate);
				String rmObjectTitle = rmObject.getTitle();
				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + rmObjectTitle);
				emailToSend.setParameters(parameters);
				recordServices.add(emailToSend);
			}
		} catch (RecordServicesException e) {
			LOGGER.error("Cannot alert users", e);
		}
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}
}
