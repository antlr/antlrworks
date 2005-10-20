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

package org.antlr.works.visualization.graphics.panel;

import edu.usfca.xj.appkit.frame.XJFrame;
import edu.usfca.xj.foundation.notification.XJNotificationCenter;
import edu.usfca.xj.foundation.notification.XJNotificationObserver;
import org.antlr.works.parser.ParserRule;
import org.antlr.works.stats.Statistics;
import org.antlr.works.util.IconManager;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.graph.GGraphGroup;
import org.antlr.works.visualization.graphics.path.GPathGroup;
import org.antlr.works.visualization.skin.nfa.NFASkin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

public class GPanel implements XJNotificationObserver {

    private XJFrame parent;

    private Container container;
    private Box pathButtonSelectionBox;
    private JLabel errorLabel;

    private GContext context;
    private GView view;

    private ParserRule rule;

    public GPanel(GContext context) {
        this.context = context;
        this.container = new JPanel(new BorderLayout());
        createNormalPanel();

        XJNotificationCenter.defaultCenter().addObserver(this, GPathGroup.NOTIF_CURRENT_PATH_DID_CHANGE);
    }

    public void close() {
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void setGraphs(List graphs) {
        view.setGraphs(graphs);
        view.refresh();
        updateCurrentAlternative();
    }

    public void setRule(ParserRule rule) {
        if(view != null)
            view.setEnable(false);

        this.rule = rule;
        createPanel();
        view.setEnable(true);
    }

    public void update() {
        updateCurrentError();
    }

    public Container getContainer() {
        return container;
    }

    public BufferedImage getImageOfView() {
        return view.getCachedImage();
    }

    public void createPanel() {
        if(rule == null)
            return;

        if(rule.hasErrors())
            createErrorPanel();
        else
            createNormalPanel();
    }

    public void setParent(XJFrame parent) {
        this.parent = parent;
        view.setParent(parent);
    }

    public void createNormalPanel() {
        container.removeAll();
        container.add(createControlPane(), BorderLayout.NORTH);
        container.add(createVisualizationPane(), BorderLayout.CENTER);
        container.validate();
    }

    public void createErrorPanel() {
        container.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createErrorPane(), BorderLayout.NORTH);
        panel.add(createVisualizationPane(), BorderLayout.CENTER);
        container.add(panel, BorderLayout.CENTER);
        container.add(createControlPane(), BorderLayout.NORTH);
        container.validate();
    }

    private Container createVisualizationPane() {
        view = new GView(context);
        view.setParent(parent);
        JScrollPane scrollPane = new JScrollPane(view);
        return scrollPane;
    }

    private Container createErrorPane() {
        Box controlPanel = new Box(BoxLayout.X_AXIS);

        if(rule.errors.size()>1) {
            controlPanel.add(createPrevErrorButton());
            controlPanel.add(createNextErrorButton());
        }
        controlPanel.add(Box.createHorizontalStrut(5));
        controlPanel.add(new JLabel(IconManager.shared().getIconWarning()));
        controlPanel.add(Box.createHorizontalStrut(3));
        controlPanel.add(errorLabel = new JLabel());

        controlPanel.add(Box.createHorizontalGlue());

        controlPanel.add(Box.createHorizontalStrut(20));
        pathButtonSelectionBox = new Box(BoxLayout.X_AXIS);
        controlPanel.add(pathButtonSelectionBox);

        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(createShowCrossLinksButton());

        return controlPanel;
    }

