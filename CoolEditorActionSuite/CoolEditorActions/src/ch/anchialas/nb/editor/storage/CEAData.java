/*
 * Copyright 2012 by Anchialas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: CEAData.java 51 2014-04-01 20:37:52Z Anchialas $
 */
package ch.anchialas.nb.editor.storage;

import ch.anchialas.lang.Is;
import ch.anchialas.nb.editor.options.RowData;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

import static java.util.stream.Collectors.toList;

/**
 * CoolEditorActions data storage.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 51 $
 */
public final class CEAData {

   private static final String NODE_CUSTOM = "Custom";
   private static final String NODE_COMMANDS = "Commands";
   //
   public static final String ACT_OPENINSYSTEM = "OpenInSystem";
   public static final String ACT_EXPLOREINSYSTEM = "ExploreInSystem";
   public static final String ACT_ZIP = "ZipToParentFolder";
   //
   public static final String ACT_COPYFILEPATH = "CopyFilePath";
   //
   public static final String TYPE_FOLDER = "folder";
   public static final String TYPE_FILE = "file";

   private CEAData() {
      // suppressed for non-instantiability
   }

   public static CEAData getInstance() {
      return Holder.SINGLETON;
   }

   private static class Holder {

      private static CEAData SINGLETON = new CEAData();
      private static Preferences prefs = NbPreferences.forModule(CEAData.class);
   }

   private static Preferences getCommandPrefs(CEAction action) {
      Preferences p = action.isCustom() ? Holder.prefs.node(NODE_CUSTOM) : Holder.prefs;
      return p.node(action.getStorageKey()).node(NODE_COMMANDS);
   }

   private static Collection<String> getAllMimeTypes() {
      // Invoke EditorSettings.getDefault().getAllMimeTypes() by reflection
      // (prevents Implementation Dependency to module "Editor Settings Storage")
      ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
      Class<?> cEditorSettings;
      try {
         cEditorSettings = cl.loadClass("org.netbeans.modules.editor.settings.storage.api.EditorSettings");
         Object editorSettings = cEditorSettings.getMethod("getDefault").invoke(cEditorSettings);
         return (Set<String>)cEditorSettings.getMethod("getAllMimeTypes").invoke(editorSettings);
      } catch (Exception ex) {
         Logger.getLogger(CEAData.class.getName()).log(Level.WARNING, "Cannot load mime types", ex);
         return Arrays.asList("text/plain", "text/html", "text/xml", "text/x-java", "text/x-php5");
      }
   }

   public List<RowData> getData(CEAction action) {
      List<RowData> data = new ArrayList<>();

      Preferences p = getCommandPrefs(action);
      try {
         for (String key : p.keys()) {
            data.add(new RowData(key, p.get(key, null)));
         }
      } catch (BackingStoreException ex) {
         Exceptions.printStackTrace(ex);
      }
      if (data.isEmpty()) {
         data.add(new RowData(TYPE_FILE, getDefaultCommand(action.getStorageKey())));
         data.add(new RowData(TYPE_FOLDER, getDefaultCommand(action.getStorageKey())));
         data.addAll(getAllMimeTypes().stream().map(type -> new RowData(type, null)).collect(toList()));
      }
      return data;
   }

   public List<CEAction> getActions() {
      List<CEAction> list = new ArrayList<>(5);
      // default actions
      list.add(getDefaultOpenInSystemAction());
      list.add(getDefaultExploreInSystemAction());
      list.add(getDefaultZipAction());
      // custom actions
      list.addAll(getCustomActions());
      return list;
   }

   //
   // Default actions stuff
   //
   private static CEAction createDefaultAction(String storageKey, String defaultName) {
      return new CEAction(storageKey,
                          Holder.prefs.node(storageKey).get(storageKey, defaultName),
                          false);
   }

   public CEAction getDefaultOpenInSystemAction() {
      return createDefaultAction(ACT_OPENINSYSTEM, "Open in System");
   }

   public CEAction getDefaultExploreInSystemAction() {
      return createDefaultAction(ACT_EXPLOREINSYSTEM, "Explore in System");
   }

   public CEAction getDefaultZipAction() {
      return createDefaultAction(ACT_ZIP, "ZIP (without hidden files)");
   }

   public boolean isDefaultOpenInSystem() {
      return Holder.prefs.node(ACT_OPENINSYSTEM).getBoolean("isDefault", true);
   }

   public void setDefaultOpenInSystem(boolean b) {
      Holder.prefs.node(ACT_OPENINSYSTEM).putBoolean("isDefault", b);
   }

   private String getDefaultCommand(String action) {
      if (ACT_OPENINSYSTEM.equals(action)) {
         return Defaults.getOpenInSystemCommand();
      } else if (ACT_EXPLOREINSYSTEM.equals(action)) {
         return Defaults.getExploreInSystemCommand();
      } else if (ACT_ZIP.equals(action)) {
         return Defaults.getZipCommand();
      }
      return null;
   }

   //
   // Custom actions stuff
   //
   public List<CEAction> getCustomActions() {
      List<CEAction> list = new ArrayList<>(4);
      Preferences custPrefs = Holder.prefs.node(NODE_CUSTOM);
      try {
         for (String storageKey : custPrefs.childrenNames()) {
            Preferences p = custPrefs.node(storageKey);
            list.add(new CEAction(storageKey, p.get(Action.NAME, storageKey), true));
         }
      } catch (BackingStoreException ex) {
         Exceptions.printStackTrace(ex);
      }
      return list;
   }

   //
   // action command
   //
   private String getCommand(CEAction action, String mimeType, boolean withDefault) {
      String command;
      try {
         command = getCommandPrefs(action).get(mimeType, null);
      } catch (Exception e) {
         command = null;
      }
      if (Is.empty(command)
              && (withDefault || TYPE_FILE.equals(mimeType) || TYPE_FOLDER.equals(mimeType))) {
         command = getDefaultCommand(action.getStorageKey());
      }
      return command;
   }

   public String getCommand(CEAction action, FileObject fo) {
      String command = getCommand(action, fo.isFolder() ? TYPE_FOLDER : fo.getMIMEType(), false);
      if (Is.empty(command)) {
         command = getCommand(action, fo.isFolder() ? TYPE_FOLDER : TYPE_FILE, true);
      }
      return command;
   }

//
//   public void setCommand(CEAction action, String mimeType, String command) {
//      try {
//         prefs.node(action.getStorageKey()).node(NODE_COMMANDS).put(mimeType, command);
//      } catch (Exception e) {
//         Exceptions.printStackTrace(e);
//      }
//   }
   public void save(Map<CEAction, List<RowData>> dataMap) {
      try {
         Holder.prefs.node(NODE_CUSTOM).removeNode();
      } catch (BackingStoreException ex) {
         Exceptions.printStackTrace(ex);
      }
      for (Map.Entry<CEAction, List<RowData>> entry : dataMap.entrySet()) {
         // set Commands
         Preferences prefs = getCommandPrefs(entry.getKey());
         try {
            prefs.clear();
         } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
         }
         for (RowData rowData : entry.getValue()) {
            prefs.put(rowData.getType(), rowData.getCommand() == null ? "" : rowData.getCommand());
         }
         // set Action name
         prefs.parent().put(Action.NAME, entry.getKey().getName());
         try {
            prefs.flush();
         } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }
}
