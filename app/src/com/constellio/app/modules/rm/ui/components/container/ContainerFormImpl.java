package com.constellio.app.modules.rm.ui.components.container;

import com.constellio.app.modules.rm.ui.pages.containers.edit.AddEditContainerPresenter;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.vaadin.ui.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Constellio on 2017-01-11.
 */
public abstract class ContainerFormImpl extends RecordForm implements ContainerForm {

    public ContainerFormImpl(RecordVO record, AddEditContainerPresenter presenter) {
        this(record, new ContainerFieldFactory((String) record.get(ContainerRecord.TYPE), presenter));
    }

    public ContainerFormImpl(final RecordVO recordVO, RecordFieldFactory formFieldFactory) {
        super(recordVO, buildFields(recordVO, formFieldFactory), formFieldFactory);
    }

    private static List<FieldAndPropertyId> buildFields(RecordVO recordVO, RecordFieldFactory formFieldFactory) {
        List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<FieldAndPropertyId>();
        for (MetadataVO metadataVO : recordVO.getFormMetadatas()) {
            Field<?> field = formFieldFactory.build(recordVO, metadataVO);
            if (field != null) {
                field.addStyleName(STYLE_FIELD);
                field.addStyleName(STYLE_FIELD + "-" + metadataVO.getCode());
                fieldsAndPropertyIds.add(new FieldAndPropertyId(field, metadataVO));
            }
        }
        return fieldsAndPropertyIds;
    }

    @SuppressWarnings("unchecked")
    public Field<String> getTypeField() {
        return (Field<String>) getField(ContainerRecord.TYPE);
    }

    @SuppressWarnings("unchecked")
    public Field<String> getDecommissioningTypeField() {
        return (Field<String>) getField(ContainerRecord.DECOMMISSIONING_TYPE);
    }

    @SuppressWarnings("unchecked")
    public Field<String> getAdministrativeUnitField() {
        return (Field<String>) getField(ContainerRecord.ADMINISTRATIVE_UNIT);
    }
}