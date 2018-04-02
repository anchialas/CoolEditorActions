/*
 * Copyright 2013 by Anchialas
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

import ch.anchialas.lang.Pair;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;

/**
 * Copies the full qualified name (FQN) of the active java class to the clipboard.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @see https://platform.netbeans.org/tutorials/nbm-copyfqn.html
 */
@ActionID(category = "Edit",
          id = "ch.anchialas.nb.editor.actions.CopyFQNAction")
@ActionRegistration(displayName = "#CTL_CopyFQNAction", lazy = true)
@ActionReferences({
   @ActionReference(path = "Menu/GoTo/Inspect", position = 510, separatorAfter = 511)
   ,
   @ActionReference(path = "Shortcuts", name = "C-C C-Q")
})
@Messages({
   "CTL_CopyFQNAction=Copy Full Qualified Name (FQN)",
   "MSG_CopyFQNFailed=Cannot copy the FQN for file ''{0}''"
})
public final class CopyFQNAction extends AbstractAction {

   private final FileObject fo;

   public CopyFQNAction() {
      this(null);
   }

   CopyFQNAction(FileObject fo) {
      super(Bundle.CTL_CopyFQNAction());
      putValue(SHORT_DESCRIPTION, "<html>" + getValue(NAME) + " <i>(Ctrl+C Ctrl+Q)</i>");
      putValue("noIconInMenu", Boolean.TRUE); // NOI18N
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("C-C C-Q"));
      this.fo = fo;
   }

   @Override
   public boolean isEnabled() {
      return getContext(fo) != null;
   }

   @Override
   public void actionPerformed(ActionEvent ev) {
//      Pair<FileObject, JavaSource> ctx = getContext(fo);
//      assert ctx != null;
//      
//      FileResolver fr = new FileResolver(ctx.first, ctx.second);
//      try {
//         Pair<URI, ElementHandle<TypeElement>> pair = fr.call();
//         String qualifiedName = pair.second.getQualifiedName();
//         Util.setClipboardContents(qualifiedName);
//
//      } catch (Exception ex) {
//         Exceptions.printStackTrace(ex);
//      }
      Pair<FileObject, JavaSource> context = getContext(fo);
      if (context == null) {
         NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.MSG_CopyFQNFailed(fo.getName()), NotifyDescriptor.INFORMATION_MESSAGE);
         DialogDisplayer.getDefault().notify(nd);
         return;
      }
      try {
         context.second.runUserActionTask((CompilationController cc) -> {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            String qualifiedName = cc.getTopLevelElements().toString().split("[\\[\\],]")[1];
            Util.setClipboardContents(qualifiedName);
         }, true);
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   static Pair<FileObject, JavaSource> getContext(FileObject fo) {
      if (fo == null) {
         fo = Utilities.actionsGlobalContext().lookup(FileObject.class);
      }
      if (fo == null) {
         DataObject dobj = Utilities.actionsGlobalContext().lookup(DataObject.class);
         if (dobj != null) {
            fo = dobj.getPrimaryFile();
         }
      }
      JavaSource js = fo != null ? JavaSource.forFileObject(fo) : null;
      if (js == null) {
         return null;
      }
      return Pair.of(fo, js);
   }
//
//   private static final class FileResolver implements Callable<Pair<URI, ElementHandle<TypeElement>>> {
//
//      private final JavaSource js;
//      private final FileObject fo;
//
//      public FileResolver(@NonNull final FileObject fo,
//                          @NonNull final JavaSource js) {
//         Parameters.notNull("fo", fo);   //NOI18N
//         Parameters.notNull("js", js);   //NOI18N
//         this.fo = fo;
//         this.js = js;
//      }
//
//      @Override
//      public Pair<URI, ElementHandle<TypeElement>> call() throws Exception {
//         final List<ElementHandle<TypeElement>> ret = new ArrayList<>(1);
//         ret.add(null);
//         js.runUserActionTask((CompilationController cc) -> {
//            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//            List<? extends TypeElement> topLevelElements = cc.getTopLevelElements();
//            ret.set(0, findMainElement(cc, fo.getName()));
//         }, true);
//
//         final ElementHandle<TypeElement> handle = ret.get(0);
//         if (handle == null) {
//            return null;
//         }
//         return Pair.<URI, ElementHandle<TypeElement>>of(fo.toURI(), handle);
//      }
//
//      @CheckForNull
//      static ElementHandle<TypeElement> findMainElement(@NonNull final CompilationController cc,
//                                                        @NonNull final String fileName) {
//         final List<? extends TypeElement> topLevels = cc.getTopLevelElements();
//         if (topLevels.isEmpty()) {
//            return null;
//         }
//         TypeElement candidate = topLevels.get(0);
//         for (int i = 1; i < topLevels.size(); i++) {
//            if (fileName.contentEquals(topLevels.get(i).getSimpleName())) {
//               candidate = topLevels.get(i);
//               break;
//            }
//         }
//         return ElementHandle.create(candidate);
//      }
//   }
}
