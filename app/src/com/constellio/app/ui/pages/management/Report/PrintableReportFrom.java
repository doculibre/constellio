package com.constellio.app.ui.pages.management.Report;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.labels.CustomLabelField;

import java.io.Serializable;

/**
 * Created by Marco on 2017-07-07.
 */
public interface PrintableReportFrom extends Serializable {

    void reload();

    void commit();

    ConstellioFactories getConstellioFactories();

    SessionContext getSessionContext();

    CustomLabelField<?> getCustomField(String metadataCode);
}
