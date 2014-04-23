/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)LocationReference

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import com.controlj.green.addonsupport.access.Location;

public class LocationReference {
    private final String displayPath;
    private final String lookupString;

    public LocationReference(Location location) {
        displayPath = location.getRelativeDisplayPath(null);
        lookupString = location.getTransientLookupString();
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public String getLookupString() {
        return lookupString;
    }
}

