"use strict";

function SignatureTextAnnotationEditor(text) {
	TextAnnotationEditor.call(this, text);
}

SignatureTextAnnotationEditor.prototype = Object.create(TextAnnotationEditor.prototype);
SignatureTextAnnotationEditor.prototype.constructor = SignatureTextAnnotationEditor;

SignatureTextAnnotationEditor.prototype.getFormHtmlElement = function(annotation, actionButtonsElement, saveButton, cancelButton) {	
	var textFieldElement = TextAnnotationEditor.prototype.getFormHtmlElement.call(this, annotation, actionButtonsElement, saveButton, cancelButton);
	textFieldElement.classList.add("signature-text-annotation-editor-input");
	textFieldElement.spellcheck=false;
	$(textFieldElement).keyup(function () {
		$(this).removeAccentedChar();
	});		
	return textFieldElement;
};
