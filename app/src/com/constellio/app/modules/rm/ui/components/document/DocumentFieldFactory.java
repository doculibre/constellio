package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.modules.rm.ui.components.RMRecordFieldFactory;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentContentFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentCopyRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.document.fields.DocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.wrappers.Document.CONTENT;
import static com.constellio.app.modules.rm.wrappers.Document.FOLDER;
import static com.constellio.app.modules.rm.wrappers.Document.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Document.TYPE;

public class DocumentFieldFactory extends RMRecordFieldFactory {

	private String folderId;
	private String currentType;
	private List<CopyRetentionRuleInRule> copyRules;
	private boolean isViewOnly;

	public DocumentFieldFactory(String folderId, String currentType, List<CopyRetentionRuleInRule> copyRules,
								boolean isViewOnly) {
		this.folderId = folderId;
		this.currentType = currentType;
		this.copyRules = copyRules;
		this.isViewOnly = isViewOnly;
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		Field<?> field;
		switch (metadataVO.getLocalCode()) {
			case TYPE:
				field = new DocumentTypeFieldLookupImpl(folderId, currentType);
				break;
			case CONTENT:
				field = new DocumentContentFieldImpl(isViewOnly, recordVO != null ? recordVO.getId() : null);
				break;
			case FOLDER:
				field = new DocumentFolderFieldImpl();
				break;
			case MAIN_COPY_RULE_ID_ENTERED:
				field = new DocumentCopyRuleFieldImpl(copyRules);
				break;
			default:
				field = super.build(recordVO, metadataVO, locale);
		}
		if (field instanceof CustomDocumentField) {
			postBuild(field, recordVO, metadataVO);
		} else {
			callPostBuildExtensions(field, recordVO, metadataVO);
		}
		return field;
	}

	@Override
	protected void postBuild(Field<?> field, RecordVO recordVO, MetadataVO metadataVO) {
		super.postBuild(field, recordVO, metadataVO);

		String schemaCode = metadataVO.getSchema().getCode();
		if (Email.SCHEMA.equals(schemaCode)) {
			List<String> readOnlyMetadataCodes = Arrays.asList(Email.EMAIL_ATTACHMENTS_LIST, Email.EMAIL_BCC_TO,
					Email.EMAIL_CC_TO, Email.EMAIL_FROM, Email.EMAIL_IN_NAME_OF, Email.EMAIL_OBJECT,
					Email.EMAIL_RECEIVED_ON, Email.EMAIL_RECEIVED_ON, Email.EMAIL_SENT_ON, Email.EMAIL_TO);
			String metadataCode = metadataVO.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (readOnlyMetadataCodes.contains(metadataCodeWithoutPrefix)) {
				field.setEnabled(false);
			}
		}
	}
}
