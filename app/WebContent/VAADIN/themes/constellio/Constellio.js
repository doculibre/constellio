var isScrolling;

var lastKnownActiveElement;

function adjustViewerPanelPositionToScroll() {
	var contentFooterWrapper = document.getElementById("content-footer-wrapper");
	// Run the callback
	//console.log("Scrolling has stopped.");
	var closableViewerLayout = document.getElementById("close-button-viewer-metadata-layout");
	var newScrollTop = contentFooterWrapper.scrollTop;
	
	if (closableViewerLayout) {
		var viewerContainer = document.getElementsByClassName("main-component-wrapper")[0];
		var constellioHeader = document.getElementsByClassName("header")[0];
		
		var headerHeight = constellio_getHeight(constellioHeader);
		var viewerHeight = constellio_getHeight(closableViewerLayout);
		var viewerContainerHeight = constellio_getHeight(viewerContainer);
		var maxViewerScrollTop = viewerContainerHeight - viewerHeight;
		if (maxViewerScrollTop < 0) {
			maxViewerScrollTop = 0;
		}
		
		var newViewerScrollTop;
		if (newScrollTop == 0) {
			newViewerScrollTop = newScrollTop;
		} else if (newScrollTop <= headerHeight) {
			newViewerScrollTop = newScrollTop;
		} else {
			newViewerScrollTop = newScrollTop - headerHeight;
		}
		
		var adjustViewerPosition;
		if (newViewerScrollTop > maxViewerScrollTop) {
			adjustViewerPosition = true;
			newViewerScrollTop = maxViewerScrollTop;
		} else {
			var lastViewerScrollTop = closableViewerLayout.style.top;
			if (!lastViewerScrollTop) {
				lastViewerScrollTop = 0;
			} else {
				lastViewerScrollTop = parseInt(lastViewerScrollTop, 10);
			}
			var outOfSightScrollTop = lastViewerScrollTop + viewerHeight;
			if (newScrollTop < lastViewerScrollTop) {
				// Scrolling up
				adjustViewerPosition = true;
			} else if (newScrollTop > outOfSightScrollTop) {
				adjustViewerPosition = true;
			} else {
				adjustViewerPosition = false;
			}
		}
		if (adjustViewerPosition) {
			closableViewerLayout.style.top = newViewerScrollTop + "px";
		}
	}
}

