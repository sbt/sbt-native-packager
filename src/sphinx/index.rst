.. raw:: html

  <div class="jumbotron" style="margin-top:-20px">
          <h1>SBT Native Packager Plugin</h1>
          <p>This sbt plugin provides you with everything you need to package your application.
          No matther if you want to build a simple standalone application or a server application.
          The JVM let's you run anywhere. SBT Native Packager let's you deploy everywhere!</p>
          <p><a class="btn btn-primary btn-lg" href="gettingstarted.html" role="button">Getting Started »</a></p>
      </div>

      <!-- Example row of columns -->
      <div class="row">
        <div class="col-md-4">
          <h2>Installation</h2>
          <p>Add the plugin to your <em>plugins.sbt</em>. If you use sbt <em>0.13.5</em> 
          or higher the you have just one line to add to your <em>build.sbt</em>:</p>
          <pre>enablePlugins(SbtNativePackager)</pre>
          <p>We provide a set of plugins. One for each supported format and for each archetype.
          Just select the one you want to use and all other plugins you require are loaded
          automatically.<br>&nbsp;</p>
          <p><a class="btn btn-default" href="gettingstarted.html" role="button">Getting Started »</a></p>
        </div>
        <div class="col-md-4">
          <h2>Archetypes</h2>
          <p>For common use cases we create so called <em>Archetypes</em>. For a standalone
          application enabling is as simple 
          simple as </p>
          <pre>enablePlugins(JavaAppPackaging)</pre>
          <p>
          The most common archtypes
          are <code>JavaAppPackaging</code> and <code>JavaServerAppPackaging</code>.
          Each archetype adds possible new settings which you can adapt to your need.<br>&nbsp;</p>
          <p><a class="btn btn-default" href="#" role="button">Learn more »</a></p>
       </div>
        <div class="col-md-4">
          <h2>Formats & Customize</h2>
          <p>SBT Native Packager comes with a rich set of packaging formats including
          <em>zip</em>, <em>tar.gz</em>, <em>debian</em>, <em>rpm</em>, <em>msi</em> and <em>docker</em>.
          It's as easy as: </p>
          <pre>sbt debian:packageBin</pre>
          <p>An archetype doesn't cover what you need? No problem. SBT Native Packager i
          build on top of some simple principles and you can customize it in many ways.
          Adding a custom packaging format or some special files.</p>
          <p><a class="btn btn-default" href="#" role="button">Learn more »</a></p>
        </div>
    </div>
  

