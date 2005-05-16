package org.antlr.works.test;

/*

[The "BSD licence"]
Copyright (c) 2004 Jean Bovet
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

import edu.usfca.xj.appkit.frame.XJFrame;
import org.antlr.analysis.DFA;
import org.antlr.analysis.NFAState;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.util.DotGenerator;
import org.antlr.works.visualization.fa.FAFactory;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.grammar.GrammarEngine;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.GEngineGraphics;
import org.antlr.works.visualization.graphics.GFactory;
import org.antlr.works.visualization.graphics.graph.GGraph;
import org.antlr.works.visualization.graphics.panel.GPanel;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.skin.Skin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class VisualizationTest {

    static GrammarEngine engine = new GrammarEngine();
    static NFAState startState;
    static DFA dfa;

    static FAState state;
    static XJFrame frame;

    static GFactory gfactory = new GFactory();
    static GContext context = new GContext();
    static GPanel panel;

    static Skin skin = new SDSkin();

    public static final String RULE_A = "a : A ;";
    public static final String RULE_B = "a : A {;} | B;";
    public static final String RULE_C = "a : (A B)? (B)?;";
    public static final String RULE_D = "a : (A {;} | B {;} | C {;} | E) (D)?;";
    public static final String RULE_E = "a : B (C)+ (D | (E)?) | A;";
    public static final String RULE_F = "a : B | (A C | A (B)* C)+;";
    public static final String RULE_G = "a : (A (C)+ | A (B {;} C | D)* C)+ | D;";
    public static final String RULE_H = "a : (A {;} | B C {;} | C)*;";

    public static final String RULE_J = "a : A | (A B {;} | C D E)+;";
    public static final String RULE_L = "a : (I J K (A {;} | B {;} | C {;} | D)*)?;";

    // Problem because transition don't have same eob
    public static final String RULE_M = "a : (A)* {;}| (B);";

    public static final String RULE_DEBUG = "a : (A {;}| A)*;";
    public static final String RULE_DEBUG1 = "a : (C {;}| B | A)+ A;";
    public static final String RULE_DEBUG2 = "a : A {;} | B | C;";

    // Test with this one the error display
    public static final String RULE_DEBUG3 = "a : a A | A;";

    public static final String RULE_DEBUG4 = "a : a A | A (C)+ | B;";
    public static final String RULE_DEBUG5 = "a : B C buzzer Z Z; buzzer : B C D;";
    public static final String RULE_DEBUG6 = "a : (buzzer)*; buzzer : B C D;";
    public static final String RULE_DEBUG7 = "a : SWITCH ID LCURLY ( caseGroup )* RCURLY ;\n" +
            "\ncaseGroup : (case)+ (a)* ;\n" +
            "case : CASE INT COLON ;\n";

    public static void main(String[] args) throws Exception {

        engine.setGrammarText("parser grammar P;\n"+RULE_DEBUG7);
        //g = new Grammar("test", new FileReader("/Users/bovet/ java.g"));
        engine.analyze(null);

        context.setSkin(skin);
        context.setEngine(new GEngineGraphics());

        //displaySingleRule("a");
        displayMultipleRules();

        //displayPaths(state);
        //runTests();

        //generateDOT();
    }

    public static void displaySingleRule(String rule) {
        createWindow(gfactory.buildGraphsForRule(engine, rule), true);
    }

    public static void displayMultipleRules() {
        createWindow(gfactory.buildGraphsForErrors(engine), false);
    }

    public static void createWindow(List graphs, boolean normal) {
        frame = new XJFrame();
        panel = new GPanel(context);
        panel.setGraphs(graphs);
        if(normal)
            panel.createNormalPanel();
        else
            panel.createErrorPanel();
        panel.setGraphs(graphs);
        frame.getContentPane().add(panel.getContainer(), BorderLayout.CENTER);
        frame.setSize(1000, 500);
        frame.setVisible(true);
    }

    public static void generateDOT() throws Exception {
        DOTGenerator dotgen = new DOTGenerator(engine.g);
        String dot = dotgen.getDOT(engine.g.getRuleStartState("a"));
        //dotgen.writeDOTFile("/nfa", dot);

        if(dfa != null && dfa.startState != null) {
            dot = dotgen.getDOT(dfa.startState);
          //  dotgen.writeDOTFile("/dfa", dot);
        }

        FAState state = new FAFactory(engine.g).buildNFA(engine.g.getRuleStartState("a"), true);
        if(state != null) {
            DotGenerator jdot = new DotGenerator(state);
            jdot.writeToFile("/simplified");
        }
    }

    public static void displayGraphicNodes(GGraph graph, GContext context) {
        Iterator iterator = graph.nodes.iterator();
        while(iterator.hasNext()) {
            GNode node = (GNode)iterator.next();
            System.out.println(node.toString(context));
        }
    }

    public static void displayPaths(FAState state) {
        List paths = new ArrayList();
        state.getPaths(new HashSet(), new String(), paths);
        Iterator iterator = paths.iterator();
        while(iterator.hasNext())
            System.out.println(iterator.next());
    }

    public static void runTests() {
        test_A();
        test_B();
        test_C();
        test_D();
        test_E();
        test_F();
        test_G();
        test_H();
    }

    public static void test_A() {
        List paths = new ArrayList();
        paths.add("0-e-2,2-A-3,3-e-1");
        testAndCompare(RULE_A, paths);
    }

    public static void test_B() {
        List paths = new ArrayList();
        paths.add("0-e-7,7-e-2,2-A-3,3-e-6,6-e-1");
        paths.add("0-e-7,7-e-4,4-B-5,5-e-6,6-e-1");
        testAndCompare(RULE_B, paths);
    }

    public static void test_C() {
        List paths = new ArrayList();
        paths.add("0-e-9,9-e-2,2-A-3,3-e-4,4-B-5,5-e-10,10-e-16,16-e-11,11-B-12,12-e-17,17-e-1");
        paths.add("0-e-9,9-e-2,2-A-3,3-e-4,4-B-5,5-e-10,10-e-16,16-e-17,17-e-1");
        paths.add("0-e-9,9-e-10,10-e-16,16-e-11,11-B-12,12-e-17,17-e-1");
        paths.add("0-e-9,9-e-10,10-e-16,16-e-17,17-e-1");
        testAndCompare(RULE_C, paths);
    }

    public static void test_D() {
        List paths = new ArrayList();
        paths.add("0-e-11,11-e-2,2-A-3,3-e-10,10-e-20,20-e-15,15-D-16,16-e-21,21-e-1");
        paths.add("0-e-11,11-e-2,2-A-3,3-e-10,10-e-20,20-e-21,21-e-1");
        paths.add("0-e-11,11-e-4,4-B-5,5-e-10,10-e-20,20-e-15,15-D-16,16-e-21,21-e-1");
        paths.add("0-e-11,11-e-4,4-B-5,5-e-10,10-e-20,20-e-21,21-e-1");
        paths.add("0-e-11,11-e-6,6-C-7,7-e-10,10-e-20,20-e-15,15-D-16,16-e-21,21-e-1");
        paths.add("0-e-11,11-e-6,6-C-7,7-e-10,10-e-20,20-e-21,21-e-1");
        paths.add("0-e-11,11-e-8,8-E-9,9-e-10,10-e-20,20-e-15,15-D-16,16-e-21,21-e-1");
        paths.add("0-e-11,11-e-8,8-E-9,9-e-10,10-e-20,20-e-21,21-e-1");
        testAndCompare(RULE_D, paths);
    }

    public static void test_E() {
        List paths = new ArrayList();
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-4,4-C-5,5-e-6,6-e-20,20-e-10,10-D-11,11-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-4,4-C-5,5-e-6,6-e-20,20-e-17,17-e-12,12-E-13,13-e-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-4,4-C-5,5-e-6,6-e-20,20-e-17,17-e-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-6,6-e-20,20-e-10,10-D-11,11-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-6,6-e-20,20-e-17,17-e-12,12-E-13,13-e-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-2,2-B-3,3-e-7,7-e-6,6-e-20,20-e-17,17-e-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-22,22-A-23,23-e-24,24-e-1");
        testAndCompare(RULE_E, paths);
    }

    public static void test_F() {
        List paths = new ArrayList();
        paths.add("0-e-25,25-e-2,2-B-3,3-e-24,24-e-1");
        paths.add("0-e-25,25-e-20,20-e-4,4-A-5,5-e-6,6-C-7,7-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-20,20-e-8,8-A-9,9-e-13,13-e-10,10-B-11,11-e-12,12-e-16,16-e-17,17-C-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-20,20-e-8,8-A-9,9-e-13,13-e-12,12-e-16,16-e-17,17-C-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-20,20-e-8,8-A-9,9-e-16,16-e-17,17-C-18,18-e-19,19-e-24,24-e-1");
        paths.add("0-e-25,25-e-20,20-e-19,19-e-24,24-e-1");
        testAndCompare(RULE_F, paths);
    }


    public static void test_G() {
        List paths = new ArrayList();
        paths.add("0-e-34,34-e-27,27-e-2,2-A-3,3-e-7,7-e-4,4-C-5,5-e-6,6-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-2,2-A-3,3-e-7,7-e-6,6-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-10,10-A-11,11-e-19,19-e-12,12-B-13,13-e-14,14-C-15,15-e-18,18-e-23,23-e-24,24-C-25,25-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-10,10-A-11,11-e-19,19-e-16,16-D-17,17-e-18,18-e-23,23-e-24,24-C-25,25-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-10,10-A-11,11-e-19,19-e-18,18-e-23,23-e-24,24-C-25,25-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-10,10-A-11,11-e-23,23-e-24,24-C-25,25-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-27,27-e-26,26-e-33,33-e-1");
        paths.add("0-e-34,34-e-31,31-D-32,32-e-33,33-e-1");
        testAndCompare(RULE_G, paths);
    }

    public static void test_H() {
        List paths = new ArrayList();
        paths.add("0-e-11,11-e-2,2-A-3,3-e-10,10-e-16,16-e-1");
        paths.add("0-e-11,11-e-4,4-B-5,5-e-6,6-C-7,7-e-10,10-e-16,16-e-1");
        paths.add("0-e-11,11-e-8,8-C-9,9-e-10,10-e-16,16-e-1");
        paths.add("0-e-11,11-e-10,10-e-16,16-e-1");
        paths.add("0-e-16,16-e-1");
        testAndCompare(RULE_H, paths);
    }

    public static void testAndCompare(String rule, List expectedPaths) {
        try {
            Grammar g = new Grammar("parser grammar P;\n"+rule);
            g.createNFAs();

            NFAState startState = g.getRuleStartState("a");

            FAState s = new FAFactory(g).buildNFA(startState, true);
            List paths = new ArrayList();
            s.getPaths(new HashSet(), new String(), paths);
            if(!expectedPaths.equals(paths)) {
                System.err.println("** Test OPTIMIZATION failed for rule \""+rule+"\"");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
