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
 * $Id: ActionUtil.java 51 2014-04-01 20:37:52Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Actions utility class.
 * <p>
 * This is a noninstantiable utility class.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 51 $
 */
public final class ActionUtil {

   private ActionUtil() {
      // omitted
   }

   public static String getAcceleratorText(KeyStroke keyStroke) {
      String acceleratorDelimiter = UIManager.getString("MenuItem.acceleratorDelimiter");
      if (acceleratorDelimiter == null) {
         acceleratorDelimiter = "+";
      }
      return getAcceleratorText(keyStroke, acceleratorDelimiter);
   }

   /**
    * Accelerator label text.
    *
    * @param acceleratorDelimiter the Accelerator delimiter string, such as {@code '+'} in
    *                             {@code 'Ctrl+C'}.
    * @return the label text for the keyStroke.
    */
   private static String getAcceleratorText(KeyStroke keyStroke, String acceleratorDelimiter) {
      String accText = "";
      if (keyStroke != null) {
         int modifiers = keyStroke.getModifiers();
         if (modifiers > 0) {
            accText = KeyEvent.getKeyModifiersText(modifiers);
            accText += acceleratorDelimiter;
         }
         int keyCode = keyStroke.getKeyCode();
         if (keyCode != 0) {
            accText += KeyEvent.getKeyText(keyCode);
         } else {
            accText += keyStroke.getKeyChar();
         }
      }
      return accText;
   }

   public static String[] splitCommandArray(String command) {
      return command.split("(?<!\\\\),");
   }

   public static String[] getCommandArray(String command) {
      List<String> matchList = new ArrayList<>();
      //Pattern regex = Pattern.compile("[^,\"']+|\"[^\"]*\"|'[^']*'");
      Pattern regex = Pattern.compile("[^,\"']+|\"([^\"]*)\"|'([^']*)'");
      Matcher m = regex.matcher(command);
      //      while (m.find()) {
      //         matchList.add(m.group());
      //      }
      while (m.find()) {
         if (m.group(1) != null) {
            // Add double-quoted string without the quotes
            matchList.add(m.group(1));
         } else if (m.group(2) != null) {
            // Add single-quoted string without the quotes
            matchList.add(m.group(2));
         } else {
            // Add unquoted word
            matchList.add(m.group());
         }
      }
      return matchList.toArray(new String[0]);
   }

   // for use from layers
   public static ContextAwareAction alwaysEnabled(Map m) {
      try {
         Class<?> aeaClass = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.openide.awt.AlwaysEnabledAction");
         Method createMethod = aeaClass.getDeclaredMethod("create", Map.class);
         createMethod.setAccessible(true);
         Action action = (Action)createMethod.invoke(aeaClass, m);
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
    * @param actionPath the path to the action in the layer.xml, e.g.
    *                   {@code Actions/Edit/org-netbeans-...-XXXAction.instance}
    * @return the action instance or {@code null} if no action found
    */
   public static Action lookupActionInLayer(String actionPath) {
      try {
         return FileUtil.getConfigObject(actionPath, Action.class);
      } catch (Exception ex) {
         Logger.getLogger(ActionUtil.class.getName()).log(Level.SEVERE, "Can't find layer action with path: {0}", actionPath);
         return null;
      }
   }

   public static Action lookupActionInLayer(String actionPath, Object... lookupObjects) {
      Action act = lookupActionInLayer(actionPath);
      if (act instanceof ContextAwareAction) {
         return ((ContextAwareAction)act).createContextAwareInstance(Lookups.fixed(lookupObjects));
      }
      return act;
   }

   public static Action createSelectNodeAction(FileObject fo, boolean isProjectTab) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
      String tab = isProjectTab ? "Projects" : "Files";
      Action act = lookupActionInLayer("Actions/Window/SelectDocumentNode/org-netbeans-modules-project-ui-SelectIn" + tab + ".instance");
      if (act instanceof ContextAwareAction) {
         return ((ContextAwareAction)act).createContextAwareInstance(Lookups.singleton(fo));
      }
      // Use reflection instead a implementation dependency to the project.ui module
      ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
      Class<?> cSelectNodeAction = cl.loadClass("org.netbeans.modules.project.ui.actions.SelectNodeAction");
      Constructor<?> con = cSelectNodeAction.getDeclaredConstructor(Icon.class, String.class, String.class, Lookup.class);
      con.setAccessible(true);

      String findIn = getProjectTabTC_ID(isProjectTab);
      tab = isProjectTab ? "Project" : "Files";
      String img = "org/netbeans/modules/project/ui/resources/" + tab + "Tab.png";
      String msg = isProjectTab ? "LBL_SelectInProjectsAction_MainMenuName" : "LBL_SelectInFilesAction_MainMenuName";
      return (Action)con.newInstance(ImageUtilities.loadImageIcon(img, false), NbBundle.getMessage(cSelectNodeAction, msg), findIn, Lookups.singleton(fo));
   }

   /**
    * @param isProjectTab true for the logical ProjectTab, false for the physical view
    * @return the ProjectTab TopComponent's Id eigther for the logical or physical view.
    */
   public static String getProjectTabTC_ID(boolean isProjectTab) {
      // see org.netbeans.modules.project.ui.ProjectTab
      return isProjectTab ? "projectTabLogical_tc" : "projectTab_tc";
   }
}
