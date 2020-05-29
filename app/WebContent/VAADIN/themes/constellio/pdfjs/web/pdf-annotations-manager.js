"use strict";

function PDFAnnotationsManager(config, signatureDataStore, pdfViewerElement) {
    this.config = config;
    this.signatureDataStore = signatureDataStore;
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
    var pageAnnotations = this.pdfAnnotations.getPageAnnotations(pageNumber);
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

PDFAnnotationsManager.prototype.getSignaturePicker = function(pageNumber, dropZoneManager) {
    var pageSignatureAnnotationPicker = this.signatureAnnotationPickers[pageNumber];
    if (!pageSignatureAnnotationPicker) {
        pageSignatureAnnotationPicker = new SignatureAnnotationPicker(this.signatureDataStore, dropZoneManager);
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
    this.pdfAnnotationsServices.certifyPDFSignatures(this.pdfAnnotations, 
        function() {
            console.info("certification process successful!");
            var successMessage = self.i10n("pdf-annotations-manager.documentCertified", "The document was successfully signed and certified.");
            alert(successMessage);
            setTimeout(function() {
                location.reload();
            }, 5000);
        },
        function(textStatus, errorThrown) {
            console.error("Unable to save PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
            var errorMessage = self.i10n("pdf-annotations-manager.documentCertificationError", "The document was not signed nor certified. Check your connection and try again.");
            alert(errorMessage);
        });
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
	} else {
		value = defaultValue;
	}
	return value;
};

