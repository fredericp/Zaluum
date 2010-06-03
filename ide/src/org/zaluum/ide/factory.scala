package org.zaluum.ide
import org.eclipse.gef._
import org.zaluum.runtime._
object ZaluumFactory extends EditPartFactory {
  def createEditPart(context: EditPart, model: Object): EditPart = model match { 
    case model : VModel => new ModelEditPart(model)
    case cbox : ComposedVBox => new ComposedEditPart(context,cbox)
    case box : VBox => new BoxEditPart(context,box)
    case port : VPort => new PortEditPart(port)
    case wire : VWire => new WireEditPart(wire)
  }
}
