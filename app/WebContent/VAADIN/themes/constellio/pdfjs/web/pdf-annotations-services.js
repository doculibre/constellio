"use strict";

function PDFAnnotationsServices(config) {
	if (config) {
		this.getServiceUrl = config["getAnnotationsServiceUrl"];
		this.saveServiceUrl = config["saveAnnotationsServiceUrl"];
		this.certifyServiceUrl = config["certifyServiceUrl"];
	}
}

PDFAnnotationsServices.prototype.fromJSON = function(json) {
	var pdfAnnotations = new PDFAnnotations(json["version"]);
	var pagesAndAnnotationJSONs = json["pagesAndAnnotations"];

	var pageNumbers = Object.keys(pagesAndAnnotationJSONs)
	for (var i = 0; i < pageNumbers.length; i++) {
		var pageNumber = pageNumbers[i];
		var pageAnnotations = [];
		var pageAnnotationJSONs = pagesAndAnnotationJSONs[pageNumber];
		for (var j = 0; j < pageAnnotationJSONs.length; j++) {
			var pageAnnotationJSON = pageAnnotationJSONs[j];
			var pageAnnotation = this.parseAnnotationJSON(pageAnnotationJSON);
			if (pageAnnotation) {
				pageAnnotations.push(pageAnnotation);
			}
		}
		pdfAnnotations.setPageAnnotations(pageNumber, pageAnnotations);
	}
	return pdfAnnotations;
};	

PDFAnnotationsServices.prototype.parseAnnotationJSON = function(json) {
	var type = json["type"];
	var annotation;
	if (type == "text-annotation") {
		annotation = new TextAnnotation();
		annotation.setText(json["text"]);
	} else if (type == "signature-text-annotation") {
		annotation = new SignatureTextAnnotation();
		annotation.setText(json["text"]);
	} else if (type == "image-annotation") {
		annotation = new ImageAnnotation();
		annotation.setUrl(json["url"]);
	} else if (type == "signature-image-annotation") {
		annotation = new SignatureImageAnnotation();
		annotation.setUrl("" + json["url"]);
	} else if (type == "signature-pad-annotation") {
		annotation = new SignatureImageAnnotation();
		annotation.setUrl(json["imageUrl"]);
	} else if (type == "sign-here-annotation") {
		annotation = new SignHereAnnotation();
		annotation.setText(json["text"]);
	} else {
		annotation = new Annotation();
	}
	annotation.id = json["id"];
	annotation.x = json["x"];
	annotation.y = json["y"];
	annotation.width = json["width"];
	annotation.height = json["height"];
	return annotation;
};

PDFAnnotationsServices.prototype.getPDFAnnotations = function(success, fail) {
	var self = this; 
	if (this.getServiceUrl) {
		$.ajaxQueue(this.getServiceUrl)
		.done(function(data, textStatus, jqXHR) {
			if (data) {
				var annotations = self.fromJSON(JSON.parse(data));
				success(annotations);			
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			fail(textStatus, errorThrown);
		});  
	}
};

PDFAnnotationsServices.prototype.savePDFAnnotations = function(pdfAnnotations, success, fail) {
	var self = this; 
	var pdfAnnotationsJson = {
		apiVersion: pdfAnnotations.apiVersion,
		version: pdfAnnotations.version,
		pagesAndAnnotations: pdfAnnotations.pagesAndAnnotations
	};
	var stringifiedPdfAnnotations = JSON.stringify(pdfAnnotationsJson);
	if (this.saveServiceUrl) {
		$.ajaxQueue({
			method: "POST",
			contentType: "application/x-www-form-urlencoded;charset=UTF-8",
			url: self.saveServiceUrl,
			data: stringifiedPdfAnnotations
		})
		.done(function(data, textStatus, jqXHR) {
			var newVersion = data;
			success(newVersion);			
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			fail(textStatus, errorThrown);
		});  
	}
};

PDFAnnotationsServices.prototype.certifyPDFSignatures = function(pdfAnnotations, success, fail) {
	var self = this;
	var atLeastOneSignatureAnnotation = false;
	var pagesWithAnnotations = pdfAnnotations.getPagesWithAnnotations();
	for (var i = 0; i < pagesWithAnnotations.length; i++) {
		var pageWithAnnotation = pagesWithAnnotations[i];
		var pageAnnotations = pdfAnnotations.getPageAnnotations(pageWithAnnotation);
		for (var j = 0; j < pageAnnotations.length; j++) {
			var pageAnnotation = pageAnnotations[j];
			var annotationType = pageAnnotation.getType();
			if ("signature-image-annotation" == annotationType 
				|| "signature-pad-annotation" == annotationType 
				|| "signature-text-annotation" == annotationType) {
				atLeastOneSignatureAnnotation = true;
			} 
		}
	}

	if (!atLeastOneSignatureAnnotation) {
		// TODO i10n
		alert("No signature annotation!");
	} else {
		// TODO : Pop window with progress bar
		alert("Certified!!!");
	}

	if (this.certifyServiceUrl) {
		var pdfAnnotationsJson = {
			apiVersion: pdfAnnotations.apiVersion,
			version: pdfAnnotations.version,
			pagesAndAnnotations: pdfAnnotations.pagesAndAnnotations
		};
		var stringifiedPdfAnnotations = JSON.stringify(pdfAnnotationsJson);
		$.ajaxQueue({
			url: self.certifyServiceUrl,
			method: "POST",
			contentType: "application/x-www-form-urlencoded;charset=UTF-8",
			data: stringifiedPdfAnnotations
		})
		.done(function(data, textStatus, jqXHR) {
			success(data);			
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			fail(textStatus, errorThrown);
		});  
	}
};
