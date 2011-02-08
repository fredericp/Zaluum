package org.zaluum.nide.zge
import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.swt.widgets.Composite
import org.zaluum.nide.model._
import org.zaluum.nide.eclipse.EclipseBoxClasspath
import org.zaluum.nide.newcompiler._
class TreeViewer(parent: Composite, controller: Controller,val global:EclipseBoxClasspath) 
  extends AbstractViewer(parent, controller) with BoxDefLayers{
  /*TOOLS*/
  lazy val imageFactory = new ImageFactory(parent.getDisplay, controller.global)
  val palette = new Palette(this, parent.getShell, controller.global)
  
  /*MODEL*/
  def tree = controller.tree.asInstanceOf[BoxDef]
  def boxDef = tree
  def owner = global.root
  def viewer = this
  def hide = clear
  def show  {} // TODO?
  override def dispose() {
    super.dispose()
    imageFactory.reg.dispose
  }
  val tool : BoxTool =  new BoxTool(this) 
  def gotoMarker(l: Location) {} // TODO
  controller.registerViewer(this)
}
