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
 * $Id$
 */
package ch.anchialas.nb.editor.options;

import ch.anchialas.lang.Is;
import ch.anchialas.nb.editor.storage.CEAData;

/**
 * Row data containing MimeType and Command value.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev$
 */
public class RowData implements Comparable<RowData> {

   String type;
   String command;

   public RowData(String type, String command) {
      this.type = type;
      this.command = command;
   }

   public String getType() {
      return type;
   }

   public String getCommand() {
      return command;
   }

   @Override
   public String toString() {
      return "RowData[" + type + ":" + command + "]";
   }

   @Override
   public int compareTo(RowData o) {
      if (Is.equals(type, o.type)) {
         return 0;
      }
      if (CEAData.TYPE_FOLDER.equals(type)) {
         return -1;
      }
      if (CEAData.TYPE_FOLDER.equals(o.type)) {
         return 1;
      }
      if (CEAData.TYPE_FILE.equals(type)) {
         return -1;
      }
      if (CEAData.TYPE_FILE.equals(o.type)) {
         return 1;
      }
      return type.compareTo(o.type);
   }
}
