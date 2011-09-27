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

import java.io.File
import sbt._
import Keys._


/**
  * CSS and Javascript compressor for SBT using YUI Compressor.
  *
  * @author Indrajit Raychaudhuri
  */
object Plugin extends sbt.Plugin {
  import YuiCompressorKeys._

  object YuiCompressorKeys {
    lazy val minSuffix        = SettingKey[String]("yui-min-suffix", "Suffix of the base of the minified files.")
    lazy val breakColumn      = SettingKey[Int]("yui-break-column", "Line break column.")
    lazy val verbose          = SettingKey[Boolean]("yui-verbose", "Enable verbose messages.")
    lazy val jsCompressorOpts = SettingKey[(Boolean, Boolean, Boolean)]("yui-js-compressor-opts", "JavaScript compressor specific options (munge, optimize, preserveSemi).")
    lazy val cssResources   = TaskKey[Seq[File]]("yui-css-resources", "CSS resources, which are manually created.")
    lazy val cssCompressor = TaskKey[Seq[File]]("yui-css-compressor", "CSS compressor task.")
    lazy val jsResources   = TaskKey[Seq[File]]("yui-js-resources", "JavaScript resources, which are manually created.")
    lazy val jsCompressor  = TaskKey[Seq[File]]("yui-js-compressor", "JavaScript compressor task.")
  }

  def compressorTask(compressor: Compressor)(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, log: Logger) = {
    val mappings = (in --- dirs) x (rebase(dirs, outdir) | flat(outdir)) map { pair => (pair._1, appendSuffix(pair._2, suffix)) }
    Compress(cacheDir, compressor, mappings, log)
  }

  private def appendSuffix(file: File, suffix: String): File =
    file.getParentFile / (file.base + suffix + "." + file.ext)

  def cssCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, breakCol: Int, verbose: Boolean, s: TaskStreams) =
    compressorTask(CssCompressor(breakCol/*, verbose*/))(cacheDir, in, outdir, dirs, suffix, s.log)

  def jsCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, breakCol: Int, verbose:Boolean, jsCompOpts: (Boolean, Boolean, Boolean), s: TaskStreams) =
    compressorTask(JsCompressor(breakCol, verbose, jsCompOpts._1, jsCompOpts._2, jsCompOpts._3))(cacheDir, in, outdir, dirs, suffix, s.log)

  def baseYuiCompressorSettings: Seq[Setting[_]] =
    Seq(
      minSuffix   := "-min",
      breakColumn := 0,
      verbose     := false,

      includeFilter in cssResources  := "*.css",
      excludeFilter in cssResources <<= excludeFilter in unmanagedResources,

      includeFilter in jsResources     := "*.js",
      excludeFilter in jsResources    <<= excludeFilter in unmanagedResources,
      jsCompressorOpts in jsCompressor := (true, true, false))

  def yuiCompressorConfigs: Seq[Setting[_]] =
    baseYuiCompressorSettings ++ Seq(
      cssResources  <<= Defaults.collectFiles(unmanagedResourceDirectories, includeFilter in cssResources, excludeFilter in cssResources),
      cssCompressor <<= (cacheDirectory, cssResources, resourceManaged, unmanagedResourceDirectories, minSuffix in cssCompressor, breakColumn in cssCompressor, verbose in cssCompressor, streams) map cssCompressorTask,

      jsResources   <<= Defaults.collectFiles(unmanagedResourceDirectories, includeFilter in jsResources, excludeFilter in jsResources),
      jsCompressor  <<= (cacheDirectory, jsResources, resourceManaged, unmanagedResourceDirectories, minSuffix in jsCompressor, breakColumn in jsCompressor, verbose in jsCompressor, jsCompressorOpts in jsCompressor, streams) map jsCompressorTask,

      watchSources in Defaults.ConfigGlobal <++= (cssResources, jsResources) map(_ ++ _),
      resourceGenerators                    <++= (cssCompressor, jsCompressor)(_ :: _ :: Nil))

  def yuiCompressorSettings: Seq[Setting[_]] = inConfig(Compile)(yuiCompressorConfigs) ++ inConfig(Test)(yuiCompressorConfigs)

}
