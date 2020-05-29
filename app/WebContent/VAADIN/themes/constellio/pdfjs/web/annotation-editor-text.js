"use strict";

function TextAnnotationEditor(text) {
	AnnotationEditor.call(this);
	this.text = text;
}

TextAnnotationEditor.prototype = Object.create(AnnotationEditor.prototype);
TextAnnotationEditor.prototype.constructor = TextAnnotationEditor;

TextAnnotationEditor.prototype.getText = function() {
	return this.text;
}

TextAnnotationEditor.prototype.setText = function(text) {
	this.text = text;
	if (this.textFieldElement) {
		this.textFieldElement.value = this.text;
	}
}

TextAnnotationEditor.prototype.getFormHtmlElement = function(annotation, saveButton, cancelButton) {	
	this.textFieldElement = document.createElement("input");
	this.textFieldElement.setAttribute("type", "text");
	this.textFieldElement.classList.add("text-annotation-editor-input");
	
	var self = this; // For nested functions
	this.textFieldElement.onchange = function(e) {
		self.setText(e.target.value);
	};
	this.textFieldElement.addEventListener("keyup", function(e) {
		// Number 13 is the "Enter" key on the keyboard
		if (event.keyCode === 13) {
			self.setText(e.target.value);
			
			// Cancel the default action, if needed
			event.preventDefault();
			// Trigger the button element with a click
			saveButton.click();
		}
	});
	setTimeout(function() {
        self.textFieldElement.focus();
		self.textFieldElement.selectionStart = self.textFieldElement.selectionEnd = 10000;
       }, 100);
	return this.textFieldElement;
};

TextAnnotationEditor.prototype.open = function(annotation, callbackContext, saveCallback, cancelCallback) {
	var textValue = annotation.getText();
	if (!textValue) {
		textValue = "";
	}
	this.setText(textValue);
	AnnotationEditor.prototype.open.call(this, annotation, callbackContext, saveCallback, cancelCallback);
	this.textFieldElement.value = this.getText();
}	
