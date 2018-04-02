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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;


/**
 * Copies the absolute path of selected {@link DataObject}s to the clipboard.
 * <p>
 * Items within JAR files are supported.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 33 $
 * @since V1.4.0
 */
@NbBundle.Messages({
   "CTL_CopyPath=Copy File Path",
   "# {0} - copied file path",
   "CTL_Status_CopyToClipboard=Copied to Clipboard: {0}"
})
final class CopyPathAction extends AbstractAction implements ClipboardOwner {

   //
   private final FileObject fo;

   public CopyPathAction(FileObject fo) {
      super(Bundle.CTL_CopyPath());
      //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
      this.fo = fo;
   }

   @Override
   public void actionPerformed(ActionEvent ae) {
      Util.setClipboardContents(getAbsolutePath(fo));
   }

   @Override
   public void lostOwnership(Clipboard clipboard, Transferable contents) {
      // do nothing
   }

   public static String getAbsolutePath(FileObject fo) {
      String fileName = fo.getPath();
      //support selected items in jars
      if (null != FileUtil.getArchiveFile(fo)) {
         String fullJARPath = FileUtil.getArchiveFile(fo).getPath();
         String archiveFileName = fo.getPath();
         boolean hasFileName = null != archiveFileName
                 && !"".equals(archiveFileName);                 //NOI18N
         if (hasFileName) {
            fileName = fullJARPath + File.pathSeparator + archiveFileName;
         } else {
            fileName = fullJARPath;
         }
      }
      return fileName;
   }

   
}
