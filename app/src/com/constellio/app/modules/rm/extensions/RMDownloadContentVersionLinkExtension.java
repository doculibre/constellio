package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.DownloadContentVersionLinkExtension;
import com.constellio.app.modules.rm.ui.components.content.ConstellioAgentLink;
import com.constellio.app.modules.rm.ui.util.ConstellioAgentUtils;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.content.UpdatableContentVersionPresenter;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.vaadin.ui.Component;

public class RMDownloadContentVersionLinkExtension implements DownloadContentVersionLinkExtension {

	@Override
	public Component getDownloadLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption,
									 String metadataCode, UpdatableContentVersionPresenter presenter) {
		Component downloadLink;
		if (!isDocumentOrUserDocument(recordVO)) {
			// Do not enable agent for non-rm entities
			return null;
		}

		String agentURL = ConstellioAgentUtils.getAgentURL(recordVO, contentVersionVO);
		if (agentURL != null) {
			downloadLink = new ConstellioAgentLink(agentURL, recordVO, contentVersionVO, caption, true, presenter, metadataCode);
		} else {
			downloadLink = new DownloadContentVersionLink(recordVO, contentVersionVO, caption, presenter, metadataCode, false);
		}
		return downloadLink;
	}

	private boolean isDocumentOrUserDocument(RecordVO recordVO) {
		String schemaType = getSchemaType(recordVO.getSchema());
		return Document.SCHEMA_TYPE.equals(schemaType) || UserDocument.SCHEMA_TYPE.equals(schemaType);
	}

	private String getSchemaType(MetadataSchemaVO schema) {
		return schema.getCode().split("_")[0];
	}
}
