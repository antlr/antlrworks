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

package org.antlr.works.visualization.graphics;

import org.antlr.works.visualization.fa.FAAnalysis;
import org.antlr.works.visualization.fa.FAState;
import org.antlr.works.visualization.fa.FATransition;
import org.antlr.works.visualization.graphics.graph.GGraph;
import org.antlr.works.visualization.graphics.primitive.GDimension;
import org.antlr.works.visualization.graphics.primitive.GPoint;
import org.antlr.works.visualization.graphics.shape.GLink;
import org.antlr.works.visualization.graphics.shape.GNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GRenderer {

    private final List<GNode> graphicNodes = new ArrayList<GNode>();

    private final FAAnalysis analysis = new FAAnalysis();
    private final Map<FAState,GNode> nodes = new HashMap<FAState, GNode>();
    private final Map<FAState, EOAInfo> endOfAlternativeInfoMap = new HashMap<FAState, EOAInfo>();

    public GRenderer() {
    }

    /** This method is the entry point of this class. It computes the size and position
     * of each node and link. The position are literal and are actually evaluated later, when
     * displaying the syntax diagram.
     *
     */
    @SuppressWarnings("unchecked")
    public synchronized GGraph render(FAState state) {
        GGraph graph = new GGraph();
        graph.setDimension(renderSize(state));
        renderPosition(state);

        /** Mark the last node in order to draw an arrow at the end of the syntax diagram
         */
        GNode lastNode = graphicNodes.get(graphicNodes.size()-1);
        if(lastNode != null)
            lastNode.lastNodeOfRule = true;

        graph.setNodes((ArrayList<GNode>)((ArrayList)graphicNodes).clone());
        return graph;
    }

    public void renderPosition(FAState state) {
        recursiveRenderPositionNode(state, null, new GPoint());
    }

    public void recursiveRenderPositionNode(FAState state, FAState endState, GPoint basePoint) {
        while(state != endState) {
            GNode node = getNode(state);
            if(node == null) {
                System.err.println("Cannot find SDNode associated with state \""+state+"\"");
                return;
            }

            node.setPosition(basePoint);

            if(state != null && state.isAlternative()) {
                state = recursiveRenderPositionAlternative(state, basePoint);
                basePoint.addX(node.nodeDimension.width+node.linkDimension.width);
            } else if(state != null && state.isSingle()) {
                basePoint.addX(node.nodeDimension.width+node.linkDimension.width);
                state = state.getNextFirstState();
            } else {
                state = null;
            }
        }
    }

    public FAState recursiveRenderPositionAlternative(FAState state, GPoint basePoint) {
        FAState alternativeEndState = alternativeEndState(state);

        // This point is used to position each transition
        GPoint point = new GPoint(basePoint);
        point.addX(GContext.NODE_WIDTH+GContext.EPSILON_WIDTH);

        GDimension firstAlternativeDimension = null;

        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            FATransition transition = state.transition(t);
            GLink link = getNode(state).getLink(transition);

            if(t == 0) {
                // We remember here the size of the first transition because if we find later
                // a "loop" transition, we will have to offset this "loop" by the size of the first
                // transition (because the "loop" is always drawed above the first transition).
                firstAlternativeDimension = link.branchDim;
            }

            if(t > 0 && !transition.loop) {
                // Offset the current point for each transition (except for a "loop" because it is
                // displayed above the first transition)
                point.addY(GContext.LINE_SPACE);
                point.addY(link.branchDim.up);
            }

            if(transition.target == alternativeEndState) {
                // The transition is simply a single transition (epsilon normally).
                if(transition.loop) {
                    // If this is a "loop", draw it above the first transition
                    GPoint vp = new GPoint(basePoint);
                    vp.subY(firstAlternativeDimension.up);
                    vp.subY(link.branchDim.down);

                    // The "virtual position" is used by the link to know where to display itself
                    // when it has to "curve" (because both start and end point are on the same y-axis value)
                    getNode(state).getLink(transition).setVirtualPosition(vp);
                } else {
                    getNode(state).getLink(transition).setVirtualPosition(point);
                    point.addY(link.branchDim.down);
                }
            } else {
                // The transition is more than a single transition, continue recursively...
                recursiveRenderPositionNode(transition.target, alternativeEndState, new GPoint(point));
                point.addY(link.branchDim.down);
            }
        }
        return alternativeEndState;
    }

    public GDimension renderSize(FAState state) {
        graphicNodes.clear();
        nodes.clear();
        endOfAlternativeInfoMap.clear();
        analysis.analyze(state);
        return recursiveRenderSizeSingle(state, null);
    }

    public GDimension recursiveRenderSizeSingle(FAState state, FAState endState) {
        GDimension dimension = new GDimension();

        dimension.addUp(GContext.NODE_UP);
        dimension.addDown(GContext.NODE_DOWN);

        while(state != endState && state != null) {
            if(state.isAlternative()) {
                GDimension altDim = recursiveRenderSizeAlternative(state);
                dimension.addWidth(GContext.NODE_WIDTH+altDim.width);
                dimension.maxUp(altDim.up);
                dimension.maxDown(altDim.down);
                state = alternativeEndState(state);
            } else if(state.isSingle()) {
                // Create the first node...
                GNode n1 = createNode(state);

                // ... and compute the size of the transition...
                FATransition transition = state.getFirstTransition();
                if(transition.isEpsilon()) {
                    n1.linkDimension.width = GContext.EPSILON_WIDTH;
                    n1.linkDimension.up = GContext.EPSILON_UP;
                    n1.linkDimension.down = GContext.EPSILON_DOWN;
                } else {
                    n1.linkDimension.width = GContext.getBoxWidth(transition.label);
                    n1.linkDimension.up = GContext.BOX_UP;
                    n1.linkDimension.down = GContext.BOX_DOWN;
                }

                dimension.addWidth(GContext.NODE_WIDTH+n1.linkDimension.width);
                dimension.maxUp(n1.linkDimension.up);
                dimension.maxDown(n1.linkDimension.down);

                // ... then create the target node...
                state = transition.target;
                GNode n2 = createNode(state);

                // ... and create the link between these two states
                GLink link = new GLink();
                link.transition = transition;
                link.target = n2;
                n1.addLink(link);

                if(state == endState) {
                    // If we have reached the end of an alternative, we must set the "last" flag
                    // to the SDLink in order for it to be correctly rendered on screen.
                    EOAInfo eoa = endOfAlternativeInfoMap.get(state);
                    if(eoa != null) {
                        link.setLast(eoa.last);
                    }
                }
            } else {
                dimension.addWidth(GContext.NODE_WIDTH);
                state = null;
            }
        }
        return dimension;
    }

    public GDimension recursiveRenderSizeAlternative(FAState state) {
        FAState alternativeEndState = alternativeEndState(state);

        GNode norigin = createNode(state);

        GDimension dimension = norigin.linkDimension;
        dimension.addWidth(GContext.EPSILON_WIDTH);

        GDimension firstTransitionDimension = null;

        for(int t=0; t<state.getNumberOfTransitions(); t++) {
            FATransition transition = state.transition(t);

            GLink link = new GLink();
            link.transition = transition;
            link.target = createNode(transition.target);
            norigin.addLink(link);

            boolean last = t == state.getNumberOfTransitions()-1;
            if(t == state.getNumberOfTransitions()-2 && state.transition(t+1).loop) {
                // If the last alternative is a loop, consider the last-1 alternative as the last one:
                // the loop will be displayed above the first transition (up) in order to see it easily
                // from the other transition(s).
                last = true;
            }

            link.setLast(last);

            if(transition.target == alternativeEndState) {
                GDimension transitionDimension = new GDimension();
                transitionDimension.addUp(GContext.EPSILON_UP);
                transitionDimension.addDown(GContext.EPSILON_DOWN);
                if(transition.loop)
                    transitionDimension.addDown(GContext.LINE_SPACE);

                if(transition.loop) {
                    link.setBranchDimension(transitionDimension);
                    dimension.maxUp(firstTransitionDimension.up+transitionDimension.up+transitionDimension.down);
                } else {
                    link.setBranchDimension(transitionDimension);
                    if(t == 0) {
                        firstTransitionDimension = transitionDimension;                        
                    }
                    dimension.addDown(transitionDimension.up);
                    dimension.addDown(transitionDimension.down);
                }
            } else {
                endOfAlternativeInfoMap.put(alternativeEndState, new EOAInfo(last));
                GDimension transitionDimension = recursiveRenderSizeSingle(transition.target, alternativeEndState);

                if(((t > 0) || (t == 0 && !state.transition(1).loop)) && !last)
                    dimension.addDown(GContext.LINE_SPACE);

                link.setBranchDimension(transitionDimension);

                transitionDimension.addWidth(GContext.EPSILON_WIDTH);
                dimension.maxWidth(transitionDimension.width);
                if(t == 0) {
                    // Remember the size of the first transition
                    firstTransitionDimension = transitionDimension;
                    // Add its "up" size to the transition "up" size
                    dimension.maxUp(transitionDimension.up);
                    dimension.addDown(transitionDimension.down);
                } else {
                    dimension.addDown(transitionDimension.up);
                    dimension.addDown(transitionDimension.down);
                }
            }
        }
        return dimension;
    }

    public GNode createNode(FAState state) {
        GNode node = getNode(state);
        if(node == null) {
            node = new GNode();
            node.setState(state);
            graphicNodes.add(node);
            nodes.put(state, node);
        }
        return node;
    }

    public GNode getNode(FAState state) {
        return nodes.get(state);
    }

    public FAState alternativeEndState(FAState alt) {
        int counter = alt.getNumberOfTransitions()-1;
        FAState state = alt;
        while(true) {
            FATransition transition = state.getFirstTransition();
            if(transition == null)
                break;

            state = transition.target;

            // Note: a state can be both an end-of-alternative and an alternative itself ;-)
            if(analysis.numberOfIncomingTransition(state)>1) {
                counter -= analysis.numberOfIncomingTransition(state)-1;
                if(counter <= 0)
                    break;
            }

            if(state.isAlternative()) {
                counter += state.getNumberOfTransitions()-1;                
            }
        }
        return state;
    }

    private class EOAInfo {
        public boolean last = false;

        public EOAInfo(boolean last) {
            this.last = last;
        }
    }
}
