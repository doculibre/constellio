package com.constellio.app.modules.rm.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.management.schemas.ListSchemaTypeViewImpl;
import com.vaadin.ui.Component;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Created by Constellio on 2017-03-16.
 */
public class RMListSchemaTypeExtension extends PagesComponentsExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public static final String RM_TAB = "rm";

	@Override
	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {
		super.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		Component mainComponent = params.getMainComponent();
		if (mainComponent instanceof ListSchemaTypeViewImpl) {
			ListSchemaTypeViewImpl view = (ListSchemaTypeViewImpl) mainComponent;
			view.addTab(RM_TAB, $("ListSchemaTypeView.rmTabCaption"));
		}
	}
}
