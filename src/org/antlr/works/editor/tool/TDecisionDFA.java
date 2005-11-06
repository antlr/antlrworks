package org.antlr.works.editor.tool;

import edu.usfca.xj.appkit.utils.XJAlert;
import org.antlr.analysis.DFA;
import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.works.editor.EditorPreferences;
import org.antlr.works.editor.EditorWindow;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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

public class TDecisionDFA implements Runnable {

    public interface TDecisionDFADelegate {
        public void decisionDFADidCompleted(TDecisionDFA decision, String error);
    }

    protected EditorWindow editor;
    protected TDecisionDFADelegate delegate;

    protected JPanel panel;
    protected ImagePanel imagePanel;

    protected String text;
    protected int line;
    protected int column;

    public int decisionNumber;

    protected String tempDOTFile;
    protected String tempImageFile;

    protected String error;

    public TDecisionDFA(EditorWindow editor, TDecisionDFADelegate delegate) {
        this.editor = editor;
        this.delegate = delegate;
    }

    public Container getContainer() {
        return panel;
    }

    public String getDOTToolPath() {
        return EditorPreferences.getDOTToolPath();
    }

    public String getDOTImageFormat() {
        return EditorPreferences.getDOTImageFormat();
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
        if(getDOTImageFormat() == null) {
            XJAlert.display(editor.getWindowContainer(), "Error", "Cannot generate the decision DFA because the DOT image format not defined.\n" +
                    "It can be defined in the Preferences.");
            return false;
        }

        new Thread(this).start();
        return true;
    }

    public void run() {
        text = editor.getText();
        line = editor.getCurrentLinePosition();
        column = editor.getCurrentColumnPosition();

        error = null;

        try {
            tempDOTFile = File.createTempFile("decisionDFA", ".dot").getAbsolutePath();
            tempImageFile = File.createTempFile("decisionDFA", ".png").getAbsolutePath();

            BufferedImage image = generate();
            if(image != null) {
                createInterface(image);
            }
        } catch(Exception e) {
            error = e.getLocalizedMessage();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                delegate.decisionDFADidCompleted(TDecisionDFA.this, error);
            }
        });

        new File(tempDOTFile).delete();
        new File(tempImageFile).delete();
    }

    private void createInterface(BufferedImage image) {
        panel = new JPanel(new BorderLayout());

        imagePanel = new ImagePanel(image);
        imagePanel.setBackground(Color.white);

        Box b = Box.createHorizontalBox();
        b.add(new JLabel("Zoom"));
        b.add(createZoomSlider());

        panel.add(b, BorderLayout.NORTH);
        panel.add(new JScrollPane(imagePanel), BorderLayout.CENTER);
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

                imagePanel.setZoom((float)slider.getValue()/100);
                imagePanel.repaint();
            }
        });
        return slider;
    }

    public BufferedImage generate() throws Exception {
        generateDOTFile();
        generateImageFile();
        return ImageIO.read(new File(tempImageFile));
    }

    private void generateDOTFile() throws Exception {
        Grammar g = new Grammar(text);
        g.createNFAs();
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
        String dot = new DOTGenerator(g).getDOT( dfa.startState );
        FileWriter fw = new FileWriter(tempDOTFile);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(dot);
        bw.close();
    }

    public void generateImageFile() throws Exception {
        String size = "size=\"30,20\"";

        String[] args = new String[] { getDOTToolPath(), "-T"+getDOTImageFormat(), "-G"+size, "-o", tempImageFile, tempDOTFile };
        Process p = Runtime.getRuntime().exec(args);

        new StreamWatcher(p.getErrorStream(), "DecisionDFA").start();
        new StreamWatcher(p.getInputStream(), "DecisionDFA").start();

        p.waitFor();
    }

    private class StreamWatcher extends Thread {

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
                    System.err.println(type + ":" + line);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private class ImagePanel extends JPanel {

        protected BufferedImage image;
        protected int width;
        protected int height;
        protected float zoom;

        public ImagePanel(BufferedImage image) {
            this.image = image;
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.zoom = 1;
            adjustSize();
        }

        public float getImageWidth() {
            return width*zoom;
        }

        public float getImageHeight() {
            return height*zoom;
        }

        public void setZoom(float zoom) {
            this.zoom = zoom;
            adjustSize();
        }

        public void adjustSize() {
            Dimension dimension = new Dimension((int)getImageWidth(), (int)getImageHeight());
            setSize(dimension);
            setPreferredSize(dimension);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // @todo try to use an image observer in order not to block the GUI for big image ?
            g.drawImage(image, 0, 0, (int)getImageWidth(), (int)getImageHeight(), null);
        }
    }
}
