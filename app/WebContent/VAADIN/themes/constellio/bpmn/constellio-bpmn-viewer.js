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
                });
            }

            // load external diagram file via AJAX and import it
            $.get(bpmnDiagramURL, importXML, 'text');

        })(window.BpmnJS, window.jQuery);
    }, 500);
});