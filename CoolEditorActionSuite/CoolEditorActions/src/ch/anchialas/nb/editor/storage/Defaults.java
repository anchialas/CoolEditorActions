/*
 * Copyright 2011-12 by Anchialas
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id: Defaults.java 51 2014-04-01 20:37:52Z Anchialas $
 */
package ch.anchialas.nb.editor.storage;

/**
 * Helper class providing default values.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 51 $
 */
public final class Defaults {

   private Defaults() {
      // omitted
   }

   public static enum OS {

      win, mac, sun, linux, unix, unknown;
   }

   public static OS getOS() {
      String os = System.getProperty("os.name").toLowerCase();
      if (os.startsWith("win")) {
         return OS.win;
      } else if (os.startsWith("mac")) {
         return OS.mac;
      } else if (os.startsWith("sunos") || os.startsWith("solaris")) {
         return OS.sun;
      } else if (os.indexOf("nix") >= 0) {
         return OS.unix;
      } else if (os.indexOf("nux") >= 0) {
         return OS.linux;
      } else {
         return OS.unknown;
      }
   }

   static String getOpenInSystemCommand() {
      switch (getOS()) {
         case win:
            return "cmd /c \"start {0}\"";
         case mac:
            // or:  open -a /Applications/TextWrangler.app {0} 
            return "open,{0}"; // open with default application
         case sun:
            return "";
         default: // Linux or Unix
            // gnome-open {0}
            // kfmclient {0}
            return "xdg-open,{0}";
      }
   }

   static String getExploreInSystemCommand() {
      switch (getOS()) {
         case win:
            return "explorer /select,{0}";
         case mac:
            return "osascript,-e,tell application \"Finder\" to reveal POSIX file \"{0}\" activate";
         case sun:
            return "";
         default: // Linux or Unix
            return "";
      }
   }

   static String getZipCommand() {
      switch (getOS()) {
         case win:
            return "";
         case mac:
            return "zip,-r,../{3}.zip,.,-x,.*,-x,*/\\.*";
         case sun:
         default:
            return "tar,--excludes,.*,-c,-z,-f,../{3}.gzip,.";
      }
   }
}
