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
 * $Id: CoolActionsFactory.java 51 2014-04-01 20:37:52Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import ch.anchialas.lang.Is;
import ch.anchialas.nb.editor.storage.CEAData;
import ch.anchialas.nb.editor.storage.CEAction;
import java.awt.Component;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import org.gpl.components.Events.JButtonMenuItemListener;
import org.gpl.components.JButtonMenuItem.ActionButton;
import org.gpl.components.JButtonMenuItem.ButtonStyle;
import org.gpl.components.JButtonMenuItem.JButtonMenuItem;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectManager.Result;
import org.netbeans.editor.BaseAction;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;

/**
 * Cool editor actions factory class.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 51 $
 *
 * @see org.netbeans.editor.ActionFactory.ToggleHighlightSearchAction
 * @see org.netbeans.editor.BaseKit#createActions()
 * @see org.netbeans.modules.editor.impl.actions.NavigationHistoryBackAction
 * @see org.netbeans.modules.editor.NbEditorKit#createActions()
 */
public final class CoolActionsFactory {

   public static final String FILE_CONTEXT_ACTION_NAME = "anchialas-file-context-action"; // NOI18N
   public static final String KEY_EXPERIMENTAL_SHOW_MENUITEMBUTTONS = "experimental-showMenuItemButtons";

//   an action registered with this annotation does not work with a Presenter 
//   -> registered manually in the layer.xml
//   @EditorActionRegistration(name = fileContextAction,
//   iconResource = "org/netbeans/core/windows/resources/info.png") // NOI18N
//   http://hg.netbeans.org/main/rev/9e29c40bc86a
   @NbBundle.Messages(FILE_CONTEXT_ACTION_NAME + "=File Context Actions")
   public static final class FileContextAction extends BaseAction implements ContextAwareAction,
                                                                             Presenter.Toolbar,
                                                                             Presenter.Menu,
                                                                             Presenter.Popup {

      private static final long serialVersionUID = -6848415848650171063L;
      //
      private Lookup lookup;

      public FileContextAction() {
         this(null);
      }

      private FileContextAction(Lookup lookup) {
         super(FILE_CONTEXT_ACTION_NAME);
         putValue(ACCELERATOR_KEY, Utilities.stringToKey("DOS-F12"));
         putValue(SHORT_DESCRIPTION, Bundle.anchialas_file_context_action()
                 + " (" + ActionUtil.getAcceleratorText(Utilities.stringToKey("DOS-F12")) + ")");
         putValue(LONG_DESCRIPTION, "<html>" + Bundle.anchialas_file_context_action() + " (CoolEditorActions)"
                 + " <b>" + ActionUtil.getAcceleratorText(Utilities.stringToKey("DOS-F12")) + "</b>");
         this.lookup = lookup;
      }

      @Override
      public void putValue(String key, Object value) {
         if ("Lookup".equals(key)) {
            lookup = (Lookup)value;
         } else {
            super.putValue(key, value);
         }
      }

      private DataObject getDataObject() {
         return lookup == null ? null : lookup.lookup(DataObject.class);
      }

      private Icon getDataObjectIcon() {
         DataObject dataObject = getDataObject();
         return dataObject == null ? null : new ImageIcon(dataObject.getNodeDelegate().getIcon(1));
      }

      @Override
      public Component getToolbarPresenter() {
         final JPopupMenu popup = new JPopupMenu();
         popup.add(new JLabel(" Please wait..."));
         popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
               popup.removeAll();
               setPopupMenuItems(getDataObject(), popup);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent pme) {
            }
         });
         // a default DropDownButton cannot display text -> use a special text icon
         JButton toolbarButton = DropDownButtonFactory.createDropDownButton((Icon)getValue(SMALL_ICON), popup);

         //toolbarButton.setAction(this);
         toolbarButton.addActionListener(this);

         //toolbarButton.setToolTipText("Cool Editor Actions");
         toolbarButton.setToolTipText(getValue(SHORT_DESCRIPTION).toString());
         toolbarButton.putClientProperty("hideActionText", Boolean.FALSE); //NOI18N

         Icon icon = getDataObjectIcon();
         if (icon != null) {
            //putValue(SMALL_ICON, icon);
            toolbarButton.setIcon(icon);
         }
         return toolbarButton;
      }

      @Override
      public JMenuItem getPopupPresenter() {
         return getMenuPresenter();
      }

      @Override
      public JMenuItem getMenuPresenter() {
         JPopupMenu popup = new JPopupMenu();
         setPopupMenuItems(getDataObject(), popup);

         JMenu menu = new JMenu(this);
         menu.setIcon(getDataObjectIcon());
         menu.setText(Bundle.anchialas_file_context_action());
         for (Component c : popup.getComponents()) {
            menu.add(c);
         }
         return menu;
      }

      void setPopupMenuItems(DataObject mainDataObj, JPopupMenu popup) {
         //Utilities.actionsToPopup(actions, lookup)
//         boolean defaultOpenInSystem = CEAData.getInstance().isDefaultOpenInSystem();

         JMenuItem menuItem;
         Project proj = null;

         for (DataObject dataObj = mainDataObj; dataObj != null; dataObj = dataObj.getFolder()) {
            FileObject fo = dataObj.getPrimaryFile();
            if (fo.isRoot()) {
               try {
                  if (fo.getFileSystem() instanceof JarFileSystem) {
                     JarFileSystem jarFS = (JarFileSystem)fo.getFileSystem();
                     fo = FileUtil.toFileObject(jarFS.getJarFile());
                     dataObj = DataObject.find(fo);
                     popup.addSeparator();
                  } else {
                     fo = null;
                  }
               } catch (IOException ex) {
                  //Exceptions.printStackTrace(ex);
                  fo = null;
               }

            }

            if (fo != null) {
               Icon icon = null;
               JMenu sub = null;
               try {
                  if (fo.getFileSystem().isReadOnly()) {
                     sub = new JMenu("<html><i>" + fo.getNameExt());
                  }
               } catch (FileStateInvalidException ex) {
               }
               if (sub == null) {
                  sub = new JMenu(fo.getNameExt());
               }


//               if (defaultOpenInSystem) {
//                  // Get SystemOpenAction and OpenAsAction from [core.ui]
//                  ContextAwareAction systemOpenAction = (ContextAwareAction)ActionUtil.lookupActionInLayer("Actions/Edit/org-netbeans-core-ui-sysopen-SystemOpenAction.instance");
//                  //ContextAwareAction openAsAction = (ContextAwareAction) ActionUtil.lookupActionInLayer("Actions/Edit/org-netbeans-core-ui-options-filetypes-OpenAsAction.instance");
//                  menuItem = sub.add(systemOpenAction.createContextAwareInstance(Lookups.singleton(dataObj)));
//                  //sub.add(openAsAction.createContextAwareInstance(Lookups.singleton(dataObj.getNodeDelegate())));
//               } else {
//                  menuItem = sub.add(new ExternalCommandAction(fo, CEAData.getInstance().getDefaultOpenInSystemAction()));
//               }
//               menuItem.setAccelerator(KeyStroke.getKeyStroke('1'));
//
//               menuItem = sub.add(new ExternalCommandAction(fo, CEAData.getInstance().getDefaultExploreInSystemAction()));
//               menuItem.setAccelerator(KeyStroke.getKeyStroke('2'));
//
//               for (CEAction cea : CEAData.getInstance().getCustomActions()) {
//                  sub.add(new ExternalCommandAction(fo, cea));
//               }

               int i = 0;
               for (CEAction cea : CEAData.getInstance().getActions()) {
                  if (CEAData.ACT_OPENINSYSTEM.equals(cea.getName())) {
                     if (CEAData.getInstance().isDefaultOpenInSystem()) {
                        // Get SystemOpenAction and OpenAsAction from [core.ui]
                        ContextAwareAction systemOpenAction = (ContextAwareAction)ActionUtil.lookupActionInLayer("Actions/Edit/org-netbeans-core-ui-sysopen-SystemOpenAction.instance");
                        //ContextAwareAction openAsAction = (ContextAwareAction) ActionUtil.lookupActionInLayer("Actions/Edit/org-netbeans-core-ui-options-filetypes-OpenAsAction.instance");
                        menuItem = sub.add(systemOpenAction.createContextAwareInstance(Lookups.singleton(dataObj)));
                     } else {
                        menuItem = sub.add(new ExternalCommandAction(fo, cea));
                     }
                  } else {
                     menuItem = sub.add(new ExternalCommandAction(fo, cea));
                  }
                  ++i;
                  if (i < 10) {
                     menuItem.setAccelerator(KeyStroke.getKeyStroke(Integer.toString(i)));
                  }
               }

               boolean isProject = false;
               Action selectInProjectsAction = null;
               Action selectInFilesAction = null;
               try {
                  if (proj == null && fo.isFolder()) {
                     Result projResult = ProjectManager.getDefault().isProject2(fo);
                     if (projResult != null) {
                        isProject = true;
                        icon = projResult.getIcon();
                        sub.setIcon(icon);
                        try {
                           proj = ProjectManager.getDefault().findProject(fo);

                           selectInProjectsAction = new OpenProjectAction(proj, null, true, true);
                           selectInProjectsAction.putValue(Action.NAME, selectInProjectsAction.getValue(Action.NAME) + " in Projects");
                           selectInFilesAction = new OpenProjectAction(proj, null, false, true);
                           selectInFilesAction.putValue(Action.NAME, selectInFilesAction.getValue(Action.NAME) + " in Files");

                        } catch (Exception ex) {
                           Exceptions.printStackTrace(ex);
                        }
                     }
                  }
                  if (selectInProjectsAction == null) {
                     selectInProjectsAction = ActionUtil.createSelectNodeAction(fo, true);
                     selectInFilesAction = ActionUtil.createSelectNodeAction(fo, false);
                  }

                  sub.addSeparator();

                  menuItem = sub.add(new CopyPathAction(fo));
                  menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

                  if (CopyFQNAction.getContext(fo) != null) {
                     sub.add(new CopyFQNAction(fo));
                  }

                  sub.addSeparator();

                  menuItem = sub.add(((Presenter.Popup)selectInProjectsAction).getPopupPresenter());
                  menuItem.setAccelerator(KeyStroke.getKeyStroke('P'));

                  menuItem = sub.add(((Presenter.Popup)selectInFilesAction).getPopupPresenter());
                  menuItem.setAccelerator(KeyStroke.getKeyStroke('F'));

                  if (isProject) {
                     CloseProjectAction closeProjectAction = new CloseProjectAction(proj);

                     if (NbPreferences.forModule(CoolActionsFactory.class).getBoolean(KEY_EXPERIMENTAL_SHOW_MENUITEMBUTTONS, false)) {
                        //JButtonMenuItem mi1 = new JButtonMenuItem("Project", icon);
                        JButtonMenuItem mi1 = new JButtonMenuItem();
                        mi1.setButtonStyle(new ButtonStyle(mi1, ButtonStyle.ROUNDED_CORNERS, ButtonStyle.ImageOrText.DISPLAY_ICON));
                        mi1.setButtons(new ActionButton(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-BuildProject.instance"),
                                                        ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/buildProject.png", false)),
                                       new ActionButton(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-RebuildProject.instance"),
                                                        ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/rebuildProject.png", false)),
                                       new ActionButton(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-RunProject.instance"),
                                                        ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/runProject.png", false)),
                                       new ActionButton(ActionUtil.lookupActionInLayer("Actions/Debug/org-netbeans-modules-debugger-ui-actions-DebugProjectAction.instance"),
                                                        ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugProject.png", false)),
                                       new ActionButton(closeProjectAction));

                        mi1.addJButtonMenuItemListener(new JButtonMenuItemListener() {
                           @Override
                           public void buttonClicked(ActionEvent e) {
                              System.out.println(e.getActionCommand());
                              if (Is.empty(e.getActionCommand())) {
                                 //selectInProjectsAction.actionPerformed(e);
                              }
                           }
                        });
                        popup.add(mi1);
                     }

                     sub.addSeparator();

                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-BuildProject.instance"));
                     menuItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/buildProject.png", false));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('5'));

                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-CleanProject.instance"));
                     menuItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/cleanProject.gif", false));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('6'));

                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-RebuildProject.instance"));
                     menuItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/rebuildProject.png", false));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('7'));

                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-RunProject.instance"));
                     menuItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/runProject.png", false));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('8'));

                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Debug/org-netbeans-modules-debugger-ui-actions-DebugProjectAction.instance"));
                     menuItem.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugProject.png", false));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('9'));

                     sub.addSeparator();

                     sub.add(new CloseProjectAction(proj));
                     // see [projectui] ProjectAction.refresh()
                     menuItem = sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-CustomizeProject.instance", proj));
                     menuItem.setText(Actions.cutAmpersand(menuItem.getText()));
                     menuItem.setAccelerator(KeyStroke.getKeyStroke('0'));
                     //sub.add(ActionUtil.lookupActionInLayer("Actions/Project/org-netbeans-modules-project-ui-SetMainProject.instance", proj));
                  }

               } catch (Exception ex) {
                  Exceptions.printStackTrace(ex);
               }
               if (icon == null) {
                  icon = new ImageIcon(dataObj.getNodeDelegate().getIcon(1));
               }

               sub.setIcon(icon);

               popup.add(sub);
               if (mainDataObj == dataObj) {
                  popup.addSeparator();
               }
            }
         }
         popup.addSeparator();
         menuItem = popup.add(OpenOptionsPanelAction.SINGLETON);
         menuItem.setAccelerator(KeyStroke.getKeyStroke("F12"));
      }

      @Override
      public Action createContextAwareInstance(Lookup actionContext) {
         return new FileContextAction(actionContext);
      }

      @Override
      protected Class getShortDescriptionBundleClass() {
         return FileContextAction.class;
      }

      @Override
      public void actionPerformed(ActionEvent evt, JTextComponent target) {
         if (evt.getModifiers() == InputEvent.BUTTON1_MASK) {
            // toolbar button has been pressed
            Action selectInProjectsAction = ActionUtil.lookupActionInLayer("Actions/Window/SelectDocumentNode/org-netbeans-modules-project-ui-SelectInProjects.instance");
            selectInProjectsAction.actionPerformed(evt);
         } else {
            DataObject dataObject = getDataObject();
            if (dataObject != null) {
               JPopupMenu popup = new JPopupMenu();
               setPopupMenuItems(dataObject, popup);
               // Show the FileContextAction popup at the current mouse position.
               Point pos = MouseInfo.getPointerInfo().getLocation();
               Frame mw = WindowManager.getDefault().getMainWindow();
               popup.show(mw, pos.x - mw.getX(), pos.y - mw.getY());
            }
         }
      }
   }

   static File getFile(FileObject fo) throws FileStateInvalidException {
      File f = FileUtil.toFile(fo);
      if (f == null && fo.getFileSystem() instanceof JarFileSystem) {
         JarFileSystem jarFS = (JarFileSystem)fo.getFileSystem();
         f = jarFS.getJarFile();
         String path = fo.getPath();
         f = new File(f, path);
      }
      return f;
   }

   static File getParentFile(FileObject fo) throws FileStateInvalidException {
      File f = getFile(fo);
      return f == null ? null : f.getParentFile();
   }

   static String getParentFilePath(FileObject fo) throws FileStateInvalidException {
      File f = getParentFile(fo);
      return f == null ? "" : f.getAbsolutePath();


   }

   private static class OpenOptionsPanelAction extends AbstractAction {

      static final Action SINGLETON = new OpenOptionsPanelAction();
      private static final long serialVersionUID = 5387150022832992144L;

      public OpenOptionsPanelAction() {
         super("Options...", ImageUtilities.loadImageIcon("ch/anchialas/nb/editor/res/options.png", false));
      }

      @Override
      public void actionPerformed(ActionEvent ae) {
         OptionsDisplayer.getDefault().open("Editor/CoolEditorActions");
      }
   }
}
