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
package eu.hydrologis.jgrass.geonotes;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * The textarea of a geonote.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TextAreaUI implements KeyListener {

    private Text textArea = null;
    private String text = null;
    private boolean isDirty = false;

    public TextAreaUI( Composite parent, int style, Color backgroundColor ) {
        textArea = new Text(parent, style);
        textArea.setBackground(backgroundColor);
        textArea.addKeyListener(this);
    }

    public Text getTextArea() {
        return textArea;
    }

    public String getText() {
        return text;
    }

    public void setText( String text ) {
        this.text = text;
    }
    
    public void keyPressed( KeyEvent arg0 ) {
    }

    public void keyReleased( KeyEvent arg0 ) {
        text = textArea.getText();
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }
}
