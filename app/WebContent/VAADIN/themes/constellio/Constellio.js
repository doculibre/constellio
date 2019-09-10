var isScrolling;

var lastScrollTop;

var lastKnownActiveElement;

function constellio_registerScrollListener() {
	var contentFooterWrapper = document.getElementById("content-footer-wrapper");
	if (contentFooterWrapper) {
		lastScrollTop = contentFooterWrapper.scrollTop;
		contentFooterWrapper.addEventListener('scroll', function ( event ) {
			// Clear our timeout throughout the scroll
			window.clearTimeout(isScrolling);

			// Set a timeout to run after scrolling ends
			isScrolling = setTimeout(function() {
				// Run the callback
				//console.log("Scrolling has stopped.");
				//var closeViewerButton = document.getElementById("close-viewer-button");
				var closableViewerLayout = document.getElementById("close-button-viewer-metadata-layout");
				var newScrollTop = contentFooterWrapper.scrollTop;
				console.log("Scroll top: " + newScrollTop);
				var scrollingUp = lastScrollTop > newScrollTop;
				if (closableViewerLayout && (scrollingUp || !constellio_isVisible(closableViewerLayout))) {
					var mainComponent = document.getElementById("main-component");
					
					var mainComponentHeight = constellio_getHeight(mainComponent);
					var closableViewerLayoutHeight = constellio_getHeight(closableViewerLayout);
					if ((newScrollTop + closableViewerLayoutHeight) > mainComponentHeight) {
						newScrollTop = mainComponentHeight - closableViewerLayoutHeight - 30;
					} else if (newScrollTop > 80) {
						newScrollTop -= 70; // Remove white space where table mode buttons are
					}
					closableViewerLayout.style.top = newScrollTop + "px";
				}
				lastScrollTop = contentFooterWrapper.scrollTop;
			}, 66);

		}, false);
	}
}

// https://stackoverflow.com/questions/19669786/check-if-element-is-visible-in-dom
function constellio_isVisible(elem) {
    if (!(elem instanceof Element)) throw Error('DomUtil: elem is not an element.');
    const style = getComputedStyle(elem);
    if (style.display === 'none') return false;
    if (style.visibility !== 'visible') return false;
    if (style.opacity < 0.1) return false;
    if (elem.offsetWidth + elem.offsetHeight + elem.getBoundingClientRect().height +
        elem.getBoundingClientRect().width === 0) {
        return false;
    }
    const elemCenter   = {
        x: elem.getBoundingClientRect().left + elem.offsetWidth / 2,
        y: elem.getBoundingClientRect().top + elem.offsetHeight / 2
    };
    if (elemCenter.x < 0) return false;
    if (elemCenter.x > (document.documentElement.clientWidth || window.innerWidth)) return false;
    if (elemCenter.y < 0) return false;
    if (elemCenter.y > (document.documentElement.clientHeight || window.innerHeight)) return false;
    let pointContainer = document.elementFromPoint(elemCenter.x, elemCenter.y);
    do {
        if (pointContainer === elem) return true;
    } while (pointContainer = pointContainer.parentNode);
    return false;
}

function constellio_getHeight(elem) {
    var rect = elem.getBoundingClientRect();
    return rect.height;
	//return elem.clientHeight;
}

function constellio_registerKeyDownListener(overflowElementId) {
    document.body.addEventListener("keydown", function(event) {
		///For IE
	    if (!event) {
	        event=window.event;
	    }
	    
	    var ignoreKeyDown;
	    var currentActiveElement = document.activeElement;
	    if (currentActiveElement) {
	    	lastKnownActiveElement = currentActiveElement;
	    	var currentActiveElementTagName = currentActiveElement.tagName;
	    	var currentActiveElementClassName = currentActiveElement.className;
	    	if (currentActiveElementTagName == "textarea") {
	    		ignoreKeyDown = true;
	    	} else if (currentActiveElementClassName && currentActiveElementClassName.indexOf("v-table") != -1) {
	    		ignoreKeyDown = true;
	    	} else {
	    		ignoreKeyDown = false;
	    	}
	    } else if (lastKnownActiveElement && lastKnownActiveElement.className.indexOf("v-table") != -1) {
	    	ignoreKeyDown = true;
	    } else {
	    	ignoreKeyDown = false;
	    }
	    
	    if (ignoreKeyDown == false) {
		    var overflowElement = document.getElementById(overflowElementId);
		    
		    var keyCodeUp = 38;
		    var keyCodeDown = 40;
		    var keyCodeHome = 36;
		    var keyCodeEnd = 35;
	        var keyCodePageUp = 33;
	        var keyCodePageDown = 34;
		    
			var keyCode = event.keyCode;
			if (keyCode == keyCodeUp) {
				scrollUp(overflowElement);
			} else if (keyCode == keyCodeDown) {
				scrollDown(overflowElement);
			} else if (keyCode == keyCodePageUp) {
				scrollPageUp(overflowElement);
			} else if (keyCode == keyCodePageDown) {
				scrollPageDown(overflowElement);
			} else if (keyCode == keyCodeHome) {
				scrollTop(overflowElement);
			} else if (keyCode == keyCodeEnd) {
				scrollEnd(overflowElement);        
			}
	    }
	}, false);
}

