package eu.hydrologis.jgrass.featureeditor.utils;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MigLayoutExample1 {

    @SuppressWarnings("nls")
    public MigLayoutExample1() {

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new MigLayout("insets 20 20 20 20", "[right][grow][right][grow]"));
        // shell.setBackground(display.getSystemColor(SWT.COLOR_RED));

        Label generalLabel = new Label(shell, SWT.NONE);
        generalLabel.setLayoutData("split, span, gaptop 10, gapbottom 30");
        generalLabel.setText("General");

        Label separatorLabel = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separatorLabel.setLayoutData("growx, wrap, gaptop 10, gapbottom 30");

        Label companyLabel = new Label(shell, SWT.NONE);
        companyLabel.setLayoutData("gap 10");
        companyLabel.setText("Company");

        Text companyText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        companyText.setLayoutData("span, growx");
        companyText.setText("");

        Label contactLabel = new Label(shell, SWT.NONE);
        contactLabel.setLayoutData("gap 10");
        contactLabel.setText("Contact");

        Text contactText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        contactText.setLayoutData("span, growx, wrap");
        contactText.setText("");

        Label propellerLabel = new Label(shell, SWT.NONE);
        propellerLabel.setLayoutData("split, span, gaptop 40, gapbottom 30");
        propellerLabel.setText("Propeller");

        Label separator2Label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator2Label.setLayoutData("growx, wrap, gaptop 40, gapbottom 30");

        Label ptikwLabel = new Label(shell, SWT.NONE);
        ptikwLabel.setLayoutData("gap 10");
        ptikwLabel.setText("PTI/KW");

        Text ptiKwText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        ptiKwText.setLayoutData("span 1, growx");
        ptiKwText.setText("");

        Label powerkwLabel = new Label(shell, SWT.NONE);
        powerkwLabel.setLayoutData("span 1 2, gapleft 20");
        powerkwLabel.setText("power/KW");
        
        Text powerkwText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        powerkwText.setLayoutData("span 1 2, growx, wrap");
        powerkwText.setText("");

        Label rmmLabel = new Label(shell, SWT.NONE);
        rmmLabel.setLayoutData("gap 10");
        rmmLabel.setText("R/mm");
        
        Text rmmText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        rmmText.setLayoutData("span 1, growx, wrap");
        rmmText.setText("");
        
        Label dmmLabel = new Label(shell, SWT.NONE);
        dmmLabel.setLayoutData("gapleft 20");
        dmmLabel.setText("D/mm");
        
        Text dmmText = new Text(shell, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        dmmText.setLayoutData("span 1, growx");
        dmmText.setText("");

        shell.setSize(400, 400);
        shell.open();
        while( !shell.isDisposed() ) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();

    }
    public static void main( String[] args ) {
        new MigLayoutExample1();

    }
}
