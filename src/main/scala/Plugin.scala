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

package org.scala_tools.sbt

import java.io.File
import sbt._
import Keys._


/**
  * CSS and Javascript compressor for SBT using YUI Compressor.
  *
  * @author Indrajit Raychaudhuri
  */
object YuiCompressorPlugin extends Plugin {

  lazy val YuiCompressor = config("yui-compressor") hide

  lazy val yuiCompressorOptions = SettingKey[Seq[String]]("yui-compressor-options", "Common options for both CSS and JavaScript compressors.")
  lazy val cssCompressorOptions = SettingKey[Seq[String]]("css-compressor-options", "Options for CSS compressor.")
  lazy val jsCompressorOptions  = SettingKey[Seq[String]]("js-compressor-options", "Options for JavaScript compressor.")

  lazy val yuiCompressorMinSuffix = SettingKey[String]("yui-compressor-min-suffix", "Suffix of the base of the minified files.")

  lazy val cssResources  = TaskKey[Seq[File]]("css-resources", "CSS resources, which are manually created.")
  lazy val jsResources   = TaskKey[Seq[File]]("js-resources", "JavaScript resources, which are manually created.")
  lazy val cssCompressor = TaskKey[Seq[File]]("css-compressor", "CSS compressor task.")
  lazy val jsCompressor  = TaskKey[Seq[File]]("js-compressor", "JavaScript compressor task.")

  def compressorTask(compressor: Compressor)(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, log: Logger) = {
    val mappings = (in --- dirs) x (rebase(dirs, outdir) | flat(outdir)) map { pair => (pair._1, appendSuffix(pair._2, suffix)) }
    Compress(cacheDir, compressor, mappings, log)
  }

  private def appendSuffix(file: File, suffix: String): File =
    file.getParentFile / (file.base + suffix + "." + file.ext)

  // def cssCompressorTask = compressorTask(CssCompressor(0, charset = IO.defaultCharset)) _
  // def jsCompressorTask = compressorTask(JsCompressor(0, verbose = logLevel < Level.Info, charset = IO.defaultCharset)) _

  def cssCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, logLevel: Level.Value, s: TaskStreams) =
    compressorTask(CssCompressor(0, charset = IO.defaultCharset))(cacheDir, in, outdir, dirs, suffix, s.log)

  def jsCompressorTask(cacheDir: File, in: Seq[File], outdir: File, dirs: Seq[File], suffix: String, logLevel: Level.Value, s: TaskStreams) =
    compressorTask(JsCompressor(0, verbose = logLevel < Level.Info, charset = IO.defaultCharset))(cacheDir, in, outdir, dirs, suffix, s.log)

  def yuiCompressorConfigs: Seq[Setting[_]] =
    inConfig(YuiCompressor)(Seq(

      yuiCompressorOptions   := Nil, // Seq("--charset", "UTF-8"), //--type <js|css> --charset <charset> --line-break <column> --verbose
      yuiCompressorMinSuffix := "-min",
      logLevel              <<=  logLevel ?? Level.Info,

      cssCompressorOptions          <<= yuiCompressorOptions,
      includeFilter in cssResources  := "*.css",
      excludeFilter in cssResources <<= excludeFilter in unmanagedResources,

      jsCompressorOptions          <<= yuiCompressorOptions,
      includeFilter in jsResources  := "*.js",
      excludeFilter in jsResources <<= excludeFilter in unmanagedResources)) ++
    Seq(
      cssResources  <<= Defaults.collectFiles(unmanagedResourceDirectories, includeFilter in YuiCompressor in cssResources, excludeFilter in YuiCompressor in cssResources),
      cssCompressor <<= (cacheDirectory, cssResources, resourceManaged, unmanagedResourceDirectories, yuiCompressorMinSuffix in YuiCompressor, logLevel in YuiCompressor, streams) map cssCompressorTask,

      jsResources   <<= Defaults.collectFiles(unmanagedResourceDirectories, includeFilter in YuiCompressor in jsResources, excludeFilter in YuiCompressor in jsResources),
      jsCompressor  <<= (cacheDirectory, jsResources, resourceManaged, unmanagedResourceDirectories, yuiCompressorMinSuffix in YuiCompressor, logLevel in YuiCompressor, streams) map jsCompressorTask,

      watchSources in Defaults.ConfigGlobal <++= (cssResources, jsResources) map(_ ++ _),
      resourceGenerators                    <++= (cssCompressor, jsCompressor)(_ :: _ :: Nil))

  def yuiCompressorSettings: Seq[Setting[_]] = inConfig(Compile)(yuiCompressorConfigs) ++ inConfig(Test)(yuiCompressorConfigs)

}
