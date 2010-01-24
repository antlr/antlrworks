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

import org.antlr.works.components.GrammarWindow;
import org.antlr.works.components.GrammarWindowMenu;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.menu.ContextualMenuFactory;
import org.antlr.works.stats.StatisticsAW;
import org.antlr.works.utils.IconManager;
import org.antlr.works.utils.Toolbar;
import org.antlr.works.visualization.graphics.GContext;
import org.antlr.works.visualization.graphics.graph.GGraphAbstract;
import org.antlr.works.visualization.graphics.graph.GGraphGroup;
import org.antlr.works.visualization.graphics.path.GPathGroup;
import org.antlr.works.visualization.skin.nfa.NFASkin;
import org.antlr.works.visualization.skin.syntaxdiagram.SDSkin;
import org.antlr.xjlib.appkit.swing.XJRollOverButton;
import org.antlr.xjlib.appkit.swing.XJRollOverButtonToggle;
import org.antlr.xjlib.foundation.notification.XJNotificationCenter;
import org.antlr.xjlib.foundation.notification.XJNotificationObserver;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

public class GPanel implements XJNotificationObserver {

    protected Container container;
    protected Box pathButtonSelectionBox;
    protected Toolbar controlPanel;
    protected JTextField errorLabel;

    protected GContext context;
    protected GView view;
    protected JScrollPane viewScrollPane;

    protected ElementRule rule;
    protected GrammarWindow editor;

    public GPanel(GrammarWindow editor, GContext context) {
        this.editor = editor;
        this.context = context;
        this.container = new JPanel(new BorderLayout());

        createNormalPanel();

        XJNotificationCenter.defaultCenter().addObserver(this, GPathGroup.NOTIF_CURRENT_PATH_DID_CHANGE);
    }

    public void close() {
        editor = null;
        context = null;
        XJNotificationCenter.defaultCenter().removeObserver(this);
    }

    public void setGraphs(List graphs) {
        view.setGraphs(graphs);
        view.refresh();
        updateCurrentAlternative();
    }

    public void setRule(ElementRule rule) {
        if(view != null)
            view.setEnable(false);

        this.rule = rule;
        createPanel();
        view.setEnable(true);
    }

    public void setPlaceholder(String placeholder) {
        view.setPlaceholder(placeholder);
    }

    public void update() {
        updateCurrentError();
    }

    public Container getContainer() {
        return container;
    }

    public GGraphAbstract getCurrentGraph() {
        return view.getCurrentGraph();
    }

    public BufferedImage getImageOfView() {
        return view.getImage();
    }

    public void createPanel() {
        if(rule == null)
            return;

        if(rule.hasErrors())
            createErrorPanel();
        else
            createNormalPanel();
    }

    private void createNormalPanel() {
        container.removeAll();
        container.add(createVisualizationPane(), BorderLayout.CENTER);
        container.add(createControlPane(), BorderLayout.SOUTH);
        container.validate();
    }

