"use strict";

function AnnotationDropZoneManager(dropZoneSelector) {
	this.dropZoneSelector = dropZoneSelector;
	
	this.edgeDelta = 10;
	this.annotationMinWidth = 10;
	this.annotationMinHeight = 10;
	this.defining = false;
	this.resizing = false;
	this.moving = false;
	
	this.pressingMouse = false;
	this.draggingMouse = false;
	this.leftMouseDrag = 0;
	this.topMouseDrag = 0;
	this.currentAnnotation = null;
	this.annotations = [];
				
	$(document).on("mousedown", dropZoneSelector, this.dropZoneMouseDown.bind(this))
				.on("mouseup", dropZoneSelector, this.dropZoneMouseUp.bind(this))
				.on("mousemove", dropZoneSelector, this.dropZoneMouseMove.bind(this));
}

AnnotationDropZoneManager.prototype.getAnnotations = function(annotation) {
	var annotationObjects = [];
	var annotationIds = Object.keys(this.annotations);
	for (var i = 0; i < annotationIds.length; i++) {
		var annotationId = annotationIds[i];
		var annotationObject = this.annotations[annotationId];
		annotationObjects.push(annotationObject);
	}
	return annotationObjects;
};	

AnnotationDropZoneManager.prototype.defineAnnotation = function(annotation) {
	this.currentAnnotation = annotation;
	this.currentAnnotationElement = null;
		
	var dropZone = $(this.dropZoneSelector);
	dropZone.addClass("annotation-defining");
	this.defining = true;
};

AnnotationDropZoneManager.prototype.loadAnnotation = function(annotation) {
	var self = this;
	if (!annotation.isAttached()) {
		var dropZone = $(this.dropZoneSelector);
		var annotationElement = annotation.getHtmlElement();
		dropZone.append(annotationElement);
		this.addAnnotationListeners(annotation);
		this.annotations[annotation.id] = annotation;	
		
		var defaultRemove = annotation.remove;
		if (!defaultRemove) {
			defaultRemove = Object.getPrototypeOf(annotation).remove;
		}		
		annotation.remove = function(e) {
			defaultRemove.call(annotation)
			delete self.annotations[annotation.id];
			self.onAnnotationRemoved(annotation);        
		};

		var defaultGetSaveCallback = annotation.getSaveCallback;
		if (!defaultGetSaveCallback) {
			defaultGetSaveCallback = Object.getPrototypeOf(annotation).getSaveCallback;
		}		
		annotation.getSaveCallback = function() {
			defaultGetSaveCallback.call(annotation)
			self.onAnnotationSaved(annotation);        
		};
	}
};

AnnotationDropZoneManager.prototype.loadAnnotations = function(annotations) {
	for (var i = 0; i < annotations.length; i++) {
		var annotation = annotations[i];
		this.loadAnnotation(annotation);
	}
};

AnnotationDropZoneManager.prototype.refreshAnnotations = function() {
	var annotations = this.annotations;
	var annotationKeys = Object.keys(annotations);
	for (var i = 0; i < annotationKeys.length; i++) {
		var annotationKey = annotationKeys[i];
		var annotation = annotations[annotationKey];
		if (annotation) {
			if (annotation.isAttached()) {
				annotation.remove();
			}
			this.loadAnnotation(annotation);
		}
	}
};	

AnnotationDropZoneManager.prototype.addAnnotationListeners = function(annotation) {
	var annotationHtmlElement = annotation.getHtmlElement();
	$(annotationHtmlElement).on("mousedown", this.annotationMouseDown.bind(this, annotation))
								.on("mouseup", this.annotationMouseUp.bind(this, annotation))
								.on("mousemove", this.annotationMouseMove.bind(this, annotation));
};

