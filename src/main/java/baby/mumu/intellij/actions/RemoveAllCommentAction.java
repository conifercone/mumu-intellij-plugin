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

import baby.mumu.intellij.kotlin.services.CommentDbService;
import baby.mumu.intellij.kotlin.tools.TranslationBundleTool;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

/**
 * 删除所有注释动作
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.3.0
 */
public class RemoveAllCommentAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    e.getPresentation().setEnabled(
      project != null && project.getService(CommentDbService.class)
        .getConnected());
    e.getPresentation()
      .setText(TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.all.comment.action.text"));
    e.getPresentation().setDescription(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.all.comment.action.description"));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }

    int result = Messages.showYesNoDialog(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.all.comment.dialog"), // 提示消息
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.all.comment.title"), // 标题
      Messages.getQuestionIcon() // 图标
    );

    // 如果用户选择了 "是"
    if (result == Messages.YES) {
      project.getService(CommentDbService.class).removeAll();
      ProjectView.getInstance(project).refresh();
    }
  }
}
