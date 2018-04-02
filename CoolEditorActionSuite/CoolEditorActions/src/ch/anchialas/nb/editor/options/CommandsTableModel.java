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
 * $Id: CommandsTableModel.java 39 2012-11-10 21:39:18Z Anchialas $
 */
package ch.anchialas.nb.editor.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle.Messages;

/**
 * MimeType/Commands TableModel
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 39 $
 */
@Messages({
   "CTL_MimeType=File Type",
   "CTL_Command=Command"
})
class CommandsTableModel extends AbstractTableModel {

   static final int COL_MIMETYPE = 0;
   static final int COL_COMMAND = 1;
   static final int COLUMN_COUNT = 2;
   //
   private final List<RowData> list;

   public CommandsTableModel() {
      list = new ArrayList<>();
   }

//
//   public void setData(List<String> fileTypeList) {
//      list.clear();
//      for (String fileType : fileTypeList) {
//         list.add(new RowData(fileType, null));
//      }
//      Collections.sort(list);
//      fireTableDataChanged();
//   }
//
   void setData(List<RowData> data) {
      list.clear();
      list.addAll(data);
      Collections.sort(list);
      fireTableDataChanged();
   }

   List<RowData> getData() {
      return list;
   }

   boolean isEmpty() {
      return list.isEmpty();
   }

   void clear() {
      list.clear();
   }

   @Override
   public int getColumnCount() {
      return COLUMN_COUNT;
   }

   @Override
   public int getRowCount() {
      return list.size();
   }

   /**
    * Removes the element at the specified position in this list (optional operation). Shifts any
    * subsequent elements to the left (subtracts one from their indices).
    *
    * @return the element that was removed from the list.
    */
   RowData removeRow(int rowIndex) {
      RowData rowData = list.remove(rowIndex);
      fireTableRowsDeleted(rowIndex, rowIndex);
      return rowData;
   }

   void insertRow(int rowIndex, String mimeType, String command) {
      list.add(rowIndex, new RowData(mimeType, command));
      fireTableRowsInserted(rowIndex, rowIndex);
   }

   public String getMimeType(int rowIndex) {
      return list.get(rowIndex).type;
   }

   public void setMimeType(int rowIndex, String type) {
      list.get(rowIndex).type = type;
      fireTableCellUpdated(rowIndex, COL_MIMETYPE);
   }

   public String getCommand(int rowIndex) {
      return list.get(rowIndex).command;
   }

   public void setCommand(int rowIndex, String command) {
      list.get(rowIndex).command = command;
      fireTableCellUpdated(rowIndex, COL_COMMAND);
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      switch (columnIndex) {
         case COL_MIMETYPE:
            return getMimeType(rowIndex);
         case COL_COMMAND:
            return getCommand(rowIndex);
         default:
            throw new ArrayIndexOutOfBoundsException("Invalid column index: " + columnIndex); //NOI18N
      }
   }

   @Override
   public void setValueAt(Object value, int rowIndex, int columnIndex) {
      switch (columnIndex) {
         case COL_MIMETYPE:
            setMimeType(rowIndex, (String)value);
            break;
         case COL_COMMAND:
            setCommand(rowIndex, (String)value);
            break;
         default:
            throw new ArrayIndexOutOfBoundsException("Invalid column index: " + columnIndex); //NOI18N
      }
   }

   @Override
   public Class<?> getColumnClass(int columnIndex) {
      return String.class;
   }

   @Override
   public String getColumnName(int columnIndex) {
      switch (columnIndex) {
         case COL_MIMETYPE:
            return Bundle.CTL_MimeType();
         case COL_COMMAND:
            return Bundle.CTL_Command();
         default:
            throw new ArrayIndexOutOfBoundsException("Invalid column index: " + columnIndex); //NOI18N
      }
   }
}
