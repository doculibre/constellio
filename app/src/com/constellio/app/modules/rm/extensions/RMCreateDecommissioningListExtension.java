package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMCreateDecommissioningListExtension extends RecordExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(RMCreateDecommissioningListExtension.class);

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rmSchemasRecordsServices;

	MetadataSchemasManager metadataSchemasManager;

	RecordServices recordServices;

	ConstellioEIMConfigs eimConfigs;

	public RMCreateDecommissioningListExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	public void recordCreated(RecordCreationEvent event) {
		if (event.isSchemaType(DecommissioningList.SCHEMA_TYPE)) {
			alertUsers(event.getRecord());
		}
		super.recordCreated(event);
	}

	private void alertUsers(Record record) {
		if(Toggle.ALERT_USERS_EMAIL.isEnabled()){
			try {
				DecommissioningList decommissioningList = rmSchemasRecordsServices.wrapDecommissioningList(record);
				String displayURL = "";
				if(decommissioningList.getDecommissioningListType() != null) {
					switch (decommissioningList.getDecommissioningListType()) {
						case FOLDERS_TO_TRANSFER:
						case FOLDERS_TO_DESTROY:
						case FOLDERS_TO_DEPOSIT:
						case FOLDERS_TO_CLOSE:
							displayURL = RMNavigationConfiguration.DECOMMISSIONING_LIST_DISPLAY;
							break;
						default:
							displayURL = RMNavigationConfiguration.DOCUMENT_DECOMMISSIONING_LIST_DISPLAY;
							break;
					}
				}

				Transaction transaction = new Transaction();

				EmailToSend emailToSend = newEmailToSend();
				List<EmailAddress> emailAddresses = new ArrayList<>();

				AuthorizationsServices authServices = modelLayerFactory.newAuthorizationsServices();

				for (User user : authServices.getUsersWithPermissionOnRecord(RMPermissionsTo.PROCESS_DECOMMISSIONING_LIST, record)) {

					emailAddresses.add(new EmailAddress(user.getTitle(), user.getEmail()));

				}
				LocalDateTime creationDate = TimeProvider.getLocalDateTime();
				emailToSend.setTo(emailAddresses);
				emailToSend.setSendOn(creationDate);
				final String subject = $("RMObject.alertWhenAvailableSubject", record.getTitle());
				emailToSend.setSubject(subject);
				emailToSend.setTemplate(RMEmailTemplateConstants.DECOMMISSIONING_LIST_CREATION_TEMPLATE_ID);
				List<String> parameters = new ArrayList<>();
				parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + subject);
				parameters.add("returnDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(creationDate));
				String rmObjectTitle = decommissioningList.getTitle();
				parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + rmObjectTitle);
				String constellioUrl = eimConfigs.getConstellioUrl();
				parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
				parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record.getId());
				emailToSend.setParameters(parameters);
				transaction.add(emailToSend);

				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				LOGGER.error("Cannot alert users", e);
			}
		}
	}

	private String formatDateToParameter(LocalDateTime datetime) {
		if(datetime == null) {
			return "";
		}
		return datetime.toString("yyyy-MM-dd  HH:mm:ss");
	}

	private EmailToSend newEmailToSend() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}
}
