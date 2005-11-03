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

package org.antlr.works.visualization.graphics.graph;

import org.antlr.analysis.NFAState;
import org.antlr.works.visualization.fa.FATransition;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.path.GPath;
import org.antlr.works.visualization.graphics.path.GPathElement;
import org.antlr.works.visualization.graphics.path.GPathGroup;
import org.antlr.works.visualization.graphics.primitive.GDimension;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GGraphGroup extends GGraphAbstract {

    public static final int TITLE_OFFSET = 100;

    public GDimension dimension = new GDimension();
    public List graphs = new ArrayList();
    public GPathGroup pathGroup = new GPathGroup();

    private GContext defaultContext;

    public GGraphGroup() {
        // The default context is used to evaluate the position of certain objects in addPath()
        // because position requires a context to be evaluated ;-)
        defaultContext = new GContext();
        defaultContext.setSkin(new SDSkin());
    }

    public void setEnable(boolean flag) {
        pathGroup.setEnable(flag);
    }

    public void setContext(GContext context) {
        super.setContext(context);
        for (Iterator iterator = graphs.iterator(); iterator.hasNext();) {
            GGraph graph = (GGraph) iterator.next();
            graph.setContext(context);
        }
        pathGroup.setContext(context);
    }

    public void add(GGraph graph) {
        dimension.maxWidth(graph.dimension.width);
        dimension.addUp(graph.dimension.up);
        dimension.addDown(graph.dimension.down);
        if(graphs.size()>0)
            dimension.addDown(GContext.LINE_SPACE);
        graphs.add(graph);
    }

    public float getWidth() {
        return getDimension().getPixelWidth(context)+TITLE_OFFSET;
    }

    public void addPath(List path, boolean disabled) {
        List elements = new ArrayList();

        // Note: first create an array of continuous state/node.
        // It may happen that some state doesn't have a corresponding node
        // if, for example, the state is located after an accepted state (because
        // nodes are built until the accepted state only).

        List stateList = new ArrayList();
        List nodeList = new ArrayList();
        for(int i=0; i<path.size(); i++) {
            NFAState state = (NFAState)path.get(i);
            GNode node = findNodeForStateNumber(state.stateNumber);
            if(node == null)
                continue;

            stateList.add(state);
            nodeList.add(node);
        }

        for(int i=0; i<stateList.size(); i++) {
            NFAState state = (NFAState)stateList.get(i);
            GNode node = (GNode)nodeList.get(i);

            elements.add(new GPathElement(node));

            if(i == stateList.size()-1) {
                // Exit here if last state
                break;
            }

            // Find the next non-skipped state
            NFAState nextState;
            while(true) {
                nextState = (NFAState)stateList.get(i+1);
                if(node.state.containsStateNumber(nextState.stateNumber))
                    i++;
                else
                    break;
            }

            if(state.getEnclosingRule().equals(nextState.getEnclosingRule())) {
                // We are still in the same rule, simply add the existing link

                FATransition transition = node.state.getTransitionToStateNumber(nextState.stateNumber);
                if(transition == null) {
                    // Probably a loop. In this case, the transition is located in the target state
                    GNode targetNode = findNodeForStateNumber(nextState.stateNumber);
                    transition = targetNode.state.getTransitionToStateNumber(node.state.stateNumber);
                    if(transition == null) {
                        // No transition found ? This is an reference to the same rule (recursive)

                        if(node.state.getFirstTransition() == null) {
                            // Add a link if the two states are not on the same axis
                            // (otherwise, we have little chance to see this transition)
                            if(node.position.getX(defaultContext) != targetNode.position.getY(defaultContext)) {
                                GPathElement element = new GPathElement(node, targetNode);
                                element.setRuleLink(true);
                                elements.add(element);
                            }
                        } else {
                            elements.add(new GPathElement(node.getLink(node.state.getFirstTransition())));

                            GNode nextNode = findNodeForStateNumber(node.state.getFirstTransition().target.stateNumber);
                            elements.add(new GPathElement(nextNode));

                            GLink link = nextNode.getLink(nextNode.state.getFirstTransition());
                            elements.add(new GPathElement(link));
                        }
                    } else
                        elements.add(new GPathElement(targetNode.getLink(transition)));
                } else
                    elements.add(new GPathElement(node.getLink(transition)));
            } else {
                // The next state is in another rule. Add the external rule ref state
                // (which should be the only target of the current rule) to the path
                if(node.state.getFirstTransition() == null) {
                    // Node without transition (probably at the end of a rule).
                    // Create the link to the other rule
                    GPathElement element = new GPathElement(node, findNodeForStateNumber(nextState.stateNumber));
                    element.setRuleLink(true);
                    elements.add(element);
                } else {
                    elements.add(new GPathElement(node.getLink(node.state.getFirstTransition())));

                    GNode nextNode = findNodeForStateNumber(node.state.getFirstTransition().target.stateNumber);
                    elements.add(new GPathElement(nextNode));

                    GLink link = nextNode.getLink(nextNode.state.getFirstTransition());
                    elements.add(new GPathElement(link));

                    // Create the link to the other rule
                    GPathElement element = new GPathElement(link, findNodeForStateNumber(nextState.stateNumber));
                    element.setRuleLink(true);
                    elements.add(element);
                }
            }
        }
        pathGroup.addPath(new GPath(elements, disabled));
    }

    public GNode findNodeForStateNumber(int stateNumber) {
        for (Iterator iterator = graphs.iterator(); iterator.hasNext();) {
            GGraph graph = (GGraph) iterator.next();
            GNode node = graph.findNodeForStateNumber(stateNumber);
            if(node != null)
                return node;
        }
        return null;
    }

    public GDimension getDimension() {
        return dimension;
    }

    public void render(float ox, float oy) {
        ox += TITLE_OFFSET;
        for (int i = 0; i<graphs.size(); i++) {
            GGraph graph = (GGraph)graphs.get(i);
            graph.render(ox, oy);
            if(i<graphs.size()-1)
                oy += graph.getDimension().getPixelHeight(context)+context.getPixelLineSpace();
        }

        setRendered(true);
    }

    public void draw() {
        pathGroup.draw();

        context.nodeColor = Color.black;
        context.linkColor = Color.black;
        context.setLineWidth(1);

        context.setIgnoreObjects(pathGroup.getObjectsToIgnore());

        for (int i = 0; i<graphs.size(); i++) {
            GGraph graph = (GGraph)graphs.get(i);
            graph.draw();
            context.setColor(Color.black);
            context.drawString(context.getRuleFont(), graph.name, TITLE_OFFSET-5, graph.offsetY, GContext.ALIGN_RIGHT);
        }

        context.resetIgnoreObjects();

        if(context.drawdimension) {
            context.setLineWidth(1);
            context.setColor(Color.lightGray);
            float width = getDimension().getPixelWidth(context);
            float up = getDimension().getPixelUp(context);
            float down = getDimension().getPixelDown(context);
            if(up+down>0)
                context.drawRect(0, 0, width, up+down, false);
        }
    }

}
