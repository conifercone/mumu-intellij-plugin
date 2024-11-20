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
import baby.mumu.intellij.kotlin.services.CommentDbService;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import org.jetbrains.annotations.NotNull;

/**
 * 注释工具窗口
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.3.0
 */
public class CommentToolWindow implements ToolWindowFactory {

  private JBTable table; // 表格实例
  private CommentTableModel tableModel; // 表格模型
  private MouseAdapter mouseAdapter;
  private JPanel mainPanel;

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setIcon(Objects.requireNonNull(
      IconLoader.findIcon("/icons/icon.svg", this.getClass())));
    createContentIfConnected(project, toolWindow);
    project.getMessageBus().connect()
      .subscribe(CommentToolWindowRefreshNotifier.TOPIC,
        (CommentToolWindowRefreshNotifier) () -> reloadComments(project, null));
  }

  private void createContentIfConnected(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    // 检查数据库连接状态
    boolean connected = project.getService(CommentDbService.class).getConnected();
    List<MuMuComment> comments =
      connected ? project.getService(CommentDbService.class).findAll(null) : new ArrayList<>();
    tableModel = new CommentTableModel(comments);

    // 初始化内容
    // 主面板
    mainPanel = new JPanel(new BorderLayout());
    table = createTable(tableModel, project);
    JBScrollPane scrollPane = new JBScrollPane(table);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    ContentFactory contentFactory = ContentFactory.getInstance();
    Content content = contentFactory.createContent(mainPanel, null, false);
    toolWindow.getContentManager().addContent(content);

    // 添加刷新按钮
    addRefreshButton(project, toolWindow);
    addSearchInput(project);
  }

  private @NotNull JBTable createTable(CommentTableModel tableModel, Project project) {
    JBTable table = new JBTable(tableModel);

    // 表格设置
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // 设置超链接渲染器
    table.getColumnModel().getColumn(0).setCellRenderer(new HyperlinkCellRenderer());
    mouseAdapter = getMouseAdapter(tableModel, project, table);
    // 添加点击事件监听器
    table.addMouseListener(mouseAdapter);
    return table;
  }

  private static @NotNull MouseAdapter getMouseAdapter(CommentTableModel tableModel,
    Project project,
    JBTable table) {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int column = table.columnAtPoint(e.getPoint());
        if (row != -1 && column == 0) {
          String relativePath = (String) tableModel.getValueAt(row, column);
          openFileByRelativePath(project, relativePath);
        }
      }
    };
  }

  private void addRefreshButton(Project project, @NotNull ToolWindow toolWindow) {
    // 创建刷新动作
    AnAction refreshAction = new AnAction("Refresh", "Reload comments", AllIcons.Actions.Refresh) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        reloadComments(project, null);
      }
    };
    toolWindow.setTitleActions(Collections.singletonList(refreshAction));
  }

  private void addSearchInput(Project project) {
    // 创建搜索框
    SearchTextField searchTextField = new SearchTextField();
    // 监听搜索框的输入
    searchTextField.addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        reloadComments(project, searchTextField.getText());
      }
    });
    mainPanel.add(searchTextField, BorderLayout.NORTH);
  }

  private void reloadComments(@NotNull Project project, String comment) {
    if (project.getService(CommentDbService.class).getConnected()) {
      List<MuMuComment> comments = project.getService(CommentDbService.class).findAll(comment);
      tableModel = new CommentTableModel(comments);
      table.setModel(tableModel);
      table.removeMouseListener(mouseAdapter);
      mouseAdapter = getMouseAdapter(tableModel, project, table);
      table.addMouseListener(mouseAdapter);
      table.getColumnModel().getColumn(0).setCellRenderer(new HyperlinkCellRenderer());
    }
  }

  private static void openFileByRelativePath(@NotNull Project project, String relativePath) {
    String basePath = project.getBasePath();
    if (basePath == null) {
      return;
    }
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(basePath);
    if (file == null) {
      return;
    }
    VirtualFile virtualFile = file.findFileByRelativePath(relativePath);
    if (virtualFile != null) {
      FileEditorManager.getInstance(project).openFile(virtualFile, true);
    }
  }
}
