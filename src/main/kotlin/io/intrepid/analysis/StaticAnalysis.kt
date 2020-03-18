package io.intrepid.analysis

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.dsl.LintOptions
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.*
import org.gradle.kotlin.dsl.*
import java.io.File
import java.io.InputStream

@Suppress("UnstableApiUsage")
open class StaticAnalysis : Plugin<Project> {
    override fun apply(project: Project) {
        val extension: StaticAnalysisExtension = project.extensions.create("staticAnalysis", StaticAnalysisExtension::class)

        project.afterEvaluate {
            createUpdateLintFileTask(project)
            createPmdTask(project, extension)
            createFindBugsTasks(project, extension)
            setupLint(project, extension)

            createCombinedTask(project)
        }
    }

    private fun lintFile(project: Project): File {
        return File(project.projectDir, "lint.xml")
    }

    private fun createUpdateLintFileTask(project: Project) {
        project.tasks.register("updateLintFile") {
            doLast {
                updateLintFile(project)
            }
        }
    }

    private fun updateLintFile(project: Project) {
        val lintFile = lintFile(project)
        val input = StaticAnalysis::class.java.getResourceAsStream("/default-lintConfig.xml")
        lintFile.writeText(input.readText())
    }

    private fun createPmdTask(project: Project, extension: StaticAnalysisExtension) {
        project.plugins.apply(PmdPlugin::class)
        val pmdExtension = project.extensions.getByType(PmdExtension::class)
        pmdExtension.toolVersion = extension.pmdVersion

        project.task<Pmd>("pmd") {
            doFirst {
                val ruleSetFile = extension.pmdRuleSetFile
                        ?: copyResourceFileToBuildDir(project, "/default-pmd-ruleset.xml")
                ruleSetFiles = project.files(ruleSetFile)
            }

            source = project.fileTree(extension.source)
            include(extension.include)
            exclude(extension.exclude)

            ignoreFailures = true

            reports {
                xml.isEnabled = true
                xml.destination = File("$project.buildDir/reports/pmd/pmd.xml")
                html.isEnabled = true
                html.destination = File("$project.buildDir/reports/pmd/pmd.html")
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun createFindBugsTasks(project: Project, extension: StaticAnalysisExtension) {
        project.plugins.apply(FindBugsPlugin::class)
        val findBugsExtension = project.extensions.getByType(FindBugsExtension::class)
        findBugsExtension.toolVersion = extension.findbugsVersion

        // Adds a findBugs task for each build variant
        val buildVariants = getBuildVariantNames(project)
        buildVariants?.forEach {
            project.task<FindBugs>("findBugs$it") {
                dependsOn("assemble$it")

                doFirst {
                    val excludeFilter = extension.findBugsExcludeFilterFile
                            ?: copyResourceFileToBuildDir(project, "/default-findbugs-filter.xml")
                    setExcludeFilter(excludeFilter)
                }

                effort = extension.findBugsEffort
                reportLevel = extension.findBugsReportLevel
                classes = extension.findBugsClasses ?: project.files("${project.buildDir}/intermediates/classes")
                classpath = project.files()

                source = project.fileTree(extension.source)
                include(extension.include)
                exclude(extension.exclude)

                ignoreFailures = true

                reports {
                    // Findbugs can only produce one type of report at the time, so we use a command line argument to specify
                    // if we want a xml report instead (usually for jenkins).
                    if (project.hasProperty("findBugsXml")) {
                        xml.isEnabled = true
                        html.isEnabled = false
                    } else {
                        xml.isEnabled = false
                        html.isEnabled = true
                    }

                    xml.destination = File("$project.buildDir/reports/findbugs/findbugs.xml")
                    html.destination = File("$project.buildDir/reports/findbugs/findbugs.html")
                }
            }
        }
    }

    private fun setupLint(project: Project, extension: StaticAnalysisExtension) {
        val androidConfig = project.extensions.getByType(AndroidConfig::class)
        val lintOptions: LintOptions = androidConfig.lintOptions as LintOptions

        lintOptions.isAbortOnError = extension.lintAbortOnError
        lintOptions.isCheckDependencies = extension.lintCheckDependencies
        lintOptions.isWarningsAsErrors = extension.lintWarningsAsErrors

        // If lint.xml file does not exist, copy it into the module's top-level directory where Android Studio can find it
        if (!lintFile(project).exists()) {
            updateLintFile(project)
        }

        val buildVariants = getBuildVariantNames(project)
        buildVariants?.forEach {
            configureLintVariant(project, it, lintOptions)
        }

        // Also setup for the top level "lint" task which runs all variants
        configureLintVariant(project, "", lintOptions)
    }

    private fun configureLintVariant(project: Project, buildVariant: String, lintOptions: LintOptions) {
        project.tasks.getByName("lint$buildVariant").doFirst {
            lintOptions.lintConfig = lintFile(project)
        }
    }

    private fun createCombinedTask(project: Project) {
        getBuildVariantNames(project)?.forEach {
            project.task<Task>("analyze$it") {
                dependsOn("pmd", "findBugs$it", "lint$it")
            }
        }
    }

    // Both pmd and findbugs requires a File object for ruleSetFiles and excludeFilter
    // We can't directly create a File object from a resource file in the jar, but we can read it as a stream.
    // So we basically copy the default config files to the build directory and create a file from there
    private fun copyResourceFileToBuildDir(project: Project, fileName: String): File {
        // Create the parent directories if they don't exist
        project.buildDir.mkdir()
        val staticAnalysisBuildDir = File(project.buildDir, "staticAnalysis")
        staticAnalysisBuildDir.mkdir()

        val input = StaticAnalysis::class.java.getResourceAsStream(fileName)
        val file = File(staticAnalysisBuildDir, fileName)
        file.writeText(input.readText())
        return file
    }

    private fun getBuildVariants(project: Project): DomainObjectSet<out BaseVariant>? {
        val plugins = project.plugins
        plugins.findPlugin(AppPlugin::class)?.let {
            val appExtension = it.extension as AppExtension
            return appExtension.applicationVariants
        }
        plugins.findPlugin(LibraryPlugin::class)?.let {
            val libraryExtension = it.extension as LibraryExtension
            return libraryExtension.libraryVariants
        }
        throw RuntimeException("The project must use either android or android-library plugin")
    }

    private fun getBuildVariantNames(project: Project): List<String>? {
        val variants = getBuildVariants(project)
        return variants?.map { it.name.capitalize() }
    }

    private fun InputStream.readText(): String {
        return bufferedReader().readText()
    }
}
