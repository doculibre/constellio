"use strict";

function SignatureImageAnnotation(imageUrl, x, y, width, height) {
	ImageAnnotation.call(this, imageUrl, x, y, width, height);
}

SignatureImageAnnotation.prototype = Object.create(ImageAnnotation.prototype);
SignatureImageAnnotation.prototype.constructor = SignatureImageAnnotation;

SignatureImageAnnotation.prototype.getType = function() {
	return "signature-image-annotation";
}

SignatureImageAnnotation.prototype.isSignature = function() {
	return this.signature;
};

SignatureImageAnnotation.prototype.setSignature = function(signature) {
	this.signature = signature;
};

SignatureImageAnnotation.prototype.getBakeInfoI10nKey = function() {
	return "annotation.signature.bakeInfo";
};

SignatureImageAnnotation.prototype.isBindIfBaked = function() {
	//return this.isSignature();
	return false;
};

SignatureImageAnnotation.prototype.toJSON = function() {
	var json = ImageAnnotation.prototype.toJSON.call(this);
	json.signature = this.isSignature();
	return json;
};

SignatureImageAnnotation.prototype.fromJSON = function(json) {
	ImageAnnotation.prototype.fromJSON.call(this, json);
	this.signature = json.signature;
};	

SignatureImageAnnotation.prototype.bind = function(htmlElement) {
	ImageAnnotation.prototype.bind.call(this, htmlElement);
	htmlElement.classList.add("signature-image-annotation");
	if (!this.isBaked()) {
		htmlElement.title = this.i10n("annotation.signature.clickToCertify", "Click on the Certify button to save the signature");
		htmlElement.classList.add("tooltip");
		$(htmlElement).tooltipster();
	}
};
