ANTLRWorks
Version 1.0 early access 2
June 12, 2005

(c) 2005 Jean Bovet & Terence Parr
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
includes ANTLR 3, StringTemplate, XJLibrary, ANTLR 2.7.5, etc.).

To run ANTLRWorks, simply double-click on the antlrworks.jar
file.  From the command line, type "java -jar antlrworks.jar".


* ADVANCED INSTALLATION *

If you want to use another version of ANTLR 3 (like an upgrade), you
will have to expand the antlrworks.jar archive to overwrite ANTLR 3.

To expand the archive, type "jar -xj antlrworks.jar".  The following
directories will be expanded:

	antlr/...			ANTLR 2.7.5
	com/jgoodies/forms/...		JGoodies
	edu/usfca/xj/...		XJLibrary
	org/antlr/...			ANTLR v3
	org/antlr/stringtemplate/...	StringTemplate
	org/antlr/works/...		ANTLRWorks

To update ANTLR v3, remove ANTLR 3 (that is everything in "org/antlr/"
*EXCEPT* "org/antlr/stringtemplate" and "org/antlr/works"). Note that
ANTLRWorks requires the "com/jgoodies/forms" and "edu/usfca/xj"
directories.

Make sure ANTLR 3 is in your CLASSPATH and then launch ANTLRWorks
using "java org.antlr.works.IDE".

* KNOWN ISSUES *

This version disables emacs key-bindings on all OS except Mac OS X.  It
will be available in future version.


* HISTORY *

06/12/05 - version 1.0ea2
        - added a launch parameter to specify a file to open at startup
          ("-f /document/example.g")
        - improved immediate colorization for comments and new lines
        - console is hidden by default (can be changed in preferences)
        - application is closed on Linux/Windows when the last project
          window is closed and an auxiliary window is still open
          (Console, Preferences or About dialog)
        - fixed a bug where ANTLRWorks would quit on Windows/Linux if
          the last opened project was not found

06/01/05 - version 1.0ea1
         - first release to the public (no source)


* WARNING *

Per the license in LICENSE.txt, this software is not guaranteed to
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

