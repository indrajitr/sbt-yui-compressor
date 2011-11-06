# SBT YUI Compressor Plugin

sbt-yui-compressor is a [SBT](https://github.com/harrah/xsbt) plugin for [YUI Compressor](http://developer.yahoo.com/yui/compressor/) to minify CSS and JavaScript.


## Installation

Add the following to your plugin definition list in `project/plugin.sbt` (per project) or `~/.sbt/plugins/plugin.sbt` (global):

    addSbtPlugin("org.scala_tools.sbt" % "sbt-yui-compressor" % "0.1-SNAPSHOT")


Alternately, add sbt-yui-compressor's git repository url as dependency in `project/plugins/project/build.scala`:

```scala
import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(
    uri("git://github.com/indrajitr/sbt-yui-compressor")
}
```


Then add the following to your `build.sbt` to  bring in the default yuiCompressorSettings:

    seq(org.scala_tools.sbt.yuiCompressor.Plugin.yuiCompressorSettings: _*)


## Usage

Once configured, `yuiCssCompressor` and `yuiJsCompressor` are registered with the list of `resourceGenerators`.
By default, this would minify the CSS and JavaScripts under `unmanagedResources` (usually, `src/main/resources`).


## Settings and Tasks

* `yui-min-suffix`: Suffix of the base of the minified files, defaults to `"-min"`.

* `yui-options`: Options passed to YUI Compressor as sequence of `Strings`. This is exactly like `scalacOptions` in SBT.

* `include-filter`: Filter for files to to be considered for compression (`".css"` for CssCompressor, `"*.js"` for JsCompressor).

* `yui-css-compressor`: CSS compressor task.

* `yui-js-compressor`: JavaScript compressor task.


## License

This software is distributed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Credits

Jon Hoffman created the original SBT plugin (for 0.7) which is [still available](https://github.com/hoffrocket/sbt-yui).
That plugin, as well as this one, is heavily influenced by David Bernard's Maven plugin available [here](https://github.com/davidB/yuicompressor-maven-plugin).
