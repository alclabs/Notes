$(function() {
    var lastSavedNote;

    $(document).tooltip();
    var ajax = function() {
        function ajax(command, lookup, note, success, complete) {
            $.ajax({
                url: 'servlets/notes',
                data: {command: command, lookup: lookup, note: JSON.stringify(note)},
                dataType: 'json',
                cache: false,
                success: success,
                complete: complete
            });
        }

        return {
            findNotes: function(success) { ajax('find', undefined, undefined, success); },
            loadNote: function(lookup, success, complete) { ajax('load', lookup, undefined, success, complete); },
            saveNote: function(lookup, note) { ajax('save', lookup, note, undefined); }
        }
    }();

    function saveNote(lookup) {
        var notesText = $('#notePanel').find('.notes-text').val();
        if (lastSavedNote.text !== notesText) {
            var curNote = {text: notesText, important: lastSavedNote.important, read: lastSavedNote.read};
            lastSavedNote = curNote;
            ajax.saveNote(lookup, curNote);
        }
    }

    function loadNote(lookup) {
        var notePanel = $('#notePanel');
        notePanel.empty();
        var noteDialog = $('<div id="notes-dialog" class="notes-dialog">' +
                              '<div class="edited-by">Last edited by <span class="edited-user"></span> on <span class="edited-date"></span></div>' +
                              '<div class="marked-important" style="display: none">Marked as important.  Marked as read by <span class="num-read">0 users</span>.</div>' +
                              '<hr/>' +
                              '<textarea class="notes-text"></textarea>'+
                           '</div>').appendTo(notePanel);
        var notesText = noteDialog.find('.notes-text');

        $(window).on('resize.notes', function() {
            noteDialog.css({
                top: notePanel.height() * 0.15 - parseInt(notePanel.css("padding-top")),
                height: notePanel.height() * 0.7,
                left: notePanel.width() * 0.25,
                width: notePanel.width() * 0.5
            });
            notesText.css({height: (notePanel.height() * 0.7) - notesText.position().top})
        });
        $(window).resize();

        var loadingTimer = setTimeout(function() { notePanel.find('.notes-text').text("Loading data...") }, 250);
        ajax.loadNote(lookup, function(note) {
            lastSavedNote = note;
            notePanel.find('.notes-text').text(note.text);
            notePanel.find('.edited-user').text(note.modbydn);
            notePanel.find('.edited-user').attr("title", "Login: "+note.modby);
            notePanel.find('.edited-date').text(note.modondesc);
            if (note.important) {
                notePanel.find('.marked-important').css({display: 'block'});
                notePanel.find('.num-read').text(note.read.length+" "+(note.read.length === 1 ? "user" : "users"));
                notePanel.find('.num-read').attr("title", note.read.sort().join(', '));
            }
        }, function() { if (loadingTimer) clearTimeout(loadingTimer); });
    }

    ajax.findNotes(function(items) {
        var pathPanel = $("#pathPanel");
        pathPanel.empty();
        var list = $("<ul/>").appendTo(pathPanel);

        var li;
        var liSelected;
        function changeSelection(newSelection) {
            if (liSelected) {
                saveNote(liSelected.attr('lookup'));
                liSelected.removeClass('list-selected');
            }
            liSelected = newSelection;
            if (liSelected) {
                liSelected.addClass('list-selected');
                loadNote(liSelected.attr('lookup'));
            }
        }

        $(window).keydown(function(e) {
            if (e.target.nodeName == "TEXTAREA")
                return;

            if (e.which === 40) {
                var next = liSelected && liSelected.next();
                changeSelection(next && next.length > 0 ? next : li.first());
            } else if (e.which === 38) {
                var prev = liSelected && liSelected.prev();
                changeSelection(prev && prev.length > 0 ? prev : li.last());
            }
        });

        for (var i = 0; i < items.length; i++) {
            var obj = items[i];
            $("<li lookup='"+obj.lookup+"'><span>"+obj.dispPath+"</span></li>").appendTo(list).on('click', function(e) {
                changeSelection($(e.delegateTarget));
            })
        }
        li = $("li");
    });
});