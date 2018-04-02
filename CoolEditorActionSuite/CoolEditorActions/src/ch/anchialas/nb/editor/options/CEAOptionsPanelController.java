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
 * $Id: CEAOptionsPanelController.java 43 2013-02-05 22:51:06Z Anchialas $
 */
package ch.anchialas.nb.editor.options;

import static ch.anchialas.nb.editor.actions.CoolActionsFactory.KEY_EXPERIMENTAL_SHOW_MENUITEMBUTTONS;

import ch.anchialas.nb.editor.actions.CoolActionsFactory;
import ch.anchialas.nb.editor.storage.CEAData;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Implementation of editor sub panel in Options Dialog.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 43 $
 * @see http://platform.netbeans.org/tutorials/nbm-options.html
 */
@OptionsPanelController.SubRegistration(displayName = "ch.anchialas.nb.editor.Bundle#CTL_CoolEditorActions_DisplayName",
                                        keywords = "ch.anchialas.nb.editor.Bundle#KW_CoolEditorActions",
                                        keywordsCategory = "Editor/CoolEditorActions",
                                        id = "CoolEditorActions",
                                        location = "Editor",
                                        position = 395)
public final class CEAOptionsPanelController extends OptionsPanelController {

   private CEAOptionsPanel optionsPanel;

   private CEAOptionsPanel getOptionsPanel() {
      if (optionsPanel == null) {
         optionsPanel = new CEAOptionsPanel();
      }
      return optionsPanel;
   }

   @Override
   public void update() {
      CEAData data = CEAData.getInstance();
      getOptionsPanel().setDefaultOpenInSystem(data.isDefaultOpenInSystem());
      optionsPanel.setActions(data.getActions());
      optionsPanel.cbShowMenuItemButtons.setSelected(NbPreferences.forModule(CoolActionsFactory.class)
              .getBoolean(KEY_EXPERIMENTAL_SHOW_MENUITEMBUTTONS, false));
   }

   @Override
   public void applyChanges() {
      CEAData data = CEAData.getInstance();
      data.setDefaultOpenInSystem(optionsPanel.isDefaultOpenInSystem());
      data.save(optionsPanel.getDataMap());
      NbPreferences.forModule(CoolActionsFactory.class).putBoolean(KEY_EXPERIMENTAL_SHOW_MENUITEMBUTTONS, 
                                                                   optionsPanel.cbShowMenuItemButtons.isSelected());
      optionsPanel.clear();
   }

   @Override
   public void cancel() {
      getOptionsPanel().clear();
   }

   @Override
   public boolean isValid() {
      return optionsPanel.isDataValid();
   }

   @Override
   public boolean isChanged() {
      return optionsPanel.isChanged();
   }

   @Override
   public HelpCtx getHelpCtx() {
      return HelpCtx.DEFAULT_HELP;
   }

   @Override
   public JComponent getComponent(Lookup masterLookup) {
      return getOptionsPanel();
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener l) {
      getOptionsPanel().addPropertyChangeListener(l);
   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener l) {
      getOptionsPanel().removePropertyChangeListener(l);
   }
}