var stepArrowScroll = 90;
var stepArrowPage = 670;

function scrollUp(overflowElement) {
	var newScrollTop = overflowElement.scrollTop - stepArrowScroll;
	if (newScrollTop < 0) {
		newScrollTop = 0;
	}
	overflowElement.scrollTop = newScrollTop;
}

function scrollDown(overflowElement) {
	var newScrollTop = overflowElement.scrollTop + stepArrowScroll;
	overflowElement.scrollTop = newScrollTop;
}     

function scrollPageUp(overflowElement) {
	var newScrollTop = overflowElement.scrollTop - stepArrowPage;
	if (newScrollTop < 0) {
		newScrollTop = 0;
	}
	overflowElement.scrollTop = newScrollTop;
}

function scrollPageDown(overflowElement) {
	var newScrollTop = overflowElement.scrollTop + stepArrowPage;
	overflowElement.scrollTop = newScrollTop;
}

function scrollTop(overflowElement) {
	overflowElement.scrollTop = 0;
}

function scrollEnd(overflowElement) {
	overflowElement.scrollTop = 9999999;
}


/*
 * Konami-JS ~ 
 * :: Now with support for touch events and multiple instances for 
 * :: those situations that call for multiple easter eggs!
 * Code: http://konami-js.googlecode.com/
 * Examples: http://www.snaptortoise.com/konami-js
 * Copyright (c) 2009 George Mandis (georgemandis.com, snaptortoise.com)
 * Version: 1.4.2 (9/2/2013)
 * Licensed under the MIT License (http://opensource.org/licenses/MIT)
 * Tested in: Safari 4+, Google Chrome 4+, Firefox 3+, IE7+, Mobile Safari 2.2.1 and Dolphin Browser
 */

var Konami = function (callback) {
	var konami = {
		addEvent: function (obj, type, fn, ref_obj) {
			if (obj.addEventListener)
				obj.addEventListener(type, fn, false);
			else if (obj.attachEvent) {
				// IE
				obj["e" + type + fn] = fn;
				obj[type + fn] = function () {
					obj["e" + type + fn](window.event, ref_obj);
				}
				obj.attachEvent("on" + type, obj[type + fn]);
			}
		},
		input: "",
		pattern: "38384040373937396665",
		load: function (link) {
			this.addEvent(document, "keydown", function (e, ref_obj) {
				if (ref_obj) konami = ref_obj; // IE
				konami.input += e ? e.keyCode : event.keyCode;
				if (konami.input.length > konami.pattern.length)
					konami.input = konami.input.substr((konami.input.length - konami.pattern.length));
				if (konami.input == konami.pattern) {
					konami.code(link);
					konami.input = "";
					e.preventDefault();
					return false;
				}
			}, this);
			this.iphone.load(link);
		},
		code: function (link) {
			window.location = link
		}, 
		iphone: {
			start_x: 0,
			start_y: 0,
			stop_x: 0,
			stop_y: 0,
			tap: false,
			capture: false,
			orig_keys: "",
			keys: ["UP", "UP", "DOWN", "DOWN", "LEFT", "RIGHT", "LEFT", "RIGHT", "TAP", "TAP"],
			code: function (link) {
				konami.code(link);
			},
			load: function (link) {
				this.orig_keys = this.keys;
				konami.addEvent(document, "touchmove", function (e) {
					if (e.touches.length == 1 && konami.iphone.capture == true) {
						var touch = e.touches[0];
						konami.iphone.stop_x = touch.pageX;
						konami.iphone.stop_y = touch.pageY;
						konami.iphone.tap = false;
						konami.iphone.capture = false;
						konami.iphone.check_direction();
					}
				});
				konami.addEvent(document, "touchend", function (evt) {
					if (konami.iphone.tap == true) konami.iphone.check_direction(link);
				}, false);
				konami.addEvent(document, "touchstart", function (evt) {
					konami.iphone.start_x = evt.changedTouches[0].pageX;
					konami.iphone.start_y = evt.changedTouches[0].pageY;
					konami.iphone.tap = true;
					konami.iphone.capture = true;
				});
			},
			check_direction: function (link) {
				x_magnitude = Math.abs(this.start_x - this.stop_x);
				y_magnitude = Math.abs(this.start_y - this.stop_y);
				x = ((this.start_x - this.stop_x) < 0) ? "RIGHT" : "LEFT";
				y = ((this.start_y - this.stop_y) < 0) ? "DOWN" : "UP";
				result = (x_magnitude > y_magnitude) ? x : y;
				result = (this.tap == true) ? "TAP" : result;

				if (result == this.keys[0]) this.keys = this.keys.slice(1, this.keys.length);
				if (this.keys.length == 0) {
					this.keys = this.orig_keys;
					this.code(link);
				}
			}
		}
	}

	typeof callback === "string" && konami.load(callback);
	if (typeof callback === "function") {
		konami.code = callback;
		konami.load();
	}

	return konami;
};

var easter_egg = new Konami();
easter_egg.code = function() { 
	try {
		eval("constellio_easter_egg_code();");
	} catch (Exception) {
	}
}
easter_egg.load();
