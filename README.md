# SBT YUI Compressor Plugin

sbt-yui-compressor is a [SBT](https://github.com/harrah/xsbt) plugin for [YUI Compressor](http://developer.yahoo.com/yui/compressor/) to minify CSS and JavaScript.


## Pre-requisite

sbt-yui-compressor requires SBT 0.11.1 or newer.


## Installation

Include pre-compiled binary form of the plugin in your plugin definition list by adding the following in `project/plugin.sbt` (per project) or `~/.sbt/plugins/plugin.sbt` (global):

```scala
// Add SBT plugin repository to the list of resolvers (not necessary for SBT 0.12 onwards)
resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
addSbtPlugin("in.drajit.sbt" % "sbt-yui-compressor" % "0.2")
```

Alternately, include source form of the plugin in your plugin definition list by pointing to sbt-yui-compressor's git repository url as dependency in `project/plugins/project/build.scala`:

```scala
import sbt._

object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(yuiPlugin)
  lazy val yuiPlugin = uri("git://github.com/sbt/sbt-yui-compressor")
}
```


## Usage

Once installed, add the following to your light build definition (`build.sbt`) and configure with the default `yuiSettings`:

    seq(yuiSettings: _*)

Alternately, if you are using the full build definition (`project/build.scala`), you can configure the plugin as so:

```scala
import sbt._

object BuildDef extends Build {
  lazy val myproject = Project("myproject", file(".")) settings(in.drajit.sbt.yuiCompressor.Plugin.yuiSettings: _*)
}
```

From hereon, `yuiCssCompressor` and `yuiJsCompressor` are registered with the list of `resourceGenerators`.
By default, this would minify the CSS and JavaScripts available under `unmanagedResourceDirectories` (usually, `src/main/resources`).


## Settings and Tasks

* `yui-min-suffix`: Suffix of the base of the minified files.
    * Default value: `"-min"`.
    * Alternatives:

        ```scala
        // Use suffix "-minified"
        `YuiCompressorKeys.minSuffix := "-minified"`
        ```

* `yui-options`: Options passed to YUI Compressor as sequence of `Strings`. This is follows the convention of setting up `scalacOptions` in SBT. See [YUI Compressor documentation](https://github.com/yui/yuicompressor/blob/master/doc/README) for the full set of options available.
    * Default value: `Nil`
    * Alternatives:

        ```scala
        // Insert a line break after column number 100 (for both CSS and JS files)
        YuiCompressorKeys.options ++= Seq("--line-break", 100)`

        // Do not obfuscate JS files, just minify them
        YuiCompressorKeys.options in YuiCompressorKeys.jsCompressor += "--nomunge"`
        ```

* `include-filter`: Filter for files to to be considered for compression.
    * Default value: `".css"` for CssCompressor, `"*.js"` for JsCompressor
    * Alternatives:

        ```scala
        // Consider "*.javascript" files for compression as well
        YuiCompressorKeys.includeFilter in YuiCompressorKeys.jsResources := "*.js" | "*.javascript"
        ```

* `exclude-filter`: Filter for files to be excluded from compression.
    * Default value: `excludeFilter in unmanagedResources`

* `unmanaged-resource-directories`: Source directories of CSS and JS files to be cosidered.
    * Default value: `unmanagedResourceDirectories` (`sourceDirectory / "resources"`)
    * Alternatives:

        ```scala
        // Consider "src/main/js" in addition to "src/main/resources" directory when in "Compile" scope
        unmanagedResourceDirectories in (Compile, YuiCompressorKeys.jsResources) <+= sourceDirectory / "js"
        ```

* `resource-managed`: Destination directory of the minified CSS and JS files.
    * Default value: `resourceManaged` (`crossTarget / "resource_managed"`)
    * Alternatives:

        ```scala
        // Keep the minified JS files in "target/scala_{scala.version}/resource_managed_js" instead when in "Test" scope
        resourceManaged in (Test, YuiCompressorKeys.jsCompressor) <<= crossTarget / "resource_managed_js"
        ```

* `yui-css-compressor`: CSS compressor task.

* `yui-js-compressor`: JavaScript compressor task.


## License

This software is distributed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).


## Credits

Jon Hoffman created the original SBT plugin (for 0.7) which is [still available](https://github.com/hoffrocket/sbt-yui).
That plugin, as well as this one, are in turn influenced by [David Bernard's Maven plugin](https://github.com/davidB/yuicompressor-maven-plugin).
