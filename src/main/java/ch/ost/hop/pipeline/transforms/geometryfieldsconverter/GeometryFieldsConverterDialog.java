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

package ch.ost.hop.pipeline.transforms.geometryfieldsconverter;

import ch.ost.hop.pipeline.transforms.geometryfieldsconverter.model.GeometryFormat;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
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
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.util.SwtSvgImageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.util.*;
import java.util.List;

import static ch.ost.hop.pipeline.transforms.geometryfieldsconverter.model.GeometryFormat.*;

public class GeometryFieldsConverterDialog extends BaseTransformDialog implements ITransformDialog {

  private static final Class<?> PKG = GeometryFieldsConverterDialog.class;

  private final GeometryFieldsConverterMeta input;
  private Group inputGroup;
  private Group outputGroup;
  private ComboVar wInputFieldCombo;
  private ComboVar wYInputFieldCombo;
  private ComboVar wOutputFieldCombo;
  private ComboVar wYOutputFieldCombo;
  private TextVar wSRIDField;
  private Button wFromWKTButton;
  private Button wFromWKBButton;
  private Button wFromPCButton;
  private Button wToWKTButton;
  private Button wToWKBButton;
  private Button wToPCButton;
  private Button wLilEndian;
  private Button wBigEndian;
  private Button wSRIDButton;
  private List<Button> fromFormatButtons = new ArrayList<>();
  private List<Button> toFormatButtons = new ArrayList<>();

  private int margin;

  private final Map<String, Integer> fields;

  private IRowMeta prevFields;

  public GeometryFieldsConverterDialog(
      Shell parent,
      IVariables variables,
      GeometryFieldsConverterMeta transformMeta,
      PipelineMeta pipelineMeta) {
    super(parent, variables, transformMeta, pipelineMeta);
    input = transformMeta;
    fields = new HashMap<>();
  }

  @Override
  public String open() {
    Shell parent = getParent();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    PropsUi.setLook(shell);
    shell.setMinimumSize(400, 520);
    setShellImage(shell, input);

    margin = PropsUi.getMargin();

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
    shell.setText(BaseMessages.getString(PKG, "GeometryFields.Name"));

    wlTransformName = new Label(shell, SWT.RIGHT);
    wlTransformName.setText(BaseMessages.getString(PKG, "GeometryFields.TransformName.Label"));
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
    fdTransformName.left = new FormAttachment(wlTransformName, 0);
    fdTransformName.top = new FormAttachment(0, 5);
    fdTransformName.width = 250;
    wTransformName.setLayoutData(fdTransformName);

    Label spacer = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment(0, 0);
    fdSpacer.top = new FormAttachment(wTransformName, 15);
    fdSpacer.right = new FormAttachment(100, 0);
    spacer.setLayoutData(fdSpacer);

    Label wIcon = new Label(shell, SWT.RIGHT);
    wIcon.setImage(getImage());
    FormData fdIcon = new FormData();
    fdIcon.top = new FormAttachment(0, 0);
    fdIcon.right = new FormAttachment(100, 0);
    fdIcon.bottom = new FormAttachment(spacer, 0);
    wIcon.setLayoutData(fdIcon);
    PropsUi.setLook(wIcon);

    Group wConversionGroup = createConversionGroup(spacer, lsSelMod);
    Group wEndiannessGroup = createEndiannessGroup(wConversionGroup, lsSelMod);
    Group wOptionsGroup = createOptionsGroup(wEndiannessGroup, lsSelMod, lsMod);
    inputGroup = createInputGroup(lsMod, wOptionsGroup);
    outputGroup = createOutputGroup(lsMod, inputGroup);

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

    setComboVars();

    setSize();
    getData();
    input.setChanged(changed);
    setComboValues();
    toggleFormatButtons();

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return transformName;
  }

