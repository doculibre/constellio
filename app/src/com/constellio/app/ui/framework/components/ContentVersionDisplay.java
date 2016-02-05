package com.constellio.app.ui.framework.components;

import java.util.List;

import com.constellio.app.api.extensions.DownloadContentVersionLinkExtension;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

public class ContentVersionDisplay extends CustomComponent {

	public ContentVersionDisplay(RecordVO recordVO, MetadataVO metadataVO) {
		this(recordVO, (ContentVersionVO) recordVO.get(metadataVO));
	}

	public ContentVersionDisplay(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		this(recordVO, contentVersionVO, contentVersionVO.toString());
	}

	public ContentVersionDisplay(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption) {
		// TODO Remove singleton use
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<DownloadContentVersionLinkExtension> downloadContentVersionLinkExtensions = extensions.downloadContentVersionLinkExtensions;
		Component downloadLink = null;
		if (downloadContentVersionLinkExtensions != null) {
			for (DownloadContentVersionLinkExtension extension : downloadContentVersionLinkExtensions) {
				downloadLink = extension.getDownloadLink(recordVO, contentVersionVO, caption);
				if (downloadLink != null) {
					break;
				}
			}
		}
		if (downloadLink == null) {
			downloadLink = new DownloadContentVersionLink(contentVersionVO, caption);
		}
		setCompositionRoot(downloadLink);
	}

}
