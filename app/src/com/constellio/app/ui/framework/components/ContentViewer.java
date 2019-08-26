package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.framework.stream.DownloadStreamResource;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.app.ui.i18n.i18n.$;

public class ContentViewer extends VerticalLayout {
	final String contentId, contentName;

	public ContentViewer(String contentId, String contentName) {
		this.contentId = contentId;
		this.contentName = contentName;
		StreamSource source = getStreamFromContent();

		Embedded viewer = new Embedded();
		StreamResource streamResource = new StreamResource(source, contentName);
		streamResource.setCacheTime(0);
		viewer.setSource(streamResource);
		viewer.setType(Embedded.TYPE_BROWSER);
		viewer.setWidth("100%");
		viewer.setHeight("1024px");

		Link download = new Link($("ReportViewer.download", contentName),
				new DownloadStreamResource(source, contentName, DownloadStreamResource.getMimeTypeFromFileName(contentName)));

		addComponents(download, viewer);
		setWidth("100%");
	}

	private StreamSource getStreamFromContent() {
		return new StreamSource() {
			@Override
			public InputStream getStream() {
				ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
				ContentManager contentManager = modelLayerFactory.getContentManager();
				InputStream inputStream = contentManager
						.getContentInputStream(contentId, contentName);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try {
					IOUtils.copy(inputStream, output);
					return new ByteArrayInputStream(output.toByteArray());
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					IOUtils.closeQuietly(inputStream);
				}
			}
		};

	}
}
