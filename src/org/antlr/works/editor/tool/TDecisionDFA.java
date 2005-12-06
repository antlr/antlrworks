package org.antlr.works.editor.tool;

import edu.usfca.xj.appkit.gview.GView;
import edu.usfca.xj.appkit.gview.base.Vector2D;
import edu.usfca.xj.appkit.gview.object.GElement;
import edu.usfca.xj.appkit.gview.object.GElementCircle;
import edu.usfca.xj.appkit.gview.object.GLink;
import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.analysis.DFA;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorTab;
import org.antlr.works.editor.EditorWindow;
import org.antlr.works.parser.Lexer;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.parser.Token;
import org.antlr.works.visualization.grammar.GrammarEngine;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
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

public class TDecisionDFA implements Runnable, EditorTab {

    protected EditorWindow editor;
    protected TDecisionDFADelegate delegate;

    protected JPanel panel;
    protected GView dfaView;

    protected String text;
    protected int line;
    protected int column;

    public int decisionNumber;
    public String ruleName;

    protected String tempDOTFile;
    protected String tempTextFile;
    protected String tempImageFile;

    protected String error;

    public TDecisionDFA(EditorWindow editor, TDecisionDFADelegate delegate) {
        this.editor = editor;
        this.delegate = delegate;
    }

    public void setRuleName(String name) {
        this.ruleName = name;
    }

    public Container getContainer() {
        return panel;
    }

    public String getDOTToolPath() {
        return EditorPreferences.getDOTToolPath();
    }

    public String getEPS() {
        return dfaView.getEPS();
    }

    public BufferedImage getImage() {
        return dfaView.getImage();
    }

