/*
 * Copyright 2014 by Anchialas
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
 * limitations under the License
 *
 * $Id$
 */
package ch.anchialas.nb.toolbar;

import ch.anchialas.nb.editor.actions.ActionUtil;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.ui.ProjectGroup;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.ToolbarPool;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

@ActionID(category = "Window",
          id = "ch.anchialas.nb.toolbar.ToolbarConfigurationAction"
)
@ActionRegistration(
        //iconBase = "ch/anchialas/nb/toolbar/configuration.png",
        displayName = "#CTL_ToolbarConfigurationAction",
        lazy = false
)
@ActionReference(path = "Toolbars/ToolbarPool", position = 2000)
@Messages({
   "CTL_ToolbarConfigurationAction=Toolbar Configuration",
   "ProjectGroups.no_group=(no Project Group)"
})
public final class ToolbarConfigurationAction extends AbstractAction implements Presenter.Toolbar {

   private JButton button;

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand() == null) {
         // show popup menu
         button.mouseDown(null, 30, 30);
      } else {
         ToolbarPool.getDefault().setConfiguration(e.getActionCommand());
         StatusDisplayer.getDefault().setStatusText("Switched to Toolbar configuration '" + e.getActionCommand() + "'");
      }
   }

   @Override
   public Component getToolbarPresenter() {
      if (button == null) {
         final JPopupMenu popup = new JPopupMenu();
         popup.add("dummy");

//      Action a = this;
//      a.putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("ch/anchialas/nb/toolbar/configuration.png", false));
//      a.putValue("iconBase", a.getValue(Action.SMALL_ICON));
//      a.putValue("noIconInMenu", Boolean.FALSE); //NOI18N
         button = DropDownButtonFactory.createDropDownButton(
                 ImageUtilities.loadImageIcon("ch/anchialas/nb/toolbar/configuration.png", false),
                 //new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)),
                 popup);

         button.addActionListener(this);
//      Actions.connect(button, a);

         PropertyChangeListener pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
               popup.removeAll();
               for (DataObject dataObject : ToolbarPool.getDefault().getFolder().getChildren()) {
                  if ("text/xml".equals(dataObject.getPrimaryFile().getMIMEType())) {
                     popup.add(dataObject.getName()).addActionListener(ToolbarConfigurationAction.this);
                  }
               }
               popup.addSeparator();

               JMenuItem mi = popup.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-groups-GroupsMenu.instance"));
               Mnemonics.setLocalizedText(mi, mi.getText());

               setProjectGroups(popup);
            }

         };
         popup.addPropertyChangeListener("ancestor", pcl);

//         popup.addSeparator();
//         setProjectGroups(popup);
      }
      return button;
   }

   private void setProjectGroups(JPopupMenu popup) {
      try {
         Class<?> c = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.modules.project.ui.groups.Group");
         // get Project Group infos
         Object activeGroup;
         Collection/*<Group>*/ groups;
         {
            Method m = c.getMethod("getActiveGroup");
            m.setAccessible(true);
            activeGroup = m.invoke(c);
         }
         // call: SortedSet<Group> Group.allGroups()
         {
            Method m = c.getMethod("allGroups");
            m.setAccessible(true);
            groups = (Collection)m.invoke(c);
         }

         ImageIcon projectsIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/projectTab.png", false);

         for (Object uiGroup : groups) {
            ProjectGroup group = getProjectGroup(uiGroup);
            //JMenuItem item = popup.add(new ProjectGroupAction(group, uiGroup));
            JMenuItem item = new JRadioButtonMenuItem(new ProjectGroupAction(group, uiGroup));
            item.setSelected(uiGroup.equals(activeGroup));
            item.setIcon(projectsIcon);
            popup.add(item);
         }
         JMenuItem item = new JRadioButtonMenuItem(new ProjectGroupAction(null, null));
         item.setText(Bundle.ProjectGroups_no_group());
         item.setSelected(activeGroup == null);
         popup.add(item);

      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   private ProjectGroup getProjectGroup(Object uiGroup) throws Exception {
      String name = (String)uiGroup.getClass().getMethod("getName").invoke(uiGroup);
      Preferences prefs = (Preferences)uiGroup.getClass().getMethod("prefs").invoke(uiGroup);
      Constructor<ProjectGroup> constructor = ProjectGroup.class.getDeclaredConstructor(String.class, Preferences.class);
      constructor.setAccessible(true);
      return constructor.newInstance(name, prefs);
   }

   private static class ProjectGroupAction extends AbstractAction {

      //private final ProjectGroup group;

      public ProjectGroupAction(final ProjectGroup group, Object uiGroup) {
         super(group == null ? null : group.getName());
         putValue("org.netbeans.modules.project.ui.groups.Group", uiGroup);
         //this.group = group;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         // Could be slow (if needs to load projects); don't block EQ.
         RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
               try {
                  Object uiGroup = getValue("org.netbeans.modules.project.ui.groups.Group");
                  // call Group.setActiveGroup(Group nue, boolean isNewGroup) {
                  Class<?> c = Lookup.getDefault().lookup(ClassLoader.class).loadClass("org.netbeans.modules.project.ui.groups.Group");
                  Method m = c.getMethod("setActiveGroup", c, boolean.class);
                  m.setAccessible(true);
                  m.invoke(c, uiGroup, false);

               } catch (Exception ex) {
                  Exceptions.printStackTrace(ex);
               }
            }
         });

      }

   }
}
