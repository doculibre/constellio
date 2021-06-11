package com.constellio.app.ui.framework.components.display;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import org.vaadin.activelink.ActiveLink;

import java.util.Objects;

public class UrlLink extends ActiveLink {

	public UrlLink(String caption) {
		this(caption, null);
	}

	public UrlLink(String caption, String url) {
		this(caption, url, null);
	}

	public UrlLink(String caption, String url, Resource icon) {
		setCaption(Objects.requireNonNull(caption != null ? caption : url, "Url's caption and url cannot be both null"));

		setResource(url != null ? new ExternalResource(url) : null);

		setIcon(icon);
	}
}
