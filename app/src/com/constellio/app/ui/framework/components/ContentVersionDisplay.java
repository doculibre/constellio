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
import com.constellio.app.ui.framework.components.content.UpdatableContentVersionPresenter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class ContentVersionDisplay extends CustomComponent {

	public ContentVersionDisplay(RecordVO recordVO, MetadataVO metadataVO, UpdatableContentVersionPresenter presenter) {
		this(recordVO, (ContentVersionVO) recordVO.get(metadataVO), presenter);
	}

	public ContentVersionDisplay(RecordVO recordVO, ContentVersionVO contentVersionVO, UpdatableContentVersionPresenter presenter) {
		this(recordVO, contentVersionVO, contentVersionVO.toString(), presenter);
	}

	public ContentVersionDisplay(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption, UpdatableContentVersionPresenter presenter) {
		// TODO Remove singleton use
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		AppLayerFactory appLayerFactory = constellioFactories.getAppLayerFactory();

		String collection = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		List<DownloadContentVersionLinkExtension> downloadContentVersionLinkExtensions = extensions.downloadContentVersionLinkExtensions;
		Component downloadLink = null;
		if (downloadContentVersionLinkExtensions != null) {
			for (DownloadContentVersionLinkExtension extension : downloadContentVersionLinkExtensions) {
				downloadLink = extension.getDownloadLink(recordVO, contentVersionVO, caption, presenter);
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
