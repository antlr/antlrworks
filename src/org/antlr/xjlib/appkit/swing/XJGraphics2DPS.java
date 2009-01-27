package org.antlr.xjlib.appkit.swing;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
Portion of this code is also copyright Terence Parr
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

public class XJGraphics2DPS extends Graphics2D {

    protected static final String NEWLINE = System.getProperty("line.separator");

    protected StringBuilder ps;
    protected Font font;
    protected Color color;
    protected Color background;
    protected Stroke stroke;
    protected AffineTransform transform;
    protected FontRenderContext fontRenderContext;
    protected Point upperLeftCorner;
    protected Point lowerRightCorner;

    protected DecimalFormat df;

    protected int marginWidth, marginHeight;

    public XJGraphics2DPS() {
        ps = new StringBuilder();
        upperLeftCorner = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        lowerRightCorner = new Point();
        transform = new AffineTransform();
        fontRenderContext = new FontRenderContext(null, false, true);

        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setDecimalSeparator('.');
        df = new DecimalFormat("###.##", s);

        setFont(null);
        setStroke(new BasicStroke());
        setMargins(0, 0);
    }

    public void setMargins(int width, int height) {
        marginWidth = width;
        marginHeight = height;
    }

    public String redefineOperator(String op, String operator) {
        return "/"+op+" { "+operator+" } bind def"+NEWLINE;
    }

    public String getPSText() {
        // Apply margins
        lowerRightCorner.x += marginWidth;
        lowerRightCorner.y += marginHeight;
        upperLeftCorner.x -= marginWidth;
        upperLeftCorner.y -= marginHeight;

        // EPS header
        StringBuilder eps = new StringBuilder();

        eps.append("%!PS-Adobe-3.0 EPSF-3.0");
        eps.append(NEWLINE);
        eps.append("%%Creator: XJGraphics2DPS (c) 2005 by Jean Bovet and Terence Parr");
        eps.append(NEWLINE);

        eps.append("%%BoundingBox: 0 0 ");
        eps.append(lowerRightCorner.x - upperLeftCorner.x);
        eps.append(" ");
        eps.append(lowerRightCorner.y - upperLeftCorner.y);
        eps.append(NEWLINE);

        eps.append("%%Origin: 0 0");
        eps.append(NEWLINE);
        eps.append("%%Pages: 1");
        eps.append(NEWLINE);
        eps.append("%%Page: 1 1");
        eps.append(NEWLINE);
        eps.append("%%EndComments");
        eps.append(NEWLINE);

        // Predefined functions
        eps.append(redefineOperator("tr", "translate"));
        eps.append(redefineOperator("sc", "scale"));
        eps.append(redefineOperator("gs", "gsave"));
        eps.append(redefineOperator("gr", "grestore"));
        eps.append(redefineOperator("m", "moveto"));
        eps.append(redefineOperator("l", "lineto"));
        eps.append(redefineOperator("c", "curveto"));
        eps.append(redefineOperator("f", "fill"));
        eps.append(redefineOperator("s", "stroke"));
        eps.append(redefineOperator("cp", "closepath"));
        eps.append(redefineOperator("rgb", "setrgbcolor"));
        eps.append(redefineOperator("sw", "setlinewidth"));
        eps.append(redefineOperator("sm", "setmiterlimit"));
        eps.append(redefineOperator("sj", "setlinejoin"));
        eps.append(redefineOperator("slc", "setlinecap"));
        eps.append(redefineOperator("sd", "setdash"));

        // Initial offset because Swing and PS have their y-axis inverted
        eps.append(-upperLeftCorner.x);
        eps.append(" ");
        eps.append(upperLeftCorner.y + (lowerRightCorner.y-upperLeftCorner.y));
        eps.append(" tr");
        eps.append(NEWLINE);

        // Append postscript code
        eps.append(ps);

        return eps.toString();
    }

    public void psAppend(double v) {
        psAppend(df.format(v));
    }