  private Group createConversionGroup(Control attachment, SelectionAdapter lsSelMod) {
    Group wConversionGroup = new Group(shell, SWT.SHADOW_NONE);
    wConversionGroup.setText(BaseMessages.getString(PKG, "GeometryFields.Conversion.Label"));
    PropsUi.setLook(wConversionGroup);
    FormLayout conversionLayout = new FormLayout();
    conversionLayout.marginWidth = 10;
    conversionLayout.marginHeight = 10;
    wConversionGroup.setLayout(conversionLayout);

    FormData fdConversionGroup = new FormData();
    fdConversionGroup.left = new FormAttachment(0, 0);
    fdConversionGroup.top = new FormAttachment(attachment, 10);
    wConversionGroup.setLayoutData(fdConversionGroup);

    Composite fromComposite = new Composite(wConversionGroup, SWT.NONE);
    fromComposite.setLayout(new GridLayout(4, false));
    FormData fdFromComposite = new FormData();
    fdFromComposite.left = new FormAttachment(0, 0);
    fdFromComposite.top = new FormAttachment(0, 0);
    fdFromComposite.right = new FormAttachment(100, 0);
    fromComposite.setLayoutData(fdFromComposite);

    Label wFromLabel = new Label(fromComposite, SWT.RIGHT);
    wFromLabel.setText(BaseMessages.getString(PKG, "GeometryFields.Conversion.From.Label"));
    PropsUi.setLook(wFromLabel);

    wFromWKTButton =
        createFormatButton(
            fromComposite,
            "GeometryFields.Conversion.WKT.Button",
            WKT,
            fromFormatButtons,
            lsSelMod);
    wFromWKBButton =
        createFormatButton(
            fromComposite,
            "GeometryFields.Conversion.WKB.Button",
            WKB,
            fromFormatButtons,
            lsSelMod);
    wFromPCButton =
        createFormatButton(
            fromComposite,
            "GeometryFields.Conversion.PC.Button",
            POINT_COORDINATE,
            fromFormatButtons,
            lsSelMod);

    Composite toComposite = new Composite(wConversionGroup, SWT.NONE);
    toComposite.setLayout(new GridLayout(4, false));
    FormData fdToComposite = new FormData();
    fdToComposite.left = new FormAttachment(0, 0);
    fdToComposite.top = new FormAttachment(fromComposite, 8);
    fdToComposite.right = new FormAttachment(100, 0);
    toComposite.setLayoutData(fdToComposite);

    Label wToLabel = new Label(toComposite, SWT.RIGHT);
    wToLabel.setText(BaseMessages.getString(PKG, "GeometryFields.Conversion.To.Label"));
    PropsUi.setLook(wToLabel);

    wToWKTButton =
        createFormatButton(
            toComposite, "GeometryFields.Conversion.WKT.Button", WKT, toFormatButtons, lsSelMod);
    wToWKBButton =
        createFormatButton(
            toComposite, "GeometryFields.Conversion.WKB.Button", WKB, toFormatButtons, lsSelMod);
    wToPCButton =
        createFormatButton(
            toComposite,
            "GeometryFields.Conversion.PC.Button",
            POINT_COORDINATE,
            toFormatButtons,
            lsSelMod);

    GridData gdLabel = new GridData();
    gdLabel.horizontalAlignment = SWT.RIGHT;
    wFromLabel.setLayoutData(gdLabel);
    wToLabel.setLayoutData(gdLabel);

    Listener fromPCListener =
        e -> {
          wYInputFieldCombo.setVisible(wFromPCButton.getSelection());
          if (wYInputFieldCombo.getText().isEmpty())
            wYInputFieldCombo.setText(input.getYInputField());
          wInputFieldCombo.setToolTipText(
              wFromPCButton.getSelection()
                  ? BaseMessages.getString(PKG, "GeometryFields.InputXFieldSelection.Tooltip")
                  : BaseMessages.getString(PKG, "GeometryFields.InputFieldSelection.Tooltip"));
          inputGroup.setText(
              wFromPCButton.getSelection()
                  ? BaseMessages.getString(PKG, "GeometryFields.Inputs.Label")
                  : BaseMessages.getString(PKG, "GeometryFields.Input.Label"));
        };
    wFromPCButton.addListener(SWT.Selection, fromPCListener);
    Listener toPCListener =
        e -> {
          wYOutputFieldCombo.setVisible(wToPCButton.getSelection());
          if (wYOutputFieldCombo.getText().isEmpty())
            wYOutputFieldCombo.setText(input.getYOutputField());
          wSRIDButton.setEnabled(!wToPCButton.getSelection());
          wOutputFieldCombo.setToolTipText(
              wToPCButton.getSelection()
                  ? BaseMessages.getString(PKG, "GeometryFields.OutputXFieldSelection.Tooltip")
                  : BaseMessages.getString(PKG, "GeometryFields.OutputFieldSelection.Tooltip"));
          outputGroup.setText(
              wToPCButton.getSelection()
                  ? BaseMessages.getString(PKG, "GeometryFields.Outputs.Label")
                  : BaseMessages.getString(PKG, "GeometryFields.Output.Label"));
        };
    wToPCButton.addListener(SWT.Selection, toPCListener);

    return wConversionGroup;
  }

