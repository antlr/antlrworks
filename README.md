# ANTLRWorks

## Release check-list
- make sure all the bugs are resolved in http://www.antlr.org/jira/browse/AW
- make sure ANTLRWorks is compiled against the correct version of ANTLR and ST sources
- update the ANTLR and ST jar files in main/lib
- change version number (and date when it applies) into these files:
    - main/build.properties
    - main/resources/properties/strings.properties
    - main/plugin/src/org/antlr/works/plugin/properties/strings.properties
- update history in:
    - main/History
- update online files (ask Terence for the path):
    - index.html
    - update.xml and such files for new versions
    - push release notes and such to doc dir
- build ANTLRWorks by running ant on the main build file:
    $ cd main
    $ ant
- verify the following in main/dist folder:
    - file versions are correct
    - jar file is running fine
    - OS X application is launching fine
- upload files online:
    - antlrworks-1.x.zip
    - antlrworks-1.x-src.zip
    - antlrworks-1.x.jar
- branch the release in p4 (main -> release/1.x)    

## Tools
http://pmd.sourceforge.net/
http://findbugs.sourceforge.net/

## Info
Enable project document: set prefs PREF_PROJET_DOCUMENT to true
DOT: /Applications/Graphviz.app/Contents/MacOS/dot
JavaC Windows Classpath example: C:\\Program Files\\Java\\jdk1.5.0_03\\bin\\javac
SD path width: modify constants in GPath and GPathGroup
Windows Prefs:
    - Command "regedit.exe"
    - HKEY_CURRENT_USER\Software\JavaSoft\Prefs\org\antlr\works

## Debugging

Problem with syntax diagram path?
Look into:
- GGraphGroup (where the path is created)
- FAFactory (where the optimized FA is created)
- GFactory (where everything is assembled before display)

Join the chat at https://gitter.im/antlr/antlrworks
