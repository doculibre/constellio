"use strict";

function ImageAnnotation(imageUrl, x, y, width, height) {
	Annotation.call(this, x, y, width, height);
	this.imageUrl = imageUrl;
}

ImageAnnotation.prototype = Object.create(Annotation.prototype);
ImageAnnotation.prototype.constructor = ImageAnnotation;

ImageAnnotation.prototype.getType = function() {
	return "image-annotation";
}

ImageAnnotation.prototype.getImageUrl = function() {
	return this.imageUrl;	
};

ImageAnnotation.prototype.setImageUrl = function(imageUrl) {
	this.imageUrl = imageUrl;
	if (this.htmlElement) {
		this.htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")"; 
	}
};

ImageAnnotation.prototype.toJSON = function() {
	var json = Annotation.prototype.toJSON.call(this);
	json.imageUrl = this.getImageUrl();
	return json;
};	

ImageAnnotation.prototype.fromJSON = function(json) {
	Annotation.prototype.fromJSON.call(this, json);
	this.imageUrl = json.imageUrl;
};	

ImageAnnotation.prototype.isBindIfBaked = function() {
	return false;
};

ImageAnnotation.prototype.bind = function(htmlElement) {
	Annotation.prototype.bind.call(this, htmlElement);
	
	htmlElement.classList.add("image-annotation");
	if (!this.isBaked() || this.isBindIfBaked()) {
		htmlElement.style.backgroundImage = "url(" + this.imageUrl + ")"; 
	}
};

ImageAnnotation.prototype.getDebugString = function() {
	return "imageUrl: " + this.getImageUrl() + ", " + Annotation.prototype.getDebugString.call(this);
};
