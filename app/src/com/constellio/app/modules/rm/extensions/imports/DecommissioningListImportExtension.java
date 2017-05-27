package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Charles Blanchette on 2017-02-17.
 */
public class DecommissioningListImportExtension extends RecordImportExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecommissioningListImportExtension.class);

    public static final String FOLDER_ID = "folderId";
    public static final String FOLDER_EXCLUDED = "folderExcluded";
    public static final String CONTAINER_RECORD_ID = "containerRecordId";
    public static final String REVERSED_SORT = "reversedSort";
    public static final String FOLDER_LINEAR_SIZE = "folderLinearSize";
    public static final String BOOLEAN_FULL = "full";
    public static final String MESSAGE = "message";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String DATE_TIME = "dateTime";
    public static final String AVAILABLE_SIZE = "availableSize";
    public static final String IS_PLACED_IN_CONTAINER = "IsPlacedInContainer";
    public static final String REQUEST_DATE = "requestDate";
    public static final String VALIDATION_DATE = "validationDate";

    private final RMSchemasRecordsServices rm;

    public DecommissioningListImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
        this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
    }

    @Override
    public String getDecoratedSchemaType() {
        return DecommissioningList.SCHEMA_TYPE;
    }


    @Override
    public void build(BuildParams buildParams) {

        List<Map<String, String>> decomListFolderDetails = buildParams.getImportRecord().getList(DecommissioningList.FOLDER_DETAILS);
        List<Map<String, String>> decomListContainerDetails = buildParams.getImportRecord().getList(DecommissioningList.CONTAINER_DETAILS);
        List<Map<String, String>> decomListValidations = buildParams.getImportRecord().getList(DecommissioningList.VALIDATIONS);
        boolean importAsLegacyId = buildParams.getImportDataOptions().isImportAsLegacyId();

        List<Map<String, String>> decomListComments = buildParams.getImportRecord().getList(DecommissioningList.COMMENTS);

        DecommissioningList decommissioningList = new DecommissioningList(buildParams.getRecord(), buildParams.getTypes());

        List<DecomListFolderDetail> decomListFolderDetailList = new ArrayList<>();
        List<DecomListContainerDetail> decomListContainerDetailList = new ArrayList<>();
        List<DecomListValidation> decomListValidationList = new ArrayList<>();
        List<Comment> decomListCommentList = new ArrayList<>();

        for (Map<String, String> decomListFolderDetail : decomListFolderDetails) {
            decomListFolderDetailList.add(buildDecomListFolderDetails(decomListFolderDetail, importAsLegacyId));
        }
        decommissioningList.setFolderDetails(decomListFolderDetailList);

        for (Map<String, String> decomListContainerDetail : decomListContainerDetails) {
            decomListContainerDetailList.add(buildDecomListContainerDetails(decomListContainerDetail, importAsLegacyId));
        }
        decommissioningList.setContainerDetails(decomListContainerDetailList);

        for(Map<String, String> decomListValidation : decomListValidations) {
            decomListValidationList.add(buildDecomListValidation(decomListValidation));
        }
        decommissioningList.setValidations(decomListValidationList);
    }

    private DecomListValidation buildDecomListValidation(Map<String, String> mapDecomListValidation) {
        DecomListValidation decomListValidation = new DecomListValidation();

        if(mapDecomListValidation.containsKey(USER_ID) && StringUtils.isNotEmpty(mapDecomListValidation.get(USER_ID))) {
            decomListValidation.setUserId(mapDecomListValidation.get(USER_ID));
        }

        if(mapDecomListValidation.containsKey(DecommissioningListImportExtension.REQUEST_DATE)
                && StringUtils.isNotEmpty(mapDecomListValidation.get(REQUEST_DATE))) {
            String requestedDate = mapDecomListValidation.get(REQUEST_DATE);
            decomListValidation.setRequestDate(LocalDate.parse(requestedDate));
        }

        if(mapDecomListValidation.containsKey(DecommissioningListImportExtension.VALIDATION_DATE)
                && StringUtils.isNotEmpty(mapDecomListValidation.get(VALIDATION_DATE))) {
            String validationDate = mapDecomListValidation.get(VALIDATION_DATE);
            decomListValidation.setRequestDate(LocalDate.parse(validationDate));
        }

        return decomListValidation;
    }

    private DecomListFolderDetail buildDecomListFolderDetails(Map<String, String> mapDecomListFolderDetail, boolean useLegacyId) {

        DecomListFolderDetail decomListFolderDetail;

        if (mapDecomListFolderDetail.containsKey(FOLDER_ID) && StringUtils
                .isNotEmpty(mapDecomListFolderDetail.get(FOLDER_ID))) {

            Folder folder;

            if(useLegacyId) {
                folder = rm.getFolderWithLegacyId(mapDecomListFolderDetail.get(FOLDER_ID));
            } else {
                folder = rm.getFolder(mapDecomListFolderDetail.get(FOLDER_ID));
            }

            decomListFolderDetail = new DecomListFolderDetail(folder);
        } else {
            decomListFolderDetail = new DecomListFolderDetail();
        }

        decomListFolderDetail.setFolderExcluded(Boolean.parseBoolean(mapDecomListFolderDetail.get(FOLDER_EXCLUDED)));
        decomListFolderDetail.setReversedSort(Boolean.parseBoolean(mapDecomListFolderDetail.get(REVERSED_SORT)));

        if (mapDecomListFolderDetail.containsKey(CONTAINER_RECORD_ID) && StringUtils
                .isNotEmpty(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID))) {
            try {
                ContainerRecord containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID));
                decomListFolderDetail.setContainerRecordId(containerRecord.getId());
            } catch (Exception e) {
                LOGGER.error("Could not find container " + mapDecomListFolderDetail.get(CONTAINER_RECORD_ID) + " for folderDetail " + mapDecomListFolderDetail.get(FOLDER_ID));
            }

            ContainerRecord containerRecord = null;
            if(useLegacyId) {
                containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID));
            } else {
                containerRecord = rm.getContainerRecord(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID));
            }
            decomListFolderDetail.setContainerRecordId(containerRecord.getId());
        }

        if (mapDecomListFolderDetail.get(FOLDER_LINEAR_SIZE) == null) {
            decomListFolderDetail.setFolderLinearSize(0.0);
        } else {
            decomListFolderDetail.setFolderLinearSize(Double.parseDouble(mapDecomListFolderDetail.get(FOLDER_LINEAR_SIZE)));
        }

        return decomListFolderDetail;
    }

    private DecomListContainerDetail buildDecomListContainerDetails(Map<String, String> mapDecomListContainerDetail, boolean isImportAsLegacyId) {

        DecomListContainerDetail decomListContainerDetail;

        if (mapDecomListContainerDetail.containsKey(CONTAINER_RECORD_ID) && StringUtils
                .isNotEmpty(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID))) {
            ContainerRecord containerRecord = null;
            if(isImportAsLegacyId) {
                containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID));
            } else {
                containerRecord = rm.getContainerRecord(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID));
            }
            decomListContainerDetail = new DecomListContainerDetail(containerRecord.getId());
            try {
                ContainerRecord containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID));
                decomListContainerDetail = containerRecord != null ? new DecomListContainerDetail(containerRecord.getId()) : new DecomListContainerDetail();
            } catch (Exception e) {
                LOGGER.error("Could not find containerDetail " + mapDecomListContainerDetail.get(CONTAINER_RECORD_ID));
                decomListContainerDetail = new DecomListContainerDetail();
            }
        } else {
            decomListContainerDetail = new DecomListContainerDetail();
        }

        decomListContainerDetail.setFull(convertStringToBoolean(mapDecomListContainerDetail.get(BOOLEAN_FULL)));

        return decomListContainerDetail;
    }

    private Boolean convertStringToBoolean(String s) {
        if(s == null) {
            return null;
        }
        return Boolean.parseBoolean(s);
    }
}
