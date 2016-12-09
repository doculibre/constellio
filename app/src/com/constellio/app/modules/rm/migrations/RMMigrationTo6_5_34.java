package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.services.factories.AppLayerFactory;

/**
* Created by Constelio on 2016-11-25.
*/

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksEmailTemplates;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.services.emails.EmailTemplatesManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class RMMigrationTo6_5_34 extends MigrationHelper implements MigrationScript {

    private String collection;

    private MigrationResourcesProvider migrationResourcesProvider;

    private AppLayerFactory appLayerFactory;

    @Override
    public String getVersion() {
        return "6.5.34";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
            throws Exception {
        this.collection = collection;
        this.migrationResourcesProvider = migrationResourcesProvider;
        this.appLayerFactory = appLayerFactory;

        reloadEmailTemplates();
    }

    private void reloadEmailTemplates() {
        if(appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0).equals("en")) {
            reloadEmailTemplate("alertAvailableTemplate_en.html", RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
        }
        else {
            reloadEmailTemplate("alertAvailableTemplate.html", RMEmailTemplateConstants.ALERT_AVAILABLE_ID);
        }
    }

    private void reloadEmailTemplate(final String templateFileName, final String templateId) {
        final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

        try {
            appLayerFactory.getModelLayerFactory().getEmailTemplatesManager().replaceCollectionTemplate(templateId, collection, templateInputStream);
        } catch (IOException | OptimisticLockingConfiguration e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(templateInputStream);
        }
    }
}
