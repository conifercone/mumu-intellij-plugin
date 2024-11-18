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

import com.intellij.openapi.ui.InputValidator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * 注释输入验证器
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.2.0
 */
public class CommentInputValidator implements InputValidator {

  @Override
  public boolean checkInput(@Nullable String inputString) {
    return !StringUtils.isBlank(inputString) && inputString.length() <= 500;
  }

  @Override
  public boolean canClose(@Nullable String inputString) {
    return true;
  }
}