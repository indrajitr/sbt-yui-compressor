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

/**
  * Convenient helper for YUI Compressor options.
  */
object Opts {
  def charset(c: String)  = Seq("-charset", c)
  def lineBreak(col: Int) = Seq("--line-break", col)
  val verbose             = "--verbose"
  object js {
    val nomunge              = "--nomunge"                 
    val preserveSemi         = "--preserve-semi" 
    val disableOptimizations = "--disable-optimizations "
  }
}

object DefaultOptions {
	import Opts._
	def cssCompressor: Seq[String] = charset("UTF-8")
  def jsCompressor: Seq[String]  = charset("UTF-8")
}
