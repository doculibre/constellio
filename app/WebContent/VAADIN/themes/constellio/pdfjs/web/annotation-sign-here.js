"use strict";

function SignHereAnnotation(text, x, y, width, height) {
	Annotation.call(this, x, y, width, height);
	if (!text) {
		var mozL10n = document.mozL10n || document.webL10n;
		if (mozL10n) {
			text = mozL10n.get("annotation.signHere.text", null, "Sign here");
		} else {
			text = "Sign here";
		}
	}
	this.setText(text);
}

SignHereAnnotation.prototype = Object.create(Annotation.prototype);
SignHereAnnotation.prototype.constructor = SignHereAnnotation;

SignHereAnnotation.prototype.getType = function() {
	return "sign-here-annotation";
}

SignHereAnnotation.prototype.getText = function() {
	return this.text;	
};

SignHereAnnotation.prototype.setText = function(text) {
	this.text = text;
	if (this.textNode) {
		this.textNode.nodeValue = text;
	}
};

SignHereAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add("sign-here-annotation");
	var textContent = this.getText();
	if (!textContent) {
		textContent = "";
	}
	this.textElement = document.createElement("span");
	this.textElement.id = this.getHtmlElementId() + "-text";
	this.textNode = document.createTextNode(textContent);
	this.textElement.appendChild(this.textNode);
	htmlElement.appendChild(this.textElement); 
};

SignHereAnnotation.prototype.isSameHtmlElement = function(htmlElement) {
	var sameHtmlElement = Annotation.prototype.isSameHtmlElement.call(this, htmlElement);
	if (!sameHtmlElement) {
		if (htmlElement instanceof jQuery) {
			htmlElement = htmlElement[0];
		}
		sameHtmlElement = htmlElement && htmlElement.id && htmlElement.id == this.textElement.id;
	}
	return sameHtmlElement;
};

SignHereAnnotation.prototype.annotationDefined = function(htmlElement) {
	if (!this.getText()) {
		this.openEditor();
	}
};

SignHereAnnotation.prototype.getSaveCallback = function() {
	this.setText(this.getEditor().getText());
};

SignHereAnnotation.prototype.getDebugString = function() {
	return "text: " + this.getText() + ", " + Annotation.prototype.getDebugString.call(this);
};
