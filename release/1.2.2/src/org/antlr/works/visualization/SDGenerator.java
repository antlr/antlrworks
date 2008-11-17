package org.antlr.works.visualization;

import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.engine.GrammarEngine;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.prefs.AWPrefs;
import org.antlr.works.visualization.graphics.*;
import org.antlr.works.visualization.graphics.graph.GGraph;
import org.antlr.works.visualization.serializable.SEncoder;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;
import org.antlr.xjlib.foundation.XJUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/*

[The "BSD licence"]
Copyright (c) 2005-2007 Jean Bovet
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

public class SDGenerator implements GContextProvider {

    public GrammarEngine engine;
    public GContext context;

    public SDGenerator(GrammarEngine engine) {
        this.engine = engine;

        context = new GContext();
        context.setSkin(new SDSkin());
        context.setProvider(this);
    }

    public void serializeRule(String name, SEncoder encoder) throws Exception {
        GGraph graph = createGraph(name);
        encoder.write(graph);
    }

    public void renderRuleToEPSFile(String ruleName, String file) throws Exception {
        GGraph graph = createGraph(ruleName);
        GEnginePS engine = new GEnginePS();
        context.setEngine(engine);
        graph.draw();
        XJUtils.writeStringToFile(engine.getPSText(), file);
    }

    public void renderRuleToBitmapFile(String ruleName, String imageFormat, String file) throws Exception {
        GGraph graph = createGraph(ruleName);

        int width = (int)(graph.getWidth()+1);
        int height = (int)(graph.getHeight()+1);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D)image.getGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        context.setEngine(new GEngineGraphics());
        context.setGraphics2D(g2d);

        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);
        graph.draw();
        g2d.dispose();

        ImageIO.write(image, imageFormat, new File(file));
    }

    private GGraph createGraph(String ruleName) throws Exception {
        GGraph graph = new GFactory().buildGraphsForRule(engine.getANTLRGrammarEngine(), ruleName);
        graph.setContext(context);
        graph.render(0,0);
        return graph;
    }

    public Color contextGetColorForLabel(String label) {
        if(label.charAt(0) == '\'' || label.charAt(0) == '"') {
            return AWPrefs.getSyntaxColor(AWPrefs.PREF_SYNTAX_STRING);
        } else {
            if(ATEToken.isLexerName(label))
                return GrammarSyntaxEngine.COLOR_LEXER;
            else
                return GrammarSyntaxEngine.COLOR_PARSER;
        }
    }

}


