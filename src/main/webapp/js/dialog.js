var com_controlj_addon_notes = function($) {
    var addonName;
    var operLogin;
    var lastSavedNote = {};
    var origUnloadCallback;

    function showDialog(_addonName, _operLogin) {
        addonName = _addonName;
        operLogin = _operLogin;

        var actionFrame = $("#actionContent");
        var actionContent = actionFrame.contents().find('body');
        var dlgDiv = $('<div id="notes-dialog"></div>');
        dlgDiv.dialog(
            {
                appendTo: actionContent,
                height: actionFrame.height() * 0.5,
                width: actionFrame.width() * 0.5,
                position: [actionFrame.width() * 0.25, actionFrame.height() * 0.25],
                dialogClass: "notes-dialog",
                modal: true,
                draggable: false,
                resizable: false,
                close: function() { dlgDiv.remove(); }
            }
        );

        origUnloadCallback=actionContent[0].onunload;
        actionContent[0].onunload=function(event) {
            saveAndClose(dlgDiv);
            origUnloadCallback && origUnloadCallback(event);
        };

        $(window).on('resize.notes', function() {
            dlgDiv.dialog('option',
                {
                    height: actionFrame.height() * 0.5,
                    width: actionFrame.width() * 0.5,
                    position: [actionFrame.width() * 0.25, actionFrame.height() * 0.25]
                });
        });

        // create the settings button and dropdown menu
        var header = $('<div class="notes-header">' +
                          '<div class="notes-settings ui-icon ui-icon-custom icon-options"/>' +
                          '<ul class="notes-settings-menu">' +
                             '<li class="notes-settings-important"><a><span class="ui-icon ui-icon-blank"></span>Mark as Important</a></li>' +
                             '<li class="notes-settings-read"><a><span class="ui-icon ui-icon-blank"></span>Mark as Read</a></li>' +
                          '</ul>' +
                       '</div>').prependTo(dlgDiv.parent());
        var menu = header.find(".notes-settings-menu").menu({
            select: function(event, ui) {
                var icon = ui.item.find(".ui-icon");
                var checked = !isIconChecked(icon);
                setIconChecked(icon, checked);
                if (ui.item.hasClass("notes-settings-important"))
                    setMarkAsReadEnabled(dlgDiv, checked);
            }
        });
        menu.hide();
        menu.hoverIntent(function() {menu.show(); }, function() {menu.hide();});
        header.find(".notes-settings").on("click", function() {
            if (!menu.is(':visible')) {
                menu.show().position({
                    my: "right top",
                    at: "right bottom",
                    of: this
                });
            } else {
                menu.hide();
            }
        });

        actionContent.find(".ui-widget-overlay").on("click.notes", function() {
            if (lastSavedNote.important && !isImportantChecked(dlgDiv)) {
                var alertDiv = $(
                    '<div title="Remove important mark?">' +
                        '<p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 20px 0;"></span>' +
                        'Removing the important mark will reset the status of users that have read this note.  Are you sure you want to do this?</p>'+
                    '</div>').appendTo(dlgDiv);
                alertDiv.dialog({
                    resizable: false,
                    modal: true,
                    buttons: {
                        Yes: function() {
                            $( this ).dialog( "close" );
                            saveAndClose(dlgDiv);
                        },
                        Cancel: function() {
                            $( this ).dialog( "close" );
                        }
                    }
                });
            } else {
                saveAndClose(dlgDiv);
            }
        });

        loadNote(dlgDiv);
    }

    function isImportantChecked(dlgDiv) {
        return isIconChecked(dlgDiv.parent().find(".notes-settings-important .ui-icon"));
    }

    function setImportantChecked(dlgDiv, checked) {
        setIconChecked(dlgDiv.parent().find(".notes-settings-important .ui-icon"), checked);
    }

    function isMarkAsReadChecked(dlgDiv) {
        return isIconChecked(dlgDiv.parent().find(".notes-settings-read .ui-icon"));
    }

    function setMarkAsReadEnabled(dlgDiv, enabled) {
        var read = dlgDiv.parent().find(".notes-settings-read");
        setIconChecked(read.find(".ui-icon"), enabled);
        if (enabled)
            read.removeClass("ui-state-disabled");
        else
            read.addClass("ui-state-disabled");
    }

    function isIconChecked(icon) {
        return icon.hasClass('ui-icon-check');
    }

    function setIconChecked(icon, checked) {
        if (checked)
            icon.addClass('ui-icon-check');
        else
            icon.removeClass('ui-icon-check');
    }

    function saveNote(dlgDiv, note) {
        ajax(dlgDiv, "save", note).fail(function(jqXHR, textStatus, errorThrown) {
            alert("Error saving note.  Failure: "+textStatus+" ("+errorThrown+")");
        });
    }
    function loadNote(dlgDiv, note) {
        ajax(dlgDiv, "load", note).fail(function() {
            close(dlgDiv);
            alert("Notes are not supported at this location");
        });
    }
    function ajax(dlgDiv, command, note) {
        var loadingTimer;
        if (command === "load" && !$.trim(dlgDiv.html()))
            loadingTimer = setTimeout(function() { dlgDiv.html("Loading data...")}, 250);

        var req = $.ajax({
            url: '/'+addonName+'/servlets/notes',
            data: {loc: treeGqlLocation, command: command, note: JSON.stringify(note)},
            dataType: command === "load" ? 'json' : 'text',
            contentType: "application/x-www-form-urlencoded;charset=UTF-8",
            cache: false
        });
        req.done(function(note) {
            try {
                dlgDiv.empty();
                lastSavedNote = note;
                displayNote(dlgDiv, note);
            } catch (e) { /* ignored, we a saving b/c the user is navigating to another page */ }
        });
        req.always(function() {
            if (loadingTimer) clearTimeout(loadingTimer);
        });
        return req;
    }

    function displayNote(dlgDiv, note) {
        $('<div class="notes-area"><textarea rows="3" class="notes-text"/></div>').appendTo(dlgDiv);
        dlgDiv.find(".notes-text").focus().val(note.text);
        setImportantChecked(dlgDiv, note.important);
        setMarkAsReadEnabled(dlgDiv, note.important);
    }

    function saveAndClose(dlgDiv) {
        var curNote = {important: isImportantChecked(dlgDiv), text: dlgDiv.find(".notes-text").val(), read: (lastSavedNote.read || []).slice()};

        if (!curNote.important)
            curNote.read = [];
        else {
            var operIndex = $.inArray(operLogin, curNote.read);
            if (isMarkAsReadChecked(dlgDiv) && operIndex === -1)
                curNote.read.push(operLogin);
            else if (!isMarkAsReadChecked(dlgDiv) && operIndex !== -1)
                curNote.read.splice(operIndex, 1);
        }

        close(dlgDiv);

        if (lastSavedNote.text !== curNote.text || lastSavedNote.important !== curNote.important || lastSavedNote.read.length !== curNote.read.length) {
            lastSavedNote = curNote;
            try {
                window.com_controlj_addon_notes_listener && com_controlj_addon_notes_listener.noteChanged(curNote);
            } catch (e) { /* just ignore it, we are navigating away */ }
            saveNote(dlgDiv, curNote);
        }
    }

    function close(dlgDiv) {
        dlgDiv.dialog("close");
        $(window).off('resize.notes');
        var actionContent = $("#actionContent").contents().find('body');
        actionContent[0].onunload=origUnloadCallback;
        actionContent.find(".ui-widget-overlay").off("click.notes");
    }

    return {
        showDialog: showDialog
    };
}(jQuery.noConflict(true));