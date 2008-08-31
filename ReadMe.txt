
***** Bugs
http://www.antlr.org:8888/browse/AW

***** Release check-list
- make sure ANTLRWorks is compiled against the correct version of ANTLR and ST sources
- update the ANTLR and ST jar files in main/lib
- change version number (and date when it applies) into these files:
    - main/build.properties
    - main/ant-for-source/build.properties
    - main/src/org/antlr/works/properties/strings.properties
    - main/plugin/src/org/antlr/works/plugin/properties/strings.properties
    - main/plugin/src/org/antlr/works/plugin/intellij/META-INF/plugin.xml
- update history file in main/History
- update online files (ask Terence for the path):
    - doc/readme.txt
    - doc/release.txt
    - update.xml (used by AW to get notified when a new version is available)
    - update_osx.xml (same but for AW running on Mac OS X)
    - index.html (version, links)
- build ANTLRWorks by running ant on the main build file:
    $ cd main
    $ ant
- verify the following in main/dist folder:
    - file versions are correct
    - jar file is running fine
    - OS X application is launching fine
- upload files online:
    - antlrworks-1.2.zip
    - antlrworks-1.2-src.zip
    - antlrworks-1.2.jar
- update the plugin on http://plugins.intellij.net/
- update JIRA:
    - close fixed issues
    - perform the release

***** Tools
http://pmd.sourceforge.net/
http://findbugs.sourceforge.net/

***** Info
Enable project document: set prefs PREF_PROJET_DOCUMENT to true
DOT: /Applications/Graphviz.app/Contents/MacOS/dot
JavaC Windows Classpath example: C:\\Program Files\\Java\\jdk1.5.0_03\\bin\\javac
SD path width: modify constants in GPath and GPathGroup
Windows Prefs:
    - Command "regedit.exe"
    - HKEY_CURRENT_USER\Software\JavaSoft\Prefs\org\antlr\works

***** Debugging

To debug plugin in IntelliJ:
    - set flag org.antlr.works.debug.plugin to true: -Dorg.antlr.works.debug.plugin=true

Problem with syntax diagram path?
Look into:
- GGraphGroup (where the path is created)
- FAFactory (where the optimized FA is created)
- GFactory (where everything is assembled before display)
