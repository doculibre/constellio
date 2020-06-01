"use strict";

function SignatureTextAnnotation(text, x, y, width, height) {
	TextAnnotation.call(this, text, x, y, width, height);
}

SignatureTextAnnotation.prototype = Object.create(TextAnnotation.prototype);
SignatureTextAnnotation.prototype.constructor = SignatureTextAnnotation;

SignatureTextAnnotation.prototype.getType = function() {
	return "signature-text-annotation";
}

SignatureTextAnnotation.prototype.getBakeInfoI10nKey = function() {
	return "annotation.signature.bakeInfo";
};

SignatureTextAnnotation.prototype.bind = function(htmlElement) {
	TextAnnotation.prototype.bind.call(this, htmlElement);
	htmlElement.classList.add("signature-text-annotation");
	if (!this.isBaked()) {
		htmlElement.title = this.i10n("annotation.signature.clickToCertify", "Click on the Certify button to save the signature");
		htmlElement.classList.add("tooltip");
		$(htmlElement).tooltipster();
	}
};
