package org.zaluum.nide.eclipse

import org.zaluum.nide.zge.TreeViewer
import org.zaluum.nide.newcompiler.BoxDef
import org.zaluum.nide.newcompiler.Serializer
import org.zaluum.nide.newcompiler.Name
import org.zaluum.nide.protobuf.BoxFileProtos
import org.zaluum.nide.newcompiler.ProtoParser
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.ui.contexts.IContextService
import org.eclipse.swt.events.ShellEvent
import org.eclipse.swt.events.ShellAdapter
import org.eclipse.swt.events.ShellListener
import org.eclipse.swt.events.DisposeListener
import org.eclipse.ui.handlers.HandlerUtil
import org.eclipse.core.commands.AbstractHandler
import org.eclipse.core.commands.ExecutionEvent
import java.io.ByteArrayInputStream
import org.eclipse.core.resources.{IFile, IMarker}
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.swt.SWT
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.widgets.{Composite, Shell}
import org.eclipse.ui.{IEditorSite, IEditorInput, IEditorPart}
import org.eclipse.ui.ide.IGotoMarker
import org.eclipse.ui.part.{EditorPart, FileEditorInput}
import org.zaluum.nide.model.Location
import org.zaluum.nide.zge.{Viewer, Controller}

class GraphicalEditor extends EditorPart with IGotoMarker {

  var viewer: TreeViewer = _
  //var guiViewer : GUIViewer = _
  var shell : Option[Shell] = None 
  def controller = viewer.controller

  def doSave(monitor: IProgressMonitor) {
    val proto = Serializer.proto(viewer.tree.asInstanceOf[BoxDef])
    val in = new ByteArrayInputStream(proto.toByteArray)
    inputFile.setContents(in, true, true, monitor);
    controller.markSaved()
    firePropertyChange(IEditorPart.PROP_DIRTY)
  }

  def doSaveAs() {}

  def init(site: IEditorSite, input: IEditorInput) {
    setSite(site)
    setInput(input)
    setPartName(inputFile.getName)
    val contextService = getSite.getService(classOf[IContextService]).asInstanceOf[IContextService]
    contextService.activateContext("org.zaluum.nide.context")
  }

  def isDirty(): Boolean = { controller.isDirty }

  def isSaveAsAllowed(): Boolean = { false }

  def inputFile = getEditorInput.asInstanceOf[FileEditorInput].getFile
  def input = inputFile.getContents(true)

  def createPartControl(parent: Composite) {
    val globalScope = new EclipseBoxClasspath(inputFile.getProject)
    val className = globalScope.toClassName(inputFile).getOrElse { throw new Exception("Cannot find class name for this file") }
    val proto = BoxFileProtos.BoxClassDef.parseFrom(input)
    val tree = ProtoParser.parse(proto,Some(className)) 
    globalScope.update()
    input.close()
    val controller = new Controller(tree, globalScope)
    controller.addListener(fireDirty)
    viewer = new TreeViewer(parent, controller, globalScope)
    controller.registerViewer(viewer)
    // TODO reopen
  }
  val fireDirty: () ⇒ Unit = () ⇒ firePropertyChange(IEditorPart.PROP_DIRTY)
  def setFocus() { viewer.canvas.setFocus }
  def openGUI() {
    if (!shell.isDefined) {
      val newshell = new Shell(getSite.getShell, SWT.MODELESS | SWT.CLOSE | SWT.RESIZE )
      newshell.setLayout(new FillLayout)
      newshell.setText(getTitle + " GUI");
      //guiViewer= new GUIViewer(newshell, controller)
      newshell.layout()
      newshell.open()
      
      newshell.addDisposeListener(new DisposeListener(){
        override def widgetDisposed(e:DisposeEvent) {
          //guiViewer.dispose()
          shell = None
        }
      });
      shell = Some(newshell)
    }
  }
  override def dispose() {
    controller.removeListener(fireDirty)
    viewer.dispose()
    shell foreach { s => if (!s.isDisposed) s.dispose } 
  }
  override def gotoMarker(marker: IMarker) {
    val str = marker.getAttribute("BLAME").asInstanceOf[String]
    viewer.gotoMarker(Location(str))
    setFocus
  }
  override def getAdapter(cl: Class[_]) = {
    if (cl == classOf[IGotoMarker]) this
    else super.getAdapter(cl)
  }
}
class OpenGUIHandler extends AbstractHandler {
  override def execute(event : ExecutionEvent) = {
    Option(HandlerUtil.getActiveEditor(event)) match {
      case Some(g:GraphicalEditor) => g.openGUI()
      case _ => 
    }
    null
  }
}