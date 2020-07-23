package com.constellio.app.services.extensions.core;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.components.fields.SignatureRecordField;
import com.constellio.app.ui.framework.components.fields.upload.ContentVersionUploadField;
import com.constellio.app.ui.pages.profile.ModifyProfileView;
import com.constellio.app.ui.util.MessageUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

@Slf4j
public class CoreUserProfileSignatureFieldsExtension extends PagesComponentsExtension {
	String collection;
	AppLayerFactory appLayerFactory;

	public CoreUserProfileSignatureFieldsExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public List<SignatureRecordField> getSignatureFields(RecordFieldsExtensionParams params) {
		ArrayList<SignatureRecordField> signatureFields = new ArrayList<>();
		if (params.getMainComponent() instanceof ModifyProfileView) {
			SignatureRecordField electronicSignatureField =
					buildElectronicSignatureField(params, getUserCredentialMetadata(UserCredential.ELECTRONIC_SIGNATURE));
			SignatureRecordField electronicInitialsField =
					buildElectronicSignatureField(params, getUserCredentialMetadata(UserCredential.ELECTRONIC_INITIALS));
			signatureFields.addAll(asList(electronicSignatureField, electronicInitialsField));
		}
		return signatureFields;
	}

	private Metadata getUserCredentialMetadata(String localCode) {
		MetadataSchemasManager schemaManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema userCredentialSchema = schemaManager.getSchemaTypes(Collection.SYSTEM_COLLECTION).getSchema(UserCredential.DEFAULT_SCHEMA);
		return userCredentialSchema.getMetadata(localCode);
	}

	private SignatureRecordField buildElectronicSignatureField(RecordFieldsExtensionParams params, Metadata metadata) {
		User user = new SchemasRecordsServices(collection, appLayerFactory.getModelLayerFactory()).wrapUser(params.getRecord());
		//OK
		UserCredential userCredentials = appLayerFactory.getModelLayerFactory().newUserServices().getUserConfigs(user.getUsername());
		Language language = Language.withCode(params.getMainComponent().getSessionContext().getCurrentLocale().getLanguage());

		ElectronicSignatureField field = new ElectronicSignatureField(user, userCredentials.getId(), metadata, language);
		field.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value != null) {
					ContentVersionVO contentVersionVO = (ContentVersionVO) value;
					if (!contentVersionVO.getMimeType().contains("image")) {
						throw new InvalidValueException($("invalid image"));
					}
				}
			}
		});

		Content currentValue = userCredentials.get(metadata);
		if (currentValue != null) {
			ContentVersionToVOBuilder builder = new ContentVersionToVOBuilder(appLayerFactory.getModelLayerFactory());
			ContentVersionVO version = builder.build(currentValue, currentValue.getCurrentVersion());
			field.setValue(version);
		}

		return field;
	}

	private class ElectronicSignatureField extends ContentVersionUploadField implements SignatureRecordField<Object> {
		private final static String CHANGE_SIGNATURE_STREAM = "ElectronicSignatureField_Change_";

		private User user;
		private Metadata metadata;

		public ElectronicSignatureField(User user, String recordId, Metadata metadata, Language language) {
			super(recordId, metadata.getCode());

			this.user = user;
			this.metadata = metadata;

			setCaption(metadata.getLabel(language));
		}

		@Override
		public String getMetadataLocalCode() {
			return metadata.getLocalCode();
		}

		@Override
		public Content getCommittableValue() {
			ContentVersionVO value = (ContentVersionVO) getValue();
			if (value == null) {
				return null;
			}

			try {
				validate();
			} catch (InvalidValueException e) {
				log.warn(MessageUtils.toMessage(e));
				return null;
			}

			InputStream stream = value.getInputStreamProvider().getInputStream(CHANGE_SIGNATURE_STREAM + getMetadataLocalCode());
			ContentManager contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
			ContentVersionDataSummary contentVersion =
					contentManager.upload(stream, value.getFileName()).getContentVersionDataSummary();

			return contentManager.createMajor(user, value.getFileName(), contentVersion);
		}
	}
}
