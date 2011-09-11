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

import java.io.{File, IOException}
import sbt._
import Keys._


/**
  * CSS and Javascript compressor for SBT using YUI Compressor.
  *
  * @author Indrajit Raychaudhuri
  */
object YuiCompressorPlugin extends Plugin {

  // val YuiCompressor = config("yui-compressor") hide
  lazy val YuiCssCompressor = config("yui-css-compressor") hide
  lazy val YuiJsCompressor  = config("yui-js-compressor") hide

  lazy val yuiCompressorOptions = SettingKey[Seq[String]]("yui-compressor-options", "Common options for both CSS and JS compressors.")
  lazy val cssCompressorOptions = SettingKey[Seq[String]]("css-compressor-options", "Options for CSS compressor.")
  lazy val jsCompressorOptions = SettingKey[Seq[String]]("js-compressor-options", "Options for JS compressor.")

  val cssResourceManaged = SettingKey[File]("yui-resource-managed", "Default managed resource directory, used when generating CSS resources.")

  lazy val cssResourceDirectories = SettingKey[Seq[File]]("css-resource-directories", "CSS resource directories, containing resources manually created by the user.")
  // lazy val jsResourceDirectories = SettingKey[Seq[File]]("js-resource-directories", "JS resource directories, containing resources manually created by the user.")
  lazy val cssResources = TaskKey[Seq[File]]("css-resources", "CSS resources, which are manually created.")

  lazy val cssCompressor = TaskKey[Seq[File]]("compress-css", "TODO")

  def cssCompressorTask(in: Seq[File], out: File, dirs: Seq[File], s: TaskStreams) = {
    // val mappings = (resrcs --- dirs) x (rebase(dirs, target) | flat(target))
    val mappings = in x (rebase(dirs, out) | flat(out))
    util.control.Exception.catching(classOf[IOException]) either {
      mappings map { tuple =>
        val (inFile, outFile) = tuple
        val outFileMin = outFile.getParentFile / (outFile.base + "-min." + outFile.ext)
        CssCompressor(0, IO.defaultCharset).compress(inFile, outFileMin, s.log)
        s.log.debug("Minified resource: %s to %s".format(inFile, outFileMin))
        outFileMin
      }
    } match {
      case Right(outList) => outList
      case Left(e)        => { s.log.warn("Failed during minification [%s]".format(e)); Nil }
    }
  }

  def yuiCompressorSettings: Seq[Setting[_]] =
    inConfig(YuiCssCompressor)(Seq(

      yuiCompressorOptions := Nil, // Seq("--charset", "UTF-8"), //--type <js|css> --charset <charset> --line-break <column> --verbose
      cssCompressorOptions <<= yuiCompressorOptions,
      // TODO: Consider removing these keys which are very unlikely to be configured individually
      // e.g., instead of `cssResourceManaged in Compile`, one can always configure `resourceManaged in YuiCssCompressor in Compile`
      cssResourceManaged in Compile <<= (cssResourceManaged in Compile) or (resourceManaged in Compile),
      cssResourceDirectories in Compile <<= (cssResourceDirectories in Compile) or (unmanagedResourceDirectories in Compile),
      includeFilter in cssResources := "*.css",
      excludeFilter in cssResources <<= (excludeFilter in cssResources) or (excludeFilter in unmanagedResources),
      cssResources in Compile <<= Defaults.collectFiles(cssResourceDirectories in Compile, includeFilter in cssResources, excludeFilter in cssResources),
      cssCompressor in Compile <<= (cssResources in Compile, cssResourceManaged in Compile, cssResourceDirectories in Compile, streams) map cssCompressorTask)) ++
    Seq(
      watchSources in Defaults.ConfigGlobal <++= cssResources in YuiCssCompressor in Compile,
      resourceGenerators in Compile <+= cssCompressor in YuiCssCompressor in Compile
    )

}
