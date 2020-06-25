"use strict";

function SignatureAnnotationPicker(signatureDataStore, dropZoneManager) {
	SignaturePicker.call(this, signatureDataStore);
    this.dropZoneManager = dropZoneManager;
}

SignatureAnnotationPicker.prototype = Object.create(SignaturePicker.prototype);
SignatureAnnotationPicker.prototype.constructor = SignatureAnnotationPicker;

SignatureAnnotationPicker.prototype.signHereAnnotationPicked = function() {
    var signHereAnnotation = new SignHereAnnotation();
    this.manageSignHereAnnotation(signHereAnnotation);
    this.dropZoneManager.defineAnnotation(signHereAnnotation);
};		

SignatureAnnotationPicker.prototype.manageSignHereAnnotation = function(signHereAnnotation) {
    var self = this;

    var signHereAnnotationEditor = new AnnotationEditor(); 
    signHereAnnotationEditor.open = function(annotation, callbackContext, saveCallback, cancelCallback) {
        self.openPicker(signHereAnnotation);
    };
    signHereAnnotation.setEditor(signHereAnnotationEditor);
};

SignatureAnnotationPicker.prototype.replaceSignHereAnnotation = function(signHereAnnotation, newAnnotation) {
    var self = this;
    var defaultRemove = newAnnotation.remove;
    if (!defaultRemove) {
        defaultRemove = Object.getPrototypeOf(newAnnotation).remove;
    }    
    newAnnotation.remove = function(e) {
        if (defaultRemove.call(newAnnotation)) {
            self.dropZoneManager.loadAnnotation(signHereAnnotation);           
        }
    };    
    var defaultBind = newAnnotation.bind;
    if (!defaultBind) {
        defaultBind = Object.getPrototypeOf(newAnnotation).bind;
    }
    newAnnotation.bind = function(htmlElement) {
        defaultBind.call(newAnnotation, htmlElement);
        signHereAnnotation.remove();        
    };
    this.dropZoneManager.loadAnnotation(newAnnotation);
    this.dropZoneManager.onAnnotationSaved(newAnnotation);
};    

SignatureAnnotationPicker.prototype.drawAnnotationPicked = function(signHereAnnotation, initials, saveCallback) {
    var signaturePadAnnotation = new SignaturePadAnnotation();
    signaturePadAnnotation.setInitials(initials);
    if (signHereAnnotation) {
        signaturePadAnnotation.setX(signHereAnnotation.getX());
        signaturePadAnnotation.setY(signHereAnnotation.getY());
        signaturePadAnnotation.setWidth(signHereAnnotation.getWidth());
        signaturePadAnnotation.setHeight(signHereAnnotation.getHeight());
    }
    signaturePadAnnotation.editor.closeWindow = function() {			
        var imageUrl = signaturePadAnnotation.editor.getImageUrl();
        saveCallback(imageUrl);
        SignaturePadAnnotationEditor.prototype.closeWindow.call(this);
    };

    if (signHereAnnotation) {
        this.replaceSignHereAnnotation(signHereAnnotation, signaturePadAnnotation);
        signaturePadAnnotation.openEditor();        
    } else {
        this.dropZoneManager.defineAnnotation(signaturePadAnnotation);
    }
};

SignatureAnnotationPicker.prototype.imageAnnotationPicked = function(signHereAnnotation, initials, imageUrl) {
    var signatureImageAnnotation = new SignatureImageAnnotation(imageUrl);
    signatureImageAnnotation.setInitials(initials);
    if (signHereAnnotation) {
        signatureImageAnnotation.setX(signHereAnnotation.getX());
        signatureImageAnnotation.setY(signHereAnnotation.getY());
        signatureImageAnnotation.setWidth(signHereAnnotation.getWidth());
        signatureImageAnnotation.setHeight(signHereAnnotation.getHeight());
    }

    if (signHereAnnotation) {
        this.replaceSignHereAnnotation(signHereAnnotation, signatureImageAnnotation);
    } else {
        this.dropZoneManager.defineAnnotation(signatureImageAnnotation);
    }
};	

SignatureAnnotationPicker.prototype.textAnnotationPicked = function(signHereAnnotation, initials) {
    var signatureTextAnnotation = new SignatureTextAnnotation();
    signatureTextAnnotation.setInitials(initials);
    if (signHereAnnotation) {
        signatureTextAnnotation.setX(signHereAnnotation.getX());
        signatureTextAnnotation.setY(signHereAnnotation.getY());
        signatureTextAnnotation.setWidth(signHereAnnotation.getWidth());
        signatureTextAnnotation.setHeight(signHereAnnotation.getHeight());
    }

    if (signHereAnnotation) {
        this.replaceSignHereAnnotation(signHereAnnotation, signatureTextAnnotation);
        signatureTextAnnotation.openEditor();        
    } else {
        this.dropZoneManager.defineAnnotation(signatureTextAnnotation);
    }
};	
