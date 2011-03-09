/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.beegisutils.jgrassported;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import eu.hydrologis.jgrass.beegisutils.utils.ImageCache;

/**
 * An SWT widget for freehand painting.
 * 
 * <p>
 * An swt editor that gives the possibility to draw over an image with 
 * different style. The drawn stuff is kept separated from the image.
 * </p>
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class SimpleSWTImageEditor {

    /**
     * The list of possible strokes to choose.
     */
    private final int[] STROKES = {1, 3, 10, 20, 40};
    /**
     * The list of possible stroke transparencies to choose.
     */
    private final int[] ALPHAS = {20, 40, 60, 80, 100};

    /**
     * The canvas on which painting occures.
     */
    private Canvas drawArea = null;
    /**
     * The current stroke color components.
     */
    private int[] strokeRGB = {0, 0, 0};
    /**
     * The current stroke transparency.
     */
    private int strokeAlpha = 255;
    /**
     * The current stroke width.
     */
    private final int[] strokeWidth = {1};
    /**
     * The list of lines that are drawn and saved.
     */
    private List<DressedStroke> lines = null;
    /**
     * The image that is used as background.
     */
    private Image backImage;

    private Image drawnImage;
    private boolean isRemoveMode = false;
    private boolean isDrawMode = false;
    private final Cursor defaultCursor;
    private final Composite mainComposite;
    private final Composite propsComposite;
    private final ScrolledComposite drawAreaScroller;
    private double baseScaleFactor = -1;
    private double scaleFactor = -1;
    private boolean doRotate = false;
    private boolean imageGotRotated = false;
    private Color fColor;
    private ImageData rotatedImageData;

    /**
     * Constructor for the image editor.
     * 
     * @param parent the parent composite.
     * @param style the swt style for the component.
     * @param preloadedLines a list of lines to be drawn.
     * @param backGroundImage a background image to use in the canvas.
     * @param minScroll the minimum dimension for the scrolling.
     * @param doZoom flag that defines if the zoom tools should be added.
     * @param doRotate flag that defines whether the rotate button has to be turned on.
     */
    @SuppressWarnings("nls")
    public SimpleSWTImageEditor( Composite parent, int style, List<DressedStroke> preloadedLines, Image backGroundImage,
            Point minScroll, boolean doZoom, boolean enableRotate ) {
        if (backGroundImage != null)
            this.backImage = backGroundImage;
        if (preloadedLines == null) {
            this.lines = new ArrayList<DressedStroke>();
        } else {
            this.lines = preloadedLines;
        }
        mainComposite = new Composite(parent, style);
        mainComposite.setLayout(new GridLayout());
        propsComposite = new Composite(mainComposite, style);
        int cols = 9;
        if (!doZoom) {
            cols = 6;
        }
        if (!enableRotate) {
            cols = cols - 1;
        }
        propsComposite.setLayout(new GridLayout(cols, false));
        propsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final ImageCombo strokeWidthCombo = new ImageCombo(propsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData strokeWidthComboGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        strokeWidthComboGD.widthHint = 30;
        strokeWidthCombo.setLayoutData(strokeWidthComboGD);
        strokeWidthCombo.add("1", ImageCache.getInstance().getImage(ImageCache.STROKE_WIDTH_1));
        strokeWidthCombo.add("2", ImageCache.getInstance().getImage(ImageCache.STROKE_WIDTH_2));
        strokeWidthCombo.add("3", ImageCache.getInstance().getImage(ImageCache.STROKE_WIDTH_3));
        strokeWidthCombo.add("4", ImageCache.getInstance().getImage(ImageCache.STROKE_WIDTH_4));
        strokeWidthCombo.add("5", ImageCache.getInstance().getImage(ImageCache.STROKE_WIDTH_5));
        strokeWidthCombo.select(0);
        strokeWidthCombo.setToolTipText("stroke width");
        strokeWidthCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = strokeWidthCombo.getSelectionIndex();
                strokeWidth[0] = STROKES[selectedIndex];
            }
        });

        // alpha
        final Combo alphaCombo = new Combo(propsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData alphaComboGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        alphaComboGD.widthHint = 30;
        alphaCombo.setLayoutData(alphaComboGD);
        String[] items = new String[ALPHAS.length];
        for( int i = 0; i < items.length; i++ ) {
            items[i] = ALPHAS[i] + "%";
        }
        alphaCombo.setItems(items);
        alphaCombo.select(ALPHAS.length - 1);
        alphaCombo.setToolTipText("stroke alpha");
        alphaCombo.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                int selectedIndex = alphaCombo.getSelectionIndex();
                int alphaInPercent = ALPHAS[selectedIndex];
                strokeAlpha = 255 * alphaInPercent / 100;
            }
        });

        // color
        final Button colorButton = new Button(propsComposite, SWT.PUSH);
        GridData colorButtonGD = new GridData(SWT.FILL, SWT.CENTER, false, false);
        colorButtonGD.widthHint = 35;
        colorButton.setLayoutData(colorButtonGD);
        colorButton.addPaintListener(new PaintListener(){
            public void paintControl( PaintEvent e ) {
                GC gc = e.gc;
                Rectangle c = gc.getClipping();
                int q = 5;
                Rectangle r = new Rectangle(c.x + q, c.y + q, c.width - 2 * q, c.height - 2 * q);
                if (fColor != null) {
                    fColor.dispose();
                }
                fColor = new Color(alphaCombo.getDisplay(), new RGB(strokeRGB[0], strokeRGB[1], strokeRGB[2]));
                gc.setBackground(fColor);
                gc.fillRectangle(r);
            }
        });
        colorButton.setToolTipText("stroke color");
        colorButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                ColorDialog colorDialog = new ColorDialog(colorButton.getShell());
                colorDialog.setRGB(new RGB(strokeRGB[0], strokeRGB[1], strokeRGB[2]));
                RGB rgb = colorDialog.open();
                if (rgb != null) {
                    strokeRGB = new int[]{rgb.red, rgb.green, rgb.blue};
                    colorButton.redraw();
                }

            }
        });

        // clear all
        Button clearButton = new Button(propsComposite, SWT.PUSH);
        clearButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        clearButton.setImage(ImageCache.getInstance().getImage(ImageCache.ERASE_ALL));
        clearButton.setToolTipText("clear the area from drawings");
        clearButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                boolean answer = MessageDialog.openQuestion(colorButton.getShell(), "Removal warning",
                        "Do you really want to clear the drawing area?");

                if (!answer) {
                    return;
                }

                lines.clear();
                drawArea.redraw();
            }
        });
        // clear shape
        Button removeButton = new Button(propsComposite, SWT.PUSH);
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        removeButton.setImage(ImageCache.getInstance().getImage(ImageCache.ERASE));
        removeButton.setToolTipText("remove selected line");
        removeButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected( SelectionEvent e ) {
                isRemoveMode = true;
                final Cursor cursor = new Cursor(drawArea.getDisplay(), SWT.CURSOR_CROSS);
                drawArea.setCursor(cursor);
            }
        });

        if (doZoom) {
            // zoom all
            Button zoomAllButton = new Button(propsComposite, SWT.PUSH);
            zoomAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            zoomAllButton.setImage(ImageCache.getInstance().getImage(ImageCache.ZOOM_ALL));
            zoomAllButton.setToolTipText("zoom to the whole extend");
            zoomAllButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    applyScale(baseScaleFactor);
                    drawArea.redraw();
                }

            });

            // zoom in
            Button zoomInButton = new Button(propsComposite, SWT.PUSH);
            zoomInButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            zoomInButton.setImage(ImageCache.getInstance().getImage(ImageCache.ZOOM_IN));
            zoomInButton.setToolTipText("zoom in");
            zoomInButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    applyScale(scaleFactor * 1.2);
                    drawArea.redraw();
                }
            });

            // zoom out
            Button zoomOutButton = new Button(propsComposite, SWT.PUSH);
            zoomOutButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            zoomOutButton.setImage(ImageCache.getInstance().getImage(ImageCache.ZOOM_OUT));
            zoomOutButton.setToolTipText("zoom out");
            zoomOutButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    applyScale(scaleFactor / 1.2);
                    drawArea.redraw();
                }
            });

        }
        if (enableRotate) {
            // rotate right
            Button rotateButton = new Button(propsComposite, SWT.PUSH);
            rotateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            rotateButton.setImage(ImageCache.getInstance().getImage(ImageCache.ROTATE));
            rotateButton.setToolTipText("rotate right");
            rotateButton.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected( SelectionEvent e ) {
                    doRotate = true;
                    drawArea.redraw();
                }
            });
        }

        drawAreaScroller = new ScrolledComposite(mainComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        drawArea = new Canvas(drawAreaScroller, SWT.None);
        drawArea.setLayout(new FillLayout());
        defaultCursor = drawArea.getCursor();
        drawAreaScroller.setContent(drawArea);
        drawAreaScroller.setExpandHorizontal(true);
        drawAreaScroller.setExpandVertical(true);
        if (minScroll != null) {
            drawAreaScroller.setMinWidth(minScroll.x * 3);
            drawAreaScroller.setMinHeight(minScroll.y * 3);
        }
        drawAreaScroller.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));

        Listener drawListener = new Listener(){
            int lastX = 0, lastY = 0;
            List<Integer> line = null;
            GC gc = null;

            public void handleEvent( Event event ) {

                if (event.type == SWT.MouseDoubleClick) {
                    if (isRemoveMode) {
                        drawArea.setCursor(defaultCursor);
                        isRemoveMode = false;
                    }
                }
                /*
                 * REMOVE MODE
                 */
                if (isRemoveMode && event.type == SWT.MouseDown) {
                    for( int i = 0; i < lines.size(); i++ ) {
                        DressedStroke stroke = lines.get(i);
                        int x = event.x;
                        int y = event.y;
                        int[] nodes = stroke.getScaledNodes();
                        for( int j = 0; j < nodes.length - 1; j = j + 2 ) {
                            Point2f linePoint = new Point2f(nodes[j], nodes[j + 1]);
                            Point2f clickPoint = new Point2f(x, y);
                            int threshold = stroke.getScaledStrokeWidth();
                            threshold = threshold < 10 ? 10 : threshold;
                            if (clickPoint.distance(linePoint) < threshold) {
                                lines.remove(i);
                                isRemoveMode = false;
                                drawArea.setCursor(defaultCursor);
                                drawArea.redraw();
                                return;
                            }
                        }

                    }

                }
                if (isRemoveMode && (event.type == SWT.MouseMove || event.type == SWT.MouseUp)) {
                    return;
                }

                /*
                 * DRAWING MODE
                 */
                if (scaleFactor == -1) {
                    calculateBaseScaleFactor();
                    applyScale(baseScaleFactor);
                }

                switch( event.type ) {
                case SWT.Paint:
                    if (drawnImage != null)
                        drawnImage.dispose();
                    drawnImage = new Image(drawArea.getDisplay(), drawArea.getBounds());
                    GC gcImage = new GC(drawnImage);
                    // draw the background image
                    if (backImage != null) {
                        Rectangle imgBounds = backImage.getBounds();
                        ImageData backImageData = backImage.getImageData();
                        if (doRotate) {
                            backImageData = rotate(backImageData, SWT.RIGHT);
                            // rotation has to be persisted
                            backImage.dispose();
                            backImage = new Image(drawArea.getDisplay(), backImageData);
                            imgBounds = backImage.getBounds();
                            doRotate = false;
                        }

                        ImageData newImageData = backImageData.scaledTo((int) Math.round(imgBounds.width * scaleFactor),
                                (int) Math.round(imgBounds.height * scaleFactor));
                        Image newImage = new Image(drawArea.getDisplay(), newImageData);
                        gcImage.drawImage(newImage, 0, 0);
                    }
                    // draw the lines
                    for( int i = 0; i < lines.size(); i = i + 1 ) {
                        DressedStroke tmpStroke = lines.get(i);
                        gcImage.setLineWidth(tmpStroke.getScaledStrokeWidth());
                        gcImage.setLineCap(SWT.CAP_ROUND);
                        gcImage.setLineJoin(SWT.JOIN_ROUND);
                        gcImage.setLineStyle(SWT.LINE_SOLID);
                        int[] rgb = tmpStroke.getRgb();
                        gcImage.setForeground(new Color(drawArea.getDisplay(), rgb[0], rgb[1], rgb[2]));
                        gcImage.setAlpha(tmpStroke.getStrokeAlpha());
                        int[] nodes = tmpStroke.getScaledNodes();
                        // at least 4 values to have two points
                        if (nodes.length > 3) {
                            Path p = new Path(drawArea.getDisplay());
                            p.moveTo(nodes[0], nodes[1]);
                            for( int j = 2; j < nodes.length - 1; j = j + 2 ) {
                                p.lineTo(nodes[j], nodes[j + 1]);
                            }
                            gcImage.drawPath(p);
                        }
                    }
                    gc = new GC(drawArea);
                    gc.drawImage(drawnImage, 0, 0);

                    gcImage.dispose();

                    break;
                case SWT.MouseMove:
                    if ((event.stateMask & SWT.BUTTON1) == 0)
                        break;
                    if (line == null)
                        break;
                    line.add(event.x);
                    line.add(event.y);
                    gc = new GC(drawArea);
                    gc.setLineWidth((int) Math.round(strokeWidth[0] * scaleFactor));
                    gc.setLineCap(SWT.CAP_ROUND);
                    gc.setLineJoin(SWT.JOIN_ROUND);
                    gc.setLineStyle(SWT.LINE_SOLID);
                    Color color = new Color(drawArea.getDisplay(), strokeRGB[0], strokeRGB[1], strokeRGB[2]);
                    gc.setForeground(color);
                    gc.setAlpha(255 * strokeAlpha / 100);
                    gc.drawLine(lastX, lastY, event.x, event.y);
                    lastX = event.x;
                    lastY = event.y;
                    gc.dispose();
                    color.dispose();
                    break;
                case SWT.MouseDown:
                    if (isRemoveMode) {
                        break;
                    }
                    lastX = event.x;
                    lastY = event.y;
                    line = new ArrayList<Integer>();
                    line.add(lastX);
                    line.add(lastY);
                    isDrawMode = true;
                    break;
                case SWT.MouseUp:
                    if (isRemoveMode || !isDrawMode)
                        break;

                    lastX = event.x;
                    lastY = event.y;
                    int[] nodes = new int[line.size()];
                    for( int i = 0; i < line.size(); i++ ) {
                        nodes[i] = (int) Math.round((double) line.get(i) / scaleFactor);
                    }
                    DressedStroke newLine = new DressedStroke(nodes);
                    newLine.setStrokeAlpha(strokeAlpha);
                    newLine.setStrokeWidth(new int[]{strokeWidth[0]});
                    newLine.setRgb(new int[]{strokeRGB[0], strokeRGB[1], strokeRGB[2]});
                    newLine.applyScaleFactor(scaleFactor);
                    lines.add(newLine);
                    line.clear();
                    drawArea.redraw();
                    calculateBaseScaleFactor();
                    break;
                case SWT.Resize:
                    break;

                }
            }
        };

        mainComposite.addControlListener(new ControlListener(){
            public void controlResized( ControlEvent e ) {
                calculateBaseScaleFactor();
            }
            public void controlMoved( ControlEvent e ) {
            }
        });

        drawArea.addListener(SWT.MouseDoubleClick, drawListener);
        drawArea.addListener(SWT.MouseDown, drawListener);
        drawArea.addListener(SWT.MouseMove, drawListener);
        drawArea.addListener(SWT.MouseUp, drawListener);
        drawArea.addListener(SWT.Paint, drawListener);

        // add popup menu
        MenuManager popManager = new MenuManager();
        Menu menu = popManager.createContextMenu(drawArea);
        drawArea.setMenu(menu);
        IAction menuAction = new SaveAction(this);
        popManager.add(menuAction);

    }
    /**
     * Getter for the drawn lines.
     * 
     * @return the user drawn lines.
     */
    public List<DressedStroke> getDrawing() {
        return lines;
    }

    /**
     * Getter for the drawing canvas.
     * 
     * @return the canvas object on which the drawing occurs.
     */
    public Canvas getCanvas() {
        return drawArea;
    }

    /**
     * Getter for the parent control.
     * 
     * @return the parent control.
     */
    public Control getMainControl() {
        return mainComposite;
    }

    /**
     * Setter for the background color.
     * 
     * @param backgroundColor the color to set.
     */
    public void setBackgroundColor( Color backgroundColor ) {
        mainComposite.setBackground(backgroundColor);
        propsComposite.setBackground(backgroundColor);
        drawAreaScroller.setBackground(backgroundColor);
        drawArea.setBackground(backgroundColor);
    }

    /**
     * Getter for the image currently drawn in the canvas.
     * 
     * @return the image over which the user draw.
     */
    public Image getImage() {
        if (drawnImage != null)
            drawnImage.dispose();

        // get drawings bounds
        Rectangle bounds = null;
        int maxStrokeWidth = 1;
        for( int i = 0; i < lines.size(); i = i + 1 ) {
            DressedStroke tmpStroke = lines.get(i);
            if (bounds == null) {
                bounds = tmpStroke.getBounds();
            } else {
                bounds.add(tmpStroke.getBounds());
            }
            int width = tmpStroke.getStrokeWidth();
            if (maxStrokeWidth < width) {
                maxStrokeWidth = width;
            }
        }
        if (backImage != null) {
            Rectangle imgBounds = backImage.getBounds();
            if (bounds != null) {
                bounds.add(imgBounds);
            } else {
                bounds = imgBounds;
            }
        }

        if (bounds == null)
            return null;
        bounds = new Rectangle(bounds.x, bounds.y, bounds.width + 2 * maxStrokeWidth, bounds.height + 2 * maxStrokeWidth);

        drawnImage = new Image(drawArea.getDisplay(), bounds);
        GC gcImage = new GC(drawnImage);
        // draw the background image
        if (backImage != null) {
            gcImage.drawImage(backImage, 0, 0);
        }
        // draw the lines
        for( int i = 0; i < lines.size(); i = i + 1 ) {
            DressedStroke tmpStroke = lines.get(i);
            gcImage.setLineWidth(tmpStroke.getStrokeWidth());
            gcImage.setLineCap(SWT.CAP_ROUND);
            gcImage.setLineJoin(SWT.JOIN_ROUND);
            gcImage.setLineStyle(SWT.LINE_SOLID);
            int[] rgb = tmpStroke.getRgb();
            gcImage.setForeground(new Color(drawArea.getDisplay(), rgb[0], rgb[1], rgb[2]));
            gcImage.setAlpha(tmpStroke.getStrokeAlpha());
            int[] nodes = tmpStroke.getNodes();
            // at least 4 values to have two points
            if (nodes.length > 3) {
                Path p = new Path(drawArea.getDisplay());
                p.moveTo(nodes[0], nodes[1]);
                for( int j = 2; j < nodes.length - 1; j = j + 2 ) {
                    p.lineTo(nodes[j], nodes[j + 1]);
                }
                gcImage.drawPath(p);
            }
        }

        gcImage.dispose();
        return drawnImage;
    }

    private void calculateBaseScaleFactor() {
        Rectangle imageBound = null;
        for( DressedStroke line : lines ) {
            Rectangle bounds = line.getBounds();
            if (imageBound == null) {
                imageBound = bounds;
            } else {
                imageBound.add(bounds);
            }
        }
        if (backImage != null) {
            if (imageBound == null) {
                imageBound = backImage.getBounds();
            } else {
                imageBound.add(backImage.getBounds());
            }
        }
        if (imageBound != null) {
            Rectangle mainCompositeBound = mainComposite.getBounds();
            double scaleFactorX = (double) (mainCompositeBound.width - 30) / (double) imageBound.width;
            double scaleFactorY = (double) (mainCompositeBound.height - 30) / (double) imageBound.height;
            baseScaleFactor = mainCompositeBound.width < mainCompositeBound.height ? scaleFactorX : scaleFactorY;
        } else {
            baseScaleFactor = 1.0;
        }
    }

    /**
     * Apply a certain scale to the drawing.
     * 
     * <p>Utility method through which all the scale manipulations
     * should go, in order to have the lines always updated.
     * 
     * @param newScale
     */
    private void applyScale( double newScale ) {
        scaleFactor = newScale;
        for( DressedStroke line : lines ) {
            line.applyScaleFactor(scaleFactor);
        }
    }

    private ImageData rotate( ImageData srcData, int direction ) {
        int bytesPerPixel = srcData.bytesPerLine / srcData.width;
        int destBytesPerLine = (direction == SWT.DOWN) ? srcData.width * bytesPerPixel : srcData.height * bytesPerPixel;
        byte[] newData = new byte[(direction == SWT.DOWN) ? srcData.height * destBytesPerLine : srcData.width * destBytesPerLine];
        int width = 0, height = 0;
        for( int srcY = 0; srcY < srcData.height; srcY++ ) {
            for( int srcX = 0; srcX < srcData.width; srcX++ ) {
                int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
                switch( direction ) {
                case SWT.LEFT: // left 90 degrees
                    destX = srcY;
                    destY = srcData.width - srcX - 1;
                    width = srcData.height;
                    height = srcData.width;
                    break;
                case SWT.RIGHT: // right 90 degrees
                    destX = srcData.height - srcY - 1;
                    destY = srcX;
                    width = srcData.height;
                    height = srcData.width;
                    break;
                case SWT.DOWN: // 180 degrees
                    destX = srcData.width - srcX - 1;
                    destY = srcData.height - srcY - 1;
                    width = srcData.width;
                    height = srcData.height;
                    break;
                }
                destIndex = (destY * destBytesPerLine) + (destX * bytesPerPixel);
                srcIndex = (srcY * srcData.bytesPerLine) + (srcX * bytesPerPixel);
                System.arraycopy(srcData.data, srcIndex, newData, destIndex, bytesPerPixel);
            }
        }
        imageGotRotated = true;
        rotatedImageData = new ImageData(width, height, srcData.depth, srcData.palette, srcData.scanlinePad, newData);
        return rotatedImageData;
    }

    /**
     * @return true if a rotation of the background image was requested.
     */
    public boolean isImageGotRotated() {
        return imageGotRotated;
    }

    /**
     * Get the rotated image in case it is needed for persistence.
     * 
     * @return the rotated image.
     */
    public ImageData getRotatedImageData() {
        return rotatedImageData;
    }

    public static void main( String[] args ) {

        Display display = new Display();
        // ImageData imgD = new ImageData("/Users/moovida/Desktop/Picture3.png");
        // Image img = new Image(display, imgD);
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        new SimpleSWTImageEditor(shell, SWT.None, null, null, new Point(600, 400), true, true);
        shell.open();
        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();

    }
}
