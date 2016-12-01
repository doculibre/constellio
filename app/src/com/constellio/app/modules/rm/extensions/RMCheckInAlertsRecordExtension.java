package com.constellio.app.modules.rm.extensions;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
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

public class RMCheckInAlertsRecordExtension extends RecordExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(RMCheckInAlertsRecordExtension.class);

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rmSchemasRecordsServices;

	MetadataSchemasManager metadataSchemasManager;

	RecordServices recordServices;

	ConstellioEIMConfigs eimConfigs;

	public RMCheckInAlertsRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		if (event.isSchemaType(Folder.SCHEMA_TYPE) && event.hasModifiedMetadata(Folder.BORROWED)) {
			alertUsers(Folder.SCHEMA_TYPE, event.getRecord());
		} else if (event.isSchemaType(Document.SCHEMA_TYPE) && event.hasModifiedMetadata(Document.CONTENT)) {
			Content content = event.getRecord().get(rmSchemasRecordsServices.documentContent());
			if (content != null && content.getCheckoutUserId() == null) {
				alertUsers(Document.SCHEMA_TYPE, event.getRecord());
			}
		}
		super.recordModified(event);
	}

	private void alertUsers(String schemaType, Record record) {
		try {
			RMObject rmObject;
			String displayURL;
			if (schemaType.equals(Folder.SCHEMA_TYPE)) {
				rmObject = rmSchemasRecordsServices.wrapFolder(record);
				displayURL = RMNavigationConfiguration.DISPLAY_FOLDER;
			} else if (schemaType.equals(Document.SCHEMA_TYPE)) {
				rmObject = rmSchemasRecordsServices.wrapDocument(record);
				displayURL = RMNavigationConfiguration.DISPLAY_DOCUMENT;
			} else {
				throw new UnsupportedOperationException("Invalid schemaType");
			}
			if (rmObject.getAlertUsersWhenAvailable().isEmpty()) {
				return;
			} else {

				Transaction transaction = new Transaction();

				for (String userId : rmObject.getAlertUsersWhenAvailable()) {
					EmailToSend emailToSend = newEmailToSend();
					User user = rmSchemasRecordsServices.getUser(userId);
					EmailAddress toAddress = new EmailAddress(user.getTitle(), user.getEmail());

					LocalDateTime returnDate = TimeProvider.getLocalDateTime();
					emailToSend.setTo(toAddress);
					emailToSend.setSendOn(returnDate);
                    final String subject = $("RMObject.alertWhenAvailableSubject", $("AddEditTaxonomyView.classifiedObject." + schemaType).toLowerCase()) + ": " + record.getTitle();
					emailToSend.setSubject(subject);
					emailToSend.setTemplate(RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
					List<String> parameters = new ArrayList<>();
					parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
					parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + returnDate.toString("yyyy-MM-dd  HH:mm:ss"));
					String rmObjectTitle = rmObject.getTitle();
					parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + rmObjectTitle);
					String constellioUrl = eimConfigs.getConstellioUrl();
					parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
					parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record.getId());
					parameters.add("recordType" + EmailToSend.PARAMETER_SEPARATOR + $("AddEditTaxonomyView.classifiedObject." + schemaType).toLowerCase());
					emailToSend.setParameters(parameters);
					transaction.add(emailToSend);
				}
				transaction.add(rmObject.setAlertUsersWhenAvailable(new ArrayList<String>()));
				recordServices.execute(transaction);
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
