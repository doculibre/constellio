package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.ui.framework.components.LocalDateLabel;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.joda.time.LocalDate;

import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class SystemInfosView extends BaseViewImpl {
	private VerticalLayout layout;
	private  SystemInfosPresenter presenter;
	@Override
	protected Component buildMainComponent(ViewChangeEvent event) throws IOException, InterruptedException {
		layout = new VerticalLayout(buildInfoItem($("UpdateManagerViewImpl.version"), "\033[31;1m"+presenter.getLinuxVersion()));
		layout.setSpacing(true);
		layout.setWidth("100%");

		/*LicenseInfo info = presenter.getLicenseInfo();
		if (info != null) {
			layout.addComponents(
					buildInfoItem($("UpdateManagerViewImpl.clientName"), info.getClientName()),
					buildInfoItem($("UpdateManagerViewImpl.expirationDate"), info.getExpirationDate()));
		}*/

		return layout;
	}
	private Component buildInfoItem(String caption, Object value) {
		Label captionLabel = new Label(caption);
		captionLabel.addStyleName(ValoTheme.LABEL_BOLD);

		Label valueLabel = value instanceof LocalDate ? new LocalDateLabel((LocalDate) value) : new Label(value.toString());

		HorizontalLayout layout = new HorizontalLayout(captionLabel, valueLabel);
		layout.setSpacing(true);

		return layout;
	}

}
