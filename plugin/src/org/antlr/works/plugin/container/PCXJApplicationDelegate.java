package org.antlr.works.plugin.container;

import org.antlr.xjlib.appkit.app.XJApplicationDelegate;
import org.antlr.works.IDE;
import org.antlr.works.utils.Localizable;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

public class PCXJApplicationDelegate extends XJApplicationDelegate {

/*    public void customizeFileMenu(XJMenu menu) {
        editor.customizeFileMenu(menu);
    }

    public void customizeEditMenu(XJMenu menu) {
        editor.customizeEditMenu(menu);
    }

    public void customizeWindowMenu(XJMenu menu) {
        editor.customizeWindowMenu(menu);
    }

    public void customizeHelpMenu(XJMenu menu) {
    }*/

    public Class appPreferencesClass() {
        return IDE.class;
    }

    public String appVersionShort() {
        return Localizable.getLocalizedString(Localizable.APP_VERSION_SHORT);
    }

    public String appVersionLong() {
        return Localizable.getLocalizedString(Localizable.APP_VERSION_LONG);
    }

}
