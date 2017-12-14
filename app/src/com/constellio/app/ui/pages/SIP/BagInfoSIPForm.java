package com.constellio.app.ui.pages.SIP;

import com.constellio.app.modules.rm.wrappers.BagInfo;
import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.BagInfoToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.apache.calcite.rel.metadata.MetadataFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.constellio.app.ui.framework.components.RecordForm.STYLE_FIELD;
import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class BagInfoSIPForm extends BaseViewImpl {
    private BagInfoSIPPresenter presenter;

    private BagInfoRecordForm recordForm;


    private CheckBox deleteCheckBox;

    private CheckBox limitSizeCheckbox;

    @PropertyId("note")
    private TextArea noteTextArea;

    @PropertyId("descriptionSommaire")
    private TextArea descriptionSommaire;

    @PropertyId("identificationOrganismeVerseurOuDonateur")
    private TextField identificationOrganismeTextField;

    @PropertyId("IDOrganismeVerseurOuDonateur")
    private TextField IDOrganismeTextField;

    @PropertyId("address")
    private TextField adresseTextField;

    @PropertyId("regionAdministrative")
    private TextField regionAdministrativeTextField;

    @PropertyId("entiteResponsable")
    private TextField entiteResponsableTextField;

    @PropertyId("identificationEntiteResponsable")
    private TextField identificationEntiteResponsableTextField;

    @PropertyId("courrielResponsable")
    private TextField courrielResponsableTextField;

    @PropertyId("telephoneResponsable")
    private TextField telephoneResponsableTextField;

    @PropertyId("categoryDocument")
    private TextField categoryDocumentTextField;

    @PropertyId("methodTransfere")
    private TextField methodeTransfereTextField;

    @PropertyId("restrictionAccessibilite")
    private TextField restrictionAccessibiliteTextField;

    @PropertyId("archiveTitle")
    private TextField archiveTitleTextField;

    @Override
    protected void initBeforeCreateComponents(ViewChangeListener.ViewChangeEvent event) {
        presenter = new BagInfoSIPPresenter(this);
    }

    @Override
    protected boolean isBreadcrumbsVisible() {
        return false;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        List<BagInfoVO> bagInfoVOList = presenter.getAllBagInfo();
        VerticalLayout layout = new VerticalLayout();
        ComboBox cb = new ComboBox($("SIPButton.predefinedBagInfo"));
        for (BagInfoVO bagInfoVO : bagInfoVOList) {
            cb.addItem(bagInfoVO);
            cb.setItemCaption(bagInfoVO, bagInfoVO.getTitle());
        }
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateValue(event.getProperty().getValue() != null ? (BagInfoVO) event.getProperty().getValue() : new BagInfoVO("", Collections.<MetadataValueVO>emptyList(), RecordVO.VIEW_MODE.FORM));
            }
        });

        if (bagInfoVOList.isEmpty()) {
            cb.setVisible(false);
            cb.setEnabled(false);
        }

        limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));

        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));

        MetadataFieldFactory factory = new MetadataFieldFactory(){
            @Override
            public Field<?> build(MetadataVO metadata) {
                if(metadata.getLocalCode().equals("title")) {
                    return null;
                }
                return super.build(metadata);
            }
        };

        recordForm =  new BagInfoRecordForm(presenter.newRecord(), factory) {

            @Override
            protected void saveButtonClick(RecordVO viewObject) throws ValidationException {
                BagInfoSIPForm.this.saveButtonClick((BagInfoVO) viewObject);
            }

            @Override
            protected void cancelButtonClick(RecordVO viewObject) {
                navigateTo().previousView();
            }
        };
        deleteCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ((BagInfoVO)recordForm.getViewObject()).setDeleteFile((boolean) event.getProperty().getValue());
            }
        });

        limitSizeCheckbox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ((BagInfoVO)recordForm.getViewObject()).setLimitSize((boolean) event.getProperty().getValue());
            }
        });

        limitSizeCheckbox.addStyleName(STYLE_FIELD);
        deleteCheckBox.addStyleName(STYLE_FIELD);
        cb.setWidth("100%");
        layout.addComponents(limitSizeCheckbox, deleteCheckBox, cb, new Hr(), recordForm);
        return layout;
    }

    private void updateValue(BagInfoVO viewObject) {
        if(viewObject.getId().isEmpty()) {
            for(Field field : recordForm.getFields()) {
                field.setValue("");
            }
        } else {
            for(MetadataVO metadataVO : viewObject.getFormMetadatas()) {
                Field field = recordForm.getField(metadataVO.getCode());
                if(field != null) {
                    field.setValue(viewObject.<String>get(metadataVO));
                }
            }
        }

    }

    protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {

    }

    static class BagInfoRecordForm extends RecordForm{
        public BagInfoRecordForm(BagInfoVO viewObject, MetadataFieldFactory metadataFactory, FieldAndPropertyId... fields) {
            super(viewObject, metadataFactory);
        }

        @Override
        protected void saveButtonClick(RecordVO viewObject) throws ValidationException {

        }

        @Override
        protected void cancelButtonClick(RecordVO viewObject) {

        }

        private static List<FieldAndPropertyId> buildFields(BagInfoVO recordVO, RecordFieldFactory formFieldFactory, FieldAndPropertyId... fields) {
            List<FieldAndPropertyId> fieldsAndPropertyIds = buildInitialFields(fields);
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

        @Override
        protected String getTabCaption(Field<?> field, Object propertyId) {
            return null;
        }

        private static List<FieldAndPropertyId> buildInitialFields(FieldAndPropertyId... fields) {
            return new ArrayList<>(asList(fields));
        }
    }

    private class Hr extends Label {
        Hr() {
            super("<hr/>", Label.CONTENT_XHTML);
        }
    }
}
