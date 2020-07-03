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

TextAnnotationEditor.prototype.getImageUrl = function(saveButton, cancelButton) {	
	return this.imageUrl;
};

TextAnnotationEditor.prototype.getFormHtmlElement = function(annotation, actionButtonsElement, saveButton, cancelButton) {	
	this.textFieldElement = document.createElement("input");
	this.textFieldElement.setAttribute("type", "text");
	this.textFieldElement.classList.add("text-annotation-editor-input");
	
	var self = this; // For nested functions
	this.textFieldElement.onchange = function(e) {
		var newText = e.target.value;
		self.setText(newText);
		annotation.setText(newText);
		self.convertTextToImage(annotation);
	};
	this.textFieldElement.addEventListener("keyup", function(e) {
		// Number 13 is the "Enter" key on the keyboard
		if (e.keyCode === 13) {
			self.setText(e.target.value);
			
			// Cancel the default action, if needed
			e.preventDefault();
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

TextAnnotationEditor.prototype.convertTextToImage = function(annotation) {
	var self = this;
	if (annotation.textElement && this.text) {		
		var imageScale = 5;

		var type = annotation.getType();
		var bigCanvas = $("<div>").appendTo('body');  // This will be the 3x sized canvas we're going to render
		bigCanvas[0].classList.add(type);
		bigCanvas[0].classList.add(type + "-copy-canvas");
		var scaledElement = $(annotation.textElement).clone()
		.css({
			'display': '',
			'transform': 'scale('+ imageScale + ',' + imageScale + ')',
			'transform-origin': '0 0'
		})
		.appendTo(bigCanvas);

		var oldWidth = scaledElement.width();
		var oldHeight = scaledElement.height();

		var newWidth = oldWidth * imageScale;
		var newHeight = oldHeight * imageScale;

		bigCanvas.css({
			'width': newWidth,
			'height': newHeight
		});
		html2canvas(bigCanvas, {
			onrendered: function(canvas) {
				var imageUrl = canvas.toDataURL("image/png");
				self.imageUrl = imageUrl;
				if (imageUrl) {
					annotation.imageUrl = imageUrl;
					annotation.htmlElement.style.backgroundImage = "url(" + imageUrl + ")";
				} else {
					annotation.imageUrl = null;
					annotation.htmlElement.style.backgroundImage = "";
				}
				bigCanvas.remove();
			}
		});
	}
};
