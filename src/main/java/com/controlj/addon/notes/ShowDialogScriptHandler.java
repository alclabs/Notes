/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2014 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ShowDialogScriptHandler

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShowDialogScriptHandler {
    private static final ShowDialogScriptHandler instance = new ShowDialogScriptHandler();

    public static ShowDialogScriptHandler getInstance() {
        return instance;
    }

    private final String script;

    private ShowDialogScriptHandler() {
        script = loadMenuActionScript();
    }

    public String getScript(String addonName, String operatorLoginName, String windowReference, String dialogCloseFunction)
    {
        return script.replaceAll("\\$\\{addon-name\\}", addonName)
                     .replaceAll("\\$\\{oper-login\\}", operatorLoginName)
                     .replaceAll("\\$\\{window-ref\\}", windowReference)
                     .replaceAll("\\$\\{close-func\\}", dialogCloseFunction);
    }

    private String loadMenuActionScript() {
        try (InputStream inputStream = getClass().getResourceAsStream("showdialog.js")) {
            return loadMenuActionScript(inputStream);
        } catch (Throwable e) {
            return "alert('error loading menu action');";
        }
    }

    private String loadMenuActionScript(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
            return sb.toString();
        }
    }
}

