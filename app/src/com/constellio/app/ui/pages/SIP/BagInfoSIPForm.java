package com.constellio.app.ui.pages.SIP;

import com.constellio.app.ui.entities.BagInfoVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.SIPButton.BagInfoForm;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BagInfoSIPForm extends BaseViewImpl {

    private List<RecordVO> objectList;

    private BagInfoVO bagInfoVO;

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
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        bagInfoVO = new BagInfoVO();
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

        return new BagInfoForm(bagInfoVO, this.objectList, this,
                limitSizeCheckbox,
                deleteCheckBox,
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
                descriptionSommaire);

    }
}