  private Group createEndiannessGroup(Group attachment, SelectionAdapter lsSelMod) {
    Group wEndiannessGroup = new Group(shell, SWT.SHADOW_NONE);
    wEndiannessGroup.setText(BaseMessages.getString(PKG, "GeometryFields.Endianness.Label"));
    PropsUi.setLook(wEndiannessGroup);
    FormLayout endiannessLayout = new FormLayout();
    endiannessLayout.marginWidth = 10;
    endiannessLayout.marginHeight = 10;
    wEndiannessGroup.setLayout(endiannessLayout);

    FormData fdEndiannessGroup = new FormData();
    fdEndiannessGroup.left = new FormAttachment(0, 0);
    fdEndiannessGroup.top = new FormAttachment(attachment, 10);
    fdEndiannessGroup.right = new FormAttachment(100, 0);
    wEndiannessGroup.setLayoutData(fdEndiannessGroup);

    wBigEndian = new Button(wEndiannessGroup, SWT.RADIO);
    wBigEndian.setText(BaseMessages.getString(PKG, "GeometryFields.BigEndian.Button"));
    PropsUi.setLook(wBigEndian);
    FormData fdBigEndian = new FormData();
    fdBigEndian.left = new FormAttachment(0, 0);
    fdBigEndian.top = new FormAttachment(0, 0);
    wBigEndian.setLayoutData(fdBigEndian);

    wLilEndian = new Button(wEndiannessGroup, SWT.RADIO);
    wLilEndian.setText(BaseMessages.getString(PKG, "GeometryFields.LittleEndian.Button"));
    PropsUi.setLook(wLilEndian);
    FormData fdLilEndian = new FormData();
    fdLilEndian.left = new FormAttachment(wBigEndian, 20);
    fdLilEndian.top = new FormAttachment(0, 0);
    wLilEndian.setLayoutData(fdLilEndian);

    Listener updateEndianness =
        e -> {
          boolean enable = wToWKBButton.getSelection();
          wEndiannessGroup.setEnabled(enable);
          wBigEndian.setEnabled(enable);
          wLilEndian.setEnabled(enable);
        };

    for (Button b : toFormatButtons) {
      b.addListener(SWT.Selection, updateEndianness);
    }

    wLilEndian.addSelectionListener(lsSelMod);
    wBigEndian.addSelectionListener(lsSelMod);
    return wEndiannessGroup;
  }

