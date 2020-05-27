"use strict";

function SignaturePadAnnotation(x, y, width, height) {
	Annotation.call(this, x, y, width, height);
	this.setEditor(new SignaturePadAnnotationEditor());
}

SignaturePadAnnotation.prototype = Object.create(Annotation.prototype);
SignaturePadAnnotation.prototype.constructor = SignaturePadAnnotation;

SignaturePadAnnotation.prototype.getType = function() {
	return "signature-pad-annotation";
}	

SignaturePadAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add("signature-pad-annotation");	
	if (this.imageUrl) {
		htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")"; 
	}
};

SignaturePadAnnotation.prototype.annotationDefined = function(htmlElement) {
	this.openEditor();
};

SignaturePadAnnotation.prototype.getSaveCallback = function() {
	this.imageUrl = this.editor.getImageUrl();
	this.htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")";  
};
