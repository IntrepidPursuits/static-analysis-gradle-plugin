package io.intrepid.analysis

import org.gradle.api.file.FileCollection
import java.io.File

open class StaticAnalysisExtension {

    var pmdVersion = "5.5.1"
    var findbugsVersion = "3.0.1"

    var source = "src"
    var include = "**/*.java"
    var exclude = "**/gen/**"

    var pmdRuleSetFile: File? = null

    var findBugsEffort = "max"
    var findBugsReportLevel = "medium"
    var findBugsClasses: FileCollection? = null
    var findBugsExcludeFilterFile: File? = null

    var lintAbortOnError = true
    var lintCheckDependencies = true
    var lintWarningsAsErrors = true
}
