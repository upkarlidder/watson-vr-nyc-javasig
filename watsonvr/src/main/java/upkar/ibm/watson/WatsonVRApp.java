package upkar.ibm.watson;

import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.Classifiers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Optional;

class WatsonVR {

    private Image image;
    private TabFolder tabFolder;
    private TabItem resultItem;
    private String filePath;
    private Scale scale;
    private Boolean isCustomChecked = false;

    private final Display display;
    private final Shell shell;

    WatsonVR() {
        shell = new Shell();
        display = Display.getDefault();
        createUI();
        System.out.println("WatsonVR constructor called ! ");

    }

    private void createUI() {


        this.shell.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));

        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.justify = true;
        rowLayout.wrap = false;
        shell.setLayout(rowLayout);

        Composite topComp = new Composite(shell, SWT.NONE);
        topComp.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
        topComp.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        topComp.setLayout(new RowLayout(SWT.HORIZONTAL));

        // Create the button
        Label lblPicture = new Label(topComp, SWT.SHADOW_IN);
        lblPicture.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
        lblPicture.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        lblPicture.setText("Drag and drop picture here ...");

        lblPicture.setLayoutData(new RowData(1150 / 2 - 15, 650));

        Composite compResult = new Composite(topComp, SWT.CENTER | SWT.SHADOW_IN);
        compResult.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
        compResult.setForeground(display.getSystemColor(SWT.COLOR_BLACK));

        compResult.setLayoutData(new RowData(1150 / 2 - 15, 650));

        compResult.setLayout(new FillLayout());

        tabFolder = new TabFolder(compResult, SWT.BORDER);
        tabFolder.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
        Rectangle clientArea = compResult.getClientArea();
        tabFolder.setLocation(clientArea.x, clientArea.y);

        resultItem = new TabItem(tabFolder, SWT.NONE);
        resultItem.setText("Result");
        tabFolder.pack();


        Composite bottomComp = new Composite(shell, SWT.NONE);
        bottomComp.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
        bottomComp.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        bottomComp.setLayout(new RowLayout(SWT.VERTICAL));


        Group classifierGroup = new Group(bottomComp, SWT.SHADOW_OUT);
        classifierGroup.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label lblGroup = new Label(classifierGroup, SWT.NONE);
        lblGroup.setText("Pick the classifiers: ");
        Button btnClassifierDefault = new Button(classifierGroup, SWT.CHECK);
        btnClassifierDefault.setText("Default");
        Button btnClassifierFood = new Button(classifierGroup, SWT.CHECK);
        btnClassifierFood.setText("Food");
        Button btnClassifierFace = new Button(classifierGroup, SWT.CHECK);
        btnClassifierFace.setText("Face");

        addCustomClassifiers(classifierGroup);

        Composite scaleComp = new Composite(bottomComp, SWT.NONE);
        scaleComp.setLayout(new RowLayout(SWT.VERTICAL));
        scale = new Scale(scaleComp, SWT.HORIZONTAL);
        scale.setMinimum(0);
        scale.setMaximum(100);
        scale.setSelection(40);
        Label scaleLabel = new Label(scaleComp, SWT.NONE);
        scaleLabel.setLayoutData(new RowData(40, 20));
        scaleLabel.setText(Double.toString(scale.getSelection() / 100.0));
        scale.addSelectionListener(getSliderSelectorListener(scaleLabel));


        Button btnAnalyze = new Button(bottomComp, SWT.CENTER);
        btnAnalyze.setText("ANALYZE");
        btnAnalyze.addSelectionListener(getAnalyzeSelectionListener(this.tabFolder, this.resultItem));

        bottomComp.setLayoutData(new RowData(1125, 150));

        // Create the drop target on the button
        DropTarget dt = new DropTarget(lblPicture, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT);
        final FileTransfer fileTransfer = FileTransfer.getInstance();
        final TextTransfer textTransfer = TextTransfer.getInstance();
        dt.setTransfer(new Transfer[]{textTransfer, fileTransfer});
        dt.addDropListener(getDropListener(shell, lblPicture, fileTransfer));

        shell.setSize(1200, 800);
        shell.pack();
        shell.open();
        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
        if (this.image != null && !image.isDisposed())
            image.dispose();
        display.dispose();
    }

    private SelectionListener getSliderSelectorListener(final Label scaleLabel) {
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Scale src = (Scale) e.getSource();
                scaleLabel.setText(Double.toString(src.getSelection() / 100.0));
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
    }

    private void addCustomClassifiers(Composite radioGroup) {
        //TODO: create the radio group here
        final Classifiers classifiers = WatsonUtil.getClassifiers();

        classifiers.getClassifiers().forEach(classifier -> {
            Button btnClassifierFace = new Button(radioGroup, SWT.CHECK);
            btnClassifierFace.setText(classifier.getName());
            btnClassifierFace.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (((Button) e.getSource()).getSelection()) {
                        WatsonVR.this.isCustomChecked = true;
                    } else {
                        WatsonVR.this.isCustomChecked = false;
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {

                }
            });
        });
    }

    private DropTargetAdapter getDropListener(Shell shell, Label lblPicture, FileTransfer fileTransfer) {
        return new DropTargetAdapter() {
            public void drop(DropTargetEvent event) {
                // check for type of data dropped
                if (fileTransfer.isSupportedType(event.currentDataType)) {
                    System.out.println("file transfer initiated");
                    String[] files = (String[]) event.data;
                    final Optional<String> file = Arrays.asList(files).stream().findFirst();

                    if (file.isPresent()) {
                        final String f = file.get();
                        WatsonVR.this.filePath = f;
                        String[] split = f.split("\\.");
                        String ext = split[split.length - 1];
                        WatsonVR.this.image = new Image(shell.getDisplay(), f);
                        ImageData imgData = WatsonVR.this.image.getImageData();
                        imgData.scaledTo(100, 100);
                        lblPicture.setImage(WatsonVR.this.image);
                    } else {
                        throw new IllegalArgumentException("No file found");
                    }
                }
            }
        };
    }

    private SelectionListener getAnalyzeSelectionListener(TabFolder folder, TabItem resultItem) {
        return new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                try {
                    final ClassifiedImages result = WatsonUtil.classify(WatsonVR.this.filePath, WatsonVR.this.scale.getSelection() / 100.0, WatsonVR.this.isCustomChecked);

                    Composite comp = new Composite(folder, SWT.NONE);
                    comp.setLayout(new RowLayout(SWT.VERTICAL));


                    // create label for each classifier and each class within that classifier
                    result.getImages().forEach(image -> {
                        image.getClassifiers().forEach(classifier -> {
                            Label lblClassifier = new Label(comp, SWT.NONE);
                            lblClassifier.setFont(new Font(WatsonVR.this.display, new FontData(WatsonVR.this.display.getSystemFont().getFontData()[0].getName(), 26, SWT.BOLD)));
                            lblClassifier.setBackground(display.getSystemColor(SWT.COLOR_CYAN));
                            lblClassifier.setText(classifier.getName());

                            // sort by score highest to lowest
                            classifier.getClasses().stream()
                                    .sorted((c1, c2) -> Float.compare(c2.getScore(), c1.getScore()))
                                    .forEach(cls -> {
                                        StyledText txt = new StyledText(comp, SWT.READ_ONLY | SWT.SINGLE);
                                        txt.setText(cls.getClassName() + "  :" + cls.getScore());
                                        StyleRange styleRange = new StyleRange();
                                        styleRange.start = 0;
                                        styleRange.length = cls.getClassName().length();
                                        styleRange.font = new Font(WatsonVR.this.display, new FontData(WatsonVR.this.display.getSystemFont().getFontData()[0].getName(), 16, SWT.NONE));
                                        styleRange.fontStyle = SWT.ITALIC | SWT.BOLD;
                                        styleRange.foreground = shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW);
                                        styleRange.background = shell.getDisplay().getSystemColor(SWT.COLOR_MAGENTA);
                                        txt.setStyleRange(styleRange);
                                    });

                        });
                    });

                    resultItem.setControl(comp);

                } catch (FileNotFoundException err) {
                    System.out.println(err.getMessage());
                }


            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println("button widgetDefaultSelected");
            }
        };
    }
}

public class WatsonVRApp {
    public static void main(String[] args) {
        new WatsonVR();
    }
}
