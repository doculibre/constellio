package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.reports.builders.decommissioning.DecommissioningListExcelReportParameters;
import com.constellio.app.modules.rm.reports.model.excel.BaseExcelReportPresenter;
import com.constellio.app.modules.rm.reports.model.search.NoSuchReportRuntimeException;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.FolderDetailWithType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.reports.ReportServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DecommissioningListExcelReportPresenter extends BaseExcelReportPresenter {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DecommissioningListExcelReportPresenter.class);

    protected transient ModelLayerFactory modelLayerFactory;
    protected transient AppLayerCollectionExtensions appCollectionExtentions;
    protected transient AppLayerSystemExtensions appSystemExtentions;
    private String decommissioningListId;
    private String reportTitle;
    private String collection;
    private String username;
    private String schemaTypeCode;
    private User userInCollection;

    public DecommissioningListExcelReportPresenter(AppLayerFactory appLayerFactory, Locale locale, String collection, DecommissioningListExcelReportParameters parameters) {
        super(appLayerFactory, locale, collection);
        this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
        this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
        this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
        this.decommissioningListId = parameters.getDecommissioningListId();
        this.reportTitle = parameters.getReportTitle();
        this.collection = parameters.getCollection();
        this.username = parameters.getUsername();
        this.schemaTypeCode = parameters.getSchemaType();
        this.userInCollection = appLayerFactory.getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
    }

    public DecommissioningListExcelReportModel build() {
        DecommissioningListExcelReportModel model = new DecommissioningListExcelReportModel();

        List<Metadata> orderedEnabledReportedMetadataList = getEnabledReportedMetadataList(modelLayerFactory);

        for (Metadata metadata : orderedEnabledReportedMetadataList) {
            model.addTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
        }

        Iterator<Folder> foldersIterator = getFoldersIteratorFromDecommissioningList();

        while (foldersIterator.hasNext()) {
            model.addLine(getRecordLine(foldersIterator.next().getWrappedRecord(), orderedEnabledReportedMetadataList));
        }

        return model;
    }

    private Iterator<Folder> getFoldersIteratorFromDecommissioningList() {
        RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
        DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);

        List<String> decommissioningListFoldersIds = new ArrayList<>(decommissioningList.getFolders());
        Map<String, FolderDetailWithType> folderDetails = new HashMap<>();

        for (FolderDetailWithType folder : decommissioningList.getFolderDetailsWithType()) {
            if (folder.isIncluded()) {
                folderDetails.put(folder.getFolderId(), folder);
            } else {
                decommissioningListFoldersIds.remove(folder.getFolderId());
            }
        }

        List<Folder> folders = new ArrayList<>(rmSchemasRecordsServices.getFolders(decommissioningListFoldersIds));
        for (Folder folder : folders) {
            final FolderDetailWithType folderDetailWithType = folderDetails.get(folder.getId());
            final DecomListFolderDetail detail = folderDetailWithType.getDetail();

            folder.setLinearSize(detail.getFolderLinearSize());
            folder.setContainer(detail.getContainerRecordId());
        }

        return folders.iterator();
    }

    private List<Metadata> getEnabledReportedMetadataList(ModelLayerFactory modelLayerFactory) {
        ReportServices reportServices = new ReportServices(modelLayerFactory, collection);
        List<ReportedMetadata> reportedMetadataList = new ArrayList<>(getReportedMetadataList(reportServices));
        orderByPosition(reportedMetadataList);
        List<Metadata> returnList = new ArrayList<>();
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaTypeCode);
        MetadataList allMetadata = schemaType.getAllMetadatas().onlyAccessibleGloballyBy(userInCollection);

        for (ReportedMetadata reportedMetadata : reportedMetadataList) {
            boolean found = false;
            for (Metadata metadata : allMetadata) {
                if (metadata.getLocalCode().equals(reportedMetadata.getMetadataLocaleCode())) {
                    //					if (metadata.isEnabled()) {
                    returnList.add(metadata);
                    //					}
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOGGER.warn("Could not find reported metadata: " + reportedMetadata.getMetadataLocaleCode());
            }
        }

        return returnList;
    }

    private void orderByPosition(List<ReportedMetadata> reportedMetadataList) {
        Collections.sort(reportedMetadataList, new Comparator<ReportedMetadata>() {
            @Override
            public int compare(ReportedMetadata o1, ReportedMetadata o2) {
                return o1.getXPosition() - o2.getXPosition();
            }
        });
    }

    private List<ReportedMetadata> getReportedMetadataList(ReportServices reportServices) {
        Report report = reportServices.getUserReport(username, schemaTypeCode, reportTitle);
        if (report == null) {
            report = reportServices.getReport(Folder.SCHEMA_TYPE, reportTitle);
        }
        if (report == null) {
            String username = null;
            if (this.username != null) {
                username = this.username;
            }
            throw new NoSuchReportRuntimeException(username, Folder.SCHEMA_TYPE, reportTitle);
        }
        if (report.getLinesCount() != 1) {
            throw new UnsupportedReportException();
        }
        return report.getReportedMetadata();
    }

    public Locale getLocale() {
        return locale;
    }
}
