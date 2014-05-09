(function($, addonName, operLogin, window, dialogCloseFunc) {
    var document = window.document;
    var $actionContent = $(document).find("#actionContent");

    var loadScript = (function() {
        var firstScript = document.getElementsByTagName('script')[0];
        var scriptHead = firstScript.parentNode;
        var re = /ded|co/;
        var onload = 'onload';
        var onreadystatechange = 'onreadystatechange';
        var readyState = 'readyState';
        var load = function(src, fn) {
            var script = document.createElement('script');
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
            } else {
                load(srces.shift(), function () {
                    if (srces.length)
                        loadMult(srces, fn);
                    else
                        fn && fn();
                });
            }
        };
        return loadMult;
    })();

    function loadCss(src) {
        var head = $actionContent.contents().find('head');
        if (head.find("link[href=\""+src+"\"]").length == 0) {
            $("<link>").appendTo($actionContent.contents().find('head'))
                       .attr({type : 'text/css', rel : 'stylesheet'})
                       .attr('href', src);
        }
    }

    /*! domready (c) Dustin Diaz 2012 - License MIT */
    !function(e,t){typeof module!="undefined"?module.exports=t():typeof define=="function"&&typeof define.amd=="object"?define(t):this[e]=t()}("domready",function(e){function p(e){h=1;while(e=t.shift())e()}var t=[],n,r=!1,i=document,s=i.documentElement,o=s.doScroll,u="DOMContentLoaded",a="addEventListener",f="onreadystatechange",l="readyState",c=o?/^loaded|^c/:/^loaded|c/,h=c.test(i[l]);return i[a]&&i[a](u,n=function(){i.removeEventListener(u,n,r),p()},r),o&&i.attachEvent(f,n=function(){/^c/.test(i[l])&&(i.detachEvent(f,n),p())}),e=o?function(n){self!=top?h?n():t.push(n):function(){try{s.doScroll("left")}catch(t){return setTimeout(function(){e(n)},50)}n()}()}:function(e){h?e():t.push(e)}});

    function loadDialog() {

        // this check is not required, you still get this message even without it (from dialog.js).  The usefulness of
        // this is that it prevents the "note" from flickering as it pops up and then we get an error when talking to
        // the server and hide it again before showing this error message.  Putting this here also prevents us from
        // having to load al this javascript and css on pages where we won't be using it.
        if (window.treeGqlLocation) {
            if (window.treeGqlLocation.indexOf("/trees/config/") === 0 || window.treeGqlLocation === "#eventtemplates" ||
                window.treeGqlLocation.indexOf("/trees/groups/") === 0) {
                alert("Notes are not supported at this location");
                return;
            }
        }

        if (window.com_controlj_addon_notes) {
            // if the action content is not visible, doing the rest of this causes errors
            if ($actionContent.css("visibility") === "visible") {
                loadCss('/'+addonName+'/css/jquery-ui-1.10.3.custom.min.css');
                loadCss('/'+addonName+'/css/notes.css');
                window.com_controlj_addon_notes.showDialog(addonName, operLogin);
                dialogCloseFunc && dialogCloseFunc();
            } else
                setTimeout(loadDialog, 50);
        } else {
            domready(function(){
                loadScript(['/'+addonName+'/js/lib/jquery-1.11.0.min.js',
                            '/'+addonName+'/js/lib/jquery.ui.position.js',
                            '/'+addonName+'/js/lib/jquery-ui-1.10.3.min.js',
                            '/'+addonName+'/js/lib/jquery.hoverIntent.minified.js',
                            '/'+addonName+'/js/dialog.js'],
                    function() {
                        $(function() { loadDialog(); });
                    }
                );
            });
        }
    }

    loadDialog();
}(jQuery, '${addon-name}', '${oper-login}', ${window-ref}, ${close-func}));