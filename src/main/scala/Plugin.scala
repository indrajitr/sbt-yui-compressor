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
    lazy val yuiMinSuffix        = SettingKey[String]("yui-min-suffix", "Suffix of the base of the minified files.")
    lazy val yuiBreakColumn      = SettingKey[Int]("yui-break-column", "Line break column.")
    lazy val yuiVerbose          = SettingKey[Boolean]("yui-verbose", "Enable verbose messages.")
    lazy val yuiMunge            = SettingKey[Boolean]("yui-munge", "Obfuscate local symbols in addition to minifying (for JavaScript resources only).")
    lazy val yuiOptimize         = SettingKey[Boolean]("yui-optimize", "Enable micro-optimization (for JavaScript resources only).")
    lazy val yuiPreserveSemi     = SettingKey[Boolean]("yui-preserve-semi", "Preserve unnecessary semicolons (for JavaScript resources only).")
    lazy val yuiJsCompressorOpts = SettingKey[(Boolean, Boolean, Boolean)]("yui-js-compressor-opts", "JavaScript compressor specific options (munge, optimize, preserveSemi).")
    lazy val yuiCssResources     = TaskKey[Seq[File]]("yui-css-resources", "CSS resources, which are manually created.")
    lazy val yuiCssCompressor    = TaskKey[Seq[File]]("yui-css-compressor", "CSS compressor task.")
    lazy val yuiJsResources      = TaskKey[Seq[File]]("yui-js-resources", "JavaScript resources, which are manually created.")
    lazy val yuiJsCompressor     = TaskKey[Seq[File]]("yui-js-compressor", "JavaScript compressor task.")
  }

  def compressorTask(compressor: Compressor)(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, log: Logger) = {
    val mappings = (in --- dirs) x (rebase(dirs, outdir) | flat(outdir)) map { pair => (pair._1, appendSuffix(pair._2, suffix)) }
    Compress(cacheDir, compressor, mappings, log)
  }

  private def appendSuffix(file: File, suffix: String): File =
    file.getParentFile / (file.base + suffix + "." + file.ext)

  def yuiCssCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, col: Int, verbose: Boolean, s: TaskStreams) =
    compressorTask(CssCompressor(col/*, verbose*/))(cacheDir, in, outdir, dirs, suffix, s.log)

  def yuiJsCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, col: Int, verbose: Boolean, opts: (Boolean, Boolean, Boolean), s: TaskStreams) =
    compressorTask(JsCompressor(col, verbose, opts._1, opts._2, opts._3))(cacheDir, in, outdir, dirs, suffix, s.log)

  def baseYuiCompressorSettings: Seq[Setting[_]] =
    Seq(
      yuiMinSuffix   := "-min",
      yuiBreakColumn := 0,
      yuiVerbose     := false,

      yuiMunge        := true,
      yuiOptimize     := true,
      yuiPreserveSemi := false,
      yuiJsCompressorOpts <<= Seq(yuiMunge, yuiOptimize, yuiPreserveSemi).join { opts => (opts(0), opts(1), opts(2)) },

      includeFilter in yuiCssResources  := "*.css",
      excludeFilter in yuiCssResources <<= excludeFilter in unmanagedResources,
      includeFilter in yuiJsResources   := "*.js",
      excludeFilter in yuiJsResources  <<= excludeFilter in unmanagedResources)

  def yuiCompressorConfigs: Seq[Setting[_]] =
    baseYuiCompressorSettings ++ Seq(
      yuiCssResources  <<= Defaults.collectFiles(unmanagedResourceDirectories in yuiCssResources, includeFilter in yuiCssResources, excludeFilter in yuiCssResources),
      yuiCssCompressor <<= (cacheDirectory, yuiCssResources, resourceManaged in yuiCssCompressor, unmanagedResourceDirectories in yuiCssCompressor, yuiMinSuffix in yuiCssCompressor, yuiBreakColumn in yuiCssCompressor, yuiVerbose in yuiCssCompressor, streams) map yuiCssCompressorTask,

      yuiJsResources   <<= Defaults.collectFiles(unmanagedResourceDirectories in yuiJsResources, includeFilter in yuiJsResources, excludeFilter in yuiJsResources),
      yuiJsCompressor  <<= (cacheDirectory, yuiJsResources, resourceManaged in yuiJsCompressor, unmanagedResourceDirectories in yuiJsCompressor, yuiMinSuffix in yuiJsCompressor, yuiBreakColumn in yuiJsCompressor, yuiVerbose in yuiJsCompressor, yuiJsCompressorOpts in yuiJsCompressor, streams) map yuiJsCompressorTask,

      watchSources in Defaults.ConfigGlobal <++= (yuiCssResources, yuiJsResources) map(_ ++ _),
      resourceGenerators                    <++= (yuiCssCompressor, yuiJsCompressor)(_ :: _ :: Nil))

  def yuiCompressorSettings: Seq[Setting[_]] = inConfig(Compile)(yuiCompressorConfigs) ++ inConfig(Test)(yuiCompressorConfigs)

}
