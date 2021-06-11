"use strict";

function PDFAnnotationsManager(config, signatureDataStore, signatureOptionsElement, pdfViewerElement) {
    this.config = config;
    this.signatureDataStore = signatureDataStore;
    this.signatureOptionsElement = signatureOptionsElement; 
    this.pdfViewerElement = pdfViewerElement;
    this.pdfAnnotations = new PDFAnnotations();
    this.currentPage = 1;
    this.dropZoneManagers = [];      
    this.signatureAnnotationPickers = {};
    this.pdfAnnotationsServices = new PDFAnnotationsServices(config);

    if (this.config) {
        this.username = config["username"];
    }
    
    var self = this;
	window.addEventListener("pagerendered", function(e) {
        // Necessary because a pagechange event will not be sent when the first page is loaded with the document
        if (self.currentPage == 1) {
            var pageAnnotations = self.pdfAnnotations.getPageAnnotations(self.currentPage);
            if (pageAnnotations && pageAnnotations.length > 0 && !self.isDropZoneManager(self.currentPage)) {
                self.newDropZoneManager(self.currentPage);
            }
        }
    });    
	document.addEventListener("pagechange", function(e) {
        var pageNumber = e.pageNumber;
        self.setCurrentPage(pageNumber);
    });
    
    this.loadPDFAnnotations();
};   

PDFAnnotationsManager.prototype.getCurrentPage = function() {
    return this.currentPage;
};

PDFAnnotationsManager.prototype.setCurrentPage = function(currentPage) {
    this.currentPage = currentPage;
    var pageAnnotations = this.pdfAnnotations.getPageAnnotations(this.currentPage);
    if (pageAnnotations && pageAnnotations.length > 0 && !this.isDropZoneManager(currentPage)) {
        this.newDropZoneManager(currentPage);
    }
};

PDFAnnotationsManager.prototype.getDropZoneManagers = function() {
    return this.dropZoneManagers;
};

PDFAnnotationsManager.prototype.isDropZoneManager = function(pageNumber) {
    return this.dropZoneManagers[pageNumber];
};

PDFAnnotationsManager.prototype.geDropZoneManager = function(pageNumber) {
    if (!this.isDropZoneManager(pageNumber)) {
        this.newDropZoneManager(pageNumber);
    }
    return this.dropZoneManagers[this.currentPage];
};

PDFAnnotationsManager.prototype.getCurrentDropZoneManager = function() {
    return this.geDropZoneManager(this.currentPage);
};

PDFAnnotationsManager.prototype.getDropZoneSelector = function(pageNumber) {
    var viewerSelector = this.pdfViewerElement && this.pdfViewerElement.id ? this.pdfViewerElement.id : ".pdfViewer";
    //var dropZoneSelector = viewerSelector + " .page[data-page-number='" + pageNumber + "'] .canvasWrapper";
    var dropZoneSelector = viewerSelector + " .page[data-page-number='" + pageNumber + "'] .customAnnotationLayer";
    return dropZoneSelector;
};    

PDFAnnotationsManager.prototype.newDropZoneManager = function(pageNumber) {
    var self = this;
    var dropZoneSelector = this.getDropZoneSelector(pageNumber);
    var dropZoneManager = new AnnotationDropZoneManager(dropZoneSelector);
    dropZoneManager.onAnnotationDefined = function(annotation) {
        var pageAnnotations = dropZoneManager.getAnnotations();
        self.pdfAnnotations.setPageAnnotations(pageNumber, pageAnnotations);
        self.savePDFAnnotations();
    };
    dropZoneManager.onAnnotationSaved = function(annotation) {
        var pageAnnotations = dropZoneManager.getAnnotations();
        self.pdfAnnotations.setPageAnnotations(pageNumber, pageAnnotations);
        self.savePDFAnnotations();
    };
    dropZoneManager.onAnnotationRemoved = function(annotation) {
        var pageAnnotations = dropZoneManager.getAnnotations();
        self.pdfAnnotations.setPageAnnotations(pageNumber, pageAnnotations);
        self.savePDFAnnotations();
    };
    dropZoneManager.onAnnotationResized = function(annotation) {
        self.savePDFAnnotations();
    };
    dropZoneManager.onAnnotationMoved = function(annotation) {
        self.savePDFAnnotations();
    };

    var pageAnnotations = this.pdfAnnotations.getPageAnnotations(pageNumber);
    if (pageAnnotations) {
        dropZoneManager.loadAnnotations(pageAnnotations);

        var signaturePicker = self.getSignaturePicker(pageNumber, dropZoneManager);
        if (signaturePicker) {
            var pageSignHereAnnotations = this.pdfAnnotations.getPageTypeAnnotations(pageNumber, "sign-here-annotation");
            for (var i = 0; i < pageSignHereAnnotations.length; i++) {
                var signHereAnnotation = pageSignHereAnnotations[i];
                signaturePicker.manageSignHereAnnotation(signHereAnnotation);
            }
        }    
    }
    this.dropZoneManagers[pageNumber] = dropZoneManager;
};

