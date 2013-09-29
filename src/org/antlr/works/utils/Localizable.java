/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
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

package org.antlr.works.utils;

import org.antlr.xjlib.appkit.utils.XJLocalizable;

public class Localizable {

    public static final String PROPERTIES_FILE = "strings";

    public static final String DOCUMENT_TYPE = "GrammarDocumentType";

    public static final String APP_NAME = "AppName";

    public static final String SPLASH_INFO = "SplashInfo";
    public static final String SPLASH_VERSION = "SplashVersion";
    public static final String SPLASH_COPYRIGHT = "SplashCopyright";

    public static final String UPDATE_XML_URL = "UpdateXMLURL";
    public static final String UPDATE_OSX_XML_URL = "UpdateOSXXMLURL";
    public static final String FEEDBACK_URL = "FeedbackURL";
    public static final String DOCUMENTATION_URL = "DocumentationURL";

    public static String getLocalizedString(String key) {
        return XJLocalizable.getString(PROPERTIES_FILE, key);
    }

}