    public boolean launch() {
        if(getDOTToolPath() == null) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the decision DFA because the DOT tool path is not defined.\n"+
                    "It can be defined in the Preferences.");
            return false;
        }
        if(!new File(getDOTToolPath()).exists()) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the decision DFA because the DOT tool does not exist at the specified path.\n" +
                    "Check the tool path in the Preferences.");
            return false;
        }

        new Thread(this).start();
        return true;
    }

    public Token findClosestDecisionToken() {
        Token ct = editor.getCurrentToken();
        List tokens = editor.getTokens();
        int nestedParen = 0;
        for(int index=tokens.indexOf(ct); index >= 0; index--) {
            Token t = (Token)tokens.get(index);
            if(t.type == Lexer.TOKEN_COLON)
                return t;
            else if(t.type == Lexer.TOKEN_RPAREN)
                nestedParen++;
            else if(t.type == Lexer.TOKEN_LPAREN) {
                if(nestedParen == 0)
                    return t;
                else
                    nestedParen--;
            }
        }
        return null;
    }

    public void run() {
        text = editor.getText();

        Token t = findClosestDecisionToken();
        if(t == null) {
            line = editor.getCurrentLinePosition();
            column = editor.getCurrentColumnPosition();
        } else {
            line = editor.getLinePositionAtIndex(t.getStartIndex());
            column = editor.getColumnPositionAtIndex(t.getStartIndex());
            editor.setCaretPosition(t.getStartIndex());
        }
        error = null;

        try {
            tempDOTFile = File.createTempFile("decisionDFA", ".dot").getAbsolutePath();
            tempTextFile = File.createTempFile("decisionDFA", ".txt").getAbsolutePath();
            tempImageFile = File.createTempFile("decisionDFA", ".png").getAbsolutePath();

            createInterface(generate());
        } catch(Exception e) {
            error = e.getLocalizedMessage();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                delegate.decisionDFADidCompleted(TDecisionDFA.this, error);
            }
        });

        new File(tempDOTFile).delete();
        new File(tempTextFile).delete();
        new File(tempImageFile).delete();
    }

    private void createInterface(GElement graph) {
        panel = new JPanel(new BorderLayout());

        dfaView = new GView();
        dfaView.setAutoAdjustSize(true);
        dfaView.setRootElement(graph);
        dfaView.setBackground(Color.white);
        dfaView.setDrawBorder(false);

        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Zoom"));
        b.add(createZoomSlider());

        panel.add(b, BorderLayout.NORTH);
        panel.add(new JScrollPane(dfaView), BorderLayout.CENTER);
    }

    private JSlider createZoomSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(1);
        slider.setMaximum(800);
        slider.setValue(100);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();

                dfaView.setZoom((float)slider.getValue()/100);
                dfaView.repaint();

                // Let the JScrollPane know that the dfaView
                // may have changed size
                dfaView.revalidate();
            }
        });
        return slider;
    }

    public GElement generate() throws Exception {
        generateDOTFile();
        generatePlainTextFile();
        return generateGraph();
    }

    private void generateDOTFile() throws Exception {
        Grammar g = new Grammar(text);
        g.createNFAs();

        ParserRule rule = editor.getCurrentRule();
        if(rule != null && rule.lexer) {
            g = GrammarEngine.createLexerGrammar(editor.console, g);
            g.createNFAs();
        }
        g.createLookaheadDFAs();

        List columns = g.getLookaheadDFAColumnsForLineInFile(line);
        int adjustedColumn = -1;
        for(int index = columns.size()-1; index >=0; index--) {
            Integer match = (Integer)columns.get(index);
            if(match.intValue() <= column) {
                adjustedColumn = match.intValue();
                break;
            } else if(index == 0)
                adjustedColumn = match.intValue();
        }

        if(adjustedColumn == -1)
            throw new Exception("No decision in the current line");

        DFA dfa = g.getLookaheadDFAFromPositionInFile(line, adjustedColumn);
        decisionNumber = dfa.getDecisionNumber();
        DOTGenerator dg = new DOTGenerator(g);
        dg.setArrowheadType("none");
        String dot = dg.getDOT( dfa.startState );
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempDOTFile));
        bw.write(dot);
        bw.close();
    }

    public void generatePlainTextFile() throws Exception {
        String[] args = new String[] { getDOTToolPath(), "-Tplain", "-o", tempTextFile, tempDOTFile };
        Process p = Runtime.getRuntime().exec(args);

        new StreamWatcher(p.getErrorStream(), "DecisionDFA").start();
        new StreamWatcher(p.getInputStream(), "DecisionDFA").start();

        p.waitFor();
    }

    public GElement generateGraph() {
        /*
graph 1.000 2.583 9.056
        */
        GElement graph = null;
        float height = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(tempTextFile));
            String line;
            while((line = br.readLine()) != null) {
                GElement element = null;
                String[] tokens = parseTokens(line);
                if(tokens[0].equals("graph")) {
                    height = Float.parseFloat(tokens[3]);
                } else if(tokens[0].equals("node"))
                    element = createGraphNode(tokens, height);
                else if(tokens[0].equals("edge"))
                    element = createGraphEdge(graph, tokens, height);
                else if(tokens[0].equals("stop"))
                    break;

                if(element != null) {
                    if(graph == null)
                        graph = element;
                    else
                        graph.addElement(element);
                }
            }
        } catch (IOException e) {
            editor.console.print(e);
        }
        return graph;
    }

    public String[] parseTokens(String line) {
        List tokens = new ArrayList();
        try {
            StreamTokenizer st = new StreamTokenizer(new StringReader(line));
            st.parseNumbers();
            int token = st.nextToken();
            while(token != StreamTokenizer.TT_EOF) {
                String element = null;
                switch(token) {
                    case StreamTokenizer.TT_NUMBER:
                        element = String.valueOf(st.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        element = st.sval;
                        break;
                    case '"':
                    case '\'':
                        element = st.sval;
                        break;
                    case StreamTokenizer.TT_EOL:
                        break;
                    case StreamTokenizer.TT_EOF:
                        break;
                    default:
                        element = String.valueOf((char)st.ttype);
                        break;
                }
                if(element != null)
                    tokens.add(element);
                token = st.nextToken();
            }
        } catch(Exception e) {
            editor.console.print(e);
            return null;
        }
        String[] result = new String[tokens.size()];
        for(int index=0; index<tokens.size(); index++)
            result[index] = (String)tokens.get(index);
        return result;
    }

    public GElement createGraphNode(String[] tokens, float height) {
        /*
node s0  1.097 1.917 0.506 0.506 s0 solid circle black lightgrey
node "s1=>2"  0.486 0.486 0.969 0.969 "s1=>2" solid doublecircle black lightgrey
        */

        float x = Float.parseFloat(tokens[2])*80;
        float y = (height - Float.parseFloat(tokens[3]))*80;
        float w = Float.parseFloat(tokens[4])*80;
        //float h = Float.parseFloat(tokens[5])*30;

        Node node = new Node();
        node.setDraggable(true);
        node.setPosition(x, y);
        node.setRadius(w/2);
        node.setLabel(tokens[6]);
        node.setDouble(tokens[8].equals("doublecircle"));

        return node;
    }

    public GElement createGraphEdge(GElement graph, String[] tokens, float height) {
//  0    1     2  3 4
//  edge start n1 7 1.153 8.556 1.125 8.417 1.097 8.236 1.111 8.083 1.111 8.042 1.125 8.014 1.125 7.972
//  g 1.194 8.194 solid black

        int controlCount = (int) Float.parseFloat(tokens[3]);
        Vector2D points[] = new Vector2D[controlCount];
        for(int index=0; index<controlCount; index++) {
            points[index] = new Vector2D(Float.parseFloat(tokens[4+index*2])*80,
                                        (height-Float.parseFloat(tokens[4+index*2+1]))*80);
        }

        int labelIndex = 3+2*controlCount+1;
        String label = tokens[labelIndex];
        Vector2D labelPosition = new Vector2D(Float.parseFloat(tokens[labelIndex+1])*80,
                                        (height-Float.parseFloat(tokens[labelIndex+2]))*80);

        GElement source = graph.findElementWithLabel(tokens[1]);
        GElement target = graph.findElementWithLabel(tokens[2]);

        GLink link = new GLink(source, GElementCircle.ANCHOR_CENTER,
                            target, GElementCircle.ANCHOR_CENTER,
                            GLink.SHAPE_BEZIER, label, 0);
        link.setBezierControlPoints(points);
        link.setBezierLabelPosition(labelPosition);
        return link;
    }

    public String getTabName() {
        return "Decision "+decisionNumber+" of \""+ruleName+"\"";
    }

    public Component getTabComponent() {
        return getContainer();
    }

    public interface TDecisionDFADelegate {
        public void decisionDFADidCompleted(TDecisionDFA decision, String error);
    }

    protected class StreamWatcher extends Thread {

        InputStream is;
        String type;

        public StreamWatcher(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ( (line = br.readLine()) != null)
                    editor.console.println(type + ":" + line);
            } catch (IOException e) {
                editor.console.print(e);
            }
        }
    }

    protected class Node extends GElementCircle {

        public boolean doublecircle;

        public void setDouble(boolean flag) {
            doublecircle = flag;
        }

        public void drawShape(Graphics2D g) {
            super.drawShape(g);

            if(doublecircle) {
                int x = (int)(getPositionX()-radius);
                int y = (int)(getPositionY()-radius);

                g.drawOval(x+3, y+3, (int)(radius*2-6), (int)(radius*2-6));
            }
        }

    }
}