AnnotationDropZoneManager.prototype.dropZoneMouseDown = function(e) {
	if (!this.pressingMouse) {
		this.pressingMouse = true;
		var dropZone = $(this.dropZoneSelector);
		var dropZoneOffset = dropZone.offset();
		
		this.leftMouseDrag = e.pageX - dropZoneOffset.left;
		this.topMouseDrag = e.pageY - dropZoneOffset.top;
		
		if (this.defining && this.currentAnnotation && !this.currentAnnotationElement) {
			this.currentAnnotationElement = this.currentAnnotation.getHtmlElement();
			this.currentAnnotationElement.style.display = "none";
			this.loadAnnotation(this.currentAnnotation);
			//dropZone.append(this.currentAnnotationElement);
			//this.addAnnotationListeners(this.currentAnnotation);
		}
	}
};

AnnotationDropZoneManager.prototype.dropZoneMouseUp = function(e) {
	var draggedMouse = this.draggingMouse;

	this.pressingMouse = false;
	this.draggingMouse = false;	
	
	var dropZone = $(this.dropZoneSelector);
	if (this.resizing) {
		dropZone.removeClass("annotation-resizing");
		this.resizing = false;
		if (this.currentAnnotation && draggedMouse) {
			this.onAnnotationResized(this.currentAnnotation);
		}
	}
	if (this.moving) {
		dropZone.removeClass("annotation-moving");
		this.moving = false;
	}
	if (this.defining) {
		// Definited annotation is too small, delete it
		if (this.currentAnnotation) {
			var currentAnnotationElement = this.currentAnnotation.getHtmlElement();
			var currentAnnotationWidth = $(currentAnnotationElement).width();
			var currentAnnotationHeight = $(currentAnnotationElement).height();
			if (currentAnnotationWidth < this.annotationMinWidth || currentAnnotationHeight < this.annotationMinHeight) {
				this.currentAnnotation.remove();
			} else {
				this.currentAnnotation.annotationDefined();
				this.onAnnotationDefined(this.currentAnnotation);
			}	
		}

		dropZone.removeClass("annotation-defining");
		this.defining = false;
	}
	this.currentAnnotation = null;
	this.currentAnnotationElement = null;
};

AnnotationDropZoneManager.prototype.dropZoneMouseMove = function(e) {
	if (this.pressingMouse && !this.draggingMouse) {
		this.draggingMouse = true;
	}
	if (this.draggingMouse && this.currentAnnotation) {
		this.annotationMouseMove(this.currentAnnotation, e);
	}
};

AnnotationDropZoneManager.prototype.annotationMouseDown = function(annotation, e) {
	if (annotation) {
		if (!this.pressingMouse) {
			this.pressingMouse = true;
			var annotationElement = $(annotation.getHtmlElement());
			var annotationOffset = annotationElement.offset();
			
			this.leftMouseDrag = e.pageX - annotationOffset.left;
			this.topMouseDrag = e.pageY - annotationOffset.top;
			
			var clickedElement = $(e.target);
			if (!annotation.isReadOnly() && annotation.isSameHtmlElement(clickedElement)) {
				var dropZone = $(this.dropZoneSelector);
				if (this.isCursorOverResizeEdge(annotation, e)) {
					dropZone.addClass("annotation-resizing");
					this.resizing = true;
				} else {
					dropZone.addClass("annotation-moving");
					this.moving = true;
				}
				this.currentAnnotation = annotation;
			}
		}
	}
};	

AnnotationDropZoneManager.prototype.annotationMouseUp = function(annotation, e) {
	if (annotation) {
		this.pressingMouse = false;
		var draggedMouse = this.draggingMouse;
		
		if (!this.draggingMouse) {
			if (e.button == 0) {
				annotation.annotationClicked(e);
			}
		} else {
			this.draggingMouse = false;	
		}
		
		var dropZone = $(this.dropZoneSelector);
		if (this.resizing) {
			dropZone.removeClass("annotation-resizing");
			this.resizing = false;
			if (draggedMouse) {
				this.onAnnotationResized(annotation);
			}
		}		
		if (this.moving) {
			dropZone.removeClass("annotation-moving");
			this.moving = false;
			if (draggedMouse) {
				this.onAnnotationMoved(annotation);			
			}	
		}
	}
};

