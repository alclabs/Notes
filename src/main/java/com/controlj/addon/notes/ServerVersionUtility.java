/*=============================================================================
                    AUTOMATED LOGIC CORPORATION
            Copyright (c) 1999 - 2016 All Rights Reserved
     This document contains confidential/proprietary information.
===============================================================================

   @(#)ServerVersionUtility

   Author(s) jmurph
   $Log: $    
=============================================================================*/
package com.controlj.addon.notes;

import com.controlj.green.addonsupport.AddOnInfo;

public class ServerVersionUtility {
   private final AddOnInfo addOnInfo;

   public ServerVersionUtility(AddOnInfo addOnInfo) {
      this.addOnInfo = addOnInfo;
   }

   public boolean isPre65() {
      String versionNumberString = addOnInfo.getServerVersion().getVersionNumber();

      try {
         double versionNumber = Double.parseDouble(versionNumberString);
         return versionNumber < 6.5;
      } catch (NumberFormatException e) {
         // all versions before 6.5 were numbers that could be parsed.  If we cannot parse it anymore, it must be
         // after 6.5 (and no longer a number?).
         return false;
      }
   }
}

