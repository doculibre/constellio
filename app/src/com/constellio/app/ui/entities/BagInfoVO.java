package com.constellio.app.ui.entities;

import java.util.List;

import static com.constellio.app.modules.rm.wrappers.BagInfo.*;

public class BagInfoVO extends RecordVO {

    private boolean limitSize;

    private boolean deleteFile;

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
        return get(NOTE);
    }

    public void setNote(String note) {
        set(NOTE, note);
    }

    public String getIdentificationOrganismeVerseurOuDonateur() {
        return get(IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR);
    }

    public void setIdentificationOrganismeVerseurOuDonateur(String identificationOrganismeVerseurOuDonateur) {
        set(IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR, identificationOrganismeVerseurOuDonateur);
    }

    public String getIDOrganismeVerseurOuDonateur() {
        return get(ID_ORGANISME_VERSEUR_OU_DONATEUR);
    }

    public void setIDOrganismeVerseurOuDonateur(String IDOrganismeVerseurOuDonateur) {
        set(ID_ORGANISME_VERSEUR_OU_DONATEUR, IDOrganismeVerseurOuDonateur);
    }

    public String getAddress() {
        return get(ADRESSE);
    }

    public void setAddress(String address) {
        set(ADRESSE, address);
    }

    public String getRegionAdministrative() {
        return get(REGION_ADMINISTRATIVE);
    }

    public void setRegionAdministrative(String regionAdministrative) {
        set(REGION_ADMINISTRATIVE, regionAdministrative);
    }

    public String getEntiteResponsable() {
        return get(ENTITE_RESPONSABLE);
    }

    public void setEntiteResponsable(String entiteResponsable) {
        set(ENTITE_RESPONSABLE, entiteResponsable);
    }

    public String getIdentificationEntiteResponsable() {
        return get(IDENTIFICATION_RESPONSABLE);
    }

    public void setIdentificationEntiteResponsable(String identificationEntiteResponsable) {
        set(IDENTIFICATION_RESPONSABLE, identificationEntiteResponsable);
    }

    public String getCourrielResponsable() {
        return get(COURRIEL_RESPONSABLE);
    }

    public void setCourrielResponsable(String courrielResponsable) {
        set(COURRIEL_RESPONSABLE, courrielResponsable);
    }

    public String getTelephoneResponsable() {
        return get(NUMERO_TELEPHONE_RESPONSABLE);
    }

    public void setTelephoneResponsable(String telephoneResponsable) {
        set(NUMERO_TELEPHONE_RESPONSABLE, telephoneResponsable);
    }

    public String getArchiveTitle() {
        return get(ARCHIVE_TITLE);
    }

    public void setArchiveTitle(String title) {
        set(ARCHIVE_TITLE, title);
    }

    public String getDescriptionSommaire() {
        return get(DESCRIPTION_SOMMAIRE);
    }

    public void setDescriptionSommaire(String descriptionSommaire) {
        set(DESCRIPTION_SOMMAIRE, descriptionSommaire);
    }

    public String getCategoryDocument() {
        return get(CATEGORIE_DOCUMENT);
    }

    public void setCategoryDocument(String categoryDocument) {
        set(CATEGORIE_DOCUMENT, categoryDocument);
    }

    public String getMethodeTransfere() {
        return get(METHODE_TRANSFERE);
    }

    public void setMethodeTransfere(String methodeTransfere) {
       set(METHODE_TRANSFERE, methodeTransfere);
    }

    public String getRestrictionAccessibilite() {
        return get(RESTRICTION_ACCESSIBILITE);
    }

    public String getEncoding() {
        return get(ENCODAGE);
    }

    public void setEncoding(String encoding){
        set(ENCODAGE, encoding);
    }

    public void setRestrictionAccessibilite(String restrictionAccessibilite) {
        set(RESTRICTION_ACCESSIBILITE, restrictionAccessibilite);
    }

    @Override
    public <T> T get(String metadataCode) {
        try{
            return super.get(metadataCode);
        } catch(RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata e ){
            return null;
        }
    }
}
