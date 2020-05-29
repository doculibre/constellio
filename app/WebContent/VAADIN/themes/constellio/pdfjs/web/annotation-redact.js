"use strict";

function RedactAnnotation(x, y, width, height) {
	Annotation.call(this, x, y, width, height);
}

RedactAnnotation.prototype = Object.create(Annotation.prototype);
RedactAnnotation.prototype.constructor = RedactAnnotation;

RedactAnnotation.prototype.getType = function() {
	return "redact-annotation";
}

RedactAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add("redact-annotation");
};
