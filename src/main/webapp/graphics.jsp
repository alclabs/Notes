<%@ page import="com.controlj.green.addonsupport.AddOnInfo" %>
<%@ page import="com.controlj.green.addonsupport.access.DirectAccess" %>
<!DOCTYPE html>
<html style="background: transparent;">
<head>
    <link type="text/css" rel="stylesheet" href="css/jquery-ui-1.10.3.custom.min.css"/>
    <link type="text/css" rel="stylesheet" href="css/notes.css"/>
    <link type="text/css" rel="stylesheet" href="css/embeddednotes.css"/>
    <script src="js/lib/jquery-1.10.2.min.js"></script>
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
                var topDoc = window.top.document;
                var com_controlj_addon_notes_loadScript = (function() {
                    var firstScript = topDoc.getElementsByTagName('script')[0];
                    var scriptHead = firstScript.parentNode;
                    var re = /ded|co/;
                    var onload = 'onload';
                    var onreadystatechange = 'onreadystatechange';
                    var readyState = 'readyState';
                    var load = function(src, fn) {
                        var script = topDoc.createElement('script');
                        script[onload] = script[onreadystatechange] = function () {
                            if (!this[readyState] || re.test(this[readyState])) {
                                script[onload] = script[onreadystatechange] = null;
                                fn && fn(script);
                                script = null;
                            }
                        };
                        script.async = true;
                        script.src = src;
                        scriptHead.insertBefore(script, firstScript);
                    };
                    var loadMult = function(srces, fn) {
                        if (typeof srces == 'string') {
                            load(srces, fn);
                            return;
                        }
                        var src = srces.shift();
                        load(src, function () {
                            if (srces.length) {
                                loadMult(srces, fn);
                            } else {
                                fn && fn();
                            }
                        });
                    };
                    return loadMult;
                })();

                function loadCss(src) {
                    var head = $(topDoc).find("#actionContent").contents().find('head');
                    if (head.find("link[href=\""+src+"\"]").length == 0) {
                        $("<link>")
                          .appendTo($(topDoc).find("#actionContent").contents().find('head'))
                          .attr({type : 'text/css', rel : 'stylesheet'})
                          .attr('href', src);
                    }
                }
                if (window.top.com_controlj_addon_notes) {
                    loadCss('/<%=addonName%>/css/jquery-ui-1.10.3.custom.min.css');
                    loadCss('/<%=addonName%>/css/notes.css');
                    window.top.com_controlj_addon_notes.showDialog('<%=addonName%>', '<%=operLogin%>');
                    updateState(dlgDiv);
                } else {
                    /*!
                    * domready (c) Dustin Diaz 2012 - License MIT
                    */
                    !function(e,t){typeof module!="undefined"?module.exports=t():typeof define=="function"&&typeof define.amd=="object"?define(t):this[e]=t()}("domready",function(e){function p(e){h=1;while(e=t.shift())e()}var t=[],n,r=!1,i=topDoc,s=i.documentElement,o=s.doScroll,u="DOMContentLoaded",a="addEventListener",f="onreadystatechange",l="readyState",c=o?/^loaded|^c/:/^loaded|c/,h=c.test(i[l]);return i[a]&&i[a](u,n=function(){i.removeEventListener(u,n,r),p()},r),o&&i.attachEvent(f,n=function(){/^c/.test(i[l])&&(i.detachEvent(f,n),p())}),e=o?function(n){self!=top?h?n():t.push(n):function(){try{s.doScroll("left")}catch(t){return setTimeout(function(){e(n)},50)}n()}()}:function(e){h?e():t.push(e)}});

                    domready(function(){
                        com_controlj_addon_notes_loadScript(
                                   ['/<%=addonName%>/js/lib/jquery-1.10.2.min.js',
                                    '/<%=addonName%>/js/lib/jquery.ui.position.js',
                                    '/<%=addonName%>/js/lib/jquery-ui-1.10.3.min.js',
                                    '/<%=addonName%>/js/lib/jquery.hoverIntent.minified.js',
                                    '/<%=addonName%>/js/dialog.js'],
                            function() {
                                $(function() {
                                    loadCss('/<%=addonName%>/css/jquery-ui-1.10.3.custom.min.css');
                                    loadCss('/<%=addonName%>/css/notes.css');
                                    window.top.com_controlj_addon_notes.showDialog('<%=addonName%>', '<%=operLogin%>');
                                    updateState(dlgDiv);
                                });
                            }
                        );
                    });
                }
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