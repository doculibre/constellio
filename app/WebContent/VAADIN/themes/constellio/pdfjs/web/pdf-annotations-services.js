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
		annotation.setInitials("" + json["initials"]);
	} else if (type == "image-annotation") {
		annotation = new ImageAnnotation();
		annotation.setUrl(json["url"]);
	} else if (type == "signature-image-annotation") {
		annotation = new SignatureImageAnnotation();
		annotation.setUrl("" + json["url"]);
		annotation.setInitials("" + json["initials"]);
	} else if (type == "signature-pad-annotation") {
		annotation = new SignatureImageAnnotation();
		annotation.setUrl(json["imageUrl"]);
		annotation.setInitials("" + json["initials"]);
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
	annotation.readOnly = json["readOnly"];
	annotation.baked = json["baked"];
	annotation.bakeUser = json["bakeUser"];
	annotation.bakeDate = json["bakeDate"];
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

PDFAnnotationsServices.prototype.getParamsAsJSON = function(url) {
	var json = {};
	var indexOfQuestionMark = url.indexOf("?");
	if (indexOfQuestionMark != -1) {
		var queryString = url.substring(indexOfQuestionMark + 1, url.length);
		var paramNamesAndValues = queryString.split('&');
		for (var i = 0; i < paramNamesAndValues.length; i++) {
			var paramNameAndValue = paramNamesAndValues[i].split('=');
			var paramName = paramNameAndValue[0];
			var paramValue = paramNameAndValue[1];
			json[paramName] = paramValue;
		}
	}
	return json;
};

PDFAnnotationsServices.prototype.getUrlWithoutParams = function(url) {
	var indexOfQuestionMark = url.indexOf("?");
	if (indexOfQuestionMark != -1) {
		url = url.substring(0, indexOfQuestionMark);
	}	
	return url;
};	

PDFAnnotationsServices.prototype.savePDFAnnotations = function(pdfAnnotations, success, fail) {
	var self = this; 

	if (this.saveServiceUrl) {
		var pdfAnnotationsJson = {
			apiVersion: "" + pdfAnnotations.apiVersion,
			version: "" + pdfAnnotations.version,
			pagesAndAnnotations: pdfAnnotations.pagesAndAnnotations
		};
		var stringifiedPdfAnnotations = JSON.stringify(pdfAnnotationsJson);
		$.ajaxQueue({
			url: this.saveServiceUrl,
			data: stringifiedPdfAnnotations,
			method: "POST",
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			processData: false,
			timeout: 30000
		})
		.done(function(data, textStatus, jqXHR) {
			var newVersion = data["newVersion"];
			success(newVersion);			
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			fail(textStatus, errorThrown);
		});  
	}
};

PDFAnnotationsServices.prototype.getSignatureAnnotations = function(pdfAnnotations) {
	var signatureAnnotations = [];
	// Using attributes instead of calling functions because pdfAnnotations may be a copy
	var pagesWithAnnotations = Object.keys(pdfAnnotations.pagesAndAnnotations);
	for (var i = 0; i < pagesWithAnnotations.length; i++) {
		var pageWithAnnotation = pagesWithAnnotations[i];
		var pageAnnotations = pdfAnnotations.pagesAndAnnotations[pageWithAnnotation];
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

PDFAnnotationsServices.prototype.makeSignatureAnnotationsReadOnly = function(pdfAnnotations) {
	var signatureAnnotations = this.getSignatureAnnotations(pdfAnnotations);
	for (var i = 0; i < signatureAnnotations.length; i++) {
		var signatureAnnotation = signatureAnnotations[i];
		signatureAnnotation["readOnly"] = true;
	}
};

PDFAnnotationsServices.prototype.certifyPDFSignatures = function(pdfAnnotations, success, fail) {
	var self = this;
	
	var signatureAnnotations = this.getSignatureAnnotations(pdfAnnotations);
	var atLeastOneSignatureAnnotation = signatureAnnotations.length > 0;

	if (!atLeastOneSignatureAnnotation) {
		var errorMessage = this.i10n("pdf-annotation-services.noSignature",  "The document doesn''t contain a signature.");
		fail(errorMessage);
	} else if (this.certifyServiceUrl) {
		// Work on a copy in case an error gets thrown
		//var pdfAnnotationsCopy = JSON.parse(JSON.stringify(pdfAnnotations));
		//this.makeSignatureAnnotationsReadOnly(pdfAnnotationsCopy);

		var pdfAnnotationsJson = {
			apiVersion: "" + pdfAnnotations.apiVersion,
			version: "" + pdfAnnotations.version,
			pagesAndAnnotations: pdfAnnotations.pagesAndAnnotations
		};
		var stringifiedPdfAnnotations = JSON.stringify(pdfAnnotationsJson);
		$.ajaxQueue({
			url: self.certifyServiceUrl,
			data: stringifiedPdfAnnotations,
			method: "POST",
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			processData: false,
			timeout: 300000
		})
		.done(function(data, textStatus, jqXHR) {
			//self.makeSignatureAnnotationsReadOnly(pdfAnnotations);
			success(data);	
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			fail(textStatus, errorThrown);
		});  
	}
};

PDFAnnotationsServices.prototype.i10n = function(key, defaultValue) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
		value = mozL10n.get(key, null, null);
	} else {
		value = defaultValue;
	}
	return value;
};
