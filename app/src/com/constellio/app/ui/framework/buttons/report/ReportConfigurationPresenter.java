package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public interface ReportConfigurationPresenter {
    MetadataSchemasManager getMetadataSchemasManager();

    SchemasDisplayManager getSchemasDisplayManager();

    String getSchemaTypeCode();

    User getReportAssociatedUser();

    Metadata[] getAllSchemaTypeMetadata();
}
