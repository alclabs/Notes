/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ServiceHistoryServlet

   Author(s) jmurph
   $Log: $
=============================================================================*/
package com.controlj.addon.notes;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.web.WebContext;
import com.controlj.green.addonsupport.web.WebContextFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

public class NotesServlet extends HttpServlet {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String JSON_CONTENT_TYPE = "json";

    private static final String COMMAND_PARAM = "command";
    private static final String NOTE_PARAM = "note";
    private static final String LOOKUP_PARAM = "lookup";
    private static final String TEXT_PARAM = "text";
    private static final String DISPLAY_PATH_PARAM = "dispPath";

    private static final String LOAD_COMMAND = "load";
    private static final String SAVE_COMMAND = "save";
    private static final String FIND_COMMAND = "find";

    @Override protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(UTF_8.name());
        String command = req.getParameter(COMMAND_PARAM);
        if (command.equalsIgnoreCase(LOAD_COMMAND) || command.equalsIgnoreCase(SAVE_COMMAND)) {
            try {
                if (WebContextFactory.hasLinkedWebContext(req)) {
                    final WebContext webContext = WebContextFactory.getLinkedWebContext(req);
                    NoteStore noteStore = new NoteStore(DirectAccess.getDirectAccess().getUserSystemConnection(req));

                    if (command.equalsIgnoreCase(SAVE_COMMAND)) {
                        String noteParam = req.getParameter(NOTE_PARAM);
                        JSONObject jsonObject = new JSONObject(noteParam);
                        jsonObject.put(TEXT_PARAM, new String(jsonObject.getString(TEXT_PARAM).getBytes(determineEncoding()), UTF_8));
                        noteStore.writeNote(webContext, jsonObject);
                    } else {
                        JSONObject jsonObject = noteStore.readNote(webContext);
                        resp.setContentType(JSON_CONTENT_TYPE);
                        resp.setCharacterEncoding(UTF_8.name());
                        jsonObject.write(resp.getWriter());
                    }
                } else {
                    String lookupParam = req.getParameter(LOOKUP_PARAM);
                    NoteStore noteStore = new NoteStore(DirectAccess.getDirectAccess().getUserSystemConnection(req));

                    if (command.equalsIgnoreCase(SAVE_COMMAND)) {
                        String noteParam = req.getParameter(NOTE_PARAM);
                        noteStore.writeNote(lookupParam, new JSONObject(noteParam));
                    } else {
                        JSONObject jsonObject = noteStore.readNote(lookupParam);
                        resp.setContentType(JSON_CONTENT_TYPE);
                        resp.setCharacterEncoding(UTF_8.name());
                        jsonObject.write(resp.getWriter());
                    }
                }
            } catch (InvalidConnectionRequestException | ActionExecutionException | WriteAbortedException | SystemException | JSONException e) {
                throw new IOException(e);
            }
        } else if (command.equalsIgnoreCase(FIND_COMMAND)) {
            try {
                NoteStore noteStore = new NoteStore(DirectAccess.getDirectAccess().getUserSystemConnection(req));
                Collection<LocationReference> locationReferences = noteStore.findNotes();
                resp.setContentType(JSON_CONTENT_TYPE);
                resp.setCharacterEncoding(UTF_8.name());
                JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
                jsonWriter.array();
                for (LocationReference reference : locationReferences) {
                    jsonWriter.object();
                    jsonWriter.key(DISPLAY_PATH_PARAM).value(reference.getDisplayPath());
                    jsonWriter.key(LOOKUP_PARAM).value(reference.getLookupString());
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
            } catch (InvalidConnectionRequestException | ActionExecutionException | SystemException | JSONException e) {
                throw new IOException(e);
            }
        }
    }

    private static AtomicReference<Charset> encodingRef = new AtomicReference<>();

    // WebCTRL versions prior to 6.5 used the default charset for request/response encodings.  In 6.5 we changed it to be UTF-8.
    private static Charset determineEncoding() {
        Charset encoding = encodingRef.get();
        if (encoding == null) {
            boolean pre65 = new ServerVersionUtility(AddOnInfo.getAddOnInfo()).isPre65();
            encoding = pre65 ? Charset.defaultCharset() : UTF_8;
            encodingRef.set(encoding);
        }
        return encoding;
    }
}

