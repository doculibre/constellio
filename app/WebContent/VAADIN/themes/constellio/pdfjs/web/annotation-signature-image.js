"use strict";

function SignatureImageAnnotation(url, x, y, width, height) {
	ImageAnnotation.call(this, url, x, y, width, height);
}

SignatureImageAnnotation.prototype = Object.create(ImageAnnotation.prototype);
SignatureImageAnnotation.prototype.constructor = SignatureImageAnnotation;

SignatureImageAnnotation.prototype.getType = function() {
	return "signature-image-annotation";
}

SignatureImageAnnotation.prototype.isInitials = function() {
	return this.initials;
};

SignatureImageAnnotation.prototype.setInitials = function(initials) {
	this.initials = initials;
};

SignatureImageAnnotation.prototype.getBakeInfoI10nKey = function() {
	return "annotation.signature.bakeInfo";
};

SignatureImageAnnotation.prototype.toJSON = function() {
	var json = ImageAnnotation.prototype.toJSON.call(this);
	json.initials = this.isInitials();
	return json;
};

SignatureImageAnnotation.prototype.fromJSON = function(json) {
	ImageAnnotation.prototype.fromJSON.call(this, json);
	this.initials = json.initials;
	if (json.imageUrl) {
		this.url = json.imageUrl;
	}
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
