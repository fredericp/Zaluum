package org.zaluum.nide
import org.eclipse.jface.resource.ImageRegistry
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

object Activator {
  val PLUGIN_ID: String = "org.zaluum.nide"
  var plugin: Activator = _
  def logError(s: String, a: AnyRef) {
    println("ERROR" + s + " " + a)
  }
  def getDefault(): Activator = {
    return plugin;
  }
}

class Activator extends AbstractUIPlugin {

  override def start(context: BundleContext) = {
    super.start(context);
    Activator.plugin = this;
  }

  override def initializeImageRegistry(reg: ImageRegistry) = {
    super.initializeImageRegistry(reg);
    /*Utils.loadIcons(reg,classOf[Icons],"zaluum","stickynote", "composed", "const", "value", "instance", "portin", "portout", "wire");
    Utils.loadImage(reg,"portInD", classOf[Icons]);
    Utils.loadImage(reg,"portInND", classOf[Icons]);
    Utils.loadImage(reg,"portOutND", classOf[Icons]);*/
  }

  override def getImageRegistry(): ImageRegistry = {
    return super.getImageRegistry();
  }

}
