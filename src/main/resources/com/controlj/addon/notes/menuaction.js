var com_controlj_addon_notes_loadScript = (function() {
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
    var head = $("#actionContent").contents().find('head');
    if (head.find("link[href=\""+src+"\"]").length == 0) {
        $("<link>")
          .appendTo($("#actionContent").contents().find('head'))
          .attr({type : 'text/css', rel : 'stylesheet'})
          .attr('href', src);
    }
}
if (window.com_controlj_addon_notes) {
    loadCss('/${addon-name}/css/jquery-ui-1.10.3.custom.min.css');
    loadCss('/${addon-name}/css/notes.css');
    com_controlj_addon_notes.showDialog('${addon-name}', '${oper-login}');
} else {
    /*!
    * domready (c) Dustin Diaz 2012 - License MIT
    */
    !function(e,t){typeof module!="undefined"?module.exports=t():typeof define=="function"&&typeof define.amd=="object"?define(t):this[e]=t()}("domready",function(e){function p(e){h=1;while(e=t.shift())e()}var t=[],n,r=!1,i=document,s=i.documentElement,o=s.doScroll,u="DOMContentLoaded",a="addEventListener",f="onreadystatechange",l="readyState",c=o?/^loaded|^c/:/^loaded|c/,h=c.test(i[l]);return i[a]&&i[a](u,n=function(){i.removeEventListener(u,n,r),p()},r),o&&i.attachEvent(f,n=function(){/^c/.test(i[l])&&(i.detachEvent(f,n),p())}),e=o?function(n){self!=top?h?n():t.push(n):function(){try{s.doScroll("left")}catch(t){return setTimeout(function(){e(n)},50)}n()}()}:function(e){h?e():t.push(e)}});

    domready(function(){
        com_controlj_addon_notes_loadScript(
                   ['/${addon-name}/js/lib/jquery-1.10.2.min.js',
                    '/${addon-name}/js/lib/jquery.ui.position.js',
                    '/${addon-name}/js/lib/jquery-ui-1.10.3.min.js',
                    '/${addon-name}/js/lib/jquery.hoverIntent.minified.js',
                    '/${addon-name}/js/dialog.js'],
            function() {
                $(function() {
                    loadCss('/${addon-name}/css/jquery-ui-1.10.3.custom.min.css');
                    loadCss('/${addon-name}/css/notes.css');
                    com_controlj_addon_notes.showDialog('${addon-name}', '${oper-login}');
                });
            }
        );
    });
}