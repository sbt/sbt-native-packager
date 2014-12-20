Contributing Documentation
##########################

You can clone the `sbt-native-packager GitHub repo <https://github.com/sbt/sbt-native-packager>`_ and modify
the `rst files <http://sphinx-doc.org/rest.html>`_ in the 
`src/sphinx/ <https://github.com/sbt/sbt-native-packager/tree/master/src/sphinx>`_ directory. 

The site is generated with the `sbt-site <https://github.com/sbt/sbt-site>`_ plugin. You'll need the following
dependencies installed:

* python-sphinx
* python-pip

and the ``sphinx_bootstrap_theme`` library available via pip: ::

    pip install sphinx_bootstrap_theme


See `the sbt-native-packager developer guide <https://github.com/sbt/sbt-native-packager/wiki/Developer-Guide#documentation>`_
for more information.
