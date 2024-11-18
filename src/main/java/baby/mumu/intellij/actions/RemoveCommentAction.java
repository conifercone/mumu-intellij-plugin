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

import baby.mumu.intellij.kotlin.dos.MuMuComment;
import baby.mumu.intellij.kotlin.services.CommentDbService;
import baby.mumu.intellij.kotlin.tools.TranslationBundleTool;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 删除注释动作
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.0.0
 */
public class RemoveCommentAction extends AnAction {

  @Override
  public void update(@NotNull AnActionEvent e) {
    //noinspection DuplicatedCode
    Project project = e.getProject();
    VirtualFile selectedFile = e.getData(PlatformCoreDataKeys.VIRTUAL_FILE);
    e.getPresentation().setEnabled(
      project != null && selectedFile != null && project.getService(CommentDbService.class)
        .getConnected()
        && project.getService(CommentDbService.class).getByRelativePath(project, selectedFile)
        != null);
    e.getPresentation()
      .setText(TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.comment.action.text"));
    e.getPresentation().setDescription(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.comment.action.description"));
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
    // 获取当前选中的文件
    VirtualFile selectedFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
    if (selectedFile == null) {
      return;
    }

    // 检查是否存在注释
    MuMuComment existingComment = project.getService(CommentDbService.class)
      .getByRelativePath(project,
        selectedFile);
    if (existingComment == null) {
      Messages.showMessageDialog(
        TranslationBundleTool.INSTANCE.getAdaptedMessage("this.file.has.no.comment"),
        TranslationBundleTool.INSTANCE.getAdaptedMessage("hint"),
        Messages.getInformationIcon());
      return;
    }

    int result = Messages.showYesNoDialog(
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.comment.dialog"), // 提示消息
      TranslationBundleTool.INSTANCE.getAdaptedMessage("delete.comment.title"), // 标题
      Messages.getQuestionIcon() // 图标
    );

    // 如果用户选择了 "是"
    if (result == Messages.YES) {
      project.getService(CommentDbService.class).removeById(existingComment.getId());
      selectedFile.refresh(false, true); // 刷新文件
      ProjectView.getInstance(project).refresh();
    }
  }
}
