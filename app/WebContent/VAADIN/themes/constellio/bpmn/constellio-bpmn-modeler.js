$.ajaxSetup({ cache: false });

$(document).ready(function() {

    setTimeout(function() {

        (function(BpmnModeler) {

            //check for duplicate
            if ($('.bjs-container').length) {
                return;
            }

            var bpmnModeler = new BpmnModeler({
                container: '#bpmn-modeler-canvas'
            });

            // save diagram on button click
            $('#bpmn-save-button').click(function() {
                // get the diagram contents
                bpmnModeler.saveXML({
                    format: true
                }, function(err, xml) {
                    $.post(saveButtonCallbackURL, {
                        'bpmnResourceKey': bpmnResourceKey,
                        'xml': xml
                    });
                    if (err) {
                        console.error('diagram save failed', err);
                    } else {
                        console.info('diagram saved');
                        // console.info(xml);
                    }
                });
            });


            $.get(bpmnDiagramURL, importBpmn, 'text');
            //  }

            // import function
            function importBpmn(xml) {
                bpmnModeler.importXML(xml, function(err) {
                    if (err) {
                        return console.error('could not import BPMN 2.0 diagram: ' + xml, err);
                    }

                    var canvas = bpmnModeler.get('canvas');
                    // zoom to fit full viewport
                    canvas.zoom('fit-viewport');

                });
            }

        })(window.BpmnJS);
    }, 500);
});