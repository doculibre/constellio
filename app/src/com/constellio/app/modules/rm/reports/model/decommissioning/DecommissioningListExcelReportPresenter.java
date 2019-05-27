package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.modules.rm.constants.RMTaxonomies;
import com.constellio.app.modules.rm.reports.model.administration.plan.ReportUtil;
import com.constellio.app.modules.rm.reports.model.excel.BaseExcelReportPresenter;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.NewReportPresenter;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWithCaptionVO;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesSearchOptions;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListExcelReportPresenter extends BaseExcelReportPresenter implements NewReportPresenter {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DecommissioningListExcelReportPresenter.class);

    protected transient ModelLayerFactory modelLayerFactory;
    protected transient AppLayerCollectionExtensions appCollectionExtentions;
    protected transient AppLayerSystemExtensions appSystemExtentions;
    private String decommissioningListId;
    private RMSchemasRecordsServices rmSchemasRecordsServices;
    private AuthorizationsServices authorizationsServices;
    private TaxonomiesSearchServices taxonomiesSearchServices;
    private TaxonomiesSearchOptions searchOptions;
    private MetadataSchemaTypes types;

    public DecommissioningListExcelReportPresenter(AppLayerFactory appLayerFactory, Locale locale, String collection, String decommissioningListId) {
        super(appLayerFactory, locale, collection);
        this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
        this.appCollectionExtentions = appLayerFactory.getExtensions().forCollection(collection);
        this.appSystemExtentions = appLayerFactory.getExtensions().getSystemWideExtensions();
        this.decommissioningListId = decommissioningListId;
    }

    private void init() {
        rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory, locale);
        authorizationsServices = modelLayerFactory.newAuthorizationsServices();
        taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
        searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all());
        types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
    }

    public DecommissioningListExcelReportModel build() {
        init();

        DecommissioningListExcelReportModel model = new DecommissioningListExcelReportModel();


        MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getDefaultSchema(
                DecommissioningList.SCHEMA_TYPE);

        List<Metadata> metadataListed = new ArrayList<>();


//        metadataListed.add(metadataSchema.getMetadata(DecommissioningList.CODE));
        metadataListed.add(metadataSchema.getMetadata(DecommissioningList.TITLE));

        for (Metadata metadata : metadataListed) {
            model.addTitle(metadata.getLabel(Language.withCode(locale.getLanguage())));
        }

        model.addTitle(i18n.$("Reports.decommissioningListExcelUserWithPermission"));
        model.addTitle(i18n.$("Reports.decommissioningListExcelGroupWithPermission"));


        if (decommissioningListId != null && !decommissioningListId.isEmpty()) {
            DecommissioningList decommissioningList = rmSchemasRecordsServices.getDecommissioningList(decommissioningListId);
            List<Object> recordLine = decommissioningListToCellContentList(metadataListed, decommissioningList);
            model.addLine($(DecommissioningListExcelReportModel.SINGLE_SHEET_CAPTION_KEY), recordLine);
        } else {
            List<DecommissioningList> rootDecommissioningList = new ArrayList<>();
            List<TaxonomySearchRecord> taxonomySearchRecords = taxonomiesSearchServices
                    .getLinkableRootConcept(User.GOD, collection,
                            RMTaxonomies.ADMINISTRATIVE_UNITS, DecommissioningList.SCHEMA_TYPE, searchOptions);

            if (taxonomySearchRecords != null) {
                for (TaxonomySearchRecord taxonomyRecord : taxonomySearchRecords) {

                    if (taxonomyRecord != null) {
                        Record record = taxonomyRecord.getRecord();
                        DecommissioningList recordDecommissioningList = new DecommissioningList(record, types/*, locale*/);

                        if (recordDecommissioningList != null) {
                            rootDecommissioningList.add(recordDecommissioningList);
                        }
                    }
                }
            }

            for (DecommissioningList decommissioningList : rootDecommissioningList) {
                List<DecommissioningList> decommissioningLists = new ArrayList<>();
                getCategoriesForRecord(decommissioningList.getWrappedRecord(), decommissioningLists);
                model.addLine("asd123"/*decommissioningList.getCode()*/, decommissioningListToCellContentList(metadataListed, decommissioningList));

                for (DecommissioningList currentDecommissioningList : decommissioningLists) {
                    model.addLine("qwerty:)"/*decommissioningList.getCode()*/, decommissioningListToCellContentList(metadataListed, currentDecommissioningList));
                }
            }
        }

        return model;
    }

    private List<Object> decommissioningListToCellContentList(List<Metadata> metadataListed,
                                                              DecommissioningList decommissioningList) {
        List<Object> recordLine = getRecordLine(decommissioningList.getWrappedRecord(), metadataListed);
        getExtraLineData(recordLine, decommissioningList);
        return recordLine;
    }

    private void updateAcces(String user, Map<String, List<String>> accessList, List<String> newAcessList) {
        List<String> currentAcces = accessList.get(user);
        if (currentAcces == null) {
            accessList.put(user, newAcessList);
        } else {
            for (String currentNewAccess : newAcessList) {
                if (!currentAcces.contains(currentNewAccess)) {
                    currentAcces.add(currentNewAccess);
                }
            }
        }
    }

    private void getCategoriesForRecord(Record record, List<DecommissioningList> decommissioningLists) {

        searchOptions = new TaxonomiesSearchOptions().setReturnedMetadatasFilter(ReturnedMetadatasFilter.all())
                .setAlwaysReturnTaxonomyConceptsWithReadAccessOrLinkable(true);

        List<TaxonomySearchRecord> children = taxonomiesSearchServices.getLinkableChildConcept(User.GOD, record,
                RMTaxonomies.ADMINISTRATIVE_UNITS, AdministrativeUnit.SCHEMA_TYPE, searchOptions);

        if (children != null) {
            for (TaxonomySearchRecord child : children) {
                if (child != null) {
                    try {
                        Record childRecord = child.getRecord();
                        if (childRecord != null) {
                            DecommissioningList decommissioningList = new DecommissioningList(childRecord, types/*, locale*/);

                            if (decommissioningList != null) {
                                decommissioningLists.add(decommissioningList);
                                getCategoriesForRecord(childRecord, decommissioningLists);
                            }
                        }
                    } catch (Exception e) {
                        // throw new RuntimeException(e);
                        LOGGER.info("This is not a category. It's a " + child.getRecord().getSchemaCode());
                    }
                }
            }
        }
    }

    private void getExtraLineData(List<Object> recordLine, DecommissioningList decommissioningList) {
        List<Authorization> authorizationList = authorizationsServices.getRecordAuthorizations(decommissioningList.getWrappedRecord());
        List<String> groupList = new ArrayList<>();
        List<String> userList = new ArrayList<>();
        Map<String, List<String>> groupHashMap = new HashMap<>();
        Map<String, List<String>> userAccessHashMap = new HashMap<>();

        for (Authorization authorization : authorizationList) {
            List<String> accessList = ReportUtil.getAccess(authorization, modelLayerFactory);
            for (String principal : authorization.getPrincipals()) {
                Record record = rmSchemasRecordsServices.get(principal);
                if (record.getSchemaCode().split("_")[0].equals(User.SCHEMA_TYPE)) {

                    updateAcces(record.getTitle(), userAccessHashMap, accessList);

                    if (!userList.contains(record.getTitle())) {
                        userList.add(record.getTitle());
                    }
                } else {
                    updateAcces(record.getTitle(), groupHashMap, accessList);
                    if (!groupList.contains(record.getTitle())) {
                        groupList.add(record.getTitle());
                    }
                }
            }
        }

        List<User> usersWithGlobalAccessInCollection = authorizationsServices.getUsersWithGlobalAccessInCollection(collection);
        for (User currentUser : usersWithGlobalAccessInCollection) {
            List<String> accessList = new ArrayList<>();
            if (currentUser.hasCollectionReadAccess()) {
                accessList.add(Role.READ);
            }
            if (currentUser.hasCollectionWriteAccess()) {
                accessList.add(Role.WRITE);
            }
            if (currentUser.hasCollectionDeleteAccess()) {
                accessList.add(Role.DELETE);
            }

            if (!accessList.isEmpty()) {
                updateAcces(currentUser.getTitle(), userAccessHashMap, accessList);
            }

            if (!userList.contains(currentUser.getTitle())) {
                userList.add(currentUser.getTitle());
            }
        }

        recordLine.add(getUserCellContent(userList, userAccessHashMap));
        recordLine.add(getUserCellContent(groupList, groupHashMap));
    }

    private String getUserCellContent(List<String> itemList, Map<String, List<String>> userAccessHashMap) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String item : itemList) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(", ");
            }

            String access = ReportUtil.accessAbreviation(userAccessHashMap.get(item));

            stringBuilder.append(item);

            if (access != null && access.length() >= 0) {
                stringBuilder.append(" (" + access + ")");
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public List<ReportWithCaptionVO> getSupportedReports() {
        return null;
    }

    @Override
    public NewReportWriterFactory getReport(String report) {
        return null;
    }

    @Override
    public Object getReportParameters(String report) {
        return null;
    }

    public Locale getLocale() {
        return locale;
    }
}
