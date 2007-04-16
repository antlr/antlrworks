ANTLRWorks
Version 1.0b10
April 15, 2007

(c) 2005-2007 Jean Bovet & Terence Parr
University of San Francisco

* INTRODUCTION *

Welcome to ANTLRWorks!  ANTLRWorks is a simple but efficient IDE for
ANTLR 3 grammars.  ANTLRWorks allows users to edit, visualize,
interpret and debug any ANTLR 3 grammar through an easy-to-use
graphical user interface.

An Online Help is available at

	http://www.antlr.org/works/help/index.html

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

Version 1.0b10 - 04/15/07

- requires Java 1.5
- can now display the line numbers in the grammar editor
- simple print capability
- ability to debug again (after having launched the debugger once in the current session)
- syntax coloring parser has been rewritten
- ability to jump to tokens defined in the options block
- reverted file extension to *.g only
- Mac OS X document icon (thanks Abe)
- save the last used path in the open/save dialogs
- minor UI and table enhancement
- fixed a bug when double-clicking a word: selection would be incorrect
- fixed a bug when trying to generate a parser/lexer from a tree grammar (would loop forever)

Bug fixes:
[AW-1] - Rename fails to rename reference inside rewrite rule
[AW-5] - Tree highlight in Windows is too dark
[AW-6] - Incorrect syntax error with double-quoted string inside ST function
[AW-7] - Invalid syntax error for double-quoted string in ST
[AW-9] - Null pointer exception when displaying the DFA for the decision in rule a
[AW-19] - Make the personal info dialog optional
[AW-22] - AST node not found in specific situation
[AW-23] - Debugger panels are incorrectly resized when a split pane divider is moved


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

