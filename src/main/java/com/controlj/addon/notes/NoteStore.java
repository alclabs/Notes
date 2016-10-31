/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)NoteStore

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.WriteAbortedException;
import com.controlj.green.addonsupport.web.WebContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class NoteStore {
    private static final int STORE_VERSION = 1;
    private static final String STORE_NAME = "notes";
    private static final String VERSION_KEY = "version";
    private static final String IMPORTANT_KEY = "important";
    private static final String TEXT_KEY = "text";
    private static final String READ_KEY = "read";
    private static final String MODIFIED_BY_KEY = "modby";
    private static final String MODIFIED_BY_DISPNAME_KEY = "modbydn";
    private static final String MODIFIED_ON_KEY = "modon";
    private static final String MODIFIED_ON_DESC_KEY = "modondesc";

    private final SystemConnection systemConnection;
    private final DateFormat modifiedOnDateFormat;

    public NoteStore(SystemConnection systemConnection) {
        this.systemConnection = systemConnection;
        this.modifiedOnDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.FULL, systemConnection.getOperator().getLocale());
    }

    public void writeNote(final WebContext webContext, JSONObject jsonObject) throws SystemException, WriteAbortedException, ActionExecutionException, IOException, JSONException {
        final Note note = fromJSON(fillInModificationInfo(jsonObject));
        systemConnection.runWriteAction("", new WriteAction() {
            public void execute(@NotNull WritableSystemAccess systemAccess) throws Exception {
                Location location = toLocation(systemAccess, webContext);
                writeNoteInternal(systemAccess, location, note);
            }
        });
    }

    public void writeNote(final String lookupString, JSONObject jsonObject) throws SystemException, WriteAbortedException, ActionExecutionException, IOException, JSONException {
        final Note note = fromJSON(fillInModificationInfo(jsonObject));
        systemConnection.runWriteAction("", new WriteAction() {
            public void execute(@NotNull WritableSystemAccess systemAccess) throws Exception {
                Location location = toLocation(systemAccess, lookupString);
                writeNoteInternal(systemAccess, location, note);
            }
        });
    }

    private void writeNoteInternal(WritableSystemAccess systemAccess, Location location, Note note) throws IOException, JSONException {
        DataStore dataStore = systemAccess.getDataStore(location, STORE_NAME);

        if (note.getText().trim().isEmpty())
            dataStore.delete();
        else {
            try (Writer writer = new OutputStreamWriter(dataStore.getOutputStream(), "UTF-8")) {
                writeNoteInternal(writer, note);
            }
        }
    }

    private void writeNoteInternal(Writer writer, Note note) throws IOException, JSONException {
        JSONObject jsonObject = toJSON(note);
        jsonObject.put(VERSION_KEY, STORE_VERSION);
        jsonObject.write(writer);
    }

    private JSONObject fillInModificationInfo(JSONObject jsonObject) throws JSONException {
        jsonObject.put(MODIFIED_BY_KEY, systemConnection.getOperator().getLoginName());
        jsonObject.put(MODIFIED_BY_DISPNAME_KEY, systemConnection.getOperator().getDisplayName());
        jsonObject.put(MODIFIED_ON_KEY, System.currentTimeMillis());
        return jsonObject;
    }

    private JSONObject toJSON(Note note) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(IMPORTANT_KEY, note.isImportant());
        jsonObject.put(TEXT_KEY, note.getText());
        jsonObject.put(READ_KEY, toJSONArray(note.getRead()));
        jsonObject.put(MODIFIED_BY_KEY, note.getLastModifiedBy());
        jsonObject.put(MODIFIED_BY_DISPNAME_KEY, note.getLastModifiedByDispName());
        jsonObject.put(MODIFIED_ON_KEY, note.getLastModifiedOn());
        return jsonObject;
    }

    private Note readNoteInternal(SystemAccess systemAccess, Location location) throws IOException, JSONException {
        DataStore dataStore = systemAccess.getDataStore(location, STORE_NAME);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataStore.getInputStream(), "UTF-8"))) {
            return readNoteInternal(reader);
        }
    }

    private Note readNoteInternal(Reader reader) throws IOException {
        JSONTokener tokener = new JSONTokener(reader);
        try {
            JSONObject jsonObject = new JSONObject(tokener);
            if (jsonObject.getInt(VERSION_KEY) != STORE_VERSION)
                return new Note();

            return fromJSON(jsonObject);
        } catch (JSONException e) {
            return new Note();
        }
    }

    private Note fromJSON(JSONObject jsonObject) throws IOException, JSONException {
        return new Note(jsonObject.getBoolean(IMPORTANT_KEY),
                        jsonObject.getString(TEXT_KEY),
                        toList(jsonObject.getJSONArray(READ_KEY)),
                        jsonObject.getString(MODIFIED_BY_KEY),
                        jsonObject.getString(MODIFIED_BY_DISPNAME_KEY),
                        jsonObject.getLong(MODIFIED_ON_KEY)
        );
    }

    private JSONObject fillInModificationDate(JSONObject jsonObject) throws JSONException {
        long modTime = jsonObject.getLong(MODIFIED_ON_KEY);
        if (modTime == 0)
            jsonObject.put(MODIFIED_ON_DESC_KEY, "[unknown date]");
        else {
            synchronized (modifiedOnDateFormat) {
                jsonObject.put(MODIFIED_ON_DESC_KEY, modifiedOnDateFormat.format(new Date(modTime)));
            }
        }
        return jsonObject;
    }

    public JSONObject readNote(final WebContext webContext) throws ActionExecutionException, SystemException, IOException, JSONException {
        Note result = systemConnection.runReadAction(new ReadActionResult<Note>() {
            @Nullable @Override public Note execute(@NotNull SystemAccess systemAccess) throws Exception {
                Location location = toLocation(systemAccess, webContext);
                return readNoteInternal(systemAccess, location);
            }
        });

        return fillInModificationDate(toJSON(result == null ? new Note() : result));
    }

    public JSONObject readNote(final String lookupString) throws ActionExecutionException, SystemException, IOException, JSONException {
        Note result = systemConnection.runReadAction(new ReadActionResult<Note>() {
            @Nullable @Override public Note execute(@NotNull SystemAccess systemAccess) throws Exception {
                Location location = toLocation(systemAccess, lookupString);
                return readNoteInternal(systemAccess, location);
            }
        });

        return fillInModificationDate(toJSON(result == null ? new Note() : result));
    }

    public Collection<LocationReference> findNotes() throws ActionExecutionException, SystemException {
        final List<LocationReference> notePaths = new ArrayList<LocationReference>();
        systemConnection.runReadAction(new ReadAction() {
            @Override public void execute(@NotNull SystemAccess systemAccess) throws Exception {
                Collection<Location> locations = systemAccess.getDataStoreLocations();
                for (Location location : locations) {
                    if (systemAccess.getDataStore(location, "notes").exists())
                        notePaths.add(new LocationReference(location));
                }
            }
        });

        return notePaths;
    }

    private Location toLocation(SystemAccess systemAccess, WebContext webContext) throws UnresolvableException {
        try {
            return webContext.getLinkedFromLocation(systemAccess.getTree(SystemTree.Geographic));
        } catch (UnresolvableException e) {
            return webContext.getLinkedFromLocation(systemAccess.getTree(SystemTree.Network));
        }
    }

    private Location toLocation(SystemAccess systemAccess, String lookupString) throws UnresolvableException {
        try {
            return systemAccess.getTree(SystemTree.Geographic).resolve(lookupString);
        } catch (UnresolvableException e) {
            return systemAccess.getTree(SystemTree.Network).resolve(lookupString);
        }
    }

    public static List<String> toList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++)
            list.add(array.getString(i));
        return list;
    }
    public static JSONArray toJSONArray(List<String> list) throws JSONException {
        JSONArray array = new JSONArray();
        for (String item : list)
            array.put(item);
        return array;
    }
}

