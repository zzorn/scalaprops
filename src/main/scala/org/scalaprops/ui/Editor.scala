package org.scalaprops.ui

import javax.swing.JComponent
import org.scalaprops.Property
import java.util.logging.{Level, Logger}
import util.TitledContainer

/**
 * Base trait for editors that can edit some type of property value.
 */
// TODO: Ability to set to read-only, for just displaying the value
// TODO: Change init method to constructor?  Do listener setup etc outside
trait Editor[T] extends JComponent with TitledContainer {

  private var _value: T = _
  private var _property: Property[T] = null
  private var _uiUpdateOngoing: Boolean = false
  private var _externalUpdateOngoing: Boolean = false

  /**
   * The property that this editor edits.
   */
  def property: Property[T] = _property

  /**
   * Initialized this editor.  Should be called only once.
   */
  private[scalaprops] final def init(property: Property[T]) {
    _property = property

    if (property != null) {
      _value = property.get
      property.addListener(valueChanged)
      val name = property.name.name // TODO: Camel case to space separated
      title = name
    }

    onInit(_value, title)
  }

  /**
   * Call when the editor is removed from the UI.
   * Stops listening to the property and removes memory references.
   */
  final def deInit() {
    if (_property != null) _property.removeListener(valueChanged)

    onDeInit()

    _value = null.asInstanceOf[T]
    _property = null
  }

  private[scalaprops] final def valueChanged(oldValue: T, newValue: T) {
    if (_value != newValue) {
      _value = newValue

      _uiUpdateOngoing = true
      onExternalValueChange(oldValue, newValue)
      _uiUpdateOngoing = false
    }
  }


  protected def value: T = _value

  /**
   * Called only once, when the editor is initialized.
   */
  protected def onInit(initialValue: T, name: String)

  /**
   * Called when the value has changed externally, and the editor should be updated to reflect the current value.
   */
  protected def onExternalValueChange(oldValue: T, newValue: T)

  /**
   * Should be called when the edit has changed, and the property value should be updated.
   */
  protected final def onEditorChange( newValue: T) {
    if (newValue != _value && !_externalUpdateOngoing && !_uiUpdateOngoing) {
      _externalUpdateOngoing = true
      _value = newValue
      try {
        if (property != null) property.set(newValue)
      } catch {
        case e: IllegalArgumentException =>
          Logger.getLogger(getClass.getName).log(Level.WARNING, "Problem when assigning value to property from editor: " + e.getMessage, e)
      } finally {
        _externalUpdateOngoing = false
      }
    }
  }

  /**
   * Called if the editor is de-initialized.
   * May not be called at all.
   */
  protected def onDeInit() {}


}