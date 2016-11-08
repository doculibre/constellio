package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.util.FileIconUtils;
import com.vaadin.server.ConnectorResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.ValoTheme;

public class DownloadLink extends Link {
	public static final String STYLE_NAME = "download-link";

	protected Resource downloadedResource;

	public DownloadLink(Resource downloadedResource, String caption) {
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

		if (Page.getCurrent().getWebBrowser().isIOS()) {
			setTargetName("_blank");
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
