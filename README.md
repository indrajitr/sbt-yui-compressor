# SBT YUI Compressor Plugin

sbt-yui-compressor is an [SBT][1] plugin for [YUI Compressor][2] to minify CSS and JavaScript.


## Pre-requisite

sbt-yui-compressor requires SBT version 0.11.2 or newer.


## Installation

Include pre-compiled binary form of the plugin in your plugin definition list by adding the following in `project/plugin.sbt` (per project) or `~/.sbt/plugins/plugin.sbt` (global):

```scala
// Add SBT plugin repository to the list of resolvers (not necessary for SBT 0.12 onwards)
resolvers += Resolver.url("sbt-plugin-releases", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
addSbtPlugin("in.drajit.sbt" % "sbt-yui-compressor" % "0.2.0")
```

Alternately, include source form of the plugin in your plugin definition list by pointing to sbt-yui-compressor's GitHub repository url as dependency in `project/project/build.scala`:

```scala
import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(yuiPlugin)
  lazy val yuiPlugin = uri("git://github.com/sbt/sbt-yui-compressor#v0.2.0")
}
```


## Usage

Once installed, add the following to your light build definition (`build.sbt`) and configure with the default `yuiSettings`:

    seq(yuiSettings: _*)

Alternately, if you are using the full build definition (`project/build.scala`), you can configure the plugin as so:

```scala
import sbt._

object BuildDef extends Build {
  lazy val myproject = Project("myproject", file(".")) settings(yuiCompressor.Plugin.yuiSettings: _*)
}
```

From here onwards, `yuiCssCompressor` and `yuiJsCompressor` are registered with the list of `resourceGenerators`. By default, they would minify CSS files and JavaScript files respectively available under `unmanagedResourceDirectories` (usually, `src/main/resources` by default).


## Settings and Tasks

* `yui-min-suffix`: Suffix of the base of the minified files.
    * Default value: `"-min"`.
    * Alternatives:

        ```scala
        // Use suffix "-minified"
        YuiCompressorKeys.minSuffix := "-minified"
        ```

* `yui-options`: Options passed to YUI Compressor as sequence of `Strings`. This is follows the convention of setting up `scalacOptions` in SBT. See [YUI Compressor documentation][3] for the full set of options available. The relevant options are conveniently available wrapped in `yuiCompressor.Opts` (see `src/main/scala/Opts.scala`).
    * Default value: `Nil`
    * Alternatives:

        ```scala
        // Insert a line break after column number 100 (for both CSS and JS files)
        YuiCompressorKeys.options ++= yuiCompressor.Opts.lineBreak(100)

        // Do not obfuscate JS files, just minify them
        YuiCompressorKeys.options in YuiCompressorKeys.jsCompressor += yuiCompressor.Opts.js.nomunge
        ```

* `include-filter`: Filter for files to to be considered for compression.
    * Default value: `".css"` for CssCompressor, `"*.js"` for JsCompressor
    * Alternatives:

        ```scala
        // Consider "*.javascript" files for compression as well in "Compile" scope
        includeFilter in (Compile, YuiCompressorKeys.jsResources) := "*.js" | "*.javascript"

        // Consider "*.javascript" files for compression as well in "Test" scope
        includeFilter in (Test, YuiCompressorKeys.jsResources) := "*.js" | "*.javascript"
        ```

* `exclude-filter`: Filter for files to be excluded from compression.
    * Default value: `excludeFilter in unmanagedResources`

* `unmanaged-resource-directories`: Source directories of CSS and JS files to be cosidered.
    * Default value: `unmanagedResourceDirectories` (`sourceDirectory / "resources"`)
    * Alternatives:

        ```scala
        // Consider "src/main/js" in addition to "src/main/resources" directory in "Compile" scope
        unmanagedResourceDirectories in (Compile, YuiCompressorKeys.jsResources) <+= sourceDirectory / "js"
        ```

* `resource-managed`: Destination directory of the minified CSS and JS files.
    * Default value: `resourceManaged` (`crossTarget / "resource_managed"`)
    * Alternatives:

        ```scala
        // Keep the minified JS files in "target/scala_{scala.version}/resource_managed_js" instead in "Test" scope
        resourceManaged in (Test, YuiCompressorKeys.jsCompressor) <<= crossTarget / "resource_managed_js"
        ```

* `yui-css-compressor`: CSS compressor task added to `resourceGenerators` task-list.

* `yui-js-compressor`: JavaScript compressor task added to `resourceGenerators` task-list.


## License

This software is distributed under [Apache License, Version 2.0][6].


## Credits

Jon Hoffman created the original SBT plugin (for 0.7) which is [still available][4].
That plugin, as well as this one, are in turn influenced by [David Bernard's Maven plugin][5].


[1]: http://github.com/harrah/xsbt
[2]: http://developer.yahoo.com/yui/compressor
[3]: http://github.com/yui/yuicompressor/blob/master/doc/README
[4]: http://github.com/hoffrocket/sbt-yui
[5]: http://github.com/davidB/yuicompressor-maven-plugin
[6]: http://www.apache.org/licenses/LICENSE-2.0.txt
