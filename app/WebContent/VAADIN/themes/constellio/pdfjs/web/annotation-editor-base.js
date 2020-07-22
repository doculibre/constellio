"use strict";

function AnnotationEditor() {
}

AnnotationEditor.prototype.getFormHtmlElement = function(annotation, actionButtonsElement, saveButton, cancelButton) {
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

	var actionButtonsElement = document.createElement("div");
	actionButtonsElement.classList.add("annotation-editor-window-action-buttons");
	
	var closeButton = document.createElement("span");
	closeButton.innerHTML = "&#10006;";
	closeButton.classList.add("annotation-editor-window-close-button");
	closeButton.onclick = function() {
		self.closeWindow.call(self);
		cancelCallback.call(callbackContext);
	};
	
	var saveButton = document.createElement("button");
	saveButton.classList.add("annotation-editor-window-save-button");
	saveButton.classList.add("primary");
	saveButton.onclick = function() {
		self.closeWindow.call(self);
		saveCallback.call(callbackContext);
	};
	saveButton.innerHTML = this.i10n("buttons.save", "Save");
	
	var cancelButton = document.createElement("button");
	cancelButton.classList.add("annotation-editor-window-cancel-button");
	cancelButton.onclick = function() {
		self.closeWindow.call(self);
		cancelCallback.call(callbackContext);
	};
	cancelButton.innerHTML = this.i10n("buttons.cancel", "Cancel");

	var formContentElement = this.getFormHtmlElement(annotation, actionButtonsElement, saveButton, cancelButton);
	formContentElement.classList.add("annotation-editor-window-form");
	
	this.windowElement.appendChild(windowContentElement);
	windowContentElement.appendChild(closeButton);
	windowContentElement.appendChild(formContentElement);
	windowContentElement.appendChild(actionButtonsElement);
	actionButtonsElement.appendChild(saveButton);
	actionButtonsElement.appendChild(cancelButton);
	
	document.body.appendChild(this.windowElement);
};

AnnotationEditor.prototype.closeWindow = function() {
	this.windowElement.parentNode.removeChild(this.windowElement);
};

AnnotationEditor.prototype.i10n = function(key, defaultValue) {
	var value;
	var mozL10n = document.mozL10n || document.webL10n;
	if (mozL10n) {
        value = mozL10n.get(key, null, null);
        if (!value || value.indexOf("{{") == 0) {
            value = defaultValue;
        }
	} else {
		value = defaultValue;
	}
	return value;
};