  private Group createOptionsGroup(
      Group attachment, SelectionAdapter lsSelMod, ModifyListener lsMod) {
    Group wOptionsGroup = new Group(shell, SWT.SHADOW_NONE);
    wOptionsGroup.setText(BaseMessages.getString(PKG, "GeometryFields.Options.Label"));
    PropsUi.setLook(wOptionsGroup);
    FormLayout optionsLayout = new FormLayout();
    optionsLayout.marginWidth = 10;
    optionsLayout.marginHeight = 10;
    wOptionsGroup.setLayout(optionsLayout);

    FormData fdOptionsGroup = new FormData();
    fdOptionsGroup.left = new FormAttachment(0, 0);
    fdOptionsGroup.top = new FormAttachment(attachment, 10);
    fdOptionsGroup.right = new FormAttachment(100, 0);
    wOptionsGroup.setLayoutData(fdOptionsGroup);

    wSRIDButton = new Button(wOptionsGroup, SWT.CHECK);
    wSRIDButton.setText(BaseMessages.getString(PKG, "GeometryFields.SRID.Button"));
    wSRIDButton.setToolTipText(BaseMessages.getString(PKG, "GeometryFields.SRID.Button.Tooltip"));
    PropsUi.setLook(wSRIDButton);
    wSRIDButton.addSelectionListener(lsSelMod);

    Listener updateSRIDFieldListener = e -> wSRIDField.setEnabled(wSRIDButton.getSelection());
    wSRIDButton.addListener(SWT.Selection, updateSRIDFieldListener);

    FormData fdSRIDButton = new FormData();
    fdSRIDButton.left = new FormAttachment(0, 0);
    fdSRIDButton.top = new FormAttachment(0, 0);
    wSRIDButton.setLayoutData(fdSRIDButton);

    Label wSRIDFieldLabel = new Label(wOptionsGroup, SWT.RIGHT);
    wSRIDFieldLabel.setText(BaseMessages.getString(PKG, "GeometryFields.SRID.Label"));
    PropsUi.setLook(wSRIDFieldLabel);
    FormData fdSRIDFieldLabel = new FormData();
    fdSRIDFieldLabel.left = new FormAttachment(wSRIDButton, margin);
    fdSRIDFieldLabel.top = new FormAttachment(attachment, 0);
    wSRIDFieldLabel.setLayoutData(fdSRIDFieldLabel);

    wSRIDField = new TextVar(variables, wOptionsGroup, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wSRIDField.setToolTipText(BaseMessages.getString(PKG, "GeometryFields.SRID.Field.Tooltip"));
    PropsUi.setLook(wSRIDField);
    wSRIDField.addModifyListener(lsMod);
    FormData fdSRIDField = new FormData();
    fdSRIDField.left = new FormAttachment(wSRIDFieldLabel, margin);
    fdSRIDField.top = new FormAttachment(attachment, 0);
    fdSRIDField.right = new FormAttachment(100, 0);
    wSRIDField.setLayoutData(fdSRIDField);
    wSRIDField.addModifyListener(e -> input.setChanged());

    wSRIDField.addListener(
        SWT.Verify,
        e -> {
          if (!e.text.matches("\\d*")) {
            e.doit = false;
          }
        });

    return wOptionsGroup;
  }

  private Group createInputGroup(ModifyListener lsMod, Control attachment) {
    Group wInputGroup = new Group(shell, SWT.SHADOW_NONE);
    wInputGroup.setText(BaseMessages.getString(PKG, "GeometryFields.Input.Label"));
    PropsUi.setLook(wInputGroup);
    FormLayout inputLayout = new FormLayout();
    inputLayout.marginWidth = 10;
    inputLayout.marginHeight = 10;
    wInputGroup.setLayout(inputLayout);

    FormData fdInputGroup = new FormData();
    fdInputGroup.left = new FormAttachment(0, 0);
    fdInputGroup.top = new FormAttachment(attachment, 10);
    fdInputGroup.right = new FormAttachment(100, 0);
    wInputGroup.setLayoutData(fdInputGroup);

    wInputFieldCombo = new ComboVar(variables, wInputGroup, SWT.DROP_DOWN | SWT.BORDER);
    wInputFieldCombo.setToolTipText(
        BaseMessages.getString(PKG, "GeometryFields.InputFieldSelection.Tooltip"));
    PropsUi.setLook(wInputFieldCombo);
    wInputFieldCombo.addModifyListener(lsMod);
    wInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    FormData fdInputField = new FormData();
    fdInputField.left = new FormAttachment(0, margin);
    fdInputField.top = new FormAttachment(attachment, margin);
    fdInputField.right = new FormAttachment(100, 0);
    wInputFieldCombo.setLayoutData(fdInputField);
    wInputFieldCombo.addModifyListener(e -> input.setChanged());

    wYInputFieldCombo = new ComboVar(variables, wInputGroup, SWT.DROP_DOWN | SWT.BORDER);
    wYInputFieldCombo.setToolTipText(
        BaseMessages.getString(PKG, "GeometryFields.YInputFieldSelection.Tooltip"));
    PropsUi.setLook(wYInputFieldCombo);
    wYInputFieldCombo.addModifyListener(lsMod);
    wYInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    FormData fdYInputField = new FormData();
    fdYInputField.left = new FormAttachment(0, margin);
    fdYInputField.top = new FormAttachment(wInputFieldCombo, margin);
    fdYInputField.right = new FormAttachment(100, 0);
    wYInputFieldCombo.setLayoutData(fdYInputField);
    wYInputFieldCombo.addModifyListener(e -> input.setChanged());

    return wInputGroup;
  }

  private Group createOutputGroup(ModifyListener lsMod, Control attachment) {
    Group wOutputGroup = new Group(shell, SWT.SHADOW_NONE);
    wOutputGroup.setText(BaseMessages.getString(PKG, "GeometryFields.Output.Label"));
    PropsUi.setLook(wOutputGroup);
    FormLayout outputLayout = new FormLayout();
    outputLayout.marginWidth = 10;
    outputLayout.marginHeight = 10;
    wOutputGroup.setLayout(outputLayout);

    FormData fdOutputGroup = new FormData();
    fdOutputGroup.left = new FormAttachment(0, 0);
    fdOutputGroup.top = new FormAttachment(attachment, 10);
    fdOutputGroup.right = new FormAttachment(100, 0);
    wOutputGroup.setLayoutData(fdOutputGroup);

    wOutputFieldCombo = new ComboVar(variables, wOutputGroup, SWT.DROP_DOWN | SWT.BORDER);
    wOutputFieldCombo.setToolTipText(
        BaseMessages.getString(PKG, "GeometryFields.OutputFieldSelection.Tooltip"));
    PropsUi.setLook(wOutputFieldCombo);
    wOutputFieldCombo.addModifyListener(lsMod);
    FormData fdSchemaPath = new FormData();
    fdSchemaPath.left = new FormAttachment(0, margin);
    fdSchemaPath.top = new FormAttachment(attachment, margin);
    fdSchemaPath.right = new FormAttachment(100, 0);
    wOutputFieldCombo.setLayoutData(fdSchemaPath);
    wOutputFieldCombo.addModifyListener(e -> input.setChanged());

    wYOutputFieldCombo = new ComboVar(variables, wOutputGroup, SWT.DROP_DOWN | SWT.BORDER);
    wYOutputFieldCombo.setToolTipText(
        BaseMessages.getString(PKG, "GeometryFields.YOutputFieldSelection.Tooltip"));
    PropsUi.setLook(wYOutputFieldCombo);
    wYOutputFieldCombo.addModifyListener(lsMod);
    wYOutputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    FormData fdYOutputField = new FormData();
    fdYOutputField.left = new FormAttachment(0, margin);
    fdYOutputField.top = new FormAttachment(wOutputFieldCombo, margin);
    fdYOutputField.right = new FormAttachment(100, 0);
    wYOutputFieldCombo.setLayoutData(fdYOutputField);
    wYOutputFieldCombo.addModifyListener(e -> input.setChanged());

    return wOutputGroup;
  }

  private Button createFormatButton(
      Composite composite,
      String messageKey,
      GeometryFormat format,
      List<Button> buttonList,
      SelectionAdapter lsSelMod) {
    Button button = new Button(composite, SWT.RADIO);
    button.setText(BaseMessages.getString(PKG, messageKey));
    button.setData(format);
    buttonList.add(button);
    button.addSelectionListener(lsSelMod);
    PropsUi.setLook(button);
    GridData gdButton = new GridData();
    gdButton.horizontalIndent = 20;
    button.setLayoutData(gdButton);
    Listener disableFormat = e -> toggleFormatButtons();
    button.addListener(SWT.Selection, disableFormat);
    return button;
  }

  private Image getImage() {
    return SwtSvgImageUtil.getImage(
        shell.getDisplay(),
        getClass().getClassLoader(),
        "sample.svg",
        ConstUi.LARGE_ICON_SIZE,
        ConstUi.LARGE_ICON_SIZE);
  }

  private void setComboVars() {
    IRowMeta prevRowMeta;
    try {
      prevRowMeta = pipelineMeta.getPrevTransformFields(variables, transformMeta);
    } catch (HopTransformException e) {
      throw new RuntimeException(e);
    }
    prevFields = prevRowMeta;

    fields.clear();
    for (int i = 0; i < prevRowMeta.size(); i++) {
      fields.put(prevRowMeta.getValueMeta(i).getName(), i);
    }

    wInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    wYInputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    wOutputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
    wYOutputFieldCombo.setItems(fields.keySet().toArray(new String[0]));
  }

  public void getData() {
    if (input.getOutputField() == null) {
      input.setOutputField("");
    }
    wInputFieldCombo.setText(input.getInputField());
    wOutputFieldCombo.setText(input.getOutputField());

    wTransformName.selectAll();
    wTransformName.setFocus();

    setSelectedFormat(fromFormatButtons, input.getInputFormat());
    setSelectedFormat(toFormatButtons, input.getOutputFormat());

    if (input.isAddSRID()) {
      wSRIDField.setText(Integer.toString(input.getSrid()));
    }

    if (input.getEndianness() == 1) {
      wBigEndian.setSelection(true);
    } else {
      wLilEndian.setSelection(true);
    }

    wYInputFieldCombo.setText(input.getYInputField());
    wYOutputFieldCombo.setText(input.getYOutputField());

    wSRIDButton.setSelection(input.isAddSRID());

    wBigEndian.setEnabled(wFromWKTButton.getSelection());
    wLilEndian.setEnabled(wFromWKTButton.getSelection());
    wSRIDField.setEnabled(wSRIDButton.getSelection());
    wYInputFieldCombo.setVisible(wFromPCButton.getSelection());
    wYOutputFieldCombo.setVisible(wToPCButton.getSelection());
    inputGroup.setText(
        wFromPCButton.getSelection()
            ? BaseMessages.getString(PKG, "GeometryFields.Inputs.Label")
            : BaseMessages.getString(PKG, "GeometryFields.Input.Label"));
    outputGroup.setText(
        wToPCButton.getSelection()
            ? BaseMessages.getString(PKG, "GeometryFields.Outputs.Label")
            : BaseMessages.getString(PKG, "GeometryFields.Output.Label"));
  }

  private void setComboValues() {
    Runnable fieldLoader =
        () -> {
          try {
            prevFields = pipelineMeta.getPrevTransformFields(variables, transformName);
          } catch (HopException e) {
            prevFields = new RowMeta();
            logError(BaseMessages.getString(PKG, "GeometryFields.DoMapping.UnableToFindInput"));
          }
          String[] prevTransformFieldNames;
          if (prevFields != null) {
            prevTransformFieldNames =
                prevFields.getValueMetaList().stream()
                    .filter(Objects::nonNull)
                    .map(IValueMeta::getName)
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);
          } else {
            prevTransformFieldNames = new String[0];
          }
          shell
              .getDisplay()
              .asyncExec(
                  () -> {
                    wInputFieldCombo.setItems(prevTransformFieldNames);
                    wYInputFieldCombo.setItems(prevTransformFieldNames);
                    wOutputFieldCombo.setItems(prevTransformFieldNames);
                    wYOutputFieldCombo.setItems(prevTransformFieldNames);
                  });
        };
    shell.getDisplay().asyncExec(fieldLoader);
  }

