package com.constellio.app.ui.application;

import com.vaadin.server.*;
import org.jsoup.nodes.Element;

@SuppressWarnings("serial")
public class ConstellioSessionInitListener implements SessionInitListener {

	@Override
	public final void sessionInit(final SessionInitEvent event)
			throws ServiceException {
		event.getSession().addBootstrapListener(new BootstrapListener() {

			@Override
			public void modifyBootstrapPage(final BootstrapPageResponse response) {
				final Element head = response.getDocument().head();
				head.appendElement("meta")
						.attr("name", "viewport")
						.attr("content",
								"width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=no");
				head.appendElement("meta")
						.attr("name", "apple-mobile-web-app-capable")
						.attr("content", "yes");
				head.appendElement("meta")
						.attr("name", "apple-mobile-web-app-status-bar-style")
						.attr("content", "black-translucent");

				String contextPath = response.getRequest().getContextPath();
				head.appendElement("link")
						.attr("rel", "apple-touch-icon")
						.attr("href",
								contextPath
								+ "/VAADIN/themes/dashboard/img/app-icon.png");
			}

			@Override
			public void modifyBootstrapFragment(
					final BootstrapFragmentResponse response) {
			}
		});
	}

}
