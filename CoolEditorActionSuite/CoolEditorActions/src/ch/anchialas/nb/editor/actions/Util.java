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
 * $Id: Util.java 47 2013-07-17 21:13:41Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.lookup.Lookups;

/**
 * Utility methods.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 47 $
 */
public final class Util {

   private Util() {
      // omitted
   }

   
   public static File getFile(FileObject fo) throws FileStateInvalidException {
      File f = FileUtil.toFile(fo);
      if (f == null && fo.getFileSystem() instanceof JarFileSystem) {
         JarFileSystem jarFS = (JarFileSystem)fo.getFileSystem();
         f = jarFS.getJarFile();
         String path = fo.getPath();
         f = new File(f, path);
      }
      return f;
   }


   public static File getParentFile(FileObject fo) throws FileStateInvalidException {
      File f = getFile(fo);
      return f == null ? null : f.getParentFile();
   }


   public static String getParentFilePath(FileObject fo) throws FileStateInvalidException {
      File f = getParentFile(fo);
      return f == null ? "" : f.getAbsolutePath();
   }

   
   // for use from layers
   public static ContextAwareAction alwaysEnabled(Map m) {
      try {
         Class<?> aeaClass = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.openide.awt.AlwaysEnabledAction");
         Method createMethod = aeaClass.getDeclaredMethod("create", Map.class);
         createMethod.setAccessible(true);
         Action action = (Action) createMethod.invoke(aeaClass, m);
         return new ContextAwareActionWrapper(action);

      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
         return null;
      }
   }
   public static ContextAwareAction contextAware(Map m) {
      ContextAwareAction fallback = (ContextAwareAction)m.get("fallback");
      return fallback;
   }
   
   
   /**
    * Lookup and get the action instance with the given path.
    * 
    * @param actionPath the path to the action in the layer.xml, e.g. {@code Actions/Edit/org-netbeans-...-XXXAction.instance}
    * @return the action instance or {@code null} if no action found
    */
   public static Action lookupActionInLayer(String actionPath) {
      try {
         return FileUtil.getConfigObject(actionPath, Action.class);
      } catch (Exception ex) {
         Logger.getLogger(Util.class.getName()).log(Level.SEVERE, "Can't find layer action with path: {0}", actionPath);
         return null;
      }
   }

   static Action createSelectNodeAction(FileObject fo, boolean isProjectTab) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
      // Use reflection instead a implementation dependency to the project.ui module
      ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
      Class<?> cSelectNodeAction = cl.loadClass("org.netbeans.modules.project.ui.actions.SelectNodeAction");
      Constructor<?> con = cSelectNodeAction.getDeclaredConstructor(Icon.class, String.class, String.class, Lookup.class);
      con.setAccessible(true);

      String findIn = getProjectTabTC_ID(isProjectTab);
      String tab = isProjectTab ? "Project" : "Files";
      String img = "org/netbeans/modules/project/ui/resources/" + tab + "Tab.png";
      String msg = isProjectTab ? "LBL_SelectInProjectsAction_MainMenuName" : "LBL_SelectInFilesAction_MainMenuName";

      return (Action) con.newInstance(ImageUtilities.loadImageIcon(img, false),
                                      NbBundle.getMessage(cSelectNodeAction, msg),
                                      findIn,
                                      Lookups.singleton(fo));
   }
   
   /**
    * @param isProjectTab true for the logical ProjectTab, false for the physical view
    * @return the ProjectTab TopComponent's Id eigther for the logical or physical view.
    */
   static String getProjectTabTC_ID(boolean isProjectTab) {
      // see org.netbeans.modules.project.ui.ProjectTab
      return isProjectTab ? "projectTabLogical_tc" : "projectTab_tc";
   }
   
   /**
    * Sets the clipboard context in textual-format.
    *
    * @param content the string to copy to the system clipboard
    */
   static void setClipboardContents(String content) {
      Clipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
      if (clipboard == null) {
         clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      }
      if (clipboard != null) {
         StatusDisplayer.getDefault().setStatusText(Bundle.CTL_Status_CopyToClipboard(content));
         clipboard.setContents(new StringSelection(content), null);
      }
   }
}
