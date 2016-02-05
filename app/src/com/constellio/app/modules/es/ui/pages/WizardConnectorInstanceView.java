package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.ui.framework.data.RecordVODataProvider;

public interface WizardConnectorInstanceView extends AddEditConnectorInstanceView {

	void setConnectorTypeDataProvider(RecordVODataProvider connectorTypeDataProvider);

	void refreshConnectorForm();

}
