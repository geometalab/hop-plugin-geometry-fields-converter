/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.project.hop.pipeline.transforms.sample;

import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.SwtSvgImageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

public class SampleDialog extends BaseTransformDialog implements ITransformDialog {
    private static final Class<?> PKG = SampleDialog.class; // Needed by Translator

    private final SampleMeta input;
    private TextVar wCSVPath;
    private TextVar wSchemaPath;

    public SampleDialog(
            Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
        super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
        input = (SampleMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
        props.setLook(shell);
        shell.setMinimumSize(400, 520);
        setShellImage(shell, input);

        int margin = props.getMargin();
        int middle = props.getMiddlePct();

        ModifyListener lsMod = e -> input.setChanged();
        SelectionAdapter lsSelMod =
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent arg0) {
                        input.setChanged();
                    }
                };
        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 15;
        formLayout.marginHeight = 15;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "SampleTransform.Shell.Title"));

        // TransformName line
        wlTransformName = new Label(shell, SWT.RIGHT);
        wlTransformName.setText(BaseMessages.getString(PKG, "SampleTransform.TransformName.Label"));
        props.setLook(wlTransformName);
        fdlTransformName = new FormData();
        fdlTransformName.left = new FormAttachment(0, 0);
        fdlTransformName.top = new FormAttachment(0, 0);
        wlTransformName.setLayoutData(fdlTransformName);

        wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wTransformName.setText(transformName);
        props.setLook(wTransformName);
        wTransformName.addModifyListener(lsMod);
        fdTransformName = new FormData();
        fdTransformName.width = 150;
        fdTransformName.left = new FormAttachment(0, 0);
        fdTransformName.top = new FormAttachment(wlTransformName, 5);
        fdTransformName.width = 250;
        wTransformName.setLayoutData(fdTransformName);

        Label spacer = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        FormData fdSpacer = new FormData();
        fdSpacer.height = 2;
        fdSpacer.left = new FormAttachment(0, 0);
        fdSpacer.top = new FormAttachment(wTransformName, 15);
        fdSpacer.right = new FormAttachment(100, 0);
        spacer.setLayoutData(fdSpacer);

        Label wicon = new Label(shell, SWT.RIGHT);
        wicon.setImage(getImage());
        FormData fdlicon = new FormData();
        fdlicon.top = new FormAttachment(0, 0);
        fdlicon.right = new FormAttachment(100, 0);
        fdlicon.bottom = new FormAttachment(spacer, 0);
        wicon.setLayoutData(fdlicon);
        props.setLook(wicon);

        Label wlCSVPathLabel = new Label(shell, SWT.RIGHT);
        wlCSVPathLabel.setText(BaseMessages.getString(PKG, "SampleTransform.CSVPath.Label"));
        props.setLook(wlCSVPathLabel);
        FormData fdlCSVPathLabel = new FormData();
        fdlCSVPathLabel.left = new FormAttachment(0, 0);
        fdlCSVPathLabel.top = new FormAttachment(spacer, margin);
        wlCSVPathLabel.setLayoutData(fdlCSVPathLabel);

        // Text field for the file path
        wCSVPath = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wCSVPath);
        wCSVPath.addModifyListener(lsMod);
        FormData fdCSVPath = new FormData();
        fdCSVPath.left = new FormAttachment(wlCSVPathLabel, margin);
        fdCSVPath.top = new FormAttachment(spacer, margin);
        fdCSVPath.right = new FormAttachment(85, 0);
        wCSVPath.setLayoutData(fdCSVPath);

        // "Browse" button to open file dialog
        Button wbBrowse = new Button(shell, SWT.PUSH);
        wbBrowse.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        props.setLook(wbBrowse);
        FormData fdbBrowse = new FormData();
        fdbBrowse.left = new FormAttachment(wCSVPath, margin);
        fdbBrowse.top = new FormAttachment(wCSVPath, 0, SWT.TOP);
        fdbBrowse.right = new FormAttachment(100, 0);
        wbBrowse.setLayoutData(fdbBrowse);

        // file dialog logic
        wbBrowse.addListener(SWT.Selection, e -> {
            FileDialog dialog = new FileDialog(shell, SWT.OPEN);
            dialog.setFilterExtensions(new String[] {"*.csv", "*.*"});
            String selected = dialog.open();
            if (selected != null) {
                wCSVPath.setText(selected);
            }
        });

        // Add a syntax file path field
        Label wlSchemaPathFieldLabel = new Label(shell, SWT.RIGHT);
        wlSchemaPathFieldLabel.setText(BaseMessages.getString(PKG, "SampleTransform.SchemaPath.Label"));
        props.setLook(wlSchemaPathFieldLabel);
        FormData fdlSchemaPathFieldLabel = new FormData();
        fdlSchemaPathFieldLabel.left = new FormAttachment(0, 0);
        fdlSchemaPathFieldLabel.top = new FormAttachment(wCSVPath, margin);
        wlSchemaPathFieldLabel.setLayoutData(fdlSchemaPathFieldLabel);

        wSchemaPath = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wSchemaPath);
        wSchemaPath.addModifyListener(lsMod);
        FormData fdSchemaPathField = new FormData();
        fdSchemaPathField.left = new FormAttachment(wlSchemaPathFieldLabel, margin);
        fdSchemaPathField.top = new FormAttachment(wCSVPath, margin);
        fdSchemaPathField.right = new FormAttachment(100, 0);
        wSchemaPath.setLayoutData(fdSchemaPathField);

        // Some buttons
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        FormData fdCancel = new FormData();
        fdCancel.right = new FormAttachment(100, 0);
        fdCancel.bottom = new FormAttachment(100, 0);
        wCancel.addListener(SWT.Selection, e -> cancel());
        wCancel.setLayoutData(fdCancel);

        wOk = new Button(shell, SWT.PUSH);
        wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        FormData fdOk = new FormData();
        fdOk.right = new FormAttachment(wCancel, -5);
        fdOk.bottom = new FormAttachment(100, 0);
        wOk.setLayoutData(fdOk);
        wOk.addListener(SWT.Selection, e -> ok());

        setButtonPositions(new Button[]{wOk, wCancel}, margin, null);

        // Set the shell size, based upon previous time...
        setSize();
        getData();

        input.setChanged(changed);

        BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

        return transformName;
    }

    private Image getImage() {
        return SwtSvgImageUtil.getImage(
                shell.getDisplay(),
                getClass().getClassLoader(),
                "sample.svg",
                ConstUi.LARGE_ICON_SIZE,
                ConstUi.LARGE_ICON_SIZE);
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData() {
        if(input.getSchemaPath() ==null){
            input.setSchemaPath("");
        }
        // Get sample text and put it on dialog's text field
        wCSVPath.setText(input.getCSVPath());
        wSchemaPath.setText(input.getSchemaPath());

        wTransformName.selectAll();
        wTransformName.setFocus();
    }

    /**
     * save data to metadata
     *
     * @param in
     */
    private void getInfo(SampleMeta in) {
        // Save sample text content
        input.setCSVPath(wCSVPath.getText());
        input.setSchemaPath(wSchemaPath.getText());
    }

    /**
     * Cancel the dialog.
     */
    private void cancel() {
        transformName = null;
        input.setChanged(changed);
        dispose();
    }

    private void ok() {
        if (Utils.isEmpty(wTransformName.getText())) {
            return;
        }

        getInfo(input);
        transformName = wTransformName.getText(); // return value
        dispose();
    }
}