PDFAnnotationsManager.prototype.defineSignHereAnnotation = function() {
    var signaturePicker = this.getCurrentSignaturePicker();
    signaturePicker.signHereAnnotationPicked();
};   

PDFAnnotationsManager.prototype.defineTextAnnotation = function() {
    var dropZoneManager = this.getCurrentDropZoneManager();
    var textAnnotation = new TextAnnotation();
    dropZoneManager.defineAnnotation(textAnnotation);
};   

PDFAnnotationsManager.prototype.openSignaturePicker = function() {
    var signaturePicker = this.getCurrentSignaturePicker();
    signaturePicker.openPicker();
};    

PDFAnnotationsManager.prototype.getCurrentSignaturePicker = function(pageNumber, dropZoneManager) {
    var dropZoneManager = this.getCurrentDropZoneManager();
    return this.getSignaturePicker(this.currentPage, dropZoneManager);
};    

PDFAnnotationsManager.prototype.getSignaturePicker = function(pageNumber, dropZoneManager) {
    var pageSignatureAnnotationPicker = this.signatureAnnotationPickers[pageNumber];
    if (!pageSignatureAnnotationPicker) {
        pageSignatureAnnotationPicker = new SignatureAnnotationPicker(this.signatureDataStore, dropZoneManager, this.signatureOptionsElement);
        this.signatureAnnotationPickers[pageNumber] = pageSignatureAnnotationPicker;
    }
    return pageSignatureAnnotationPicker;
};    

