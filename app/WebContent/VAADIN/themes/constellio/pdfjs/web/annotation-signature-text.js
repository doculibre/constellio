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
};
