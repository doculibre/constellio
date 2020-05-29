function PDFAnnotations(version) {
    this.apiVersion = 1.0;
    if (version) {
        this.version = version;
    } else {
        this.version = 1.0;
    }
    this.pagesAndAnnotations = {};
}

PDFAnnotations.prototype.getApiVersion = function() {
    return this.apiVersion;
};   

PDFAnnotations.prototype.getVersion = function() {
    return this.version;
};     

PDFAnnotations.prototype.setVersion = function(version) {
    this.version = version;
};    

PDFAnnotations.prototype.getPagesWithAnnotations = function() {
    return Object.keys(this.pagesAndAnnotations);
};

PDFAnnotations.prototype.getPageAnnotations = function(pageNumber) {
    return this.pagesAndAnnotations[pageNumber];
}; 

PDFAnnotations.prototype.setPageAnnotations = function(pageNumber, annotations) {
    this.pagesAndAnnotations[pageNumber] = annotations;
}; 

PDFAnnotations.prototype.getPageTypeAnnotations = function(pageNumber, type) {
    var matchingAnnotations = [];
    var pageAnnotations = this.getPageAnnotations(pageNumber);
    if (pageAnnotations) {
        for (var j = 0; j < pageAnnotations.length; j++) {
            var pageAnnotation = pageAnnotations[j];
            if (pageAnnotation.getType() == type) {
                matchingAnnotations.push(pageAnnotation);
            }
        }
    }
    return matchingAnnotations;
}; 
