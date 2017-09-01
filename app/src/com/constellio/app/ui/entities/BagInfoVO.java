package com.constellio.app.ui.entities;

import java.util.List;

public class BagInfoVO  {

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

    private String encodage;

    public BagInfoVO() {
        setNote("");
        setIdentificationEntiteResponsable("");
        setIdentificationOrganismeVerseurOuDonateur("");
        setIDOrganismeVerseurOuDonateur("");
        setAddress("");
        setRegionAdministrative("");
        setEntiteResponsable("");
        setCourrielResponsable("");
        setTelephoneResponsable("");
        setDescriptionSommaire("");
        setCategoryDocument("");
        setMethodeTransfere("");
        setRestrictionAccessibilite("");
        setEncodage("");

    }

    public String getNote() {
        return note;
    }

    public BagInfoVO setNote(String note) {
        this.note = note;
        return this;
    }

    public String getIdentificationOrganismeVerseurOuDonateur() {
        return identificationOrganismeVerseurOuDonateur;
    }

    public BagInfoVO setIdentificationOrganismeVerseurOuDonateur(String identificationOrganismeVerseurOuDonateur) {
        this.identificationOrganismeVerseurOuDonateur = identificationOrganismeVerseurOuDonateur;
        return this;
    }

    public String getIDOrganismeVerseurOuDonateur() {
        return IDOrganismeVerseurOuDonateur;
    }

    public BagInfoVO setIDOrganismeVerseurOuDonateur(String IDOrganismeVerseurOuDonateur) {
        this.IDOrganismeVerseurOuDonateur = IDOrganismeVerseurOuDonateur;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public BagInfoVO setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getRegionAdministrative() {
        return regionAdministrative;
    }

    public BagInfoVO setRegionAdministrative(String regionAdministrative) {
        this.regionAdministrative = regionAdministrative;
        return this;
    }

    public String getEntiteResponsable() {
        return entiteResponsable;
    }

    public BagInfoVO setEntiteResponsable(String entiteResponsable) {
        this.entiteResponsable = entiteResponsable;
        return this;
    }

    public String getIdentificationEntiteResponsable() {
        return identificationEntiteResponsable;
    }

    public BagInfoVO setIdentificationEntiteResponsable(String identificationEntiteResponsable) {
        this.identificationEntiteResponsable = identificationEntiteResponsable;
        return this;
    }

    public String getCourrielResponsable() {
        return courrielResponsable;
    }

    public BagInfoVO setCourrielResponsable(String courrielResponsable) {
        this.courrielResponsable = courrielResponsable;
        return this;
    }

    public String getTelephoneResponsable() {
        return telephoneResponsable;
    }

    public BagInfoVO setTelephoneResponsable(String telephoneResponsable) {
        this.telephoneResponsable = telephoneResponsable;
        return this;
    }

    public String getDescriptionSommaire() {
        return descriptionSommaire;
    }

    public BagInfoVO setDescriptionSommaire(String descriptionSommaire) {
        this.descriptionSommaire = descriptionSommaire;
        return this;
    }

    public String getCategoryDocument() {
        return categoryDocument;
    }

    public BagInfoVO setCategoryDocument(String categoryDocument) {
        this.categoryDocument = categoryDocument;
        return this;
    }

    public String getMethodeTransfere() {
        return methodeTransfere;
    }

    public BagInfoVO setMethodeTransfere(String methodeTransfere) {
        this.methodeTransfere = methodeTransfere;
        return this;
    }

    public String getRestrictionAccessibilite() {
        return restrictionAccessibilite;
    }

    public BagInfoVO setRestrictionAccessibilite(String restrictionAccessibilite) {
        this.restrictionAccessibilite = restrictionAccessibilite;
        return this;
    }

    public String getEncodage() {
        return encodage;
    }

    public BagInfoVO setEncodage(String encodage) {
        this.encodage = encodage;
        return this;
    }
}
