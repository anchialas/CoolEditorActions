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

import ch.anchialas.lang.Is;
import ch.anchialas.nb.editor.storage.CEAData;
import ch.anchialas.nb.editor.storage.CEAction;
import ch.anchialas.nb.editor.storage.Defaults;
import ch.anchialas.nb.editor.storage.Defaults.OS;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
/**
 * Action to execute an external (system) command.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 33 $
 */
@NbBundle.Messages({ "MSG_NoCommand=No Command defined for this file (MIME Type: {0})", "MSG_InvalidFile=Invalid file: {0}", "MSG_CommandSuccess=Command ''{0}'' executed successfully: {1}",
    "MSG_CommandFailed=Command ''{0}'' failed: {1} [Reason: {2}]", "MSG_CommandFailed_html=<html>Command failed:<br><code>{0}</code><br>Reason: {1}</html>" })
final class ExternalCommandAction extends AbstractAction {
    private static final long serialVersionUID = 5190769094552659871L;
    //
    private final FileObject fo;
    private final CEAction cea;

    public ExternalCommandAction(FileObject fo, CEAction cea) {
        super(cea.getName());
        putValue(SHORT_DESCRIPTION, CEAData.getInstance().getCommand(cea, fo));
        this.fo = fo;
        this.cea = cea;
    }

    @Override
    public boolean isEnabled() {
        return Is.notEmpty(CEAData.getInstance().getCommand(cea, fo));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        final String commandPattern = CEAData.getInstance().getCommand(cea, fo);
        //         if (commandPattern == null) {
        //            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.MSG_NoCommand(fo.getMIMEType()), NotifyDescriptor.ERROR_MESSAGE);
        //            DialogDisplayer.getDefault().notify(nd);
        //            return;
        //         }
        File f = null;
        try {
            f = Util.getFile(fo);
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (f == null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.MSG_InvalidFile(fo), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        final String filePath = f.getAbsolutePath();
        RequestProcessor.getDefault().post(() -> {
            String command = commandPattern;
            try {
                command = MessageFormat.format(commandPattern, filePath, Util.getParentFilePath(fo), fo.getNameExt(), fo.getName());
                File workingDir = FileUtil.toFile(fo);
                if (workingDir != null && workingDir.isFile()) {
                    workingDir = workingDir.getParentFile();
                }
                Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.INFO, "Executing command ''{0}'' in working directory ''{1}''", new Object[] { command, workingDir });
                Process p = Defaults.getOS() == OS.win ? Runtime.getRuntime().exec(command, null, workingDir) : Runtime.getRuntime().exec(ActionUtil.splitCommandArray(command), null, workingDir);
                int exitValue = p.waitFor();
                if (exitValue == 0 || (exitValue == 1 && command.toLowerCase().startsWith("explorer"))) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.INFO, line);
                        }
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                    StatusDisplayer.getDefault().setStatusText(Bundle.MSG_CommandSuccess(cea.getName(), command));
                } else {
                    Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.WARNING, "Error while executing command: {0}", command);
                    String error = "";
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.WARNING, line);
                            error += line;
                        }
                        if (Is.empty(line)) {
                            reader.close();
                            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while ((line = reader.readLine()) != null) {
                                Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.WARNING, line);
                                error += line;
                            }
                        }
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                    Logger.getLogger(ExternalCommandAction.class.getName()).log(Level.WARNING, Bundle.MSG_CommandFailed(cea.getName(), command, error));
                    NotificationDisplayer.getDefault().notify(cea.getName(), ImageUtilities.loadImageIcon("org/netbeans/modules/dialogs/error.gif", false),
                        Bundle.MSG_CommandFailed_html(command, error), null);
                }
            } catch (Exception ex) {
                //Exceptions.printStackTrace(ex);
                NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.MSG_CommandFailed_html(command, ex.getMessage()), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        });
    }
}
