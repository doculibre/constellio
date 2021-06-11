"use strict";

function SignatureTextAnnotation(text, x, y, width, height) {
	TextAnnotation.call(this, text, x, y, width, height);
	this.setEditor(new SignatureTextAnnotationEditor(this.getText())); 
}

SignatureTextAnnotation.prototype = Object.create(TextAnnotation.prototype);
SignatureTextAnnotation.prototype.constructor = SignatureTextAnnotation;

SignatureTextAnnotation.prototype.getType = function() {
	return "signature-text-annotation";
}

SignatureTextAnnotation.prototype.isSignature = function() {
	return this.signature;
};

SignatureTextAnnotation.prototype.setSignature = function(signature) {
	this.signature = signature;
};

SignatureTextAnnotation.prototype.getBakeInfoI10nKey = function() {
	return "annotation.signature.bakeInfo";
};

SignatureTextAnnotation.prototype.toJSON = function() {
	var json = TextAnnotation.prototype.toJSON.call(this);
	json.signature = this.isSignature();
	return json;
};

SignatureTextAnnotation.prototype.fromJSON = function(json) {
	TextAnnotation.prototype.fromJSON.call(this, json);
	this.signature = json.signature;
};	

SignatureTextAnnotation.prototype.getPreText = function() {
	var preText = TextAnnotation.prototype.getPreText.call(this);
	var annotationText = this.getText();
	if (annotationText && (annotationText.startsWith("f") || annotationText.startsWith("j") || annotationText.startsWith("t"))) {
		preText = " ";
	}
	return preText;
}	

SignatureTextAnnotation.prototype.bind = function(htmlElement) {
	TextAnnotation.prototype.bind.call(this, htmlElement);
	htmlElement.classList.add("signature-text-annotation");
	if (!this.isBaked()) {
		htmlElement.title = this.i10n("annotation.signature.clickToCertify", "Click on the Certify button to save the signature");
		htmlElement.classList.add("tooltip");
		$(htmlElement).tooltipster();
	}
};
