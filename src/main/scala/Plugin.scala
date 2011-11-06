/*
 * Copyright 2011 Indrajit Raychaudhuri
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

package org.scala_tools.sbt.yuiCompressor

import sbt._
import Keys._


/**
  * CSS and Javascript compressor for SBT using YUI Compressor.
  *
  * @author Indrajit Raychaudhuri
  */
object Plugin extends sbt.Plugin {

  object YuiCompressorKeys {
    lazy val minSuffix     = SettingKey[String]("yui-min-suffix", "Suffix of the base of the minified files.")
    lazy val options       = SettingKey[Seq[String]]("yui-options", "YUI Compressor options.")
    lazy val cssResources  = TaskKey[Seq[File]]("yui-css-resources", "CSS resources to be minified.")
    lazy val jsResources   = TaskKey[Seq[File]]("yui-js-resources", "JavaScript resources to be minified.")
    lazy val cssCompressor = TaskKey[Seq[File]]("yui-css-compressor", "CSS compressor task.")
    lazy val jsCompressor  = TaskKey[Seq[File]]("yui-js-compressor", "JavaScript compressor task.")
  }

  private val yui = YuiCompressorKeys

  private def compressorTask(resources: TaskKey[Seq[File]], task: TaskKey[Seq[File]]) =
    (cacheDirectory in task, resources, resourceManaged in task, unmanagedResourceDirectories in task, yui.minSuffix in task, state in task, runner in task, yui.options in task, streams) map {
      (cache, in, outdir, dirs, suf, state, runner, opts, s) => compressorTask0(cache, in, outdir, dirs, suf, state, runner, opts, s.log)
    }

  private def compressorTask0(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, state: State, runner: ScalaRun, options: Seq[String], log: Logger) = {
    def appendSuffix(file: File, suffix: String): File = file.getParentFile / (file.base + suffix + "." + file.ext)
    val mappings = (in --- dirs) x (rebase(dirs, outdir) | flat(outdir)) map { pair => (pair._1, appendSuffix(pair._2, suffix)) }
    Compressor(cacheDir, mappings, Project.extract(state).currentUnit.unit.plugins.classpath, runner, options, log)
  }

  private def yuiCollectFiles(key: TaskKey[Seq[File]]) =
    Defaults.collectFiles(unmanagedResourceDirectories in key, includeFilter in key, excludeFilter in key)

  private def generatorConfigCommon(key: TaskKey[Seq[File]]) =
    inTask(key)(Seq(
      cacheDirectory ~= (_ / key.key.label),
      Defaults.runnerTask))

  lazy val yuiBaseSettings: Seq[Setting[_]] =
    Seq(
      yui.minSuffix := "-min",
      yui.options   := Nil,
      // fork := true,
      trapExit := true,
      includeFilter in yui.cssResources  := "*.css",
      excludeFilter in yui.cssResources <<= excludeFilter in unmanagedResources,
      includeFilter in yui.jsResources   := "*.js",
      excludeFilter in yui.jsResources  <<= excludeFilter in unmanagedResources)

  lazy val yuiResourceConfig: Seq[Setting[_]] =
    Seq(
      yui.cssResources <<= yuiCollectFiles(yui.cssResources),
      yui.jsResources  <<= yuiCollectFiles(yui.jsResources),
      watchSources in Defaults.ConfigGlobal <++= (yui.cssResources, yui.jsResources) map (_ ++ _))

  lazy val yuiGeneratorConfig: Seq[Setting[_]] =
    generatorConfigCommon(yui.cssCompressor) ++
    generatorConfigCommon(yui.jsCompressor) ++
    Seq(
      yui.cssCompressor <<= compressorTask(yui.cssResources, yui.cssCompressor),
      yui.jsCompressor <<= compressorTask(yui.jsResources, yui.jsCompressor),
      resourceGenerators <++= (yui.cssCompressor, yui.jsCompressor)(_ :: _ :: Nil)
    )

  lazy val yuiCompressorConfigs: Seq[Setting[_]] = yuiBaseSettings ++ yuiResourceConfig ++ yuiGeneratorConfig

  lazy val yuiSettings: Seq[Setting[_]] = inConfig(Compile)(yuiCompressorConfigs) ++ inConfig(Test)(yuiCompressorConfigs)

}