    public void psAppend(String s) {
        if(ps.length() > 0) {
            char c = ps.charAt(ps.length()-1);
            if(c != ' ' && !String.valueOf(c).equals(NEWLINE))
                ps.append(' ');
        }
        ps.append(s);
    }

    public void psGSave() {
        psAppend("gs");
        psAppend(NEWLINE);
    }

    public void psGRestore() {
        psAppend("gr");
        psAppend(NEWLINE);
    }

    public void psMoveTo(double x, double y) {
        psAppend(x);
        psAppend(y);
        psAppend("m");
        psAppend(NEWLINE);
    }

    public void psLineTo(double x, double y) {
        psAppend(x);
        psAppend(y);
        psAppend("l");
        psAppend(NEWLINE);
    }

    public void psCurveTo(double x0, double y0, double x1, double y1, double x2, double y2) {
        psAppend(x0);
        psAppend(y0);
        psAppend(x1);
        psAppend(y1);
        psAppend(x2);
        psAppend(y2);
        psAppend("c");
        psAppend(NEWLINE);
    }

    public void psTranslate(double x, double y) {
        psAppend(x);
        psAppend(y);
        psAppend("tr");
        psAppend(NEWLINE);
    }

    public void psScale(double x, double y) {
        psAppend(x);
        psAppend(y);
        psAppend("sc");
        psAppend(NEWLINE);
    }

    public void psFill() {
        psAppend("f");
        psAppend(NEWLINE);
    }

    public void psStroke() {
        psAppend("s");
        psAppend(NEWLINE);
    }

    public void psClosePath() {
        psAppend("cp");
        psAppend(NEWLINE);
    }

    public void psDrawShape(Shape s, boolean fill) {
        double coord[] = new double[6];
        double x0, y0, x1, y1, x2, y2;
        double cpx = 0, cpy = 0;    // current point

        // Transform the shape
        s = transform.createTransformedShape(s);

        // Update the bounding box
        Rectangle2D r = s.getBounds2D();
        upperLeftCorner.x = (int) Math.min(upperLeftCorner.x, r.getMinX());
        upperLeftCorner.y = (int) Math.min(upperLeftCorner.y, r.getMinY());
        lowerRightCorner.x = (int) Math.max(lowerRightCorner.x, r.getMaxX());
        lowerRightCorner.y = (int) Math.max(lowerRightCorner.y, r.getMaxY());

        // Draw the shape using ps operations
        PathIterator iter = s.getPathIterator(null);
        while(!iter.isDone()) {
            int seg = iter.currentSegment(coord);
            x0 = coord[0];
            y0 = -coord[1];
            x1 = coord[2];
            y1 = -coord[3];
            x2 = coord[4];
            y2 = -coord[5];

            switch(seg) {
                case PathIterator.SEG_MOVETO:
                    psMoveTo(x0, y0);
                    cpx = x0; cpy = y0;
                    break;

                case PathIterator.SEG_LINETO:
                    psLineTo(x0, y0);
                    cpx = x0; cpy = y0;
                    break;

                case PathIterator.SEG_CUBICTO:
                    psCurveTo(x0, y0, x1, y1, x2, y2);
                    cpx = x2; cpy = y2;
                    break;

                case PathIterator.SEG_QUADTO:
                    psCurveTo(  cpx+2/3.0*(x0-cpx), cpy+2/3.0*(y0-cpy),
                            x0+1/3.0*(x1-x0), y0+1/3.0*(y1-y0),
                            x1, y1);
                    cpx = x1; cpy = y1;
                    break;

                case PathIterator.SEG_CLOSE:
                    psClosePath();
                    break;
            }
            iter.next();
        }
        if(fill)
            psFill();
        else
            psStroke();
    }

    public String arrayToString(float[] array) {
        StringBuilder sb = new StringBuilder();
        if(array != null) {
            for(int index=0; index<array.length; index++) {
                sb.append(array[index]);
                if(index < array.length-1)
                    sb.append(" ");
            }
        }
        return sb.toString();
    }

    public void draw(Shape s) {
        psDrawShape(s, false);
    }

    public void fill(Shape s) {
        psDrawShape(s, true);
    }

