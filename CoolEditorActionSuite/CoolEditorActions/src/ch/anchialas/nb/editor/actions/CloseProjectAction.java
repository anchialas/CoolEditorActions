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
 * $Id: CoolActionsFactory.java 33 2012-07-19 06:27:46Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Close Project Action.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 33 $
 */
@NbBundle.Messages("CTL_CloseProject=Close Project")
final class CloseProjectAction extends AbstractAction {

   private static final long serialVersionUID = 3431477213032537853L;
   //
   private final Project project;

   public CloseProjectAction(Project project) {
      super(Bundle.CTL_CloseProject(),
            ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/closeProject.gif", false));
      this.project = project;
   }

   @Override
   public boolean isEnabled() {
      return OpenProjects.getDefault().isProjectOpen(project);
   }

   @Override
   public void actionPerformed(ActionEvent ae) {
      OpenProjects.getDefault().close(new Project[]{project});
   }
}
