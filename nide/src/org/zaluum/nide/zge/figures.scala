package org.zaluum.nide.zge
import draw2dConversions._
import org.eclipse.draw2d.{ ColorConstants, Figure, ImageFigure, Polyline }
import org.eclipse.draw2d.geometry.{ Rectangle, Point ⇒ EPoint, Dimension ⇒ EDimension }
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.zaluum.nide.model.{ Point ⇒ MPoint, Dimension, Vector2, Line, Route }
import org.zaluum.nide.newcompiler._
import scala.collection.mutable.Buffer

// TREE SPECIFIC FIGURES

class ImageValFigure(val tree: ValDef, val container: BoxDefContainer) extends ImageFigure with Item with RectFeedback {
  type T = ValDef
  def sym = tree.symbol.asInstanceOf[ValSymbol]
  def size = Dimension(getImage.getBounds.width, getImage.getBounds.height)
  def pos = tree.pos
  def myLayer = container.layer
  override def update() {
    setImage(container.viewer.imageFactory(tree.tpe))
    super.update()
    val l = sym.tpe match {
      case b: BoxTypeSymbol ⇒
        b.ports.values.collect {
          case s: PortSymbol ⇒
            new PortFigure(s.extPos + Vector2(getBounds.x, getBounds.y),
              s, s.dir == In, Some(sym), container)
        }.toList
      case _ ⇒ List()
    }
    helpers.appendAll(l);
  }
}

class LineFigure(l: Line, bdf: BoxDefContainer, val con: Option[ConnectionDef] = None) extends Polyline with Selectable {
  //setAntialias(1)
  setForegroundColor(ColorConstants.gray)
  var complete = false
  var feedback = false
  showComplete
  def showFeedback { feedback = true; calcStyle }
  def hideFeedback { feedback = false; calcStyle }
  def showComplete { complete = true; calcStyle }
  def showIncomplete { complete = false; calcStyle }
  def calcStyle {
    if (feedback) {
      setLineStyle(SWT.LINE_DASH)
      setLineWidth(2)
    } else {
      setLineWidth(1)
      if (complete) {
        setLineStyle(SWT.LINE_SOLID)
      } else {
        setLineStyle(SWT.LINE_DOT)
      }
    }
  }
  def show() {
    setStart(new EPoint(l.from.x, l.from.y))
    setEnd(new EPoint(l.end.x, l.end.y))
    calcStyle
    bdf.connectionsLayer.add(this)
  }
  def hide() {
    if (bdf.connectionsLayer.getChildren.contains(this))
      bdf.connectionsLayer.remove(this)
  }
}
class ConnectionPainter(bdf: BoxDefContainer) {
  val lines = Buffer[LineFigure]()
  def paintRoute(route: Route, con: Option[ConnectionDef] = None) {
    clear()
    route.lines foreach { l ⇒ lines += new LineFigure(l, bdf, con) }
    lines foreach { _.show }
  }
  def clear() {
    lines.foreach { _.hide }
    lines.clear
  }
}
// TODO not really a figure right now... no children
class ConnectionFigure(val tree: ConnectionDef, bdf: BoxDefContainer)
  extends Figure with ShowHide{
  val painter = new ConnectionPainter(bdf)
  def calcRoute = {
    // TODO paint incomplete connections gracefully
    def portFigure(tree: Tree): Option[PortFigure] = tree match {
      case PortRef(v@ValRef(_), portName, in) ⇒ bdf.findPortFigure(v.symbol.name, portName, in)
      case PortRef(ThisRef, portName, in) ⇒ bdf.findPortFigure(portName, in)
      case _ ⇒ None
    }
    def position(tree: Tree): Option[MPoint] = portFigure(tree) map { p ⇒ p.anchor }
    val route = (position(tree.a), position(tree.b)) match {
      case (Some(a), Some(b)) ⇒ Some(Route(a, b))
      case _ ⇒ None
    }
    route
  }
  def show() =
    calcRoute match {
      case Some(r) ⇒ painter.paintRoute(r, Some(tree))
      case None ⇒ painter.clear()
    }
  def hide() {
    painter.clear()
  }
}
