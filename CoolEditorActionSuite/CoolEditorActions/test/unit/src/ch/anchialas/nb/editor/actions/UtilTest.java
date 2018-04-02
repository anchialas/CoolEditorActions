/*
 * Copyright 2012 by Anchialas.
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
 * $Id: ActionUtil.java 32 2012-06-02 06:10:31Z Anchialas $
 */
package ch.anchialas.nb.editor.actions;

import java.io.IOException;
import java.util.Map;
import javax.swing.Action;
import org.junit.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.rules.TestName;
import static org.junit.Assert.*;

/**
 * ActionUtil unit test.
 *
 * @author Anchialas <anchialas@gmail.com>
 * @version $Rev$
 */
public class UtilTest {

   @Rule
   public TestName name = new TestName();

   @Before
   public void setUp() {
      System.out.println("--- test " + name.getMethodName() + " ---");
   }

   @After
   public void tearDown() {
   }

   @Test
   public void shouldExecuteCommandWithArguments() throws IOException, InterruptedException {
      String command = "osascript,-e,tell application \"Finder\" to reveal POSIX file \"~/Documents\"";
      String[] expResult = new String[]{
         "osascript",
         "-e",
         "tell application \"Finder\" to reveal POSIX file \"~/Documents\""
      };
      String[] result = ActionUtil.getCommandArray(command);
      assertThat(result, is(expResult));

      Process p = Runtime.getRuntime().exec(expResult);
      int exitValue = p.waitFor();

   }
}
