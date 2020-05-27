"use strict";

function ImageAnnotation(url, x, y, width, height) {
	Annotation.call(this, x, y, width, height);
	this.url = url;
}

ImageAnnotation.prototype = Object.create(Annotation.prototype);
ImageAnnotation.prototype.constructor = ImageAnnotation;

ImageAnnotation.prototype.getType = function() {
	return "image-annotation";
}

ImageAnnotation.prototype.getUrl = function() {
	return this.url;	
};

ImageAnnotation.prototype.setUrl = function(url) {
	this.url = url;
	if (this.htmlElement) {
		this.htmlElement.style.backgroundImage = "url(" + this.url + ")"; 
	}
};

ImageAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add("image-annotation");
	htmlElement.style.backgroundImage = "url(" + this.url + ")"; 
};

ImageAnnotation.prototype.getDebugString = function() {
	return "url: " + this.getUrl() + ", " + Annotation.prototype.getDebugString.call(this);
};