PDFAnnotationsManager.prototype.loadPDFAnnotations = function() {
    var self = this;
    this.pdfAnnotationsServices.getPDFAnnotations(
        function(pdfAnnotations) {
            self.pdfAnnotations = pdfAnnotations;
        },
        function(textStatus, errorThrown) {
            console.error("Unable to load PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
        });
};    

PDFAnnotationsManager.prototype.savePDFAnnotations = function() {
    var self = this;
    this.pdfAnnotationsServices.savePDFAnnotations(this.pdfAnnotations, 
        function(newVersion) {
            self.pdfAnnotations.setVersion(newVersion);
        },
        function(textStatus, errorThrown) {
            console.error("Unable to save PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
        });
};   

PDFAnnotationsManager.prototype.certifyPDFSignatures = function() {
    var self = this;

    var certifyDialogWindow = document.createElement("div");
    certifyDialogWindow.classList.add("certify-dialog-window");

    var certifyDialogWindowContent = document.createElement("div");
    certifyDialogWindowContent.classList.add("certify-dialog-window-content");
    
    var dialogTitleElement = document.createElement("div");
    dialogTitleElement.innerHTML = self.i10n("pdf-annotations-manager.certify-dialog.title", "Clicking on the Certify button will alter the document to add your signature.");
    dialogTitleElement.classList.add("certify-dialog-title");
    
    var progressMessageElement = document.createElement("div");
    progressMessageElement.classList.add("certify-dialog-progress");
    
    var actionButtonsElement = document.createElement("div");
    actionButtonsElement.classList.add("certify-dialog-action-buttons");
    
    var certifyButton = document.createElement("button");
    certifyButton.innerHTML = self.i10n("buttons.certify", "Certify");
    certifyButton.classList.add("certify-dialog-certify-button");
    certifyButton.classList.add("primary");
    
    var cancelButton = document.createElement("button");
    cancelButton.innerHTML = self.i10n("buttons.cancel", "Cancel");
    cancelButton.classList.add("certify-dialog-cancel-button");
    
    var closeButton = document.createElement("button");
    closeButton.innerHTML = self.i10n("buttons.close", "Close");
    closeButton.classList.add("certify-dialog-close-button");
    
    var setVisible = function(htmlElement, visible) {
        if (visible) {
            htmlElement.style.display = "";
        } else {
            htmlElement.style.display = "none";
        }
    };

    var reloadOnClose = false;

    var closeDialog = function() {
        if (reloadOnClose) {
            location.reload();
        } else if (certifyDialogWindow.parentNode) {
            certifyDialogWindow.parentNode.removeChild(certifyDialogWindow);
        }
    };

    var setLoading = function(loading) {
        if (loading) {
            progressMessageElement.classList.add("certify-dialog-loading");
        } else {
            progressMessageElement.classList.remove("certify-dialog-loading");
        }
    };

    var updateProgress = function(progressMessage, done, reloadIfDone) {
        if (done && reloadIfDone) {
            reloadOnClose = reloadIfDone;
        }
        if (done) {
            setLoading(false);
            setVisible(closeButton, true);
        }
        progressMessageElement.innerHTML = progressMessage;
    };

    var certifyFailed = function(errorMessage) {
        setLoading(false);
        setVisible(closeButton, true);
        progressMessageElement.classList.add("certify-dialog-error");
        progressMessageElement.innerHTML = errorMessage;
    };
 
    var confirmCertify = function() {
        self.pdfAnnotationsServices.certifyPDFSignatures(self.pdfAnnotations, 
            function() {
                console.info("certification process successful!");
                var successMessage = self.i10n("pdf-annotations-manager.documentCertified", "The document was successfully signed and certified.");
                updateProgress(successMessage, true, true);
            },
            function(textStatus, errorThrown) {
                if (errorThrown) {
                    var errorMessage;
                    if (errorThrown == "cannotAddAnnotation") {
                    	errorMessage = self.i10n("pdf-annotations-manager.documentCertificationError.cannotAddAnnotation", "Cannot add annotation.");
                    } else if (errorThrown == "cannotAddSignature") {
                    	errorMessage = self.i10n("pdf-annotations-manager.documentCertificationError.cannotAddSignature", "Cannot add signature.");
                    } else {
                    	errorMessage = self.i10n("pdf-annotations-manager.documentCertificationError", "The document was not signed and was not certified. Check your connection and try again.");
                    }
                    certifyFailed(errorMessage);
                    console.error("Unable to save PDF annotations (status: " + textStatus + ")");
                    console.error(errorThrown);
                } else {
                    certifyFailed(textStatus);
                }
            }
        );
    };

    cancelButton.onclick = function() {
        closeDialog();
    };

    certifyButton.onclick = function() {
        setLoading(true);
        
        var certificationStartedMessage = self.i10n("pdf-annotations-manager.documentCertificationStarted", "Please wait while the document is being signed and certified...");
        updateProgress(certificationStartedMessage, false);
        setVisible(dialogTitleElement, false);
        setVisible(certifyButton, false);
        setVisible(cancelButton, false);
        
        confirmCertify();
    };

    closeButton.onclick = function() {
        closeDialog();
    };

    setVisible(closeButton, false);
    
    certifyDialogWindow.appendChild(certifyDialogWindowContent);
    certifyDialogWindowContent.appendChild(dialogTitleElement);
    certifyDialogWindowContent.appendChild(progressMessageElement);
    certifyDialogWindowContent.appendChild(actionButtonsElement);
    actionButtonsElement.appendChild(certifyButton);
    actionButtonsElement.appendChild(cancelButton);
    actionButtonsElement.appendChild(closeButton);

    document.body.appendChild(certifyDialogWindow);
};    

PDFAnnotationsManager.prototype.refreshDropZone = function(pageNumber) {
    var self = this;
    setTimeout(function() {
        var dropZoneManager = self.dropZoneManagers[pageNumber];
        if (dropZoneManager) {
            dropZoneManager.refreshAnnotations();        
        }
    }, 100);    
};    

PDFAnnotationsManager.prototype.refreshDropZones = function() {
    var dropZonePageNumbers = Object.keys(this.dropZoneManagers);
    for (var i = 0; i < dropZonePageNumbers.length; i++) {
        var pageNumber = dropZonePageNumbers[i];
        this.refreshDropZone(pageNumber);
    }    
};

PDFAnnotationsManager.prototype.i10n = function(key, defaultValue) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
        value = mozL10n.get(key, null, null);
        if (!value || value.indexOf("{{") == 0) {
            value = defaultValue;
        }
	} else {
		value = defaultValue;
	}
	return value;
};

