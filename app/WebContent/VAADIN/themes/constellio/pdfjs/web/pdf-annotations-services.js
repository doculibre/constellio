"use strict";

function PDFAnnotationsServices(config) {
	if (config) {
		this.getServiceUrl = config["getAnnotationsServiceUrl"];
		this.saveServiceUrl = config["saveAnnotationsServiceUrl"];
		this.certifyServiceUrl = config["certifyServiceUrl"];
	}
}

PDFAnnotationsServices.prototype.fromJSON = function(json) {
	return PDFAnnotations.prototype.fromJSON(json);
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
		var pdfAnnotationsJson = pdfAnnotations.toJSON();
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

PDFAnnotationsServices.prototype.makeSignatureAnnotationsReadOnly = function(pdfAnnotations) {
	var signatureAnnotations = pdfAnnotations.getSignatureAnnotations();
	for (var i = 0; i < signatureAnnotations.length; i++) {
		var signatureAnnotation = signatureAnnotations[i];
		signatureAnnotation["readOnly"] = true;
	}
};

PDFAnnotationsServices.prototype.certifyPDFSignatures = function(pdfAnnotations, success, fail) {
	var self = this;
	
	var signatureAnnotations = pdfAnnotations.getSignatureAnnotations();
	var atLeastOneSignatureAnnotation = signatureAnnotations.length > 0;

	if (!atLeastOneSignatureAnnotation) {
		var errorMessage = this.i10n("pdf-annotation-services.noSignature",  "The document doesn''t contain a signature.");
		fail(errorMessage);
	} else if (this.certifyServiceUrl) {
		// Work on a copy in case an error gets thrown
		//var pdfAnnotationsCopy = JSON.parse(JSON.stringify(pdfAnnotations));
		//this.makeSignatureAnnotationsReadOnly(pdfAnnotationsCopy);

		var pdfAnnotationsJson = pdfAnnotations.toJSON();
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
