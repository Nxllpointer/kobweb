@file:Suppress("LeakingThis") // Following official Gradle guidance

package com.varabyte.kobweb.gradle.application.tasks

import com.varabyte.kobweb.common.navigation.RoutePrefix
import com.varabyte.kobweb.gradle.application.BuildTarget
import com.varabyte.kobweb.gradle.application.extensions.app
import com.varabyte.kobweb.gradle.application.templates.SilkSupport
import com.varabyte.kobweb.gradle.application.templates.createMainFunction
import com.varabyte.kobweb.gradle.core.extensions.KobwebBlock
import com.varabyte.kobweb.gradle.core.kmp.jsTarget
import com.varabyte.kobweb.gradle.core.tasks.KobwebModuleTask
import com.varabyte.kobweb.gradle.core.util.hasTransitiveJsDependencyNamed
import com.varabyte.kobweb.gradle.core.util.searchZipFor
import com.varabyte.kobweb.project.conf.KobwebConf
import com.varabyte.kobweb.project.frontend.AppData
import com.varabyte.kobweb.project.frontend.FrontendData
import kotlinx.serialization.json.Json
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class KobwebGenerateSiteEntryTask @Inject constructor(
    private val kobwebConf: KobwebConf,
    kobwebBlock: KobwebBlock,
    @get:Input val buildTarget: BuildTarget,
    @get:InputFile val kspGenFile: Provider<RegularFile>,
) : KobwebModuleTask(kobwebBlock, "Generate entry code (i.e. main.kt) for this Kobweb project") {

    @InputFiles
    fun getCompileClasspath() = project.configurations.named(project.jsTarget.compileClasspath)

    @OutputFile
    fun getGenMainFile() = kobwebBlock.getGenJsSrcRoot(project).resolve("main.kt")

    @TaskAction
    fun execute() {
        val appData = Json.decodeFromString(AppData.serializer(), kspGenFile.get().asFile.readText())
        val routePrefix = RoutePrefix(kobwebConf.site.routePrefix)
        val mainFile = getGenMainFile()
        mainFile.parentFile.mkdirs()

        val libData = buildList {
            getCompileClasspath().get().files.forEach { file ->
                file.searchZipFor("frontend.json") { bytes ->
                    add(Json.decodeFromString(FrontendData.serializer(), bytes.decodeToString()))
                }
            }
        }

        mainFile.writeText(
            createMainFunction(
                appData,
                libData,
                when {
                    project.hasTransitiveJsDependencyNamed("kobweb-silk") -> SilkSupport.FULL
                    project.hasTransitiveJsDependencyNamed("silk-foundation") -> SilkSupport.FOUNDATION
                    else -> SilkSupport.NONE
                },
                kobwebBlock.app,
                routePrefix,
                buildTarget
            )
        )
    }
}