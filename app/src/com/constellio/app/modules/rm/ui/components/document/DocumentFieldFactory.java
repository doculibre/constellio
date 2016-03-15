package com.constellio.app.modules.rm.ui.components.document;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Document.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Document.TYPE;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.ui.components.RMMetadataFieldFactory;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentCopyRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.Field;

public class DocumentFieldFactory extends RMMetadataFieldFactory {
	private String folderId;
	private String currentType;
	private List<CopyRetentionRuleInRule> copyRules;

	public DocumentFieldFactory(String folderId, String currentType, List<CopyRetentionRuleInRule> copyRules) {
		this.folderId = folderId;
		this.currentType = currentType;
		this.copyRules = copyRules;
	}

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		switch (metadata.getLocalCode()) {
		case TYPE:
			field = new DocumentTypeFieldLookupImpl(folderId, currentType);
			break;
		case CONTENT:
			field = new DocumentContentFieldImpl();
			break;
		case FOLDER:
			field = new DocumentFolderFieldImpl();
			break;
		case MAIN_COPY_RULE_ID_ENTERED:
			field = new DocumentCopyRuleFieldImpl(copyRules);
			break;
		default:
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
