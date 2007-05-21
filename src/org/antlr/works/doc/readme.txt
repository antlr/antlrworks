ANTLRWorks
Version 1.0.1
May 17, 2007

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

