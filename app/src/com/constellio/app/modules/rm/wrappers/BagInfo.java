package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class BagInfo extends RecordWrapper {
    public final static String SCHEMA_TYPE = "bagInfo";

    public final static String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

    public final static String IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR = "identificationOrganismeVerseurOuDonateur";

    public final static String ID_ORGANISME_VERSEUR_OU_DONATEUR = "IdOrganismeVerseurOuDonateur";

    public final static String ADRESSE = "address";

    public final static String REGION_ADMINISTRATIVE = "regionAdministrative";

    public final static String ENTITE_RESPONSABLE = "entiteResponsable";

    public final static String IDENTIFICATION_RESPONSABLE = "identificationResponsable";

    public final static String COURRIEL_RESPONSABLE = "courrielResponsable";

    public final static String NUMERO_TELEPHONE_RESPONSABLE = "numeroTelephoneResponsable";

    public final static String DESCRIPTION_SOMMAIRE = "descriptionSommaire";

    public final static String CATEGORIE_DOCUMENT = "categoryDocument";

    public final static String METHODE_TRANSFERE = "methodTransfere";

    public final static String RESTRICTION_ACCESSIBILITE = "restrictionAccessibilite";

    public final static String ENCODAGE = "encodage";

    public final static String NOTE = "note";

    public BagInfo(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public String getIdentificationOrganisme() {
        return get(IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR);
    }

    public String getIDOrganismeVerseurOuDonateur() {
        return get(ID_ORGANISME_VERSEUR_OU_DONATEUR);
    }

    public String getAdresse() {
        return get(ADRESSE);
    }

    public String getRegionAdministrative() {
        return get(REGION_ADMINISTRATIVE);
    }

    public String getEntiteResponsable() {
        return get(ENTITE_RESPONSABLE);
    }

    public String getIdentificationResponsable(){
        return get(IDENTIFICATION_RESPONSABLE);
    }

    public String getCourrielResponsable() {
        return get(COURRIEL_RESPONSABLE);
    }

    public String getNumeroTelephoneResponsable() {
        return get(NUMERO_TELEPHONE_RESPONSABLE);
    }

    public String getDescriptionSommaire() {
        return get(DESCRIPTION_SOMMAIRE);
    }

    public String getCategoryDocument() {
        return get(CATEGORIE_DOCUMENT);
    }

    public String getMethodTransfert() {
        return get(METHODE_TRANSFERE);
    }

    public String getRestrictionAccessibilite() {
        return get(RESTRICTION_ACCESSIBILITE);
    }

    public String getEncodage() {
        return get(ENCODAGE);
    }

    public String getNote() {
        return get(NOTE);
    }


    public BagInfo setIdentificationOrganisme(String value) {
        return set(IDENTIFICATION_ORGANISME_VERSEUR_OU_DONATEUR, value);
    }

    public BagInfo getIDOrganismeVerseurOuDonateur(String value) {
        return set(ID_ORGANISME_VERSEUR_OU_DONATEUR, value);
    }

    public BagInfo setAdresse(String value) {
        return set(ADRESSE, value);
    }

    public BagInfo setRegionAdministrative(String value) {
        return set(REGION_ADMINISTRATIVE, value);
    }

    public BagInfo setEntiteResponsable(String value) {
        return set(ENTITE_RESPONSABLE, value);
    }

    public BagInfo setIdentificationResponsable(String value) {
        return set(IDENTIFICATION_RESPONSABLE, value);
    }

    public BagInfo setCourrielResponsable(String value) {
        return set(COURRIEL_RESPONSABLE, value);
    }

    public BagInfo setNumeroTelephoneResponsable(String value) {
        return set(NUMERO_TELEPHONE_RESPONSABLE, value);
    }

    public BagInfo setDescriptionSommaire(String value) {
        return set(DESCRIPTION_SOMMAIRE, value);
    }

    public BagInfo setCategoryDocument(String value) {
        return set(CATEGORIE_DOCUMENT, value);
    }

    public BagInfo setMethodTransfert(String value) {
        return set(METHODE_TRANSFERE, value);
    }

    public BagInfo setRestrictionAccessibilite(String value) {
        return set(RESTRICTION_ACCESSIBILITE, value);
    }

    public BagInfo setEncodage(String value) {
        return set(ENCODAGE, value);
    }

    public BagInfo setNote(String value) {
        return set(NOTE, value);
    }

}
