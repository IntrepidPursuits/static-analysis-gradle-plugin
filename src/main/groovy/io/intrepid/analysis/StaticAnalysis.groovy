package io.intrepid.analysis

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.plugins.PluginContainer
import org.gradle.api.plugins.quality.*

class StaticAnalysis implements Plugin<Project> {
    @Override
    void apply(Project project) {
        StaticAnalysisExtension extension = project.extensions.create("staticAnalysis", StaticAnalysisExtension)

        project.afterEvaluate {
            createPmdTask(project, extension)
            createFindBugTasks(project, extension)
        }
    }

    private static void createPmdTask(Project project, StaticAnalysisExtension extension) {
        project.plugins.apply(PmdPlugin)
        def pmdExtension = project.extensions.getByType(PmdExtension)
        pmdExtension.toolVersion = extension.pmdVersion

        project.task("pmd", type: Pmd) {
            doFirst {
                if (extension.pmdRuleSetFile) {
                    ruleSetFiles = project.files(extension.pmdRuleSetFile)
                } else {
                    File file = copyResourceFileToBuildDir(project, "/default-pmd-ruleset.xml")
                    ruleSetFiles = new SimpleFileCollection(file)
                }
            }

            source extension.source
            include extension.include
            exclude extension.exclude

            ignoreFailures = true

            reports {
                xml.enabled = true
                html.enabled = true
                xml {
                    destination "$project.buildDir/reports/pmd/pmd.xml"
                }
                html {
                    destination "$project.buildDir/reports/pmd/pmd.html"
                }
            }
        }
    }

    private static void createFindBugTasks(Project project, StaticAnalysisExtension extension) {
        project.plugins.apply(FindBugsPlugin)
        def findBugsExtension = project.extensions.getByType(FindBugsExtension)
        findBugsExtension.toolVersion = extension.findbugsVersion

        // Adds a findBugs task for each build variant
        def variants = getBuildVariants(project)
        variants.all { bt ->
            def buildVariant = "${bt.name.capitalize()}"
            project.task("findBugs$buildVariant", type: FindBugs, dependsOn: "assemble$buildVariant") {
                doFirst {
                    if (extension.findBugsExcludeFilter) {
                        excludeFilter = new File(extension.findBugsExcludeFilter)
                    } else {
                        excludeFilter = copyResourceFileToBuildDir(project, "/default-findbugs-filter.xml")
                    }
                }

                effort = extension.findBugsEffort
                reportLevel = extension.findBugsReportLevel

                if (extension.findBugsClasses) {
                    classes = extension.findBugsClasses
                } else {
                    classes = project.files("${project.buildDir}/intermediates/classes")
                }
                classpath = project.files()

                source extension.source
                include extension.include
                exclude extension.exclude

                ignoreFailures = true

                reports {
                    // Findbugs can only produce one type of report at the time, so we use a command line argument to specify
                    // if we want a xml report instead (usually for jenkins).
                    if (project.hasProperty('findBugsXml')) {
                        xml.enabled = true
                        html.enabled = false
                    } else {
                        xml.enabled = false
                        html.enabled = true
                    }

                    xml {
                        destination "$project.buildDir/reports/findbugs/findbugs.xml"
                    }
                    html {
                        destination "$project.buildDir/reports/findbugs/findbugs.html"
                    }
                }
            }
        }
    }

    // Both pmd and findbugs requires a File object for ruleSetFiles and excludeFilter
    // We can't directly create a File object from a resource file in the jar, but we can read it as a stream.
    // So we basically copy the default config files to the build directory and create a file from there
    private static File copyResourceFileToBuildDir(Project project, String fileName) {
        // Create the parent directories if they don't exist
        project.buildDir.mkdir()
        File staticAnalysisBuildDir = new File(project.buildDir, "staticAnalysis")
        staticAnalysisBuildDir.mkdir()

        InputStream input = StaticAnalysis.class.getResourceAsStream(fileName)
        File file = new File(staticAnalysisBuildDir, fileName)
        file.text = input.text
        return file
    }

    private static def getBuildVariants(Project project) {
        PluginContainer plugins = project.plugins
        if (plugins.findPlugin('android')) {
            return project.android.applicationVariants
        } else if (plugins.findPlugin('android-library')) {
            return project.android.libraryVariants
        } else {
            throw new RuntimeException("The project must use either android or android-library plugin")
        }
    }
}
