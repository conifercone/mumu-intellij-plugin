/*
 * Copyright (c) 2024-2024, the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package baby.mumu.intellij.toolwindows;

import baby.mumu.intellij.kotlin.dos.MuMuComment;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * 注释表格模型
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.3.0
 */
public class CommentTableModel extends AbstractTableModel {

  private final List<MuMuComment> comments;
  private final String[] columnNames = {"Relative Path", "Comment"};

  public CommentTableModel(List<MuMuComment> comments) {
    this.comments = comments;
  }

  @Override
  public int getRowCount() {
    return comments.size();
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    MuMuComment comment = comments.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> comment.getRelativePath();
      case 1 -> comment.getComment();
      default -> throw new IllegalArgumentException("Invalid column index");
    };
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0, 1 -> String.class;
      default -> Object.class;
    };
  }
}