/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.gradle.internal

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.compilerRunner.ArgumentUtils
import java.io.File

@Suppress("UNCHECKED_CAST")
private fun <T : CommonToolArguments> divideCompilerArguments(compArgs: T): Array<Array<String>> {
    val argsClass = compArgs::class.java
    val classpathParts = argsClass.declaredMethods.find { it.name == "getClasspath" }?.let {
        (it.invoke(compArgs) as? String)?.split(File.pathSeparator)
            ?.map { cp -> cp.replace('\\', '/') }?.toTypedArray()
    } ?: emptyArray()
    val pluginClasspaths = argsClass.declaredMethods.find { it.name == "getPluginClasspaths" }?.let {
        (it.invoke(compArgs) as? Array<String>)?.map { cp -> cp.replace('\\', '/') }?.toTypedArray()
    } ?: emptyArray()
    val friendPaths = argsClass.declaredMethods.find { it.name == "getFriendPaths" }?.let {
        (it.invoke(compArgs) as? Array<String>)?.map { cp -> cp.replace('\\', '/') }?.toTypedArray()
    } ?: emptyArray()
    compArgs.apply {
        argsClass.declaredMethods.find { it.name == "setClasspath" }?.invoke(this, null)
        argsClass.declaredMethods.find { it.name == "setPluginClasspaths" }?.invoke(this, null)
        argsClass.declaredMethods.find { it.name == "setFriendPaths" }?.invoke(this, null)
    }
    return arrayOf(ArgumentUtils.convertArgumentsToStringList(compArgs).toTypedArray(), classpathParts, pluginClasspaths, friendPaths)
}

interface CompilerArgumentAware<T : CommonToolArguments> {
    val serializedCompilerArguments: List<String>
        get() = ArgumentUtils.convertArgumentsToStringList(prepareCompilerArguments())

    val serializedCompilerArgumentsIgnoreClasspathIssues: List<String>
        get() = ArgumentUtils.convertArgumentsToStringList(prepareCompilerArguments(ignoreClasspathResolutionErrors = true))

    val defaultSerializedCompilerArguments: List<String>
        get() = createCompilerArgs()
            .also { setupCompilerArgs(it, defaultsOnly = true) }
            .let(ArgumentUtils::convertArgumentsToStringList)

    val filteredArgumentsMap: Map<String, String>
        get() = CompilerArgumentsGradleInput.createInputsMap(prepareCompilerArguments())

    val serializedCompilerArgumentsForBucket: Array<Array<String>>
        get() = divideCompilerArguments(prepareCompilerArguments())

    val defaultSerializedCompilerArgumentsForBucket: Array<Array<String>>
        get() = divideCompilerArguments(createCompilerArgs().also { setupCompilerArgs(it, defaultsOnly = true) })

    fun createCompilerArgs(): T
    fun setupCompilerArgs(args: T, defaultsOnly: Boolean = false, ignoreClasspathResolutionErrors: Boolean = false)
}

internal fun <T : CommonToolArguments> CompilerArgumentAware<T>.prepareCompilerArguments(ignoreClasspathResolutionErrors: Boolean = false) =
    createCompilerArgs().also { setupCompilerArgs(it, ignoreClasspathResolutionErrors = ignoreClasspathResolutionErrors) }

interface CompilerArgumentAwareWithInput<T : CommonToolArguments> : CompilerArgumentAware<T> {
    @get:Internal
    override val serializedCompilerArguments: List<String>
        get() = super.serializedCompilerArguments

    @get:Internal
    override val defaultSerializedCompilerArguments: List<String>
        get() = super.defaultSerializedCompilerArguments

    @get:Internal
    override val serializedCompilerArgumentsIgnoreClasspathIssues: List<String>
        get() = super.serializedCompilerArgumentsIgnoreClasspathIssues

    @get:Input
    override val filteredArgumentsMap: Map<String, String>
        get() = super.filteredArgumentsMap

    @get:Internal
    override val serializedCompilerArgumentsForBucket: Array<Array<String>>
        get() = super.serializedCompilerArgumentsForBucket

    @get:Internal
    override val defaultSerializedCompilerArgumentsForBucket: Array<Array<String>>
        get() = super.defaultSerializedCompilerArgumentsForBucket
}