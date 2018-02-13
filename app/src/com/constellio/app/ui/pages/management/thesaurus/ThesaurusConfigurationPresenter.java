package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import org.apache.commons.vfs2.FileName;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationPresenter extends BasePresenter<ThesaurusConfigurationView> {
    public static final String THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME = "thesaurusConfigurationPresenterStreamName";
    ThesaurusConfig thesaurusConfig = null;
    RMSchemasRecordsServices rm;
    RecordServices recordServices;

    public ThesaurusConfigurationPresenter(ThesaurusConfigurationView view) {
        super(view);

        SearchServices searchService = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        List<Record> thesaurusConfigRecordFound = searchService.search(new LogicalSearchQuery(LogicalSearchQueryOperators
                .from(rm.thesaurusSchema()).returnAll()));

        if(thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() ==  1) {
            thesaurusConfig = rm.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
        }
    }

    public boolean haveThesaurusConfiguration(){
        if(thesaurusConfig == null) {
            return false;
        } else {
            if(thesaurusConfig.getContent() == null) {
                return false;
            }
        }

        return true;
    }

    public StreamResource inputStreamToStreamRessource(final InputStream inputStream, String fileName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {

            public InputStream getStream() {
                return inputStream;

            }
        };
        StreamResource resource = new StreamResource ( source, fileName);
        return resource;
    }

    public void downloadThesaurusFile() {
        if(haveThesaurusConfiguration()) {
            InputStream inputStream = null;

            try {
                inputStream =  modelLayerFactory.getContentManager().getContentDao().getContentInputStream(thesaurusConfig.getContent().getCurrentVersion().getHash(),THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME);

                Page.getCurrent().open(inputStreamToStreamRessource(inputStream, $("ThesaurusConfigurationView.thesaurus")), null, false);
            } catch (ContentDaoException.ContentDaoException_NoSuchContent contentDaoException_noSuchContent) {
                contentDaoException_noSuchContent.printStackTrace();
            }

        }
    }

    public void saveDenidedTerms(String denidedTerms) throws RecordServicesException {
        String[] termsPerLine = denidedTerms.split("[\\r\\n]+");

        List<String> termsPerLineAsList = Arrays.asList(termsPerLine);
        boolean isNew = false;
        if(thesaurusConfig == null) {
            thesaurusConfig = rm.newThesaurusConfig();
            isNew = true;
        }

        thesaurusConfig.setDenidedWords(termsPerLineAsList);
        if(isNew) {
            recordServices.add(thesaurusConfig);
        } else {
            recordServices.update(thesaurusConfig);
        }
    }

    public String getDenidedTerms() {
        String currentDenidedTerms = "";

        if(thesaurusConfig != null) {
            StringBuilder stringBuilder = new StringBuilder();

            for(String denidedWord : thesaurusConfig.getDenidedWords()) {
                stringBuilder.append(denidedWord).append("\n");
            }

            currentDenidedTerms = stringBuilder.toString();
        }

        return currentDenidedTerms;
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
