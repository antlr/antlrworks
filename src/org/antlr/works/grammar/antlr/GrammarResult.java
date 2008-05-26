package org.antlr.works.grammar.antlr;

import org.antlr.tool.Message;

import java.util.LinkedList;
import java.util.List;

/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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

public class GrammarResult {

    public Exception e;
    public final List<Message> errors = new LinkedList<Message>();
    public final List<Message> warnings = new LinkedList<Message>();

    public GrammarResult(Exception e) {
        this.e = e;
    }

    public GrammarResult() {
    }

    public List<Message> getErrors() {
        return errors;
    }

    public void setErrors(List<Message> errors) {
        this.errors.clear();
        if(errors != null) {
            this.errors.addAll(errors);
        }
    }

    public String getFirstErrorMessage() {
        if(getErrorCount() > 0) {
            return errors.get(0).toString();
        } else {
            return null;
        }
    }

    public List<Message> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Message> warnings) {
        this.warnings.clear();
        if(warnings != null) {
            this.warnings.addAll(warnings);
        }
    }

    public String getFirstWarningMessage() {
        if(getWarningCount() > 0) {
            return warnings.get(0).toString();
        } else {
            return null;
        }
    }

    public int getErrorCount() {
        if(errors == null) {
            return 0;
        } else {
            return errors.size();
        }
    }

    public int getWarningCount() {
        if(warnings == null) {
            return 0;
        } else {
            return warnings.size();
        }
    }

    public boolean isSuccess() {
        return getErrorCount() == 0 && getWarningCount() == 0 && e == null;
    }

    public void clear() {
        errors.clear();
        warnings.clear();
    }
}