    private void createErrorPanel() {
        container.removeAll();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createErrorPane(), BorderLayout.NORTH);
        panel.add(createVisualizationPane(), BorderLayout.CENTER);
        container.add(panel, BorderLayout.CENTER);
        container.add(createControlPane(), BorderLayout.SOUTH);
        container.validate();
    }

    private Container createVisualizationPane() {
        view = new CustomGView(this, context);
        viewScrollPane = new JScrollPane(view);
        viewScrollPane.setWheelScrollingEnabled(true);
        return viewScrollPane;
    }

    private Container createErrorPane() {
        controlPanel = Toolbar.createHorizontalToolbar();

        if(rule.errors.size()>1) {
            controlPanel.addElement(createPrevErrorButton());
            controlPanel.addElement(createNextErrorButton());
            controlPanel.addGroupSeparator();
        }
        controlPanel.addElement(new JLabel(IconManager.shared().getIconWarning()));
        controlPanel.addElement(errorLabel = new JTextField());
        errorLabel.setBorder(null);
        errorLabel.setEditable(false);
        errorLabel.setBackground(getContainer().getBackground());

        controlPanel.add(Box.createHorizontalGlue());

        pathButtonSelectionBox = new Box(BoxLayout.X_AXIS);
        controlPanel.addElement(pathButtonSelectionBox);

        controlPanel.addGroupSeparator();
        controlPanel.addElement(createShowCrossLinksButton());

        return controlPanel;
    }

    private JButton createPrevErrorButton() {
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconBackward());
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
        JButton button = XJRollOverButton.createMediumButton(IconManager.shared().getIconForward());
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

        StringBuilder sb = new StringBuilder();
        int count = view.getGraphs().size();
        int index = view.getCurrentGraphIndex();
        if(count>1) {
            sb.append("(");
            sb.append(index + 1);
            sb.append("/");
            sb.append(count);
            sb.append(") ");
        }
        sb.append(rule.getErrorMessageString(index));
        errorLabel.setText(sb.toString());
        controlPanel.revalidate();
    }

    private void createPathSelectionButtons() {
        pathButtonSelectionBox.removeAll();

        if(!(view.getCurrentGraph() instanceof GGraphGroup))
            return;

        GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
        int count = gg.getPathGroup().getNumberOfPaths();
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
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JCheckBox button = (JCheckBox)event.getSource();
                GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
                gg.getPathGroup().setPathVisible(Integer.parseInt(button.getName()), button.isSelected());
                gg.getPathGroup().makeSureCurrentPathIsVisible();
                view.cacheRerender();
                view.repaint();
            }
        });

        GGraphGroup gg = (GGraphGroup)view.getCurrentGraph();
        button.setSelected(gg.getPathGroup().isPathVisible(pathIndex));

        return button;
    }

    private JToggleButton createShowCrossLinksButton() {
        XJRollOverButtonToggle button = XJRollOverButtonToggle.createMediumButton(IconManager.shared().getIconShowLinks());
        button.setSelected(true);
        button.setFocusable(false);
        button.setToolTipText("Show links between rules");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                for (Object o : view.getGraphs()) {
                    GGraphGroup gg = (GGraphGroup) o;
                    gg.getPathGroup().toggleShowRuleLinks();
                }
                view.cacheRerender();
                view.repaint();
            }
        });
        return button;
    }

    private Container createControlPane() {
        Toolbar box = Toolbar.createHorizontalToolbar();

        box.addElement(new JLabel("Zoom"));
        box.addElement(createFactorSlider());

        //box.addElement(new JLabel("Line space"));
        //box.addElement(createLineSpaceSlider());

        //box.addElement(new JLabel("Epsilon width"));
        //box.addElement(createEpsilonWidthSlider());

        //box.add(createDrawNodeButton());
        //box.add(createDrawDimensionButton());
        box.addElement(new JLabel("Show:"));
        box.addElement(createShowNFAButton());
        box.addElement(createShowRuleNameButton());
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

    /*private JSlider createLineSpaceSlider() {
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
    }      */

    private JCheckBox createShowNFAButton() {
        JCheckBox button = new JCheckBox("NFA");
        button.setFocusable(false);
        button.setSelected(context.skin instanceof NFASkin);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                StatisticsAW.shared().recordEvent(StatisticsAW.EVENT_TOGGLE_SD_NFA);

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

    private JCheckBox createShowRuleNameButton() {
        final JCheckBox button = new JCheckBox("Rule Name");
        button.setFocusable(false);
        button.setSelected(context.isShowRuleName());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                context.setShowRuleName(button.isSelected());
                view.refresh();
            }
        });
        return button;
    }

    /*private JCheckBox createUseCacheButton() {
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
    } */

    public void notificationFire(Object source, String name) {
        if(name.equals(GPathGroup.NOTIF_CURRENT_PATH_DID_CHANGE))
            updateCurrentAlternative();
    }

    protected class CustomGView extends GView {

        public CustomGView(GPanel panel, GContext context) {
            super(panel, context);
        }

        public JPopupMenu getContextualMenu() {
            ContextualMenuFactory factory = editor.createContextualMenuFactory();
            factory.addItem(GrammarWindowMenu.MI_EXPORT_AS_EPS);
            factory.addItem(GrammarWindowMenu.MI_EXPORT_AS_IMAGE);
            return factory.menu;
        }

    }

}
