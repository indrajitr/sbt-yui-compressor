/*
 * Copyright 2011-2012 Indrajit Raychaudhuri
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

package yuiCompressor

import sbt._
import Cache.{hConsCache, hNilCache}
import Tracked.{inputChanged, outputChanged}
import FileInfo.{exists, lastModified}


object Compressor {

  val compressorMain = "com.yahoo.platform.yui.compressor.YUICompressor"

  def compress(in: File, out: File, classpath: Seq[File], runner: ScalaRun, options: Seq[String], log: Logger) {
    IO.createDirectory(out.getParentFile)
    runner.run(compressorMain, classpath, options ++ Seq("-o", out.absolutePath, in.absolutePath, "-Xss5120k"), log)
  }

  def apply(cacheDir: File, mappings: Seq[(File, File)], classpath: Seq[File], runner: ScalaRun, options: Seq[String], log: Logger): Seq[File] =
    mappings map { pair =>
      // compress(pair._1, pair._2, classpath, runner, options, log)
      cached(cacheDir, pair._1, pair._2, classpath, runner, options, log)
      pair._2
    }

  def cached(cache: File, source: File, output: File, classpath: Seq[File], runner: ScalaRun, options: Seq[String], log: Logger) {
    type Inputs = ModifiedFileInfo :+: HNil
    val inputs: Inputs = lastModified(source) :+: HNil

    val flatPath: File => String = _.absolutePath.replace(Path.sep, '_')

    val cachedCompress = inputChanged(cache / ("input-" + flatPath(source))) { (inChanged, in: Inputs) =>
      outputChanged(cache / ("output-" + flatPath(output))) { (outChanged, out: PlainFileInfo) =>
        if(inChanged || outChanged)
          compress(source, out.file, classpath, runner, options, log)
        else
          log.debug("File minified and uptodate: " + out.file)
      }
    }

    cachedCompress(inputs)(() => exists(output))
  }

}
