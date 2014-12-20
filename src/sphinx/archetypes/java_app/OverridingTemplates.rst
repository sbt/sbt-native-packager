Template Customization and Overrides
####################################

While the native packager tries to provide robust BASH/BAT scripts for your applications, they may not always be enough.
The native packager provides a mechanism where the template used to create each script can be customized or directly 
overridden. 

The easiest way to add functionality to the default script is by adding ``bashScriptExtraDefines`` :doc:`as described
in adding configuration for applications </archetypes/java_server/AddingConfiguration>`. Customizing the bash
script will effect all platform-specific builds. The server archetype provides a further level of customization for
specific System Loaders and Package types. These template file are described in 
:doc:`configuring servers </archetypes/java_server/AddingConfiguration>`.

Overriding Complete Templates
-----------------------------

In order to override full templates, like the default bash script, create a file in ``src/templates/bash-template`` 

.. code-block:: bash

    #!/usr/bin/env bash

    realpath() {
      # TODO - The original bash template has a robust mechanism to find the true
      #        path to your application, following multiple symlinks.
      #        
    }

    addJava() {
      # Here we override the original templates addJava method to do nothing,
      # since this was how we were adding configuration before.
    }

    declare -r real_script_path="$(realpath "$0")"

    # We have to provide an app_home for the default bash declarations to work.
	declare -r app_home="$(realpath "$(dirname "$real_script_path")")"

	# The auto-generated classpath relies on this variable existing
	# and pointing at the lib directory.
    declare -r lib_dir="$(realpath "${app_home}/../lib")"

    # This line tells the native packager template engine to inject
    # all of its settings into this spot in the bash file.
    ${{template_declares}}

    # Here we make use of two of the injected settings for the bash file:
    # * app_classpath - represents the full list of JARs for this applciation.
    # * app_mainclass - represents the class with a main method we should call.
    exec java -cp $app_classpath $app_mainclass $@


Similarly the windows BAT template can be overridden by placing a new template in ``src/templates/bat-template``

.. code-block:: bat

    @REM A bat starter script
    @echo off

    @REM Here we need to set up a "home" variable for our classpath.
    @REM The APP_ENV_NAME variable is replaced by the packager template engine
    @REM with an "environment variable friendly" name for the app.
    if "%@@APP_ENV_NAME@@_HOME%"=="" set "@@APP_ENV_NAME@@_HOME=%~dp0\\.."
    set "APP_LIB_DIR=%@@APP_ENV_NAME@@_HOME%\lib\"

    @REM - This tells the template engine to inject any custom defines into our bat file here.
    @@APP_DEFINES@@

    @REM - Here we use the provided APP_CLASSPATH and APP_MAIN_CLASS parameters
    java -cp "%APP_CLASSPATH%" %APP_MAIN_CLASS% %*


While we just replaced the default templates with simpler templates, this should really only be done if:

1. There is a bug in one of the script templates you need to workaround
2. There is a deficiency in the features of one of the templates you need to fix.

In general, the templates are intended to provide enough utility that customization is only necessary for truly custom scripts.

Next, let's look at how to :doc:`document the application <WritingDocumentation>`.
