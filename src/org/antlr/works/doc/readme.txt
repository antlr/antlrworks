ANTLRWorks
Version 1.0b7
December 03, 2006

(c) 2005-2006 Jean Bovet & Terence Parr
University of San Francisco

* INTRODUCTION *

Welcome to ANTLRWorks!  ANTLRWorks is a simple but efficient IDE for
ANTLR 3 grammars.  ANTLRWorks allows users to edit, visualize,
interpret and debug any ANTLR 3 grammar through an easy-to-use
graphical user interface.  This is an early-access (no source) release
and so you should not expect ANTLRWorks to work at this point.

The User's Guide is available at

	http://www.antlr.org/works/antlrworks.pdf

More information is available at

	http://www.antlr.org/works/index.html

If you have problems or think you have found a bug in ANTLRWorks,
report it at

	http://www.antlr.org/misc/feedback


* INSTALLATION *

Download the complete antlrworks.jar at

	http://www.antlr.org/works/index.html

This archive contains everything needed to work with ANTLRWorks (it
includes ANTLR 3.x, StringTemplate, XJLibrary, ANTLR 2.x, etc.).

To run ANTLRWorks, simply double-click on the antlrworks.jar
file.  From the command line, type "java -jar antlrworks.jar".


* ADVANCED INSTALLATION *

If you want to use another version of ANTLR 3 (like an upgrade), you
will have to expand the antlrworks.jar archive to overwrite ANTLR 3.

To expand the archive, type "jar -xj antlrworks.jar".  The following
directories will be expanded:

	antlr/...			ANTLR 2.x
	com/jgoodies/forms/...		JGoodies
	edu/usfca/xj/...		XJLibrary
	org/antlr/...			ANTLR 3.x
	org/antlr/stringtemplate/...	StringTemplate
	org/antlr/works/...		ANTLRWorks

To update ANTLR v3, remove ANTLR 3 (that is everything in "org/antlr/"
*EXCEPT* "org/antlr/stringtemplate" and "org/antlr/works"). Note that
ANTLRWorks requires the "com/jgoodies/forms" and "edu/usfca/xj"
directories.

Make sure ANTLR 3 is in your CLASSPATH and then launch ANTLRWorks
using "java org.antlr.works.IDE".

* WARNINGS *

- Once GraphViz installed on Windows, the 'dot' tool can usually be found
  here: c:\Program Files\ATT\Graphviz\bin\dot.exe
- This version disables emacs key-bindings on all OS except Mac OS X.
  It will be available in future version.


* HISTORY *

12/03/06 - Version 1.0b7

- handles now the "package" statement found in the @header block (both parser and lexer)
- new idea to convert invalid grammar name
- prefs: auto-indent on colon in rule can be disabled now
- interpreter now correctly identify rules to ignore when using skip() in action
- display the read/write state of each grammar document (disable editing if grammar is read-only)

- fixed the default size of the preferences window that was too small
- fixed various end of line problem on Windows causing syntax coloring and debugger token highlighting to fail
- fixed a bug where the AST/Parse tree contextual menu would not work when the focus was not in the panel
- fixed a bug where generating code for a read-only grammar could result in an infinite loop
- fixed a bug where the window size/position would not be recorded when AW is closed
- fixed a bug where an editor thread would not be stopped when a document was closed
- fixed a bug where the end of line pop-up menu in the debugger input dialog would be populated twice
- fixed a bug in the interpreter where the "guess" button would not work properly
- fixed a bug in the debugger where the red cursor would not be displayed on Java 1.4

11/18/06 - Version 1.0b6

- added line endings preferences in debugger input text dialog and interpreter panel
- read now the token vocab file from the output path (in addition to the default grammar location)
- output path is now specified in the preferences
- alert after checking grammar
- added shift-delete to behave like delete when typing with the shift key pressed

- fixed a bug when storing strings greated than 8KB (it is now truncated and an alert is displayed)
- fixed a bug that prevented some DFA diagram from being completely displayed (scrollable zone too small)
- fixed a bug in the syntax coloring parser when a multi-line comment where located at the end of file
- fixed a bug where some menu items were not disabled in debug mode
- fixed a bug where AST node where not correctly added when a node became parent (nil node)
- fixed a bug where errors in the grammar where not reported when checking or generating the grammar

10/22/06 - Version 1.0b5

- allow traversal of bundle on Mac OS X when choosing files
- fixed bug in syntax coloring with escape character
- plugin: now clear the read-only status


* WARNING *

Per the license in license.txt, this software is not guaranteed to
work and might even destroy all life on this planet:

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

