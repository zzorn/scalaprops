package org.scalaprops.ui.editors

import org.scalaprops.ui.{EditorFactory, Editor}
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JTextArea, JTextField}
import javax.swing.event.{DocumentEvent, DocumentListener}
import org.scalaprops.ui.util.NamedPanel

case class StringEditorFactory(columns: Int = 15) extends EditorFactory[String]{
  protected def createEditorInstance = new StringEditor(columns)
}

/**
 * One-line string editor.
 */
class StringEditor(columns: Int) extends NamedPanel with Editor[String] {
  private val textField: JTextField = new JTextField(columns)

  textField.getDocument.addDocumentListener(new DocumentListener {
    def changedUpdate(e: DocumentEvent) {textChanged()}
    def removeUpdate(e: DocumentEvent) {textChanged()}
    def insertUpdate(e: DocumentEvent) {textChanged()}
  })

  add(textField, "align right")

  private def textChanged() {
    onEditorChange(textField.getText)
  }

  protected def onExternalValueChange(oldValue: String, newValue: String) {
    textField.setText(newValue)
  }

  protected def onInit(initialValue: String, name: String) {
    textField.setText(initialValue)
  }
}