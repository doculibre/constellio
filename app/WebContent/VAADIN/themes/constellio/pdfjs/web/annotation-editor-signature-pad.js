"use strict";

function SignaturePadAnnotationEditor() {
	AnnotationEditor.call(this);
}

SignaturePadAnnotationEditor.prototype = Object.create(AnnotationEditor.prototype);
SignaturePadAnnotationEditor.prototype.constructor = SignaturePadAnnotationEditor;

 /**
  * Conserve aspect ratio of the original region. Useful when shrinking/enlarging
  * images to fit into a certain area.
  *
  * @param {Number} srcWidth width of source image
  * @param {Number} srcHeight height of source image
  * @param {Number} maxWidth maximum available width
  * @param {Number} maxHeight maximum available height
  * @return {Object} { width, height }
  */
SignaturePadAnnotationEditor.prototype.calculateAspectRatioFit = function(srcWidth, srcHeight, maxWidth, maxHeight) {
    var ratio = Math.min(maxWidth / srcWidth, maxHeight / srcHeight);
    return { width: srcWidth*ratio, height: srcHeight*ratio };
};

SignaturePadAnnotationEditor.prototype.initSignaturePad = function(annotation, actionButtonsElement) {
	if (!this.formElement) {
		var self = this;
		
		var signaturePadContainerElement = document.createElement("div");
		signaturePadContainerElement.classList.add("signature-pad-annotation-editor-container");
		
		var signaturePadElement = document.createElement("div");
		//signaturePadElement.classList.add("signature-pad");
		
		var signaturePadBodyElement = document.createElement("div");
		//signaturePadBodyElement.classList.add("signature-pad--body");
		
		this.canvasElement = document.createElement("canvas");		
		//this.canvasElement.classList.add("signature-annotation-editor-canvas");
		var annotationWidth = annotation.getWidth();
		var annotationHeight = annotation.getHeight();
		var canvasMaxWidth = document.documentElement.clientWidth * 0.7;
		var canvasMaxHeight = 400;
		var canvasRatioFit = this.calculateAspectRatioFit(annotationWidth, annotationHeight, canvasMaxWidth, canvasMaxHeight);
		this.canvasElement.width = canvasRatioFit.width;
		this.canvasElement.height = canvasRatioFit.height;
		
		this.signaturePad = new SignaturePad(this.canvasElement, {
			// It's Necessary to use an opaque color when saving image as JPEG;
			// this option can be omitted if only saving as PNG or SVG
			//backgroundColor: 'rgb(255, 255, 255)'
		});
				
		var signaturePadFooterElement = document.createElement("div");
		//signaturePadFooterElement.classList.add("signature-pad--footer");
		
		var descriptionElement = document.createElement("div");
		//descriptionElement.classList.add("description");
		descriptionElement.innerHTML = this.i10n("signature-pad.signAbove", "Sign above");	
		
		var signaturePadActionsElement = document.createElement("div");
		//signaturePadActionsElement.classList.add("signature-pad--actions");
		
		this.clearButton = document.createElement("button");
		this.clearButton.onclick = function(e) {
			self.signaturePad.clear();
		};
		this.clearButton.innerHTML = this.i10n("buttons.clear", "Clear");
		
		this.undoButton = document.createElement("button");
		this.undoButton.onclick = function(e) {
			var data = self.signaturePad.toData();
			if (data) {
				data.pop(); // remove the last dot or line
				self.signaturePad.fromData(data);
			}
		};
		this.undoButton.innerHTML = this.i10n("buttons.undo", "Undo");	

		signaturePadContainerElement.appendChild(signaturePadElement);
		signaturePadElement.appendChild(signaturePadBodyElement);
		signaturePadBodyElement.appendChild(this.canvasElement);
		signaturePadContainerElement.appendChild(signaturePadFooterElement);
		//signaturePadFooterElement.appendChild(descriptionElement);
		signaturePadFooterElement.appendChild(signaturePadActionsElement);
		
		this.formElement = signaturePadContainerElement;
	}
	if (!document.body.contains(this.clearButton)) {
		actionButtonsElement.appendChild(this.clearButton);
		actionButtonsElement.appendChild(this.undoButton);
	}
};	

SignaturePadAnnotationEditor.prototype.getFormHtmlElement = function(annotation, actionButtonsElement, saveButton, cancelButton) {
	this.initSignaturePad(annotation, actionButtonsElement);
	return this.formElement;
};	

SignaturePadAnnotationEditor.prototype.getImageUrl = function(saveButton, cancelButton) {	
	return this.signaturePad.toDataURL();
};	

SignaturePadAnnotationEditor.prototype.getSVGImageUrl = function(saveButton, cancelButton) {	
	return this.signaturePad.toDataURL("image/svg+xml");
};
