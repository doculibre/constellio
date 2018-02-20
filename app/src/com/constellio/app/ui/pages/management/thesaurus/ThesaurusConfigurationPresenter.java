package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.content.DownloadContentVersionLink;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.thesaurus.ThesaurusBuilder;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.io.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class ThesaurusConfigurationPresenter extends BasePresenter<ThesaurusConfigurationView> {
    public static final String THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME = "thesaurusConfigurationPresenterStreamName";
    public static final String THESAURUS_TEMPORARY_FILE = "thesaurusTemporaryFile";
    public static final String THESAURUS_FILE = "thesaurus.xml";

    ThesaurusConfig thesaurusConfig = null;
    SchemasRecordsServices rm;
    RecordServices recordServices;
    ThesaurusManager thesaurusManager;
    ThesaurusService tempThesarusService = null;

    boolean isInStateToBeSaved;

    public ThesaurusConfigurationPresenter(ThesaurusConfigurationView view) {
        super(view);

        SearchServices searchService = modelLayerFactory.newSearchServices();
        recordServices = modelLayerFactory.newRecordServices();
        rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        List<Record> thesaurusConfigRecordFound = searchService.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators
                .from(rm.thesaurusConfig.schemaType()).returnAll()));

        if(thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() ==  1) {
            thesaurusConfig = rm.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
        }

        thesaurusManager = modelLayerFactory.getThesaurusManager();
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

    public void saveNewThesaurusFile(TempFileUpload tempFileUpload) {
        boolean isNew = false;
        IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

        InputStream inputStreamFromFile = null;
        InputStream inputStream2FromFile = null;
        try {


            inputStreamFromFile = ioServices.newFileInputStream(tempFileUpload.getTempFile(), THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME);
            thesaurusManager.set(inputStreamFromFile);

            if(thesaurusConfig == null)  {
                thesaurusConfig = rm.newThesaurusConfig();
                isNew = true;
            }
            inputStream2FromFile = ioServices.newFileInputStream(tempFileUpload.getTempFile(), THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME);

            ContentManager contentManager = modelLayerFactory.getContentManager();
            ContentVersionDataSummary contentVersionDataSummary = upload(inputStream2FromFile, THESAURUS_FILE,contentManager);
            ioServices.closeQuietly(inputStream2FromFile);
            Content content = contentManager.createMajor(getCurrentUser(), THESAURUS_FILE,contentVersionDataSummary);
            thesaurusConfig.setContent(content);
            if(isNew){
                recordServices.add(thesaurusConfig);
            } else {
                recordServices.update(thesaurusConfig);
            }

            modelLayerFactory.getContentManager().getContentDao().moveFileToVault(tempFileUpload.getTempFile(),contentVersionDataSummary.getHash());
            view.enableSKOSSaveButton(false);
        } catch (IOException e) {
            e.printStackTrace();
            view.showErrorMessage($("ThesaurusConfigurationView.ErrorWhileSavingThesaurusFile"));
        } catch (RecordServicesException e) {
            e.printStackTrace();
            view.showErrorMessage($("ThesaurusConfigurationView.ErrorWhileSavingThesaurusFile"));
        } finally {
            ioServices.closeQuietly(inputStreamFromFile);
            ioServices.closeQuietly(inputStream2FromFile);
        }
        tempFileUpload.delete();
        Page.getCurrent().reload();
    }

    public ContentVersionDataSummary upload(InputStream resource, String fileName, ContentManager contentManager) {
        return contentManager.upload(resource, new ContentManager.UploadOptions(fileName)).getContentVersionDataSummary();
    }

    public void deleteButtonClick() {
        thesaurusConfig.setContent(null);
        try {
            recordServices.update(thesaurusConfig);
            Page.getCurrent().reload();
        } catch (RecordServicesException e) {
            e.printStackTrace();
            view.showErrorMessage($("ThesaurusConfigurationView.ErrorWhileSavingThesaurusFile"));
        }
    }

    public void valueChangeInFileSelector(TempFileUpload tempFileUpload) {
        if(tempFileUpload == null) {
            isInStateToBeSaved = false;
            view.enableSKOSSaveButton(false);
            return;
        }

        try {
            tempThesarusService = ThesaurusBuilder.getThesaurus(new FileInputStream(tempFileUpload.getTempFile()));
            isInStateToBeSaved = true;
            view.enableSKOSSaveButton(true);
            view.loadDescriptionFieldsWithFileValue();
        } catch (FileNotFoundException e) {
            isInStateToBeSaved = false;
            view.enableSKOSSaveButton(false);
            e.printStackTrace();
            throw new RuntimeException("ThesaurusConfigurationPresenter - Internal Error", e);
        } catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
            isInStateToBeSaved = false;
            view.enableSKOSSaveButton(false);
            view.toNoThesaurusAvalible();
            view.showErrorMessage($("ThesaurusConfigurationView.errorInvalidFileFormat"));
            view.removeAllTheSelectedFile();
        }
    }

    public ContentVersionVO getContentVersionForDownloadLink() {
        if(haveThesaurusConfiguration()) {
            return new RecordToVOBuilder().build(thesaurusConfig.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext()).get(ThesaurusConfig.CONTENT);
        }
        throw new IllegalStateException("A thesaurusFile need to be avalible when calling this method");
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
        ThesaurusService thesaurusService;
        if(isInStateToBeSaved) {
            return tempThesarusService.getRdfAbout();
        }
        else if((thesaurusService = thesaurusManager.get()) != null) {
            return thesaurusService.getRdfAbout();
        }
        return "";
    }

    public String getTitle() {
        if(isInStateToBeSaved) {
            return tempThesarusService.getDcTitle();
        }

        ThesaurusService thesaurusService;
        if((thesaurusService = thesaurusManager.get()) != null) {
            return thesaurusService.getDcTitle();
        }
        return "";
    }

    public String getDescription() {
        if(isInStateToBeSaved) {
            return tempThesarusService.getDcDescription();
        }

        ThesaurusService thesaurusService;
        if((thesaurusService = thesaurusManager.get()) != null) {
            return thesaurusService.getDcDescription();
        }
        return "";
    }

    public boolean haveSomethingToBeSaved(){
        return isInStateToBeSaved;
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if(isInStateToBeSaved && tempThesarusService.getDcDate() != null) {
            return sdf.format(tempThesarusService.getDcDate());
        }

        ThesaurusService thesaurusService;
        if((thesaurusService = thesaurusManager.get()) != null && thesaurusService.getDcDate() != null) {
            return sdf.format(thesaurusService.getDcDate());
        }
        return "";
    }

    public String getCreator() {
        if(isInStateToBeSaved) {
            return tempThesarusService.getDcDescription();
        }

        ThesaurusService thesaurusService;
        if((thesaurusService = thesaurusManager.get()) != null) {
            return thesaurusService.getDcCreator();
        }
        return "";
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return true;
    }
}
