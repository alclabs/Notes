/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Note

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import java.util.Collections;
import java.util.List;

public class Note {
    private final boolean important;
    private final String text;
    private final List<String> read;
    private final String lastModifiedBy;
    private final String lastModifiedByDispName;
    private final long lastModifiedOn;

    public Note() {
        this(false, "", Collections.<String>emptyList(), "", "", 0);
    }

    public Note(boolean important, String text, List<String> read, String lastModifiedBy, String lastModifiedByDispName, long lastModifiedOn) {
        this.important = important;
        this.text = text;
        this.read = read;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedByDispName = lastModifiedByDispName;
        this.lastModifiedOn = lastModifiedOn;
    }

    public boolean isImportant() {
        return important;
    }

    public String getText() {
        return text;
    }

    public List<String> getRead() {
        return Collections.unmodifiableList(read);
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getLastModifiedByDispName() {
        return lastModifiedByDispName;
    }

    public long getLastModifiedOn() {
        return lastModifiedOn;
    }
}

