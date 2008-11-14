package org.antlr.works.test;
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

public interface TestConstants {
    String PREFIX = "../files/";

    String TEST = PREFIX+"test.g";
    String REFERENCES = PREFIX+"references.g";
    String BLOCKS = PREFIX+"blocks.g";

    String REFACTOR_PREFIX = PREFIX+"refactor/";
    String REFACTOR_ORIGINAL_A = REFACTOR_PREFIX+"original_a.g";
    String REFACTOR_ORIGINAL_B = REFACTOR_PREFIX+"original_b.g";
    String RENAME_BAR = REFACTOR_PREFIX+"rename_bar.g";
    String RENAME_OTHER = REFACTOR_PREFIX+"rename_other.g";
    String RENAME_B = REFACTOR_PREFIX+"rename_b.g";

    String MANTRA = PREFIX+"mantra/mantra.g";
    String CODE_GEN_PHASE = PREFIX+"mantra/CodeGenPhase.g";
    String RESOLVE_PHASE = PREFIX+"mantra/ResolvePhase.g";
    String SEMANTIC_PHASE = PREFIX+"mantra/SemanticPhase.g";
}
