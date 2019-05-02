package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.ConnectorResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

public class DownloadButton extends Button {

	public static final String STYLE_NAME = "download-button";

	protected Resource downloadedResource;

	public DownloadButton(Resource downloadedResource, String caption) {
		super(caption, wrapForDownload(downloadedResource));
		this.downloadedResource = downloadedResource;
		addStyleName(STYLE_NAME);
		addStyleName(ValoTheme.BUTTON_LINK);

		if (downloadedResource instanceof ConnectorResource) {
			ConnectorResource connectorResource = (ConnectorResource) downloadedResource;
			String fileName = connectorResource.getFilename();
			Resource icon = FileIconUtils.getIcon(fileName);
			if (icon != null) {
				setIcon(icon);
			}
		}
	}

	public static Resource wrapForDownload(Resource downloadedResource) {
		Resource resourceOrWrapper;
		if (downloadedResource instanceof ConnectorResource) {
			final ConnectorResource adaptee = (ConnectorResource) downloadedResource;
			resourceOrWrapper = new ConnectorResource() {
				@Override
				public String getMIMEType() {
					return adaptee.getMIMEType();
				}

				@Override
				public DownloadStream getStream() {
					DownloadStream stream = adaptee.getStream();
					if (stream.getParameter("Content-Disposition") == null) {
						// Content-Disposition: attachment generally forces download
						stream.setParameter("Content-Disposition",
								"attachment; filename=\"" + stream.getFileName() + "\"");
						stream.setParameter("Cache-Control", "no-cache");
					}
					return stream;
				}

				@Override
				public String getFilename() {
					return adaptee.getFilename();
				}
			};
		} else {
			resourceOrWrapper = downloadedResource;
		}
		return resourceOrWrapper;
	}

}
