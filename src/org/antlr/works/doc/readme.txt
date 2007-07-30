ANTLRWorks
Version 1.1.1
July 29, 2007

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
	org/antlr/...			ANTLR 3.x
	org/antlr/stringtemplate/...	StringTemplate
	org/antlr/xjlib/...		XJLibrary
	org/antlr/works/...		ANTLRWorks

To update ANTLR v3, remove ANTLR 3 (that is everything in "org/antlr/"
*EXCEPT* "org/antlr/stringtemplate" and "org/antlr/works"). Note that
ANTLRWorks requires the "com/jgoodies/forms" and "org/antlr/xjlib"
directories.

Make sure ANTLR 3 is in your CLASSPATH and then launch ANTLRWorks
using "java org.antlr.works.IDE".

* WARNINGS *

- Once GraphViz installed on Windows, the 'dot' tool can usually be found
  here: c:\Program Files\ATT\Graphviz\bin\dot.exe
- This version disables emacs key-bindings on all OS except Mac OS X.
  It will be available in future version.


* HISTORY *

Version 1.1.1 - 07/29/07

Bug fix:
    [AW-98] - Fail to detect external file modification in desktop mode
    [AW-97] - Error messages are not reset when checking the grammar
    [AW-99] - Find does not wrap when reaching end/beginning of document

Version 1.1 - 07/22/07

New feature:
    [AW-94] - Ability to choose a text file for the debugger input within ANTLRWorks

Improvement:
    [AW-71] - Floating window should remember last size when detached
    [AW-82] - Add ctrl-d to "delete char under cursor" as in emacs
    [AW-85] - Double-click on the GoToRule popup now jumps to the rule
    [AW-86] - Added Visual Studio-style auto-completion menu
    [AW-87] - Double/triple click to select word + extending selection + dragging of word/line
    [AW-89] - Visual clue when the grammar is not saved when not on Mac OS X
    [AW-91] - Fix Find and Replace Dialog and add alert when reaching the beginning/end of document
    [AW-93] - Decision DFA grammar highlighting
    [AW-95] - Improve tree rendering speed on screen

Bug fix:
    [AW-76] - Export Bitmap for syntax diagram does not work
    [AW-79] - Disable CTRL-Z when debuggin in text editor (check other shortcuts also)
    [AW-80] - Yellow triangle is visible over the bottom component of the window when scrolling with the editor
    [AW-81] - Display an additional / after a complex comment
    [AW-84] - Plugin does not save the editor when Save Project is invoked

Version 1.0.2 - 06/01/07

Bug fix:
    [AW-72] - Grammar is generated and compiled each time the debugger is invoked even if nothing changed
    [AW-73] - Change ANTLR 3 template to allow the debugger port to be specified

Version 1.0.1 - 05/27/07

Bug fix:
    [AW-48] - IntelliJ 6 does not always quit after running ANTLRWorks plugin debugger
    [AW-51] - Find dialog replaces even non-matching text and performs replace with previous find value
    [AW-53] - Wrong version of ST included in the jar (2.3b10 instead of 3.0.1)
    [AW-55] - Freezes when opening the Find dialog in desktop mode on Windows
    [AW-56] - Don't change the string quote (double/single) in actions when refactoring
    [AW-57] - Creating a new rule automatically introduces always a new line
    [AW-59] - Window menu does not select the correct window if a panel is visible (like the Find panel)
    [AW-61] - AST tree node is not correctly selected when selecting input token "class" in the attached grammar
    [AW-62] - Decision does not show up for assignment ref (in rule stat)
    [AW-63] - Opening ANTLRWorks from the Mac OS X Finder by double-clicking a grammar file fails
    [AW-64] - CTRL-F10 does not work on Windows in desktop mode
    [AW-67] - There are some hard coded links to icons that prevent ANTLRWorks to launch correctly
    [AW-68] - Read-only document prevents menu shortcut to be invoked
    [AW-52] - Bring windows to the front when show() is invoked
    [AW-58] - Move the last closed document to the top of the Recent Files list
    [AW-60] - In desktop mode, the grammar text area is not focused at startup
    [AW-65] - Generate test-ring with a different port number to avoid conflict under Windows Vista
    [AW-69] - Shift-HOME or Shift-END do not select the line on Mac OS X
    [AW-50] - Merged XJLib into ANTLR project

Version 1.0 - 05/17/07

New feature:
    [AW-41] - Added a desktop-mode where all windows are in a "desktop" frame (mostly for Windows/Linux)

Bug fix:
    [AW-2] - When dialog box is closed, focus automatically to the main window
    [AW-38] - Mismatched DBEvent during debugging
    [AW-39] - Re-open all closed documents may fail to display something on Windows/Linux if no document is found
    [AW-40] - Windows position are stored in such a way in the preferences that they might cause key length issues
    [AW-24] - Make sure a window is visible in one of the available screen
    [AW-42] - Always "guess" the rules to ignore before running the interpreter
    [AW-44] - Able to generate grammar for language other than Java from within ANTLRWorks
    [AW-47] - Do not expand all subtree after they have been collapsed


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

