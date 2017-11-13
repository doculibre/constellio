package com.constellio.app.ui.entities;

import java.util.List;

public class BagInfoVO extends RecordVO{

    private boolean limitSize;

    private boolean deleteFile;

    private String id;

    private String note;

    private String identificationOrganismeVerseurOuDonateur;

    private String IDOrganismeVerseurOuDonateur;

    private String address;

    private String regionAdministrative;

    private String entiteResponsable;

    private String identificationEntiteResponsable;

    private String courrielResponsable;

    private String telephoneResponsable;

    private String descriptionSommaire;

    private String categoryDocument;

    private String methodeTransfere;

    private String restrictionAccessibilite;

    public BagInfoVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
        super(id, metadataValues, viewMode);
    }

    public boolean isLimitSize() {
        return limitSize;
    }

    public void setLimitSize(boolean limitSize) {
        this.limitSize = limitSize;
    }

    public boolean isDeleteFile() {
        return deleteFile;
    }

    public void setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
    }

    public String getNote() {
        return note != null ? note : "";
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getIdentificationOrganismeVerseurOuDonateur() {
        return identificationOrganismeVerseurOuDonateur != null ? identificationOrganismeVerseurOuDonateur : "";
    }

    public void setIdentificationOrganismeVerseurOuDonateur(String identificationOrganismeVerseurOuDonateur) {
        this.identificationOrganismeVerseurOuDonateur = identificationOrganismeVerseurOuDonateur;
    }

    public String getIDOrganismeVerseurOuDonateur() {
        return IDOrganismeVerseurOuDonateur != null ? IDOrganismeVerseurOuDonateur : "";
    }

    public void setIDOrganismeVerseurOuDonateur(String IDOrganismeVerseurOuDonateur) {
        this.IDOrganismeVerseurOuDonateur = IDOrganismeVerseurOuDonateur;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegionAdministrative() {
        return regionAdministrative != null ? regionAdministrative : "";
    }

    public void setRegionAdministrative(String regionAdministrative) {
        this.regionAdministrative = regionAdministrative;
    }

    public String getEntiteResponsable() {
        return entiteResponsable != null ? entiteResponsable : "";
    }

    public void setEntiteResponsable(String entiteResponsable) {
        this.entiteResponsable = entiteResponsable;
    }

    public String getIdentificationEntiteResponsable() {
        return identificationEntiteResponsable != null ? identificationEntiteResponsable : "";
    }

    public void setIdentificationEntiteResponsable(String identificationEntiteResponsable) {
        this.identificationEntiteResponsable = identificationEntiteResponsable;
    }

    public String getCourrielResponsable() {
        return courrielResponsable != null ? courrielResponsable : "";
    }

    public void setCourrielResponsable(String courrielResponsable) {
        this.courrielResponsable = courrielResponsable;
    }

    public String getTelephoneResponsable() {
        return telephoneResponsable != null ? telephoneResponsable : "";
    }

    public void setTelephoneResponsable(String telephoneResponsable) {
        this.telephoneResponsable = telephoneResponsable;
    }

    public String getDescriptionSommaire() {
        return descriptionSommaire != null ? descriptionSommaire : "";
    }

    public void setDescriptionSommaire(String descriptionSommaire) {
        this.descriptionSommaire = descriptionSommaire;
    }

    public String getCategoryDocument() {
        return categoryDocument != null ? categoryDocument : "";
    }

    public void setCategoryDocument(String categoryDocument) {
        this.categoryDocument = categoryDocument;
    }

    public String getMethodeTransfere() {
        return methodeTransfere != null ? methodeTransfere  : "";
    }

    public void setMethodeTransfere(String methodeTransfere) {
        this.methodeTransfere = methodeTransfere;
    }

    public String getRestrictionAccessibilite() {
        return restrictionAccessibilite != null ? restrictionAccessibilite : "";
    }

    public void setRestrictionAccessibilite(String restrictionAccessibilite) {
        this.restrictionAccessibilite = restrictionAccessibilite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
