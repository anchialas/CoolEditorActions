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
 * $Id$
 */
package ch.anchialas.nb.editor.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Exceptions;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Open Project Action.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 33 $
 */
final class OpenProjectAction extends AbstractAction implements Presenter.Popup, PropertyChangeListener {

   private static final long serialVersionUID = -5904248865623321452L;
   //
   private final Project project;
   private final boolean isProjectTab;
   private final boolean doSelect;

   public OpenProjectAction(Project project, Icon icon, boolean isProjectTab, boolean doSelect) {
      super(doSelect ? "Open & Select Project" : "Open Project", icon);
      this.project = project;
      this.isProjectTab = isProjectTab;
      this.doSelect = doSelect;
      putValue(Action.LONG_DESCRIPTION, project == null ? "Project can't be recognized by IDE" : "Project: " + project.getProjectDirectory().getName());
   }

   @Override
   public Object getValue(String key) {
      if (Action.NAME.equals(key)) {
         StringBuilder sb = new StringBuilder();
         if (!OpenProjects.getDefault().isProjectOpen(project)) {
            sb.append("Open");
         }
         if (doSelect) {
            if (sb.length() > 0) {
               sb.append(" & ");
            }
            sb.append("Select");
         }
         sb.append(" ");
         sb.append("Project");
         sb.append(" ");
         sb.append(isProjectTab ? "in Projects" : "in Files");
         return sb.toString();
      }
      return super.getValue(key);
   }

   @Override
   public boolean isEnabled() {
      return project != null && (doSelect || OpenProjects.getDefault().isProjectOpen(project));
   }

   @Override
   public void actionPerformed(ActionEvent ae) {
      if (OpenProjects.getDefault().isProjectOpen(project)) {
         if (doSelect) {
            selectProjectNode();
         }
      } else {
         OpenProjects.getDefault().open(new Project[]{project}, false, true);
         OpenProjects.getDefault().addPropertyChangeListener(this);
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent pce) {
      if ("openProjects".equalsIgnoreCase(pce.getPropertyName())) {
         // org.netbeans.modules.project.ui.OpenProjectList.PROPERTY_OPEN_PROJECTS
         Project[] projs = (Project[]) pce.getNewValue();
         if (Arrays.asList(projs).contains(project)) {
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  selectProjectNode();
               }
            });
            OpenProjects.getDefault().removePropertyChangeListener(this);
         }
      }
   }

   private void selectProjectNode() {
      TopComponent tc = WindowManager.getDefault().findTopComponent(ActionUtil.getProjectTabTC_ID(isProjectTab));
      tc.open();
      tc.requestActive();
      if (tc instanceof ExplorerManager.Provider) {
         String projectName = ProjectUtils.getInformation(project).getName();
         String nodeName = isProjectTab ? project.getProjectDirectory().getNameExt() : projectName;
         Node projectNode = NodeOp.findChild(((ExplorerManager.Provider) tc).getExplorerManager().getRootContext(), nodeName);
         if (projectNode != null) {
            try {
               ((ExplorerManager.Provider) tc).getExplorerManager().setExploredContextAndSelection(projectNode, new Node[]{projectNode});
            } catch (PropertyVetoException ex) {
               Exceptions.printStackTrace(ex);
            }
         }
      }
   }

   @Override
   public JMenuItem getPopupPresenter() {
      return new JMenuItem(this);
   }
}
