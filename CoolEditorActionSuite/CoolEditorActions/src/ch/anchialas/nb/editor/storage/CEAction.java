/*
 * Copyright 2012 Anchialas.
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
 * $Id: CEAction.java 47 2013-07-17 21:13:41Z Anchialas $
 */
package ch.anchialas.nb.editor.storage;

import ch.anchialas.lang.Is;

/**
 * CoolEditor Action definition.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev: 47 $
 */
public class CEAction implements Comparable<CEAction> {

     private final boolean isCustom;
     private final String storageKey;
     private String name;

     public CEAction(int storageKey, String name, boolean isCustom) {
          this(String.valueOf(storageKey), name, isCustom);
     }

     public CEAction(String storageKey, String name, boolean isCustom) {
          this.storageKey = storageKey;
          this.name = name;
          this.isCustom = isCustom;
     }

     public String getStorageKey() {
          return storageKey;
     }

     public String getName() {
          return name == null ? getStorageKey() : name;
     }

     public void setName(String name) {
          if (!isCustom) {
               throw new IllegalArgumentException("Cannot modify name of a default action!");
          }
          this.name = name;
     }

     public boolean isCustom() {
          return isCustom;
     }

     @Override
     public String toString() {
          //return "CEAction[" + storageKey + "=" + name + "]";
          return Is.empty(name) ? storageKey : name
                  + (isCustom ? " (Custom)" : "");
     }

     @Override
     public boolean equals(Object obj) {
          if (obj == this) {
               return true;
          }
          if (!(obj instanceof CEAction)) {
               return false;
          }
          final CEAction other = (CEAction) obj;
          return Is.equals(this.storageKey, other.storageKey)/*
                   * && Is.equals(this.name, other.name)
                   */;

     }

     @Override
     public int hashCode() {
          int hash = 7;
          hash = 71 * hash + (this.storageKey != null ? this.storageKey.hashCode() : 0);
          //hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
          return hash;
     }

     @Override
     public int compareTo(CEAction other) {
          if (!isCustom) {
               return other.isCustom
                       ? -1
                       : getName().compareToIgnoreCase(other.getName());
          }
          return !other.isCustom
                  ? 1
                  : getName().compareToIgnoreCase(other.getName());
     }
}
