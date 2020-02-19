package com.constellio.app.modules.rm.services.menu.behaviors.ui;

import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

@Slf4j
public class SendReturnReminderEmailButton extends BaseButton {
	private RecordServices recordServices;
	private RMSchemasRecordsServices rm;
	private ConstellioEIMConfigs eimConfigs;
	private MetadataSchemaTypes schemaTypes;
	private BaseView view;

	private String schemaType;
	private Record borrowedRecord;
	private User borrower;
	private List<String> params;

	public SendReturnReminderEmailButton(String collection, AppLayerFactory appLayerFactory, BaseView view,
										 String schemaType,
										 Record borrowedRecord, User borrower, String previewReturnDate) {
		this.view = view;
		this.schemaType = schemaType;
		this.borrowedRecord = borrowedRecord;
		this.borrower = borrower;

		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		recordServices = modelLayerFactory.newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		eimConfigs = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager());
		schemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);

		buildParameters(previewReturnDate);
		setCaption($("SendReturnReminderEmailButton.reminderReturn"));
	}

	private void buildParameters(String previewReturnDate) {
		params = new ArrayList<>();
		params.add("borrowedRecordType" + EmailToSend.PARAMETER_SEPARATOR + getBorrowedRecordType());
		params.add("previewReturnDate" + EmailToSend.PARAMETER_SEPARATOR + previewReturnDate);
		params.add("borrower" + EmailToSend.PARAMETER_SEPARATOR + borrower.getUsername());

		params.add("borrowedRecordTitle" + EmailToSend.PARAMETER_SEPARATOR + borrowedRecord.getTitle());
		boolean isAddingRecordIdInEmails = eimConfigs.isAddingRecordIdInEmails();
		if (isAddingRecordIdInEmails) {
			params.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("SendReturnReminderEmailButton.reminderReturn")
					   + " \"" + borrowedRecord.getTitle() + "\" (" + borrowedRecord.getId() + ")");
		} else {
			params.add("title" + EmailToSend.PARAMETER_SEPARATOR + $("SendReturnReminderEmailButton.reminderReturn")
					   + " \"" + borrowedRecord.getTitle() + "\"");
		}

		String constellioUrl = eimConfigs.getConstellioUrl();
		params.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
		params.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + getBorrowedRecordNavConfig()
				   + "/" + borrowedRecord.getId());
	}

	private String getBorrowedRecordType() {
		switch (schemaType) {
			case Document.SCHEMA_TYPE:
				return $("SendReturnReminderEmailButton.document");
			case Folder.SCHEMA_TYPE:
				return $("SendReturnReminderEmailButton.folder");
			case ContainerRecord.SCHEMA_TYPE:
				return $("SendReturnReminderEmailButton.container");
		}

		return null;
	}

	private String getBorrowedRecordNavConfig() {
		switch (schemaType) {
			case Document.SCHEMA_TYPE:
				return RMNavigationConfiguration.DISPLAY_DOCUMENT;
			case Folder.SCHEMA_TYPE:
				return RMNavigationConfiguration.DISPLAY_FOLDER;
			case ContainerRecord.SCHEMA_TYPE:
				return RMNavigationConfiguration.DISPLAY_CONTAINER;
		}

		return null;
	}

	@Override
	protected void buttonClick(ClickEvent event) {
		try {
			EmailToSend emailToSend = newEmailToSend();

			EmailAddress borrowerAddress = new EmailAddress(borrower.getTitle(), borrower.getEmail());
			emailToSend.setTo(Arrays.asList(borrowerAddress));
			emailToSend.setSendOn(TimeProvider.getLocalDateTime());
			emailToSend.setSubject($("SendReturnReminderEmailButton.reminderReturn") + borrowedRecord.getTitle());
			emailToSend.setTemplate(RMEmailTemplateConstants.REMIND_BORROW_TEMPLATE_ID);

			emailToSend.setParameters(params);

			recordServices.add(emailToSend);
			view.showMessage($("SendReturnReminderEmailButton.reminderEmailSent"));
		} catch (RecordServicesException e) {
			log.error("SendReturnReminderEmailButton.cannotSendEmail", e);
			view.showMessage($("SendReturnReminderEmailButton.cannotSendEmail"));
		}
	}

	private EmailToSend newEmailToSend() {
		MetadataSchema schema = schemaTypes.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, schemaTypes);
	}
}