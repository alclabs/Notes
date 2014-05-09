/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2013 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)Menu

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import com.controlj.green.addonsupport.AddOnInfo;
import com.controlj.green.addonsupport.access.Operator;
import com.controlj.green.addonsupport.web.menus.*;
import org.jetbrains.annotations.NotNull;

public class NotesMenu implements SystemMenuProvider {
    private static final String ENTRY_KEY = NotesMenu.class.getPackage().getName()+".Notes";

    @Override public void updateMenu(@NotNull Operator operator, @NotNull com.controlj.green.addonsupport.web.menus.Menu menu) {
        String actionJS = ShowDialogScriptHandler.getInstance().getScript(AddOnInfo.getAddOnInfo().getName(),
                                                                          operator.getLoginName(), "window", "null");

        MenuEntryFactory entryFactory = MenuEntryFactory.newEntry(ENTRY_KEY)
                                                        .display("Notes")
                                                        .action(new MenuAction(actionJS));
        menu.addMenuEntry(entryFactory.create());
    }

    private static class MenuAction implements Action {
        private final String menuAction;
        public MenuAction(String menuAction) { this.menuAction = menuAction; }
        @NotNull @Override public String getJavaScript() { return menuAction; }
    }
}