function constellio_registerScrollListener() {
	var contentFooterWrapper = document.getElementById("content-footer-wrapper");
	if (contentFooterWrapper) {
		contentFooterWrapper.addEventListener('scroll', function ( event ) {
			// Clear our timeout throughout the scroll
			window.clearTimeout(isScrolling);

			// Set a timeout to run after scrolling ends
			isScrolling = setTimeout(function() {
				adjustViewerPanelPositionToScroll();
			}, 66);

		}, false);
	}
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

function isElementIsScrolledToTop(element){
	return element.scrollTop === 0;
}

function registerScrollCallback(callback, elementQuerySelector){
    if(!elementQuerySelector){
        elementQuerySelector = "#content-footer-wrapper";
    }

	const element = document.querySelector(elementQuerySelector);

	if(element){
		if(!element.registeredScrollCallbacks){
			element.registeredScrollCallbacks = [];

			let isScrolling = false;

			element.addEventListener("scroll", function(ev){

				if(!element.constellio_scrollProgrammaticaly){
					if(!isScrolling){
						isScrolling = true;
						const scrollStart = element.scrollTop;

						handleScrolling(element, 1, function(){
							const scrollEnd = element.scrollTop;
							const isScrollingUp = scrollEnd < scrollStart;

							element.registeredScrollCallbacks.forEach(function(registeredCallback){
								registeredCallback(isScrollingUp);
							});

							isScrolling = false;
						}, 500);
					}
				}else{
					element.constellio_scrollProgrammaticaly = false;
				}

			});
		}

		const index = element.registeredScrollCallbacks.indexOf(callback);
		if (index === -1) {
			element.registeredScrollCallbacks.push(callback);
		}
	}
}
function unregisterScrollCallback(callback, elementQuerySelector){
    if(!elementQuerySelector){
        elementQuerySelector = "#content-footer-wrapper";
    }

	const element = document.querySelector(elementQuerySelector);

	if(element.registeredScrollCallbacks){
		const index = element.registeredScrollCallbacks.indexOf(callback);
		if (index > -1) {
			element.registeredScrollCallbacks.splice(index, 1);
		}
	}
}
function handleScrolling(element, isScrollingWhenScrollDeltaIsHigherThanThis, whatToDoWhenScrollingIsOver, timeoutInterval){
    if(!isScrollingWhenScrollDeltaIsHigherThanThis){
        isScrollingWhenScrollDeltaIsHigherThanThis = 10;
    }

    if(!whatToDoWhenScrollingIsOver){
        whatToDoWhenScrollingIsOver =  function(){};
    }

    if(!timeoutInterval){
        timeoutInterval =  500;
    }


	const scrollStart = element.scrollTop;

	setTimeout(function(){
		const scrollEnd = element.scrollTop;
		const scrollDelta = Math.abs(scrollStart - scrollEnd);

		const isStillScrolling = scrollDelta > isScrollingWhenScrollDeltaIsHigherThanThis;

		if(isStillScrolling){
			const variableWaiting = scrollDelta/isScrollingWhenScrollDeltaIsHigherThanThis;
			handleScrolling(element, isScrollingWhenScrollDeltaIsHigherThanThis, whatToDoWhenScrollingIsOver, variableWaiting);
		}else{
			whatToDoWhenScrollingIsOver();
		}
	}, timeoutInterval);
}

function elementIsVisibleOnScreen(elementRequiredId, callback, overflowElementQuerySelector){
	const overflowElement = document.querySelector(overflowElementQuerySelector);
	const elementRequired = document.getElementById(elementRequiredId);

	if(!callback){
	    callback = function(topIsVisible, scrollTopValueToShowTopOfRequiredElement, bottomIsVisible, scrollTopValueToShowBottomOfRequiredElement){};
	}

	if(!overflowElementQuerySelector){
        overflowElementQuerySelector = "#content-footer-wrapper";
    }

	if(overflowElement && elementRequired){
		const overflowClientRect = overflowElement.getBoundingClientRect();
		const overflowVisibleTop = overflowClientRect.top;
		const overflowVisibleBottom = overflowVisibleTop + overflowElement.clientHeight;

		const elementRequiredClientRect = elementRequired.getBoundingClientRect();
		const elementRequiredHeight = elementRequired.clientHeight;
		const elementRequiredRelativeTop = elementRequiredClientRect.top - overflowClientRect.top;
		const elementRequiredRelativeBottom = elementRequiredRelativeTop + elementRequiredHeight;

		const topIsHigherThanVisibleViewport = elementRequiredRelativeTop < 0;
		const topIsLowerThanVisibleViewport = elementRequiredRelativeTop > overflowVisibleBottom;
		const topIsVisible = !(topIsLowerThanVisibleViewport || topIsHigherThanVisibleViewport);

		let scrollTopValueToShowTopOfRequiredElement = 0;
		if(!topIsVisible){
			if(topIsHigherThanVisibleViewport){
				scrollTopValueToShowTopOfRequiredElement = overflowElement.scrollTop + elementRequiredRelativeTop;
			}else{
				scrollTopValueToShowTopOfRequiredElement = overflowElement.scrollTop + elementRequiredRelativeTop - overflowElement.clientHeight + 1;
			}
		}

		const bottomIsHigherThanVisibleViewport = elementRequiredRelativeBottom < 0;
		const bottomIsLowerThanVisibleViewport = elementRequiredRelativeBottom > overflowVisibleBottom;
		const bottomIsVisible = !(bottomIsHigherThanVisibleViewport || bottomIsLowerThanVisibleViewport);

		let scrollTopValueToShowBottomOfRequiredElement = 0;
		if(!bottomIsVisible){
			if(topIsHigherThanVisibleViewport){
				scrollTopValueToShowBottomOfRequiredElement = overflowElement.scrollTop + elementRequiredRelativeBottom;
			}else{
				scrollTopValueToShowBottomOfRequiredElement = overflowElement.scrollTop + elementRequiredRelativeBottom - overflowElement.clientHeight + 1;
			}
		}

		if(callback){
			callback(topIsVisible, scrollTopValueToShowTopOfRequiredElement, bottomIsVisible, scrollTopValueToShowBottomOfRequiredElement);
		}
	}
}

function getElementTopLocationRelativeToScreenAndScrollableElement(elementId, callback, overflowElementQuerySelector){
	if(!callback){
	    callback = function(){};
	}

	if(!overflowElementQuerySelector){
	    overflowElementQuerySelector = "#content-footer-wrapper";
	}

	const element = document.getElementById(elementId);
	const overflowElement = document.querySelector(overflowElementQuerySelector);

	if(element && overflowElement){
		const elementRect = element.getBoundingClientRect();
		const overflowClientRect = overflowElement.getBoundingClientRect();

		const elementRequiredRelativeTop = elementRect.top - overflowClientRect.top;
		if(callback){
			callback(elementRequiredRelativeTop);
		}
	}
}

function setElementAtTopPositionRelativeToScreenAndScrollableElement(elementId, top, overflowElementQuerySelector){
    if(!overflowElementQuerySelector){
        overflowElementQuerySelector = "#content-footer-wrapper";
    }

	const element = document.getElementById(elementId);
	const overflowElement = document.querySelector(overflowElementQuerySelector);

	if(element && overflowElement){
		const overflowClientRect = overflowElement.getBoundingClientRect();

		const relativeTop = top + overflowClientRect.top;
		let deltaToGetToRequiredTop = element.getBoundingClientRect().top - relativeTop;

		const newScrollTopValue = overflowElement.scrollTop + deltaToGetToRequiredTop;
		overflowElement.constellio_scrollProgrammaticaly = true;
		overflowElement.scrollTop = newScrollTopValue;
	}
}

function setElementAtTopOfScreen(elementId, overflowElementQuerySelector){
    if(!overflowElementQuerySelector){
        overflowElementQuerySelector = "#content-footer-wrapper";
    }

	setElementAtTopPositionRelativeToScreenAndScrollableElement(elementId, 0, overflowElementQuerySelector);
}

function setElementAtBottomOfScreen(elementId, overflowElementQuerySelector){
    if(!overflowElementQuerySelector){
        overflowElementQuerySelector = "#content-footer-wrapper";
    }

	const element = document.getElementById(elementId);
	const overflowElement = document.querySelector(overflowElementQuerySelector);

	if(element && overflowElement){
		const elementHeight = element.clientHeight;
		const visibleHeight = overflowElement.clientHeight;

		const topPosition = visibleHeight - elementHeight;

		setElementAtTopPositionRelativeToScreenAndScrollableElement(elementId, topPosition, overflowElementQuerySelector);
	}
}

function setElementAtCenterOfScreen(elementId, overflowElementQuerySelector){
    if(!overflowElementQuerySelector){
        overflowElementQuerySelector = "#content-footer-wrapper";
    }

	const element = document.getElementById(elementId);
	const overflowElement = document.querySelector(overflowElementQuerySelector);

	if(element && overflowElement){
		const elementHeight = element.clientHeight;
		const visibleHeight = overflowElement.clientHeight;

		const topPosition = visibleHeight/2 - elementHeight / 2;

		setElementAtTopPositionRelativeToScreenAndScrollableElement(elementId, topPosition, overflowElementQuerySelector);
	}
}

function areTheNumbersAlmostEqual(num1, num2, validDelta){
    if(!validDelta){
        validDelta = Number.EPSILON;
    }

    return Math.abs( num1 - num2 ) < validDelta;
}

function forceScrollToTop(querySelector){
    if(!querySelector){
        querySelector = "#content-footer-wrapper";
    }

    document.querySelector(querySelector).scrollTop = 0;
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
				};
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