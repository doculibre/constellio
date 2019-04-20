$.ajaxSetup({ cache: false });

$(document).ready(function() {

    setTimeout(function() {

        (function(BpmnViewer, $) {

            //check for duplicate
            if ($('.bjs-container').length) {
              return;
            }

            // create viewer
            var bpmnViewer = new BpmnViewer();
            bpmnViewer.attachTo('#bpmn-viewer-canvas');

            // import function
            function importXML(xml) {

                // import diagram
                bpmnViewer.importXML(xml, function(err) {
                    if (err) {
                        console.log('could not render diagram', err);
                    }

                    var canvas = bpmnViewer.get('canvas');
                    // zoom to fit full viewport
                    canvas.zoom('fit-viewport');

	        	     $.post(selectionCallbackURL, {
                        'bpmnResourceKey': bpmnResourceKey,
                        'ready':  true
                    }).done(function (data) {
                    	var dataJSON = JSON.parse(data);
                    	
                    	var deselectedIds = dataJSON['deselectedIds'];
                    	if (deselectedIds) {
                    		for (var i = 0; i < deselectedIds.length; i++) {
                    			var deselectedId = selectedIds[i];
                            	canvas.removeMarker(deselectedId);
                    		}
                    	}
                    	
                    	var selectedIds = dataJSON['selectedIds'];
                    	if (selectedIds) {
                    		for (var i = 0; i < selectedIds.length; i++) {
                    			var selectedId = selectedIds[i];
                            	canvas.addMarker(selectedId, 'highlight');
                    		}
                    	}
                    });
                });
            }

            // load external diagram file via AJAX and import it
            $.get(bpmnDiagramURL, importXML, 'text');

            var eventBus = bpmnViewer.get('eventBus');

	         // you may hook into any of the following events
	         var bpmnEvents = [
	           //'element.hover',
	           //'element.out',
	           'element.click',
	           //'element.dblclick',
	           //'element.mousedown',
	           //'element.mouseup'
	         ];

	         bpmnEvents.forEach(function(event) {
	             eventBus.on(event, function(e) {
	                 // e.element = the model element
	                 // e.gfx = the graphical element
	        	     $.post(selectionCallbackURL, {
                         'bpmnResourceKey': bpmnResourceKey,
                         'id':  e.element.id
                     }).done(function (data) {
                    	var dataJSON = JSON.parse(data);
                    	
                    	var deselectedIds = dataJSON['deselectedIds'];
                    	if (deselectedIds) {
                    		for (var i = 0; i < deselectedIds.length; i++) {
                    			var deselectedId = selectedIds[i];
                            	canvas.removeMarker(deselectedId);
                    		}
                    	}
                    	
                    	var selectedIds = dataJSON['selectedIds'];
                    	if (selectedIds) {
                    		for (var i = 0; i < selectedIds.length; i++) {
                    			var selectedId = selectedIds[i];
                            	canvas.addMarker(selectedId, 'highlight');
                    		}
                    	}
                    });
	        	     // console.info(event + ":" + e.element.id);
	           });
	         });

        })(window.BpmnJS, window.jQuery);
    }, 1000);
});