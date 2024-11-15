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
package baby.mumu.intellij.actions;

import baby.mumu.intellij.services.CommentService;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 切换注释可见性操作
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
public class ToggleCommentVisibilityAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }

    CommentService commentService = ApplicationManager.getApplication()
      .getService(CommentService.class);
    // 切换注释显示状态
    boolean newState = !commentService.isCommentsVisible();
    commentService.setCommentsVisible(newState);

    // 刷新 Project View 以更新显示
    VirtualFile projectFile = project.getProjectFile();
    if (projectFile == null) {
      Messages.showMessageDialog("Project file cannot be obtained", "Error",
        Messages.getErrorIcon());
      return;
    }
    project.getProjectFile().refresh(false, true);
    ProjectView.getInstance(e.getProject()).refresh();
  }
}