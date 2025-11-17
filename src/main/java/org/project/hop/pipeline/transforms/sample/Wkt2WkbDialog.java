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
import org.apache.hop.ui.core.PropsUi;
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

public class Wkt2WkbDialog extends BaseTransformDialog implements ITransformDialog {

  private static final Class<?> PKG = Wkt2WkbDialog.class; // Needed by Translator

  private final Wkt2WkbMeta input;
  private TextVar wInputField;
  private TextVar wOutputField;
  private Button wWktToWkb;
  private Button wWkbToWkt;

  public Wkt2WkbDialog(
      Shell parent, IVariables variables, Object in, PipelineMeta pipelineMeta, String sname) {
    super(parent, variables, (BaseTransformMeta) in, pipelineMeta, sname);
    input = (Wkt2WkbMeta) in;
  }

  @Override
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    PropsUi.setLook(shell);
    shell.setMinimumSize(400, 520);
    setShellImage(shell, input);

    int margin = PropsUi.getMargin();
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
    shell.setText(BaseMessages.getString(PKG, "Wkt2Wkb.Shell.Title"));

    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "Wkt2Wkb.TransformName.Label"));
    PropsUi.setLook(wlTransformName);
    fdlTransformName = new FormData();
    fdlTransformName.left = new FormAttachment(0, 0);
    fdlTransformName.top = new FormAttachment(0, 0);
    wlTransformName.setLayoutData(fdlTransformName);

    wTransformName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wTransformName.setText(transformName);
    PropsUi.setLook(wTransformName);
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
    PropsUi.setLook(wicon);

    // Radio buttons for conversion direction
    Group wConversionGroup = new Group(shell, SWT.SHADOW_NONE);
    wConversionGroup.setText("Conversion Direction");
    PropsUi.setLook(wConversionGroup);
    FormLayout conversionLayout = new FormLayout();
    conversionLayout.marginWidth = 10;
    conversionLayout.marginHeight = 10;
    wConversionGroup.setLayout(conversionLayout);

    FormData fdConversionGroup = new FormData();
    fdConversionGroup.left = new FormAttachment(0, 0);
    fdConversionGroup.top = new FormAttachment(spacer, 10);
    fdConversionGroup.right = new FormAttachment(100, 0);
    wConversionGroup.setLayoutData(fdConversionGroup);

// "WKT to WKB" radio button
    wWktToWkb = new Button(wConversionGroup, SWT.RADIO);
    wWktToWkb.setText("WKT to WKB");
    PropsUi.setLook(wWktToWkb);
    FormData fdWktToWkb = new FormData();
    fdWktToWkb.left = new FormAttachment(0, 0);
    fdWktToWkb.top = new FormAttachment(0, 0);
    wWktToWkb.setLayoutData(fdWktToWkb);

// "WKB to WKT" radio button
    wWkbToWkt = new Button(wConversionGroup, SWT.RADIO);
    wWkbToWkt.setText("WKB to WKT");
    PropsUi.setLook(wWkbToWkt);
    FormData fdWkbToWkt = new FormData();
    fdWkbToWkt.left = new FormAttachment(wWktToWkb, 20);
    fdWkbToWkt.top = new FormAttachment(0, 0);
    wWkbToWkt.setLayoutData(fdWkbToWkt);

    wWktToWkb.addSelectionListener(lsSelMod);
    wWkbToWkt.addSelectionListener(lsSelMod);

    Control InputFieldSelection = createInputFieldSelection(lsMod, wConversionGroup, margin);
    createOutputFieldSelection(lsMod, InputFieldSelection, margin);

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

  private Control createOutputFieldSelection(ModifyListener lsMod, Control attachment, int margin) {
    Label wlOutputFieldLabel = new Label(shell, SWT.RIGHT);
    wlOutputFieldLabel.setText(BaseMessages.getString(PKG, "Wkt2Wkb.OutputFieldSelection.Label"));
    PropsUi.setLook(wlOutputFieldLabel);
    FormData fdlOutputFieldLabel = new FormData();
    fdlOutputFieldLabel.left = new FormAttachment(0, 0);
    fdlOutputFieldLabel.top = new FormAttachment(attachment, margin);
    wlOutputFieldLabel.setLayoutData(fdlOutputFieldLabel);

    wOutputField = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wOutputField);
    wOutputField.addModifyListener(lsMod);
    FormData fdSchemaPath = new FormData();
    fdSchemaPath.left = new FormAttachment(wlOutputFieldLabel, margin);
    fdSchemaPath.top = new FormAttachment(attachment, margin);
    fdSchemaPath.right = new FormAttachment(100, 0); // extend to the right edge
    wOutputField.setLayoutData(fdSchemaPath);

    return wOutputField;
  }

  private Control createInputFieldSelection(ModifyListener lsMod, Control attachment, int margin) {
    Label wlInputFieldLabel = new Label(shell, SWT.RIGHT);
    wlInputFieldLabel.setText(BaseMessages.getString(PKG, "Wkt2Wkb.InputFieldSelection.Label"));
    PropsUi.setLook(wlInputFieldLabel);
    FormData fdlFilePathLabel = new FormData();
    fdlFilePathLabel.left = new FormAttachment(0, 0);
    fdlFilePathLabel.top = new FormAttachment(attachment, margin);
    wlInputFieldLabel.setLayoutData(fdlFilePathLabel);

    wInputField = new TextVar(variables, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wInputField);
    wInputField.addModifyListener(lsMod);
    FormData fdInputField = new FormData();
    fdInputField.left = new FormAttachment(wlInputFieldLabel, margin);
    fdInputField.top = new FormAttachment(attachment, margin);
    fdInputField.right = new FormAttachment(100, 0); // extend to the right edge
    wInputField.setLayoutData(fdInputField);

    return wInputField;
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
    if (input.getOutputField() == null) {
      input.setOutputField("");
    }
    // Get sample text and put it on dialog's text field
    wInputField.setText(input.getInputField());
    wOutputField.setText(input.getOutputField());

    wTransformName.selectAll();
    wTransformName.setFocus();

    if (input.isWktToWkb()) {
      wWktToWkb.setSelection(true);
    } else {
      wWkbToWkt.setSelection(true);
    }
  }

  /**
   * save data to metadata
   *
   * @param in
   */
  private void getInfo(Wkt2WkbMeta in) {
    // Save sample text content
    input.setInputField(wInputField.getText());
    input.setOutputField(wOutputField.getText());
    input.setWktToWkb(wWktToWkb.getSelection());
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
