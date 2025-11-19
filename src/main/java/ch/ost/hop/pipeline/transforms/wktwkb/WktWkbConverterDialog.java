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

package ch.ost.hop.pipeline.transforms.wktwkb;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.ITransformDialog;
import org.apache.hop.ui.core.ConstUi;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.widget.ComboVar;
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

import java.util.*;

public class WktWkbConverterDialog extends BaseTransformDialog implements ITransformDialog {

  private static final Class<?> PKG = WktWkbConverterDialog.class; // Needed by Translator

  private final WktWkbConverterMeta input;
  private ComboVar wInputFieldCombo;
  private ComboVar wOutputFieldCombo;
  private Button wWktToWkb;
  private Button wWkbToWkt;
  private Button wLilEndian;
  private Button wBigEndian;
  private Button wSRIDButton;

  private final Map<String, Integer> fields;

  private IRowMeta prevFields;

  public WktWkbConverterDialog(
      Shell parent, IVariables variables, WktWkbConverterMeta in, PipelineMeta pipelineMeta) {
    super(parent, variables, in, pipelineMeta);
    input = in;
    fields = new HashMap<>();
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
    shell.setText(BaseMessages.getString(PKG, "WktWkb.Name"));

    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "WktWkb.TransformName.Label"));
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
    wConversionGroup.setText(BaseMessages.getString(PKG, "WktWkb.Conversion.Label"));
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
    wWktToWkb.setText(BaseMessages.getString(PKG, "WktWkb.Conversion.WKTtoWKB.Button"));
    PropsUi.setLook(wWktToWkb);
    FormData fdWktToWkb = new FormData();
    fdWktToWkb.left = new FormAttachment(0, 0);
    fdWktToWkb.top = new FormAttachment(0, 0);
    wWktToWkb.setLayoutData(fdWktToWkb);

    // "WKB to WKT" radio button
    wWkbToWkt = new Button(wConversionGroup, SWT.RADIO);
    wWkbToWkt.setText(BaseMessages.getString(PKG, "WktWkb.Conversion.WKBtoWKT.Button"));
    PropsUi.setLook(wWkbToWkt);
    FormData fdWkbToWkt = new FormData();
    fdWkbToWkt.left = new FormAttachment(wWktToWkb, 20);
    fdWkbToWkt.top = new FormAttachment(0, 0);
    wWkbToWkt.setLayoutData(fdWkbToWkt);

    wWktToWkb.addSelectionListener(lsSelMod);
    wWkbToWkt.addSelectionListener(lsSelMod);

    // Endianness Selection
    Group wEndiannessGroup = new Group(shell, SWT.SHADOW_NONE);
    wEndiannessGroup.setText(BaseMessages.getString(PKG, "WktWkb.Endianness.Label"));
    PropsUi.setLook(wEndiannessGroup);
    FormLayout endiannessLayout = new FormLayout();
    endiannessLayout.marginWidth = 10;
    endiannessLayout.marginHeight = 10;
    wEndiannessGroup.setLayout(endiannessLayout);

    FormData fdEndiannessGroup = new FormData();
    fdEndiannessGroup.left = new FormAttachment(0, 0);
    fdEndiannessGroup.top = new FormAttachment(wConversionGroup, 10);
    fdEndiannessGroup.right = new FormAttachment(100, 0);
    wEndiannessGroup.setLayoutData(fdEndiannessGroup);

    // "Big Endian" radio button
    wBigEndian = new Button(wEndiannessGroup, SWT.RADIO);
    wBigEndian.setText(BaseMessages.getString(PKG, "WktWkb.BigEndian.Button"));
    PropsUi.setLook(wBigEndian);
    FormData fdBigEndian = new FormData();
    fdBigEndian.left = new FormAttachment(0, 0);
    fdBigEndian.top = new FormAttachment(0, 0);
    wBigEndian.setLayoutData(fdBigEndian);

    // "Little Endian" radio button
    wLilEndian = new Button(wEndiannessGroup, SWT.RADIO);
    wLilEndian.setText(BaseMessages.getString(PKG, "WktWkb.LittleEndian.Button"));
    PropsUi.setLook(wLilEndian);
    FormData fdLilEndian = new FormData();
    fdLilEndian.left = new FormAttachment(wBigEndian, 20);
    fdLilEndian.top = new FormAttachment(0, 0);
    wLilEndian.setLayoutData(fdLilEndian);

    Listener updateEndianness =
        e -> {
          boolean enable = wWktToWkb.getSelection();
          wEndiannessGroup.setEnabled(enable);
          wBigEndian.setEnabled(enable);
          wLilEndian.setEnabled(enable);
        };

    wWktToWkb.addListener(SWT.Selection, updateEndianness);
    wWkbToWkt.addListener(SWT.Selection, updateEndianness);

    wLilEndian.addSelectionListener(lsSelMod);
    wBigEndian.addSelectionListener(lsSelMod);

    // SRID Selection
    Group wSRIDGroup = new Group(shell, SWT.SHADOW_NONE);
    wSRIDGroup.setText(BaseMessages.getString(PKG, "WktWkb.SRID.Label"));
    PropsUi.setLook(wSRIDGroup);
    FormLayout sridLayout = new FormLayout();
    sridLayout.marginWidth = 10;
    sridLayout.marginHeight = 10;
    wSRIDGroup.setLayout(sridLayout);

    FormData fdSRIDGroup = new FormData();
    fdSRIDGroup.left = new FormAttachment(0, 0);
    fdSRIDGroup.top = new FormAttachment(wEndiannessGroup, 10);
    fdSRIDGroup.right = new FormAttachment(100, 0);
    wSRIDGroup.setLayoutData(fdSRIDGroup);

    // "SRID" radio button
    wSRIDButton = new Button(wSRIDGroup, SWT.RADIO);
    wSRIDButton.setText(BaseMessages.getString(PKG, "WktWkb.SRID.Button"));
    wSRIDButton.setToolTipText(BaseMessages.getString(PKG, "WktWkb.SRID.Tooltip"));
    wSRIDButton.setSelection(true);
    PropsUi.setLook(wSRIDButton);
    FormData fdSRIDButton = new FormData();
    fdSRIDButton.left = new FormAttachment(0, 0);
    fdSRIDButton.top = new FormAttachment(0, 0);
    wSRIDButton.setLayoutData(fdSRIDButton);

    wSRIDButton.addSelectionListener(lsSelMod);

    Control inputFieldSelection = createInputFieldSelection(lsMod, wSRIDGroup, margin);
    Control outputFieldSelection = createOutputFieldSelection(lsMod, inputFieldSelection, margin);

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

    setButtonPositions(new Button[] {wOk, wCancel}, margin, null);

    shell
        .getDisplay()
        .asyncExec(
            () -> {
              try {
                IRowMeta row = pipelineMeta.getPrevTransformFields(variables, transformMeta);
                prevFields = row;

                fields.clear();
                for (int i = 0; i < row.size(); i++) {
                  fields.put(row.getValueMeta(i).getName(), i);
                }

                if (!wInputFieldCombo.isDisposed() && !wOutputFieldCombo.isDisposed()) {
                  wInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
                  wOutputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
                }
              } catch (Exception e) {
                logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
              }
            });

    setSize();
    getData();
    input.setChanged(changed);
    setComboValues();

    wBigEndian.setEnabled(wWktToWkb.getSelection());
    wLilEndian.setEnabled(wWktToWkb.getSelection());

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private Control createInputFieldSelection(ModifyListener lsMod, Control attachment, int margin) {
    Label wlInputFieldLabel = new Label(shell, SWT.RIGHT);
    wlInputFieldLabel.setText(BaseMessages.getString(PKG, "WktWkb.InputFieldSelection.Label"));
    PropsUi.setLook(wlInputFieldLabel);
    FormData fdlFilePathLabel = new FormData();
    fdlFilePathLabel.left = new FormAttachment(0, 0);
    fdlFilePathLabel.top = new FormAttachment(attachment, margin);
    wlInputFieldLabel.setLayoutData(fdlFilePathLabel);

    wInputFieldCombo = new ComboVar(variables, shell, SWT.DROP_DOWN | SWT.BORDER);
    PropsUi.setLook(wInputFieldCombo);
    wInputFieldCombo.addModifyListener(lsMod);
    wInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    FormData fdInputField = new FormData();
    fdInputField.left = new FormAttachment(wlInputFieldLabel, margin);
    fdInputField.top = new FormAttachment(attachment, margin);
    fdInputField.right = new FormAttachment(100, 0);
    wInputFieldCombo.setLayoutData(fdInputField);

    wInputFieldCombo.addModifyListener(
        e -> {
          input.setChanged();
        });

    return wInputFieldCombo;
  }

  private Control createOutputFieldSelection(ModifyListener lsMod, Control attachment, int margin) {
    Label wlOutputFieldLabel = new Label(shell, SWT.RIGHT);
    wlOutputFieldLabel.setText(BaseMessages.getString(PKG, "WktWkb.OutputFieldSelection.Label"));
    PropsUi.setLook(wlOutputFieldLabel);
    FormData fdlOutputFieldLabel = new FormData();
    fdlOutputFieldLabel.left = new FormAttachment(0, 0);
    fdlOutputFieldLabel.top = new FormAttachment(attachment, margin);
    wlOutputFieldLabel.setLayoutData(fdlOutputFieldLabel);

    wOutputFieldCombo = new ComboVar(variables, shell, SWT.DROP_DOWN | SWT.BORDER);
    PropsUi.setLook(wOutputFieldCombo);
    wOutputFieldCombo.addModifyListener(lsMod);
    FormData fdSchemaPath = new FormData();
    fdSchemaPath.left = new FormAttachment(wlOutputFieldLabel, margin);
    fdSchemaPath.top = new FormAttachment(attachment, margin);
    fdSchemaPath.right = new FormAttachment(100, 0);
    wOutputFieldCombo.setLayoutData(fdSchemaPath);

    wOutputFieldCombo.addModifyListener(
        e -> {
          input.setChanged();
        });

    return wOutputFieldCombo;
  }

  private Image getImage() {
    return SwtSvgImageUtil.getImage(
        shell.getDisplay(),
        getClass().getClassLoader(),
        "sample.svg",
        ConstUi.LARGE_ICON_SIZE,
        ConstUi.LARGE_ICON_SIZE);
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (input.getOutputField() == null) {
      input.setOutputField("");
    }
    // Get sample text and put it on dialog's text field
    wInputFieldCombo.setText(input.getInputField());
    wOutputFieldCombo.setText(input.getOutputField());

    wTransformName.selectAll();
    wTransformName.setFocus();

    if (input.isWktToWkb()) {
      wWktToWkb.setSelection(true);
    } else {
      wWkbToWkt.setSelection(true);
    }

    if (input.getEndianness() == 0) {
      wBigEndian.setSelection(true);
    } else {
      wLilEndian.setSelection(true);
    }
  }

  private void setComboValues() {
    Runnable fieldLoader =
        () -> {
          try {
            prevFields = pipelineMeta.getPrevTransformFields(variables, transformName);
          } catch (HopException e) {
            prevFields = new RowMeta();
            String msg = BaseMessages.getString(PKG, "WktWkb.DoMapping.UnableToFindInput");
            logError(msg);
          }
          String[] prevTransformFieldNames =
              prevFields != null ? prevFields.getFieldNames() : new String[0];
          Arrays.sort(prevTransformFieldNames);
        };
    shell.getDisplay().asyncExec(fieldLoader);
  }

  private void getInfo(WktWkbConverterMeta in) {
    input.setInputField(wInputFieldCombo.getText());
    input.setOutputField(wOutputFieldCombo.getText());
    input.setWktToWkb(wWktToWkb.getSelection());
    input.setEndianness(wBigEndian.getSelection() ? 0 : 1);
    input.setSRIDEnabled(wLilEndian.getSelection());
  }

  /** Cancel the dialog. */
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