AnnotationDropZoneManager.prototype.annotationMouseMove = function(annotation, e) {
	if (annotation) {
		if (this.pressingMouse && !this.draggingMouse) {
			this.draggingMouse = true;
		}
		if (this.draggingMouse && annotation == this.currentAnnotation) {
			if (this.defining) {
				this.resizeDefinedAnnotation(annotation, e);
			} else if (this.resizing) {
				this.resizeAnnotation(annotation, e);
			} else if (this.moving) {
				this.moveAnnotation(annotation, e);
			}
		} else {
			this.adjustCursor(annotation, e);
		}
	}
};	

AnnotationDropZoneManager.prototype.resizeDefinedAnnotation = function(annotation, e) {
	if (annotation) {
		var dropZone = $(this.dropZoneSelector);
		var dropZoneOffset = dropZone.offset();
		var dropZoneWidth = dropZone.width();
		var dropZoneHeight = dropZone.height();

		var top = e.pageY - dropZoneOffset.top - this.topMouseDrag;
		var left = e.pageX - dropZoneOffset.left - this.leftMouseDrag;

		var newWidthPixels = Math.abs(left);
		var newHeightPixels = Math.abs(top);
		
		var maxX = dropZoneWidth - newWidthPixels;
		var maxY = dropZoneHeight - newHeightPixels;

		var newXPixels;
		var newYPixels;	
		if (left < 0) {
			newXPixels = e.pageX;
		} else if (this.leftMouseDrag > maxX) {
			newXPixels = maxX;
		} else {
			newXPixels = this.leftMouseDrag;
		}	
		if (top < 0) {
			newYPixels = e.pageY;
		} else if (this.topMouseDrag > maxY) {
			newYPixels = maxY;
		} else {
			newYPixels = this.topMouseDrag;
		}
		
		var newXPercent = (newXPixels / dropZoneWidth) * 100;
		var newYPercent = (newYPixels / dropZoneHeight) * 100;
		var newWidthPercent = (newWidthPixels / dropZoneWidth) * 100;
		var newHeightPercent = (newHeightPixels / dropZoneHeight) * 100;
		
		if (annotation.getX() != newXPercent) {
			annotation.setX(newXPercent);
		}
		if (annotation.getY() != newYPercent) {
			annotation.setY(newYPercent);
		}
		if (annotation.getWidth() != newWidthPercent) {
			annotation.setWidth(newWidthPercent);
		}
		if (annotation.getHeight() != newHeightPercent) {
			annotation.setHeight(newHeightPercent);
		}

		var annotationHtmlElement = annotation.getHtmlElement();
		if (annotationHtmlElement.style.display == "none") {
			annotationHtmlElement.style.display = "";
		}
	}
};	

AnnotationDropZoneManager.prototype.resizeAnnotation = function(annotation, e) {
	if (annotation) {	
		var dropZone = $(this.dropZoneSelector);
		var dropZoneWidth = dropZone.width();
		var dropZoneHeight = dropZone.height();

		var annotationElement = annotation.getHtmlElement();
		var annotationOffset = $(annotationElement).offset();
		
		var newWidthPixels = e.pageX - annotationOffset.left;
		var newHeightPixels = e.pageY - annotationOffset.top;	
		
		if (newWidthPixels < this.annotationMinWidth) {
			newWidthPixels = this.annotationMinWidth;
		}
		if (newHeightPixels < this.annotationMinHeight) {
			newHeightPixels = this.annotationMinHeight;
		}
		
		var newWidthPercent = (newWidthPixels / dropZoneWidth) * 100;
		var newHeightPercent = (newHeightPixels / dropZoneHeight) * 100;
		if (annotation.getWidth() != newWidthPercent) {
			annotation.setWidth(newWidthPercent);
		}
		if (annotation.getHeight() != newHeightPercent) {
			annotation.setHeight(newHeightPercent);
		}
	}
};