    private JButton createPrevErrorButton() {
        JButton button = new JButton(IconManager.shared().getIconBackward());
        button.setFocusable(false);
        button.setToolTipText("Show Previous Error");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(view.setPrevGraph()) {
                    view.refresh();
                    createPathSelectionButtons();
                    updateCurrentError();
                }
            }
        });
        return button;
    }

    private JButton createNextErrorButton() {
        JButton button = new JButton(IconManager.shared().getIconForward());
        button.setFocusable(false);
        button.setToolTipText("Show Next Error");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(view.setNextGraph()) {
                    view.refresh();
                    createPathSelectionButtons();
                    updateCurrentError();
                }
            }
        });
        return button;
    }

    private void updateCurrentAlternative() {
        if(view.getCurrentGraph() instanceof GGraphGroup) {
            if(pathButtonSelectionBox != null && pathButtonSelectionBox.getComponentCount() == 0)
                createPathSelectionButtons();
        }
    }

    private void updateCurrentError() {
        if(rule == null || !rule.hasErrors())
            return;

        StringBuffer sb = new StringBuffer();
        int count = view.getGraphs().size();
        int index = view.getCurrentGraphIndex();
        if(count>1) {
            sb.append("("+(index+1)+"/"+count+") ");
        }
        sb.append(rule.getErrorMessageString(index));
        errorLabel.setText(sb.toString());
    }

    private void createPathSelectionButtons() {
        pathButtonSelectionBox.removeAll();

        if(!(view.getCurrentGraph() instanceof GGraphGroup))
            return;

        GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
        int count = gg.pathGroup.getNumberOfPaths();
        if(count <= 1)
            pathButtonSelectionBox.add(new JLabel("Alternative:"));
        else
            pathButtonSelectionBox.add(new JLabel("Alternatives:"));

        for(int i=0; i<count; i++) {
            pathButtonSelectionBox.add(createPathSelectionButton(i));
        }
    }

    private JCheckBox createPathSelectionButton(int pathIndex) {
        JCheckBox button = new JCheckBox(String.valueOf(pathIndex+1));
        button.setName(String.valueOf(pathIndex));
        button.setFocusable(false);
        button.setToolTipText("Alternate "+(pathIndex+1));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JCheckBox button = (JCheckBox)event.getSource();
                GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
                gg.pathGroup.setPathVisible(Integer.parseInt(button.getName()), button.isSelected());
                view.cacheRerender();
                view.repaint();
            }
        });

        GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
        button.setSelected(gg.pathGroup.isPathVisible(pathIndex));

        return button;
    }

    private JButton createShowCrossLinksButton() {
        JButton button = new JButton(IconManager.shared().getIconShowLinks());
        button.setFocusable(false);
        button.setToolTipText("Show links between rules");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Iterator iterator = view.getGraphs().iterator();
                while(iterator.hasNext()) {
                    GGraphGroup gg = (GGraphGroup)iterator.next();
                    gg.pathGroup.toggleShowRuleLinks();
                }
                view.cacheRerender();
                view.repaint();
            }
        });
        return button;
    }

    private Container createControlPane() {
        Box box = new Box(BoxLayout.X_AXIS);
        //box.setBorder(BorderFactory.createTitledBorder("Settings"));

        box.add(new JLabel("Zoom"));
        box.add(createFactorSlider());

        //box.add(new JLabel("Line space"));
        //box.add(createLineSpaceSlider());

        //box.add(new JLabel("Epsilon width"));
        //box.add(createEpsilonWidthSlider());

        //box.add(createDrawNodeButton());
        //box.add(createDrawDimensionButton());
        box.add(createShowNFAButton());
        //box.add(createUseCacheButton());

        //box.setPreferredSize(new Dimension(160, 0));
        return box;
    }

    private JSlider createFactorSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(1);
        slider.setMaximum(800);
        slider.setValue((int)(context.getFactor()*40));

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();
                context.setFactor((float)slider.getValue()/40);
                view.refreshSizeChanged(slider.getValueIsAdjusting());
            }
        });
        return slider;
    }

    private JSlider createLineSpaceSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(0);
        slider.setMaximum(800);
        slider.setValue((int)(context.skin.getValueLineSpace()*80));
        slider.setToolTipText("LINE_SPACE");

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();
                context.skin.setValueLineSpace((float)slider.getValue()/80);
                view.refreshSizeChanged(slider.getValueIsAdjusting());
            }
        });
        return slider;
    }

    private JSlider createEpsilonWidthSlider() {
        JSlider slider = new JSlider();
        slider.setFocusable(false);
        slider.setMinimum(1);
        slider.setMaximum(800);
        slider.setValue((int)(context.skin.getValueEpsilonWidth()*20));
        slider.setToolTipText("EPSILON_WIDTH");

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider slider = (JSlider)event.getSource();
                context.skin.setValueEpsilonWidth((float)slider.getValue()/20);
                view.refreshSizeChanged(slider.getValueIsAdjusting());
            }
        });
        return slider;
    }

    private JCheckBox createDrawNodeButton() {
        JCheckBox button = new JCheckBox("Show Nodes");
        button.setFocusable(false);
        button.setSelected(context.drawnode);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JCheckBox button = (JCheckBox)event.getSource();
                context.drawnode = button.isSelected();
                view.cacheRerender();
                view.repaint();
            }
        });
        return button;
    }

    private JCheckBox createDrawDimensionButton() {
        JCheckBox button = new JCheckBox("Show Dimension");
        button.setFocusable(false);
        button.setSelected(context.drawdimension);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JCheckBox button = (JCheckBox)event.getSource();
                context.drawdimension = button.isSelected();
                view.cacheRerender();
                view.repaint();
            }
        });
        return button;
    }

    private JCheckBox createShowNFAButton() {
        JCheckBox button = new JCheckBox("Show NFA");
        button.setFocusable(false);
        button.setSelected(context.skin instanceof NFASkin);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Statistics.shared().recordEvent(Statistics.EVENT_TOGGLE_SD_NFA);

                JCheckBox button = (JCheckBox)event.getSource();
                if(button.isSelected())
                    context.setSkin(new NFASkin());
                else
                    context.setSkin(new SDSkin());

                view.refresh();
            }
        });
        return button;
    }

    private JCheckBox createUseCacheButton() {
        JCheckBox button = new JCheckBox("Use cache image");
        button.setFocusable(false);
        button.setSelected(view.isCachedEnabled());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JCheckBox button = (JCheckBox)event.getSource();
                view.setCacheEnabled(button.isSelected());
                view.refresh();
            }
        });
        return button;
    }

    public void notificationFire(Object source, String name) {
        if(name.equals(GPathGroup.NOTIF_CURRENT_PATH_DID_CHANGE))
            updateCurrentAlternative();
    }

}
