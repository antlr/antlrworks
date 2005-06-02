ANTLRWorks
Version 1.0 early access 1
June 1, 2005

(c) 2005 Jean Bovet & Terence Parr
University of San Francisco

* INTRODUCTION *

Welcome to ANTLRWorks! ANTLRWorks is a simple but efficient IDE for ANTLR 3 grammar. ANTLRWorks allows users to edit, visualize, interpret and debug any ANTLR 3 grammar through an easy-to-use graphical user interface.

The User's Guide is available at

	http://www.antlr.org/works/antlrworks.pdf

More information is available at

	http://www.antlr.org/works/index.html

If you have problems or think you have found a bug in ANTLRWorks, report it at

	http://www.antlr.org/misc/feedback


* INSTALLATION *

Download the complete archive antlrworks.jar at
	http://www.antlr.org/works/index.html

This archive contains everything needed to work with ANTLRWorks (it includes ANTLR 3, StringTemplate, XJLibrary, etc.).

To run ANTLRWorks, simply double-click on the antlrworks.jar file. From the command line, type "java -jar antlrworks.jar".


* ADVANCED INSTALLATION *

If you want to use another version of ANTLR 3 (like an upgrade), you will have to expand the antlrworks.jar archive to remove ANTLR 3.

To expand the archive, type "jar -xj antlrworks.jar". The following directories will be expanded:

	antlr
	com/jgoodies/forms
	edu/usfca/xj
	org/antlr/
	org/antlr/stringtemplate
	org/antlr/works

You may then remove ANTLR 3 (that is everything in "org/antlr/" except "org/antlr/stringtemplate" and "org/antlr/works"). Note that ANTLRWorks requires the "com/jgoodies/forms" and "edu/usfca/xj" directories.

Make sure ANTLR 3 is in your class-path and then launch ANTLRWorks using "java org.antlr.works.IDE".

* KNOWN ISSUE *

This version disable emacs key-bindings in all OS except Mac OS X. It will be available in future version.


* HISTORY *

06/01/05 - version 1.0ea1
	- first public release


* LICENCE *

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

