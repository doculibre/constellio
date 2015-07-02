/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.pages.containers.edit;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServicesException;

import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class EditContainerPresenter extends BasePresenter<EditContainerView> {

    private String containerId;

    public EditContainerPresenter(EditContainerView view) {
        super(view);
    }

    public RecordVO getContainerRecord(String params) {
        Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
        containerId = paramsMap.get("containerId");
        Record container = recordServices().getDocumentById(containerId);
        return new RecordToVOBuilder().build(container, VIEW_MODE.FORM, view.getSessionContext());
    }

    @Override
    protected boolean hasPageAccess(String params, User user) {
        return user.has(RMPermissionsTo.MANAGE_DECOMMISSIONING).globally();
    }

    public void saveButtonClicked(RecordVO recordVO) {
        Record record = modelLayerFactory.newRecordServices().getDocumentById(containerId);
        MetadataSchemaTypes types = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(view.getCollection());
        ContainerRecord containerRecord = new ContainerRecord(record, types);
        for(MetadataVO metadataVO : recordVO.getFormMetadatas()){
            String code = metadataVO.getCode();
            Metadata metadata = types.getMetadata(code);
            record.set(metadata, recordVO.get(code));
        }
        /*Double capacity = (Double)recordVO.get(ContainerRecord.CAPACITY);
        containerRecord.setCapacity(capacity);
        Double ratio = (Double)recordVO.get(ContainerRecord.FILL_RATIO_ENTRED);
        containerRecord.setFillRatioEntered(ratio);*/
        try {
            recordServices().update(record);
            view.navigateTo().displayContainer(containerId);
        } catch (RecordServicesException e) {
            view.showErrorMessage($("EditContainerViewImpl.errorWhenSavingContainer"));
        }

    }

    public void cancelButtonClicked() {
        view.navigateTo().displayContainer(containerId);
    }
}
