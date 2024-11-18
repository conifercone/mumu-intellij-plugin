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
package baby.mumu.intellij.kotlin.tools

import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.MuMuBundle"

/**
 * 翻译工具
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.2.0
 */
object TranslationBundleTool : TranslationDynamicBundle(BUNDLE)

fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return TranslationBundleTool.getMessage(key, *params)
}

fun adaptedMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return TranslationBundleTool.getAdaptedMessage(key, *params)
}