  private void getInfo(GeometryFieldsConverterMeta in) {
    input.setInputField(wInputFieldCombo.getText());
    input.setOutputField(wOutputFieldCombo.getText());
    if (input.getInputFormat() == POINT_COORDINATE) {
      input.setYInputField(wYInputFieldCombo.getText());
    } else if (input.getOutputFormat() == POINT_COORDINATE) {
      input.setYOutputField(wYOutputFieldCombo.getText());
    }
    input.setInputFormat(getSelectedFormat(fromFormatButtons));
    input.setOutputFormat(getSelectedFormat(toFormatButtons));

    input.setEndianness(wBigEndian.getSelection() ? 1 : 2);
    input.setAddSRID(wSRIDButton.getSelection() && wSRIDButton.isEnabled());
    input.setSrid(Integer.parseInt(!wSRIDField.getText().isEmpty() ? wSRIDField.getText() : "0"));
  }

  private GeometryFormat getSelectedFormat(List<Button> list) {
    for (Button b : list) {
      if (b.getSelection()) {
        return (GeometryFormat) b.getData();
      }
    }
    return null;
  }

  private void setSelectedFormat(List<Button> list, GeometryFormat format) {
    for (Button b : list) {
      b.setSelection(b.getData() == format);
    }
  }

  private void toggleFormatButtons() {
    Button fromButton, toButton;
    for (int i = 0; i < fromFormatButtons.size(); i++) {
      fromButton = fromFormatButtons.get(i);
      toButton = toFormatButtons.get(i);
      boolean enableFromFormat = !toButton.getSelection();
      boolean enableToFormat = !fromButton.getSelection();
      fromButton.setEnabled(enableFromFormat);
      toButton.setEnabled(enableToFormat);
    }
  }

  private void cancel() {
    transformName = null;
    input.setChanged(changed);
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wTransformName.getText())) {
      return;
    } else if (wSRIDButton.getSelection() && wSRIDField.getText().trim().isEmpty()) {
      wSRIDField.setFocus();
      return;
    } else if (getSelectedFormat(fromFormatButtons) == null
        || getSelectedFormat(toFormatButtons) == null) {
      return;
    }

    getInfo(input);
    transformName = wTransformName.getText();
    dispose();
  }
}
