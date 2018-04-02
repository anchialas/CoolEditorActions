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
 * $Id: CEAOptionsPanel.java 43 2013-02-05 22:51:06Z Anchialas $
 */
package ch.anchialas.nb.editor.options;

import ch.anchialas.nb.editor.storage.CEAData;
import ch.anchialas.nb.editor.storage.CEAction;
import ch.anchialas.nb.editor.storage.Defaults;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.activation.MimeType;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * CoolEditorActions configuration panel for the Options Dialog.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 43 $
 */
class CEAOptionsPanel extends JPanel {

   private boolean changed;
   private Map<CEAction, CommandsTableModel> modelMap;

   public CEAOptionsPanel() {
      initComponents();
      init();
   }

   private void init() {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
         notSupportedLabel.setVisible(false);
      } else {
         rbOISDefault.setEnabled(false);
         notSupportedLabel.setVisible(true);
      }
      commandCommaHintLabel.setVisible(Defaults.getOS() != Defaults.OS.win);

      commandTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      commandTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
         @Override
         public void valueChanged(ListSelectionEvent evt) {
            commandTableValueChanged(evt);
         }
      });
      commandTable.getTableHeader().setReorderingAllowed(false);

      commandTextField.getDocument().addDocumentListener(new DocumentListener() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
         }

         @Override
         public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
         }

         @Override
         public void changedUpdate(DocumentEvent e) {
            int row = commandTable.getSelectedRow();
            if (row >= 0) {
               getModel().setCommand(commandTable.getSelectedRow(), commandTextField.getText());
               changed = true;
            }
         }
      });
   }

   void setToolbarButtonPosition(int position) {
      FileObject fo = FileUtil.getConfigFile("Editors/Toolbars/Default/anchialas-file-context-action");
//      FileObject fo = FileUtil.getConfigFile("Editors/Toolbars/Default/separator-before-anchialas-file-context-action.instance");
      Object pos = fo.getAttribute("position");

      try {
         fo.setAttribute("position", String.valueOf(pos));
      } catch (IOException ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   void setActions(List<CEAction> actionList) {
      if (modelMap == null) {
         modelMap = new HashMap<CEAction, CommandsTableModel>();
      }
      Collections.sort(actionList);
      for (CEAction action : actionList) {
         if (!modelMap.containsKey(action)) {
            modelMap.put(action, null);
         }
      }
      actionChooseComboBox.setModel(new DefaultComboBoxModel(actionList.toArray()));
      actionChooseComboBox.setSelectedItem(null);
      actionChooseComboBox.setSelectedIndex(0);

      commandTableValueChanged(null);
   }

   void setSelectedAction(CEAction action) {
      actionChooseComboBox.setSelectedItem(action);
   }

   CEAction getSelectedAction() {
      return (CEAction)actionChooseComboBox.getSelectedItem();
   }

   void setDefaultOpenInSystem(boolean b) {
      if (b) {
         rbOISDefault.doClick();
      } else {
         rbOISCustom.doClick();
      }
   }

   boolean isDefaultOpenInSystem() {
      return rbOISDefault.isSelected();
   }

   Map<CEAction, List<RowData>> getDataMap() {
      Map<CEAction, List<RowData>> map = new HashMap<CEAction, List<RowData>>();
      for (Map.Entry<CEAction, CommandsTableModel> entry : modelMap.entrySet()) {
         if (entry.getValue() != null) {
            map.put(entry.getKey(), entry.getValue().getData());
         }
      }
      return map;
   }

   void clear() {
      getModel().clear();
      changed = false;
   }

   boolean isDataValid() {
      return true;
   }

   boolean isChanged() {
      return changed;
   }

   private CommandsTableModel getModel() {
      return (CommandsTableModel)commandTable.getModel();
   }

   private void commandTableValueChanged(ListSelectionEvent evt) {
      int index = commandTable.getSelectedRow();

      if (index < 0 || index >= commandTable.getRowCount()) {
         commandRemoveButton.setEnabled(false);
         commandTextField.setEditable(false);
         commandTextField.setText(null);

      } else {
         commandTextField.setText(getModel().getCommand(index)); //NOI18N
         commandTextField.getCaret().setDot(0);

         String fileType = getModel().getMimeType(commandTable.getSelectedRow());
         boolean active = rbOISCustom.isSelected()
                 || (getSelectedAction() != null && !CEAData.ACT_OPENINSYSTEM.equals(getSelectedAction().getStorageKey()));

         commandTextField.setEditable(active);
         commandRemoveButton.setEnabled(active
                 && !CEAData.TYPE_FILE.equals(fileType) && !CEAData.TYPE_FOLDER.equals(fileType));
      }
   }

   /**
    * This method is called from within the constructor to initialize the form. WARNING: Do NOT
    * modify this code. The content of this method is always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      buttonGroup1 = new ButtonGroup();
      commandScrollPane = new JScrollPane();
      commandTable = new JTable();
      jLabel2 = new JLabel();
      commandLabel = new JLabel();
      commandTextField = new JTextField();
      commandAddButton = new JButton();
      commandRemoveButton = new JButton();
      commandInfoLabel = new JLabel();
      OISButtonPanel = new JPanel();
      rbOISCustom = new JRadioButton();
      notSupportedLabel = new JLabel();
      rbOISDefault = new JRadioButton();
      actionChooseComboBox = new JComboBox();
      editActionsButton = new JButton();
      commandInfoLabel1 = new JLabel();
      commandCommaHintLabel = new JLabel();
      cbShowMenuItemButtons = new JCheckBox();

      commandTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      commandScrollPane.setViewportView(commandTable);

      jLabel2.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.jLabel2.text")); // NOI18N

      commandLabel.setLabelFor(commandTextField);
      commandLabel.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandLabel.text")); // NOI18N

      commandTextField.setFont(new Font("Lucida Console", 0, 13)); // NOI18N
      commandTextField.addKeyListener(new KeyAdapter() {
         public void keyTyped(KeyEvent evt) {
            commandTextFieldKeyTyped(evt);
         }
      });

      commandAddButton.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandAddButton.text")); // NOI18N
      commandAddButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            commandAddButtonActionPerformed(evt);
         }
      });

      commandRemoveButton.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandRemoveButton.text")); // NOI18N
      commandRemoveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            commandRemoveButtonActionPerformed(evt);
         }
      });

      commandInfoLabel.setForeground(UIManager.getDefaults().getColor("ComboBox.disabledForeground"));
      commandInfoLabel.setLabelFor(commandTextField);
      commandInfoLabel.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandInfoLabel.text")); // NOI18N

      buttonGroup1.add(rbOISCustom);
      rbOISCustom.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.rbOISCustom.text")); // NOI18N
      rbOISCustom.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            radioButtonActionPerformed(evt);
         }
      });

      notSupportedLabel.setForeground(UIManager.getDefaults().getColor("nb.errorForeground"));
      notSupportedLabel.setIcon(new ImageIcon(getClass().getResource("/ch/anchialas/nb/editor/res/warning.png"))); // NOI18N
      notSupportedLabel.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.notSupportedLabel.text")); // NOI18N
      notSupportedLabel.setToolTipText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.notSupportedLabel.toolTipText")); // NOI18N
      notSupportedLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

      buttonGroup1.add(rbOISDefault);
      rbOISDefault.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.rbOISDefault.text")); // NOI18N
      rbOISDefault.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            radioButtonActionPerformed(evt);
         }
      });

      GroupLayout OISButtonPanelLayout = new GroupLayout(OISButtonPanel);
      OISButtonPanel.setLayout(OISButtonPanelLayout);
      OISButtonPanelLayout.setHorizontalGroup(
         OISButtonPanelLayout.createParallelGroup(Alignment.LEADING)
         .addGroup(OISButtonPanelLayout.createSequentialGroup()
            .addGroup(OISButtonPanelLayout.createParallelGroup(Alignment.LEADING)
               .addGroup(OISButtonPanelLayout.createSequentialGroup()
                  .addComponent(rbOISDefault)
                  .addGap(24, 24, 24)
                  .addComponent(notSupportedLabel))
               .addComponent(rbOISCustom))
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      OISButtonPanelLayout.setVerticalGroup(
         OISButtonPanelLayout.createParallelGroup(Alignment.LEADING)
         .addGroup(OISButtonPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(OISButtonPanelLayout.createParallelGroup(Alignment.BASELINE)
               .addComponent(rbOISDefault)
               .addComponent(notSupportedLabel))
            .addGap(4, 4, 4)
            .addComponent(rbOISCustom)
            .addContainerGap())
      );

      actionChooseComboBox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent evt) {
            actionChooseComboBoxItemStateChanged(evt);
         }
      });

      editActionsButton.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.editActionsButton.text")); // NOI18N
      editActionsButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            editActionsButtonActionPerformed(evt);
         }
      });

      commandInfoLabel1.setForeground(UIManager.getDefaults().getColor("ComboBox.disabledForeground"));
      commandInfoLabel1.setLabelFor(commandTextField);
      commandInfoLabel1.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandInfoLabel1.text")); // NOI18N

      commandCommaHintLabel.setForeground(UIManager.getDefaults().getColor("ComboBox.disabledForeground"));
      commandCommaHintLabel.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.commandCommaHintLabel.text")); // NOI18N

      cbShowMenuItemButtons.setText(NbBundle.getMessage(CEAOptionsPanel.class, "CEAOptionsPanel.cbShowMenuItemButtons.text")); // NOI18N

      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(Alignment.LEADING)
         .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(Alignment.TRAILING)
               .addGroup(layout.createSequentialGroup()
                  .addContainerGap()
                  .addComponent(jLabel2)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(actionChooseComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(editActionsButton, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGap(25, 25, 25)
                  .addGroup(layout.createParallelGroup(Alignment.LEADING)
                     .addComponent(commandScrollPane)
                     .addComponent(commandTextField)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(commandInfoLabel)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(commandInfoLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(commandLabel)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(commandCommaHintLabel)))))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
               .addComponent(commandRemoveButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(commandAddButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(15, 15, 15))
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
               .addComponent(OISButtonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(cbShowMenuItemButtons)
                  .addGap(0, 0, Short.MAX_VALUE))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(cbShowMenuItemButtons)
            .addGap(12, 12, 12)
            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
               .addComponent(jLabel2)
               .addComponent(actionChooseComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
               .addComponent(editActionsButton))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(OISButtonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(commandAddButton)
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(commandRemoveButton))
               .addComponent(commandScrollPane, GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
               .addComponent(commandLabel)
               .addComponent(commandCommaHintLabel))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(commandTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
               .addComponent(commandInfoLabel)
               .addComponent(commandInfoLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(25, 25, 25))
      );
   }// </editor-fold>//GEN-END:initComponents

   private void radioButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_radioButtonActionPerformed

      boolean active = rbOISCustom.isSelected()
              || (getSelectedAction() != null && !CEAData.ACT_OPENINSYSTEM.equals(getSelectedAction().getStorageKey()));
      commandTextField.setEditable(active);

      changed = true;
   }//GEN-LAST:event_radioButtonActionPerformed

   private void commandTextFieldKeyTyped(KeyEvent evt) {//GEN-FIRST:event_commandTextFieldKeyTyped
   }//GEN-LAST:event_commandTextFieldKeyTyped

   private void commandAddButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandAddButtonActionPerformed
      askAddFileType(null);
   }//GEN-LAST:event_commandAddButtonActionPerformed

   private void commandRemoveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_commandRemoveButtonActionPerformed
      String command = getModel().getCommand(commandTable.getSelectedRow());
      if (command != null && !command.isEmpty()) {
         String fileType = getModel().getMimeType(commandTable.getSelectedRow());
         NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                 "Delete the Command for File Type '" + fileType + "'?",
                 NotifyDescriptor.OK_CANCEL_OPTION);
         if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return;
         }
      }
      int row = commandTable.getSelectedRow();
      getModel().removeRow(row);
      commandTable.requestFocus();
      row = Math.min(row, commandTable.getRowCount() - 1);
      commandTable.setRowSelectionInterval(row, row);
   }//GEN-LAST:event_commandRemoveButtonActionPerformed

   private void actionChooseComboBoxItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_actionChooseComboBoxItemStateChanged
      if (evt.getStateChange() == ItemEvent.SELECTED) {
         CEAction action = (CEAction)evt.getItem();
         CommandsTableModel commandsTableModel = modelMap.get(action);
         if (commandsTableModel == null) {
            // model not yet loaded for the action
            commandsTableModel = new CommandsTableModel();
            modelMap.put(action, commandsTableModel);
         }
         if (commandsTableModel.isEmpty()) {
            commandsTableModel.setData(CEAData.getInstance().getData(action));
         }
         commandTable.setModel(commandsTableModel);

         OISButtonPanel.setVisible(CEAData.ACT_OPENINSYSTEM.equals(action.getStorageKey()));
      }
   }//GEN-LAST:event_actionChooseComboBoxItemStateChanged

   private void editActionsButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editActionsButtonActionPerformed
      EditActionsPanel eap = new EditActionsPanel();
      eap.setActions(new ArrayList<CEAction>(modelMap.keySet()));

      DialogDescriptor d = new DialogDescriptor(eap, "Actions");
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         // save actions
         CEAction selectedAction = eap.getSelectedAction();
         List<CEAction> actionList = eap.getActionList();
         for (Iterator<CEAction> it = modelMap.keySet().iterator(); it.hasNext();) {
            CEAction act = it.next();
            if (!actionList.contains(act)) {
               it.remove();
            }
         }
         setActions(actionList);
         if (selectedAction != null) {
            setSelectedAction(selectedAction);
         }
         changed = true;
      }
   }//GEN-LAST:event_editActionsButtonActionPerformed

   private void askAddFileType(String fileTypePreset) {
      NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("Mime Type: ", "Add File Type (Mime Type)");
      d.setInputText(fileTypePreset);
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
         String fileType = d.getInputText();
         if (fileType == null || fileType.isEmpty()) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("Mime Type cannot be empty!", NotifyDescriptor.ERROR_MESSAGE));
            return;
         }
         try {
            new MimeType(fileType);
         } catch (Exception e) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message("Mime Type is invalid...\nPlease try again!", NotifyDescriptor.ERROR_MESSAGE));
            askAddFileType(fileType);
            return;
         }
         getModel().insertRow(2, fileType, null);
         commandTable.setRowSelectionInterval(2, 2);
         commandTextField.requestFocus();
         changed = true;
      }

   }
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private JPanel OISButtonPanel;
   private JComboBox actionChooseComboBox;
   private ButtonGroup buttonGroup1;
   JCheckBox cbShowMenuItemButtons;
   private JButton commandAddButton;
   private JLabel commandCommaHintLabel;
   private JLabel commandInfoLabel;
   private JLabel commandInfoLabel1;
   private JLabel commandLabel;
   private JButton commandRemoveButton;
   private JScrollPane commandScrollPane;
   private JTable commandTable;
   private JTextField commandTextField;
   private JButton editActionsButton;
   private JLabel jLabel2;
   private JLabel notSupportedLabel;
   private JRadioButton rbOISCustom;
   private JRadioButton rbOISDefault;
   // End of variables declaration//GEN-END:variables
}
