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

import com.intellij.AbstractBundle
import com.intellij.DynamicBundle
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.annotations.PropertyKey
import java.util.*

/**
 * 翻译工具
 *
 * @author <a href="mailto:kaiyu.shan@outlook.com">kaiyu.shan</a>
 * @since 1.2.0
 */
open class TranslationDynamicBundle(private val pathToBundle: String) :
    AbstractBundle(pathToBundle) {

    private val adaptedControl =
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)

    private val adaptedBundle: AbstractBundle? by lazy {
        val dynamicLocale = dynamicLocale ?: return@lazy null
        if (dynamicLocale.toLanguageTag() == Locale.ENGLISH.toLanguageTag()) {
            object : AbstractBundle(pathToBundle) {
                override fun findBundle(
                    pathToBundle: String,
                    loader: ClassLoader,
                    control: ResourceBundle.Control
                ): ResourceBundle {
                    val dynamicBundle = ResourceBundle.getBundle(
                        pathToBundle,
                        dynamicLocale,
                        loader,
                        adaptedControl
                    )
                    return dynamicBundle ?: super.findBundle(pathToBundle, loader, control)
                }
            }
        } else null
    }

    override fun findBundle(
        pathToBundle: String,
        loader: ClassLoader,
        control: ResourceBundle.Control
    ): ResourceBundle {
        return dynamicLocale?.let { ResourceBundle.getBundle(pathToBundle, it, loader, control) }
            ?: super.findBundle(pathToBundle, loader, control)
    }

    fun getAdaptedMessage(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any
    ): String {
        return adaptedBundle?.getMessage(key, *params) ?: getMessage(key, *params)
    }

    companion object {
        private val LOGGER = Logger.getInstance(TranslationDynamicBundle::class.java)

        val dynamicLocale: Locale? by lazy {
            try {
                DynamicBundle.getLocale()
            } catch (e: NoSuchMethodError) {
                LOGGER.debug("NoSuchMethodError: DynamicBundle.getLocale()")
                null
            }
        }
    }
}