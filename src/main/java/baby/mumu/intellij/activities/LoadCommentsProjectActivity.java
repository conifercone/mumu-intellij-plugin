/*
 * Copyright (c) 2024-2025, the original author or authors.
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
package baby.mumu.intellij.activities;

import baby.mumu.intellij.kotlin.services.CommentDbService;
import baby.mumu.intellij.toolwindows.CommentToolWindowRefreshNotifier;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import java.util.List;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 加载项目所有注释
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">Kaiyu Shan</a>
 * @since 1.0.0
 */
public class LoadCommentsProjectActivity implements ProjectActivity {

  @Override
  public @Nullable Object execute(@NotNull Project project,
    @NotNull Continuation<? super Unit> continuation) {
    project.getService(CommentDbService.class).connectDatabase(project);
    project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES,
      new BulkFileListener() {
        @Override
        public void after(@NotNull List<? extends VFileEvent> events) {
          if (project.getService(CommentDbService.class).getConnected()) {
            List<VFileDeleteEvent> fileDeleteEvents = events.stream()
              .filter(event -> event instanceof VFileDeleteEvent)
              .map(event -> (VFileDeleteEvent) event)
              .toList();
            List<VFilePropertyChangeEvent> filePropertyChangeEvents = events.stream()
              .filter(event -> event instanceof VFilePropertyChangeEvent)
              .map(event -> (VFilePropertyChangeEvent) event)
              .toList();
            List<VFileMoveEvent> fileMoveEvents = events.stream()
              .filter(event -> event instanceof VFileMoveEvent)
              .map(event -> (VFileMoveEvent) event)
              .toList();
            fileDeleteEvents.forEach(event -> {
              VirtualFile file = event.getFile();
              project.getService(CommentDbService.class).removeByRelativePath(project, file);
            });
            filePropertyChangeEvents.forEach(
              event -> processPathChange(project, event.getOldPath(), event.getNewPath()));
            fileMoveEvents.forEach(
              event -> processPathChange(project, event.getOldPath(), event.getNewPath()));
            project.getMessageBus().syncPublisher(CommentToolWindowRefreshNotifier.TOPIC).refresh();
          }
        }

        private void processPathChange(@NotNull Project project, String oldPath, String newPath) {
          String oldRelativePath = project.getService(CommentDbService.class)
            .getRelativePath(project,
              oldPath);
          String newRelativePath = project.getService(CommentDbService.class)
            .getRelativePath(project,
              newPath);
          // 如果路径发生变化，更新数据库中的数据
          if (!oldRelativePath.equals(newRelativePath)) {
            project.getService(CommentDbService.class)
              .updateRelativePathByRelativePath(oldRelativePath,
                newRelativePath);
          }
        }
      });
    return null;
  }
}
