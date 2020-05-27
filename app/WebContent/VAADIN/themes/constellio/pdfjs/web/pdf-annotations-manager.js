"use strict";

function PDFAnnotationsManager(config, pdfViewerElement) {
    this.config = config;
    this.pdfViewerElement = pdfViewerElement;
    this.pdfAnnotations = new PDFAnnotations();
    this.currentPage = 1;
    this.dropZoneManagers = [];      
    this.pdfAnnotationsServices = new PDFAnnotationsServices(config);

    if (this.config) {
        this.username = config["username"];
    }
    
    var self = this;
    // pagechange, pagerendered
	window.addEventListener("fullscreenchange", function(e) {
        console.info("fullscreenchange");
        //self.refreshDropZones();
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
    if (!this.isDropZoneManager(currentPage)) {
        this.newDropZoneManager(pageNumber);
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
    }
    this.dropZoneManagers[pageNumber] = dropZoneManager;
};

PDFAnnotationsManager.prototype.loadPDFAnnotations = function() {
    var self = this;
    console.info("Pre PDF annotations loaded!");
    this.pdfAnnotationsServices.getPDFAnnotations(
        function(pdfAnnotations) {
            console.info("PDF annotations loaded!");
            self.pdfAnnotations = pdfAnnotations;
        },
        function(textStatus, errorThrown) {
            console.error("Unable to load PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
        });
};    

PDFAnnotationsManager.prototype.savePDFAnnotations = function() {
    var self = this;
    console.info("Pre PDF annotations saved!");
    this.pdfAnnotationsServices.savePDFAnnotations(this.pdfAnnotations, 
        function(newVersion) {
            self.pdfAnnotations.setVersion(newVersion);
            console.info("PDF annotations saved!");
        },
        function(textStatus, errorThrown) {
            console.error("Unable to save PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
        });
};    

PDFAnnotationsManager.prototype.certifyPDFSignatures = function() {
    console.info("Pre PDF signatures certified!");
    this.pdfAnnotationsServices.certifyPDFSignatures(this.pdfAnnotations, 
        function() {
            console.info("PDF signatures certified!");
            location.reload();
        },
        function(textStatus, errorThrown) {
            console.error("Unable to save PDF annotations (status: " + textStatus + ")");
            console.error(errorThrown);
            alert("Unable to certify the PDF signatures: check your connection and try again.");
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

