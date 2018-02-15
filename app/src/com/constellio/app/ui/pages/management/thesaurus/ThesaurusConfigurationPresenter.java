package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.ThesaurusBuilder;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileName;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationPresenter extends BasePresenter<ThesaurusConfigurationView> {
    public static final String THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME = "thesaurusConfigurationPresenterStreamName";
    public static final String THESAURUS_TEMPORARY_FILE = "thesaurusTemporaryFile";

    ThesaurusConfig thesaurusConfig = null;
    RMSchemasRecordsServices rm;
    RecordServices recordServices;
    ThesaurusManager thesaurusManager;
    ThesaurusService thesaurusService;

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

        thesaurusManager = modelLayerFactory.getThesaurusManager();
        thesaurusService = thesaurusManager.get();
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

    public void saveNewThesaurusFile(InputStream inputStream) {
        IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
        File thesaurusTemporaryFile = ioServices.newTemporaryFile(THESAURUS_TEMPORARY_FILE);

        byte[] buffer;
        try {
            buffer = new byte[inputStream.available()];
            OutputStream outStream = new FileOutputStream(thesaurusTemporaryFile);
            outStream.write(buffer);
            ioServices.closeQuietly(outStream);
            InputStream inputStreamFromFile = FileUtils.openInputStream(thesaurusTemporaryFile);
            ThesaurusBuilder.getThesaurus(inputStreamFromFile);
            ioServices.closeQuietly(inputStream);
            ioServices.closeQuietly(inputStreamFromFile);
            if(thesaurusConfig == null)  {
                thesaurusConfig = rm.newThesaurusConfig();
            }
            //thesaurusConfig.setContent();
            //modelLayerFactory.getContentManager().getContentDao().moveFileToVault(thesaurusTemporaryFile,);
        } catch (IOException e) {
            e.printStackTrace();
            view.showErrorMessage($("ThesaurusConfigurationView.ErrorWhileSavingThesaurusFile"));
        }

        ioServices.deleteQuietly(thesaurusTemporaryFile);
    }

    public void downloadThesaurusFile() {
        if(haveThesaurusConfiguration()) {
            InputStream inputStream;

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

    public String getAbout() {

        if(thesaurusService != null) {
            return thesaurusService.getRdfAbout();
        }
        return "";
    }

    public String getTitle() {

        if(thesaurusService != null) {
            return thesaurusService.getDcTitle();
        }
        return "";
    }

    public String getDescription() {

        if(thesaurusService != null) {
            return thesaurusService.getDcDescription();
        }
        return "";
    }

    public String getDate() {

        if(thesaurusService != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(thesaurusService.getDcDate());
        }
        return "";
    }

    public String getCreator() {
        if(thesaurusService != null) {
            return thesaurusService.getDcCreator();
        }
        return "";
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
