"use strict";

function AnnotationEditor() {
}

AnnotationEditor.prototype.getFormHtmlElement = function(annotation, saveButton, cancelButton) {
	var formContent = document.createElement("div");
	formContent.innerHTML = "Override me!";
	return formContent;
}

AnnotationEditor.prototype.open = function(annotation, callbackContext, saveCallback, cancelCallback) {
	var self = this;
	
	this.windowElement = document.createElement("div");
	this.windowElement.classList.add("annotation-editor-window");
	
	var windowContentElement = document.createElement("div");
	windowContentElement.classList.add("annotation-editor-window-content");
	
	var closeButton = document.createElement("span");
	closeButton.innerHTML = "&#10006;";
	closeButton.classList.add("annotation-editor-window-close-button");
	closeButton.onclick = function() {
		self.closeWindow.call(self);
		cancelCallback.call(callbackContext);
	};
	
	var saveButton = document.createElement("button");
	saveButton.classList.add("annotation-editor-window-save-button");
	saveButton.onclick = function() {
		self.closeWindow.call(self);
		saveCallback.call(callbackContext);
	};
	saveButton.innerHTML = "Save";
	
	var cancelButton = document.createElement("button");
	cancelButton.classList.add("annotation-editor-window-cancel-button");
	cancelButton.onclick = function() {
		self.closeWindow.call(self);
		cancelCallback.call(callbackContext);
	};
	cancelButton.innerHTML = "Cancel";
		
	var formContentElement = this.getFormHtmlElement(annotation, saveButton, cancelButton);
	formContentElement.classList.add("annotation-editor-window-form");
	
	this.windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	windowContentElement.appendChild(formContentElement);
	windowContentElement.appendChild(saveButton);
	windowContentElement.appendChild(cancelButton);
	
	document.body.appendChild(this.windowElement);
};

AnnotationEditor.prototype.closeWindow = function() {
	this.windowElement.parentNode.removeChild(this.windowElement);
};