AnnotationDropZoneManager.prototype.moveAnnotation = function(annotation, e) {
	if (annotation) {
		var dropZone = $(this.dropZoneSelector);
		var dropZoneWidth = dropZone.width();
		var dropZoneHeight = dropZone.height();

		var annotationHtmlElement = annotation.getHtmlElement();
		
		// How many pixels inside the drop zone div
		var dropZoneOffset = dropZone.offset();
		var annotationOffset = $(annotationHtmlElement).offset();
		
		var top = e.pageY - dropZoneOffset.top - this.topMouseDrag;
		var left = e.pageX - dropZoneOffset.left - this.leftMouseDrag;
		
		var maxX = dropZoneWidth - $(annotationHtmlElement).width();
		var maxY = dropZoneHeight - $(annotationHtmlElement).height();
		
		var newXPixels;
		var newYPixels;	
		if (left < 0) {
			newXPixels = 0;
		} else if (left > maxX) {
			newXPixels = maxX;
		} else {
			newXPixels = left;
		}	
		if (top < 0) {
			newYPixels = 0;
		} else if (top > maxY) {
			newYPixels = maxY;
		} else {
			newYPixels = top;
		}
		
		var newXPercent = (newXPixels / dropZoneWidth) * 100;
		var newYPercent = (newYPixels / dropZoneHeight) * 100;
		if (annotation.getX() != newXPercent) {
			annotation.setX(newXPercent);
		}
		if (annotation.getY() != newYPercent) {
			annotation.setY(newYPercent);
		}
	}
};	

AnnotationDropZoneManager.prototype.isCursorOverResizeEdge = function(annotation, e) {
	if (annotation) {
		var annotationElement = annotation.getHtmlElement();
		var rect = annotationElement.getBoundingClientRect();
		var x = e.clientX - rect.left,      // the relative mouse postion to the element
			y = e.clientY - rect.top,       // ...
			w = rect.right - rect.left,     // width of the element
			h = rect.bottom - rect.top;     // height of the element
		
		var cursorOverResizeEdge = false;
		if (y > h - this.edgeDelta && x > w - this.edgeDelta) {
			cursorOverResizeEdge = true;
		} else {
			cursorOverResizeEdge = false;
		}
		return cursorOverResizeEdge;
	}	
};	

AnnotationDropZoneManager.prototype.adjustCursor = function(annotation, e) {
	if (annotation) {
		var annotationElement = annotation.getHtmlElement();
		var rect = annotationElement.getBoundingClientRect();
		var x = e.clientX - rect.left,      // the relative mouse postion to the element
			y = e.clientY - rect.top,       // ...
			w = rect.right - rect.left,     // width of the element
			h = rect.bottom - rect.top;     // height of the element
		
		// Which cursor to use
		var c = "";           
		/*
		if (y < this.edgeDelta) {
			// North
			c += "n";
		} else if (y > h - this.edgeDelta) { 
			// South
			c += "s";    
		}	
		if (x < this.edgeDelta) {
			// West
			c += "w";     
		} else if (x > w - this.edgeDelta) { 
			// East
			c += "e";     
		}	
		*/
		if (y > h - this.edgeDelta && x > w - this.edgeDelta) {
			c = "se";
		}
		if (c && !annotation.isReadOnly()) {                               
			// If we are hovering at the border area (c is not empty), set the according cursor 
		   annotationElement.style.cursor = c + "-resize";
		} else if (this.moving) {
			annotationElement.style.cursor = "all-scroll";  
		} else {
			// Otherwise, set to default
			annotationElement.style.cursor = "pointer";  
		}
	}	
};		

AnnotationDropZoneManager.prototype.onAannotationDefined = function(annotation) {
};		

AnnotationDropZoneManager.prototype.onAnnotationSaved = function(annotation) {
};

AnnotationDropZoneManager.prototype.onAnnotationResized = function(annotation) {
};

AnnotationDropZoneManager.prototype.onAnnotationMoved = function(annotation) {
};

AnnotationDropZoneManager.prototype.onAnnotationRemoved = function(annotation) {
};
