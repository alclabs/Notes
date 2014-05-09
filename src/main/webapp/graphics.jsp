<%@ page import="com.controlj.green.addonsupport.AddOnInfo" %>
<%@ page import="com.controlj.green.addonsupport.access.DirectAccess" %>
<%@ page import="com.controlj.addon.notes.ShowDialogScriptHandler" %>
<!DOCTYPE html>
<html style="background: transparent;">
<head>
    <link type="text/css" rel="stylesheet" href="css/jquery-ui-1.10.3.custom.min.css"/>
    <link type="text/css" rel="stylesheet" href="css/notes.css"/>
    <link type="text/css" rel="stylesheet" href="css/embeddednotes.css"/>
    <script src="js/lib/jquery-1.11.0.min.js"></script>
    <script src="js/lib/jquery.ui.position.js"></script>
    <script src="js/lib/jquery-ui-1.10.3.min.js"></script>
    <script src="js/lib/jquery.hoverIntent.minified.js"></script>
    <script src="js/lib/jquery.ba-bbq.min.js"></script>
    <%
        String addonName = AddOnInfo.getAddOnInfo().getName();
        String operLogin = DirectAccess.getDirectAccess().getUserSystemConnection(request).getOperator().getLoginName();
    %>
</head>
<body>
    <div id="notes-dialog" style="display: none;"><div><div class="label">Notes</div></div></div>
    <script type="text/javascript">
        $(function() {
            var animationIntervalId;
            function updateState(dlgDiv) {
                $.ajax({
                    url: '/<%=addonName%>/servlets/notes',
                    data: {loc: addonUtility.getTreeLocationPath(), command: "load"},
                    dataType: 'json',
                    cache: false,
                    success: function(note) {
                        window.top.com_controlj_addon_notes_listener.noteChanged(note);
                    }
                });
            }

            function animateLabel(label) {
               var adjustAmount = parseInt(label.css('fontSize')) / 3;
               label.css({fontSize: '-='+adjustAmount});
               label.animate({fontSize: '+='+adjustAmount}, {
                   duration: 400
               });
            }

            var dlgDiv = $("#notes-dialog");
            window.top.com_controlj_addon_notes_listener = {
                noteChanged: function(note) {
                    var noteEmpty = $.trim(note.text) === "";
                    dlgDiv.css({display: noteEmpty ? 'none' : 'block'});
                    var label = dlgDiv.find(".label");
                    if (note.important && $.inArray('<%=operLogin%>', note.read) === -1) {
                        label.addClass("notes-settings-important");
                        if (!animationIntervalId)
                            animationIntervalId = setInterval(function() { animateLabel(label); }, 3000);
                    }
                    else {
                        label.removeClass("notes-settings-important");
                        if (animationIntervalId)
                            animationIntervalId = clearInterval(animationIntervalId);
                    }
                }
            };

            updateState(dlgDiv);

            var params = $.deparam.querystring( $.deparam.querystring().url );
            //dlgDiv.width(params.w_ctx);
            //dlgDiv.height(params.h_ctx);
            dlgDiv.addClass("notes-small");
            dlgDiv.on('click', function() {
                <%= ShowDialogScriptHandler.getInstance().getScript(addonName, operLogin, "window.top", "function() { updateState(dlgDiv); }") %>
            });

            function resizeNote(windowHeight, windowWidth) {
                var rotationDegrees = -4;
                var rotationRadians = (rotationDegrees * Math.PI) / 180;
                var rotCos = Math.cos(rotationRadians);
                var rotSin = Math.sin(rotationRadians);
                var h = rotCos * windowHeight + rotSin * windowWidth - 5/*to make room for the tape*/;
                var w = rotCos * windowWidth + rotSin * windowHeight;
                $(".notes-small").css({
                    height: h,
                    top: (windowHeight - h) / 2,
                    width: w,
                    left: (windowWidth - w) / 2
                });
                resizeNoteText(h, w);
            }
            function resizeNoteText(noteHeight, noteWidth) {
                var fontSize = Math.min(noteWidth/3.35, noteHeight/2);
                var diff = noteWidth/2.85 - fontSize;
                $(".notes-small .label").css({
                    'font-size': fontSize,
                    'padding-top': (noteHeight - (fontSize+diff)) / 2,
                    'padding-bottom': fontSize/2,
                    'padding-left': diff
                });
            }

            // resize to fit the space available
            var $window = $(window);
            resizeNote($window.height(), $window.width());

            var $iframe;
            $(window.parent.document).find('iframe').each(function(idx, frame) {
                if (frame.contentWindow == window)
                    $iframe = $(frame);
            });
            if ($iframe != undefined) {
                $window.keypress(function(e) {
                    console.log(e.which);
                    if (e.which == '110' /*n*/)
                        $iframe.css({width: '-=1'});
                    else if (e.which == '119' /*w*/)
                        $iframe.css({width: '+=1'});
                    else if (e.which == '115' /*s*/)
                        $iframe.css({height: '-=1'});
                    else if (e.which == '116' /*t*/)
                        $iframe.css({height: '+=1'});

                    resizeNote($window.height(), $window.width());
                });
            }
        });
    </script>
</body>
</html>