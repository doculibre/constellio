package com.constellio.app.modules.rm.ui.components.document;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Document.TYPE;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.ui.components.RMMetadataFieldFactory;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.Field;

public class DocumentFieldFactory extends RMMetadataFieldFactory {

	private String folderId;
	private String currentType;

	public DocumentFieldFactory(String folderId, String currentType) {
		this.folderId = folderId;
		this.currentType = currentType;
	}

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		if (TYPE.equals(metadataCode) || TYPE.equals(metadataCodeWithoutPrefix)) {
			field = new DocumentTypeFieldLookupImpl(folderId, currentType);
		} else if (CONTENT.equals(metadataCode) || CONTENT.equals(metadataCodeWithoutPrefix)) {
			field = new DocumentContentFieldImpl();
		} else if (FOLDER.equals(metadataCode) || FOLDER.equals(metadataCodeWithoutPrefix)) {
			field = new DocumentFolderFieldImpl();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof CustomDocumentField) {
			postBuild(field, metadata);
		}
		return field;
	}

	@Override
	protected void postBuild(Field<?> field, MetadataVO metadata) {
		super.postBuild(field, metadata);

		String schemaCode = metadata.getSchema().getCode();
		if (Email.SCHEMA.equals(schemaCode)) {
			List<String> readOnlyMetadataCodes = Arrays.asList(Email.EMAIL_ATTACHMENTS_LIST, Email.EMAIL_BCC_TO,
					Email.EMAIL_CC_TO, Email.EMAIL_CONTENT, Email.EMAIL_FROM, Email.EMAIL_IN_NAME_OF, Email.EMAIL_OBJECT,
					Email.EMAIL_RECEIVED_ON, Email.EMAIL_RECEIVED_ON, Email.EMAIL_SENT_ON, Email.EMAIL_TO);
			String metadataCode = metadata.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (readOnlyMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				field.setEnabled(false);
			}
		}
	}

}
