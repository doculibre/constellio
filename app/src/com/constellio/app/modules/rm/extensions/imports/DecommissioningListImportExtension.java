package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.structures.Comment;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Charles Blanchette on 2017-02-17.
 */
public class DecommissioningListImportExtension extends RecordImportExtension {

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
        List<Map<String, String>> decomListComments = buildParams.getImportRecord().getList(DecommissioningList.COMMENTS);

        DecommissioningList decommissioningList = new DecommissioningList(buildParams.getRecord(), buildParams.getTypes());

        List<DecomListFolderDetail> decomListFolderDetailList = new ArrayList<>();
        List<DecomListContainerDetail> decomListContainerDetailList = new ArrayList<>();
        List<Comment> decomListCommentList = new ArrayList<>();

        for (Map<String, String> decomListFolderDetail : decomListFolderDetails) {
            decomListFolderDetailList.add(buildDecomListFolderDetails(decomListFolderDetail));
        }
        decommissioningList.setFolderDetails(decomListFolderDetailList);

        for (Map<String, String> decomListContainerDetail : decomListContainerDetails) {
            decomListContainerDetailList.add(buildDecomListContainerDetails(decomListContainerDetail));
        }
        decommissioningList.setContainerDetails(decomListContainerDetailList);

        for (Map<String, String> decomListComment : decomListComments) {
            decomListCommentList.add(buildDecomListComments(decomListComment));
        }
        decommissioningList.setComments(decomListCommentList);
    }

    private DecomListFolderDetail buildDecomListFolderDetails(Map<String, String> mapDecomListFolderDetail) {

        DecomListFolderDetail decomListFolderDetail;

        if (mapDecomListFolderDetail.containsKey(FOLDER_ID) && StringUtils
                .isNotEmpty(mapDecomListFolderDetail.get(FOLDER_ID))) {
            Folder folder = rm.getFolderWithLegacyId(mapDecomListFolderDetail.get(FOLDER_ID));
            decomListFolderDetail = new DecomListFolderDetail(folder.getId());
        } else {
            decomListFolderDetail = new DecomListFolderDetail();
        }

        decomListFolderDetail.setFolderExcluded(Boolean.parseBoolean(mapDecomListFolderDetail.get(FOLDER_EXCLUDED)));
        decomListFolderDetail.setReversedSort(Boolean.parseBoolean(mapDecomListFolderDetail.get(REVERSED_SORT)));

        if (mapDecomListFolderDetail.containsKey(CONTAINER_RECORD_ID) && StringUtils
                .isNotEmpty(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID))) {
            ContainerRecord containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListFolderDetail.get(CONTAINER_RECORD_ID));
            decomListFolderDetail.setContainerRecordId(containerRecord.getId());
        }

        if (mapDecomListFolderDetail.get(FOLDER_LINEAR_SIZE) == null) {
            decomListFolderDetail.setFolderLinearSize(0.0);
        } else {
            decomListFolderDetail.setFolderLinearSize(Double.parseDouble(mapDecomListFolderDetail.get(FOLDER_LINEAR_SIZE)));
        }

        return decomListFolderDetail;
    }

    private DecomListContainerDetail buildDecomListContainerDetails(Map<String, String> mapDecomListContainerDetail) {

        DecomListContainerDetail decomListContainerDetail;

         if (mapDecomListContainerDetail.containsKey(CONTAINER_RECORD_ID) && StringUtils
                .isNotEmpty(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID))) {
            ContainerRecord containerRecord = rm.getContainerRecordWithLegacyId(mapDecomListContainerDetail.get(CONTAINER_RECORD_ID));
            decomListContainerDetail = containerRecord != null ? new DecomListContainerDetail(containerRecord.getId()) : new DecomListContainerDetail();
        } else {
            decomListContainerDetail = new DecomListContainerDetail();
        }

        decomListContainerDetail.setFull(Boolean.parseBoolean(mapDecomListContainerDetail.get(BOOLEAN_FULL)));

        return decomListContainerDetail;
    }

    private Comment buildDecomListComments(Map<String, String> mapDecomListComments) {

        Comment comment = new Comment();

        comment.setMessage(mapDecomListComments.get(MESSAGE));
        comment.setDateTime(new LocalDateTime(mapDecomListComments.get(DATE_TIME)));
        comment.setUser(rm.newUserWithId(mapDecomListComments.get(USER_ID)).setUsername(mapDecomListComments.get(USERNAME)));

        return comment;
    }
}