    public void drawString(String str, int x, int y) {
        drawString(str, (float)x, (float)y);
    }

    public void drawString(String s, float x, float y) {
        if(s == null || s.length() == 0)
            return;

        AttributedString as = new AttributedString(s);
        as.addAttribute(TextAttribute.FONT, getFont());
        drawString(as.getIterator(), x, y);
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float)x, (float)y);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        TextLayout layout = new TextLayout(iterator, getFontRenderContext());
        Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
        fill(shape);
    }

    public void drawChars(char data[], int offset, int length, int x, int y) {
        drawString(new String(data, offset, length), x, y);
    }

    @SuppressWarnings("deprecation")
    public void drawBytes(byte data[], int offset, int length, int x, int y) {
        drawString(new String(data, 0, offset, length), x, y);
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        fill(g.getOutline(x, y));
    }

    public Graphics create() {
        return new XJGraphics2DPS();
    }

    public void translate(int x, int y) {
        translate((double)x, (double)y);
    }

    public void translate(double tx, double ty) {
        transform(AffineTransform.getTranslateInstance(tx, ty));
    }

    public void rotate(double theta) {
        transform(AffineTransform.getRotateInstance(theta));
    }

    public void rotate(double theta, double x, double y) {
        transform(AffineTransform.getRotateInstance(theta, x, y));
    }

    public void scale(double sx, double sy) {
        transform(AffineTransform.getScaleInstance(sx, sy));
    }

    public void shear(double shx, double shy) {
        transform(AffineTransform.getShearInstance(shx, shy));
    }

    public void transform(AffineTransform Tx) {
        transform.concatenate(Tx);
    }

    public void setTransform(AffineTransform Tx) {
        if(Tx == null)
            transform = new AffineTransform();
        else
            transform = Tx;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font==null?Font.decode(null):font;
        // Do not use it in Postscript because string are drawed using a shape
        //psAppend("/"+this.font.getPSName()+" findfont "+this.font.getSize()+" scalefont setfont\n");
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        this.color = c;
        psAppend(c.getRed()/255.0);
        psAppend(c.getGreen()/255.0);
        psAppend(c.getBlue()/255.0);
        psAppend("rgb");
        psAppend(NEWLINE);
    }

    public void setBackground(Color color) {
        background = color;
    }

    public Color getBackground() {
        return background;
    }

    public void setStroke(Stroke s) {
        this.stroke = s;
        if(s instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)s;
            psAppend(bs.getLineWidth()+" sw"+NEWLINE);
            psAppend(Math.max(1, bs.getMiterLimit())+" sm"+NEWLINE);
            psAppend(bs.getLineJoin()+" sj"+NEWLINE);
            psAppend(bs.getEndCap()+" slc"+NEWLINE);
            psAppend("["+arrayToString(bs.getDashArray())+"] "+bs.getDashPhase()+" sd"+NEWLINE);
        }
    }

    public Stroke getStroke() {
        return stroke;
    }

    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    public FontMetrics getFontMetrics(Font f) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        return g.getFontMetrics(f);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        draw(new Line2D.Float(x1, y1, x2, y2));
    }

    public void drawRect(int x, int y, int width, int height) {
        draw(new Rectangle(x, y, width, height));
    }

    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle(x, y, width, height));
    }

    public void clearRect(int x, int y, int width, int height) {
        Color oldColor = getColor();
        setColor(background);
        fillRect(x, y, width, height);
        setColor(oldColor);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    public void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x, y, width, height));
    }

    public void fillOval(int x, int y, int width, int height) {
        fill(new Ellipse2D.Float(x, y, width, height));
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE));
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        if(nPoints == 0)
            return;

        GeneralPath path = new GeneralPath();
        path.moveTo(xPoints[0], yPoints[0]);
        for(int p=1; p<nPoints; p++)
            path.lineTo(xPoints[p], yPoints[p]);
        draw(path);
    }

    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        draw(new Polygon(xPoints, yPoints, nPoints));
    }

    public void drawPolygon(Polygon p) {
        draw(p);
    }

    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        fill(new Polygon(xPoints, yPoints, nPoints));
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, Color.white, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return drawImage(img, x, y, width, height, Color.white, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return drawImage(img, x, y, img.getWidth(null), img.getHeight(null), bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return drawImage(img, x, y, x+width, y+height, 0, 0, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.white, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        int width = dx2-dx1;
        int height = dy2-dy1;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.drawImage(img, 0, 0, width, height, sx1, sy1, sx2, sy2, bgcolor, observer);

        psGSave();
        psTranslate(dx1, -(dy1+height));
        psAppend("/picstr "+width *ComponentsPerPixel +" string def"+NEWLINE);
        psScale(width, height);
        psAppend("/displayimage {"+NEWLINE);
        psAppend(width +" "+height +" "+BitsPerComponent+" ["+width +" 0 0 -"+height +" 0 "+height +"]"+NEWLINE);
        psAppend("{currentfile picstr readhexstring pop} false "+ComponentsPerPixel+" colorimage} def"+NEWLINE);
        psAppend("displayimage"+NEWLINE);

        boolean success = true;
        try {
            getPixels(img, 0, 0, width, height);
        } catch (Exception e) {
            System.err.println("XJGraphics2DPS: draw image error ("+e+")");
            success = false;
        }

        psGRestore();

        return success;
    }

    // ************ Terence Parr **********
    // This portion of code has been copied (and modified) from Terence Parr
    // EPSImage.java, (c) March 1999 MageLang Institute, with its authorization

    protected static final int BytesPerComponent = 1;
    protected static final int BitsPerComponent = BytesPerComponent * 8;
    protected static final int ComponentsPerPixel = 3;
    protected static char[] hexmap = {  '0','1','2','3','4',
                                        '5','6','7','8','9',
                                        'A','B','C','D','E','F'};

    /** Walk an image and convert each pixle to RGB triplet.
     *  Adapted from javadoc for PixelGrabber class
     */
    public void getPixels(Image img, int x, int y, int w, int h)
            throws Exception {
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
        pg.grabPixels();
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            throw new Exception("image fetch aborted or errored");
        }
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                getSinglePixel(x+i, y+j, pixels[j * w + i]);
            }
            ps.append(NEWLINE);
        }
    }

    /** Dump an RGB triplet in hex to the output.
     *  Adapted from javadoc for PixelGrabber class
     */
    public void getSinglePixel(int x, int y, int pixel) {
        //int alpha = (pixel >> 24) & 0xFF;
        int red   = (pixel >> 16) & 0xFF;
        int green = (pixel >>  8) & 0xFF;
        int blue  = (pixel      ) & 0xFF;
        char[] hexValue = new char[2];
        ASCIIHexEncode(red, hexValue); ps.append(hexValue);
        ASCIIHexEncode(green, hexValue); ps.append(hexValue);
        ASCIIHexEncode(blue, hexValue); ps.append(hexValue);
    }

    /** Convert a byte to a two-char hex sequence; no Strings allocation
     *  because this will be called a LOT!
     */
    public static void ASCIIHexEncode(int b, char[] c) {
        c[0]=hexmap[b>>4];	// get hi nybble
        c[1]=hexmap[b&0xF]; // get lo nybble
    }

    // ************************************
    // Unsupported operations

    public void dispose() {
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return false;
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return false;
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return null;
    }

    public void setComposite(Composite comp) {
    }

    public void setPaint(Paint paint) {
    }

    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
    }

    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return null;
    }

    public void setRenderingHints(Map hints) {
    }

    public void addRenderingHints(Map hints) {
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

    public void setPaintMode() {
    }

    public void setXORMode(Color c1) {
    }

    public Rectangle getClipBounds() {
        return null;
    }

    public void clipRect(int x, int y, int width, int height) {
    }

    public void setClip(int x, int y, int width, int height) {
    }

    public Shape getClip() {
        return null;
    }

    public void setClip(Shape clip) {
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    }

    public Paint getPaint() {
        return null;
    }

    public Composite getComposite() {
        return null;
    }

    public void clip(Shape s) {
    }

}
