package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import static com.constellio.app.ui.i18n.i18n.$;

public class BagInfoSIPForm extends BaseViewImpl {
    private BagInfoSIPPresenter presenter;

    @PropertyId("deleteFile")
    private CheckBox deleteCheckBox;

    @PropertyId("limitSize")
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
        ComboBox cb = new ComboBox($("SIPButton.predefinedBagInfo"));
        for(BagInfoVO bagInfoVO : presenter.getAllBagInfo()) {
            cb.addItem(bagInfoVO);
            cb.setItemCaption(bagInfoVO, bagInfoVO.getTitle());
        }
        cb.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateValue((BagInfoVO) event.getProperty().getValue());
            }
        });

        limitSizeCheckbox = new CheckBox($("SIPButton.limitSize"));
        limitSizeCheckbox.setId("limitSize");

        deleteCheckBox = new CheckBox($("SIPButton.deleteFilesLabel"));
        deleteCheckBox.setId("deleteFile");

        identificationOrganismeTextField = new TextField($("BagInfoForm.identificationOrganisme"));
        identificationOrganismeTextField.setId("identificationOrganismeVerseurOuDonateur");
        identificationOrganismeTextField.setNullRepresentation("");

        IDOrganismeTextField = new TextField($("BagInfoForm.IDOrganisme"));
        IDOrganismeTextField.setId("IDOrganismeVerseurOuDonateur");
        IDOrganismeTextField.setNullRepresentation("");

        adresseTextField = new TextField($("BagInfoForm.address"));
        adresseTextField.setId("address");
        adresseTextField.setNullRepresentation("");

        regionAdministrativeTextField = new TextField($("BagInfoForm.regionAdministrative"));
        regionAdministrativeTextField.setId("regionAdministrative");
        regionAdministrativeTextField.setNullRepresentation("");

        entiteResponsableTextField = new TextField($("BagInfoForm.entiteResponsable"));
        entiteResponsableTextField.setId("entiteResponsable");
        entiteResponsableTextField.setNullRepresentation("");

        identificationEntiteResponsableTextField = new TextField($("BagInfoForm.identificationEntiteResponsable"));
        identificationEntiteResponsableTextField.setId("identificationEntiteResponsable");
        identificationEntiteResponsableTextField.setNullRepresentation("");

        courrielResponsableTextField = new TextField($("BagInfoForm.courrielResponsable"));
        courrielResponsableTextField.setId("courrielResponsable");
        courrielResponsableTextField.setNullRepresentation("");

        telephoneResponsableTextField = new TextField($("BagInfoForm.telephoneResponsable"));
        telephoneResponsableTextField.setId("telephoneResponsable");
        telephoneResponsableTextField.setNullRepresentation("");

        categoryDocumentTextField = new TextField($("BagInfoForm.categoryDocument"));
        categoryDocumentTextField.setId("categoryDocument");
        categoryDocumentTextField.setNullRepresentation("");

        methodeTransfereTextField = new TextField($("BagInfoForm.methodeTransfere"));
        methodeTransfereTextField.setId("methodeTransfere");
        methodeTransfereTextField.setNullRepresentation("");

        restrictionAccessibiliteTextField = new TextField($("BagInfoForm.restrictionAccessibilite"));
        restrictionAccessibiliteTextField.setId("restrictionAccessibilite");
        restrictionAccessibiliteTextField.setNullRepresentation("");

        noteTextArea = new TextArea($("BagInfoForm.note"));
        noteTextArea.setId("note");
        noteTextArea.setNullRepresentation("");

        descriptionSommaire = new TextArea($("BagInfoForm.descriptionSommaire"));
        descriptionSommaire.setId("descriptionSommaire");
        descriptionSommaire.setNullRepresentation("");

        return new BaseForm<BagInfoVO>(presenter.newRecord(),this,
                limitSizeCheckbox,
                deleteCheckBox,
                cb,
                identificationOrganismeTextField,
                IDOrganismeTextField,
                adresseTextField,
                regionAdministrativeTextField,
                entiteResponsableTextField,
                identificationEntiteResponsableTextField,
                courrielResponsableTextField,
                telephoneResponsableTextField,
                categoryDocumentTextField,
                methodeTransfereTextField,
                restrictionAccessibiliteTextField,
                noteTextArea,
                descriptionSommaire) {
            @Override
            protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {
                BagInfoSIPForm.this.saveButtonClick(viewObject);
            }

            @Override
            protected void cancelButtonClick(BagInfoVO viewObject) {
                navigateTo().previousView();
            }
        };

    }

    private void updateValue(BagInfoVO viewObject) {
        identificationOrganismeTextField.setValue(viewObject.getIDOrganismeVerseurOuDonateur());
        IDOrganismeTextField.setValue(viewObject.getIdentificationOrganismeVerseurOuDonateur());
        adresseTextField.setValue(viewObject.getAddress());
        regionAdministrativeTextField.setValue(viewObject.getRegionAdministrative());
        entiteResponsableTextField.setValue(viewObject.getEntiteResponsable());
        identificationEntiteResponsableTextField.setValue(viewObject.getIdentificationEntiteResponsable());
        courrielResponsableTextField.setValue(viewObject.getCourrielResponsable());
        telephoneResponsableTextField.setValue(viewObject.getTelephoneResponsable());
        categoryDocumentTextField.setValue(viewObject.getCategoryDocument());
        methodeTransfereTextField.setValue(viewObject.getMethodeTransfere());
        restrictionAccessibiliteTextField.setValue(viewObject.getRestrictionAccessibilite());
        noteTextArea.setValue(viewObject.getNote());
        descriptionSommaire.setValue(viewObject.getDescriptionSommaire());
    }

    protected void saveButtonClick(BagInfoVO viewObject) throws ValidationException {

    }

    @Override
    protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
        return null;
    }
}
