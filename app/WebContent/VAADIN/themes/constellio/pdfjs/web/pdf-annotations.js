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
    return this.pagesAndAnnotations["" + pageNumber];
}; 

PDFAnnotations.prototype.setPageAnnotations = function(pageNumber, annotations) {
    this.pagesAndAnnotations["" + pageNumber] = annotations;
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

PDFAnnotations.prototype.getSignatureAnnotations = function() {
	var signatureAnnotations = [];
	// Using attributes instead of calling functions because pdfAnnotations may be a copy
	var pagesWithAnnotations = Object.keys(this.pagesAndAnnotations);
	for (var i = 0; i < pagesWithAnnotations.length; i++) {
		var pageWithAnnotation = pagesWithAnnotations[i];
		var pageAnnotations = this.pagesAndAnnotations[pageWithAnnotation];
		for (var j = 0; j < pageAnnotations.length; j++) {
			var pageAnnotation = pageAnnotations[j];
			var annotationType = pageAnnotation["type"];
			if (!pageAnnotation["readOnly"] && 
				("signature-image-annotation" == annotationType 
				|| "signature-pad-annotation" == annotationType 
				|| "signature-text-annotation" == annotationType)) {
				signatureAnnotations.push(pageAnnotation);
			} 
		}
	}
	return signatureAnnotations;
};	

PDFAnnotations.prototype.fromJSON = function(json) {
	var pdfAnnotations = new PDFAnnotations(json["version"]);
	var pagesAndAnnotationJSONs = json["pagesAndAnnotations"];

	var pageNumbers = Object.keys(pagesAndAnnotationJSONs)
	for (var i = 0; i < pageNumbers.length; i++) {
		var pageNumber = pageNumbers[i];
		var pageAnnotations = [];
		var pageAnnotationJSONs = pagesAndAnnotationJSONs[pageNumber];
		for (var j = 0; j < pageAnnotationJSONs.length; j++) {
			var pageAnnotationJSON = pageAnnotationJSONs[j];
			var pageAnnotation = PDFAnnotations.prototype.parseAnnotationJSON(pageAnnotationJSON);
			if (pageAnnotation) {
				pageAnnotations.push(pageAnnotation);
			}
		}
		pdfAnnotations.setPageAnnotations(pageNumber, pageAnnotations);
	}
	return pdfAnnotations;
};	

PDFAnnotations.prototype.parseAnnotationJSON = function(json) {
	var type = json["type"];
	var annotation;
	if (type == "text-annotation") {
		annotation = new TextAnnotation();
	} else if (type == "signature-text-annotation") {
		annotation = new SignatureTextAnnotation();
	} else if (type == "image-annotation") {
		annotation = new ImageAnnotation();
	} else if (type == "signature-image-annotation") {
		annotation = new SignatureImageAnnotation();
	} else if (type == "signature-pad-annotation") {
		annotation = new SignatureImageAnnotation();
	} else if (type == "sign-here-annotation") {
		annotation = new SignHereAnnotation();
	} else {
		annotation = new Annotation();
	}
	annotation.fromJSON(json);
	if (!annotation.getX() || !annotation.getY()) {
		annotation = null;
	}
	return annotation;
};	

PDFAnnotations.prototype.toJSON = function() {
	var pagesAndAnnotationsJSON = {};
	var pagesWithAnnotations = Object.keys(this.pagesAndAnnotations);
	for (var i = 0; i < pagesWithAnnotations.length; i++) {
		var pageWithAnnotation = pagesWithAnnotations[i];
        var pageAnnotations = this.pagesAndAnnotations[pageWithAnnotation];
        var pageAnnotationsJSON = [];
		for (var j = 0; j < pageAnnotations.length; j++) {
			var pageAnnotation = pageAnnotations[j];
			if (pageAnnotation.getX() && pageAnnotation.getY()) {
				var pageAnnotationJSON = pageAnnotation.toJSON();
				pageAnnotationsJSON.push(pageAnnotationJSON);
			}
        }
        pagesAndAnnotationsJSON[pageWithAnnotation] = pageAnnotationsJSON;
    }        
	return {
		apiVersion: "" + this.getApiVersion(),
		version: "" + this.getVersion(),
		pagesAndAnnotations: pagesAndAnnotationsJSON
	};
};	
