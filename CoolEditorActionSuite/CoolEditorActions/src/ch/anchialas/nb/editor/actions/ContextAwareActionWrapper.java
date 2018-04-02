/*
 * Copyright 2011-12 by Anchialas
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
 * $Id: ContextAwareActionWrapper.java 39 2012-11-10 21:39:18Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * ContextAwareActionWrapper
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 39 $
 */
public class ContextAwareActionWrapper implements ContextAwareAction, Presenter.Toolbar {

   private final Action action;

   public ContextAwareActionWrapper(Action action) {
      this.action = action;
   }

   @Override
   public Action createContextAwareInstance(Lookup actionContext) {
      try {
         Method getDelegateMethod = action.getClass().getDeclaredMethod("getDelegate");
         getDelegateMethod.setAccessible(true);
         Action act = (Action)getDelegateMethod.invoke(action);
         action.putValue("Lookup", actionContext);
         if (act instanceof ContextAwareAction) {
            act = ((ContextAwareAction)act).createContextAwareInstance(actionContext);
         }
         String iconBase = (String)action.getValue("iconBase");
         if (iconBase != null) {
            act.putValue(SMALL_ICON, ImageUtilities.loadImageIcon(iconBase, true));
         }
         return act;
      } catch (Exception ex) {
         return ((ContextAwareAction)action).createContextAwareInstance(actionContext);
      }
   }

   @Override
   public Object getValue(String key) {
      //      Logger.getLogger(ContextAwareActionWrapper.class.getName()).log(
      //              Level.INFO, "getValue({0}) for {1}", new Object[]{key, action});
      return action.getValue(key);
   }

   @Override
   public void putValue(String key, Object value) {
      action.putValue(key, value);
   }

   @Override
   public void setEnabled(boolean b) {
      action.setEnabled(b);
   }

   @Override
   public boolean isEnabled() {
      return action.isEnabled();
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener listener) {
      action.addPropertyChangeListener(listener);
   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener listener) {
      action.removePropertyChangeListener(listener);
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      action.actionPerformed(e);
   }

   @Override
   public Component getToolbarPresenter() {
      if (action instanceof Presenter.Toolbar) {
         return ((Presenter.Toolbar)action).getToolbarPresenter();
      }
      return null;
   }
}
