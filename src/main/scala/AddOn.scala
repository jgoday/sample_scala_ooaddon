package sample.oo

import scala.collection.mutable.{ListBuffer}

import com.sun.star.util.{URL}
import com.sun.star.lib.uno.helper.{Factory, WeakBase}
import com.sun.star.lang.{XInitialization, XServiceInfo, XSingleComponentFactory}
import com.sun.star.frame.{DispatchDescriptor, XDesktop, XModel,
                           XDispatch, XDispatchProvider, XStatusListener, XFrame}
import com.sun.star.registry.{XRegistryKey}
import com.sun.star.uno.{UnoRuntime, XComponentContext}
import com.sun.star.awt.{XToolkit, XWindowPeer, XMessageBox, WindowAttribute,
                         WindowClass, WindowDescriptor, Rectangle}

object SampleAddon  {
    val _serviceNames = Array("scala.oo.ProtocolHandler")

    def serviceNames = _serviceNames
}

/**
 * Allow us easy pattern matching against com.sun.star.util.URL
 * to compare protocol and path
 */
case class SUrl(protocol: String, path: String)
object SUrl {
    def apply(aUrl: URL) = new SUrl(aUrl.Protocol, aUrl.Path)
}

class SampleAddonImpl(ctx: XComponentContext) extends WeakBase
                         with XDispatchProvider
                         with XDispatch
                         with XInitialization
                         with XServiceInfo {

    // The component context, that gives access to the service manager and all registered services
    private var _xCmpCtx: XComponentContext = ctx
    // The toolkit, that we can create UNO dialogs
    private var _xToolkit: XToolkit = _
    // The frame where the addon depends on
    private var _xFrame: XFrame = _
    private var _xStatusListener: XStatusListener = _

    /** This method is a member of the interface for initializing an object
      * directly after its creation.
      * @param object This array of arbitrary objects will be passed to the
      * component after its creation.
      * @throws Exception Every exception will not be handled, but will be
      * passed to the caller.
      */
    def initialize(objects: Array[Object]) = {
        Log("SampleAddon -> initialize")

        if (objects.length > 0) {
            _xFrame = UnoHelper.queryInterface[XFrame](objects(0))
        }

        // Create the toolkit to have access to it later
        _xToolkit = UnoHelper.queryInterface[XToolkit](
            ServiceHelper.service("com.sun.star.awt.Toolkit", _xCmpCtx)
        )
    }

    /** This method returns an array of all supported service names.
      * @return Array of supported service names.
      */
    def getSupportedServiceNames(): Array[String] = SampleAddon.serviceNames

    /** This method returns true, if the given service will be
     * supported by the component.
     * @param stringService Service name.
     * @return True, if the given service name will be supported.
     */
    def supportsService(sService: String): Boolean = SampleAddon.serviceNames.contains(sService)

    /** Return the class name of the component.
     * @return Class name of the component.
     */
    def getImplementationName(): String = classOf[SampleAddonImpl].getName

    // XDispatchProvider
    def queryDispatch(aURL: com.sun.star.util.URL,
                      sTargetFrameName: String,
                      iSearchFlags: Int): XDispatch = {
        Log("SampleAddon -> queryDispatch -> protocol " + aURL.Protocol)

        var xRet: XDispatch = SUrl(aURL) match {
            case SUrl("sample.oo.ProtocolHandler:", _) => this
            case _ => null
        }

        return xRet
    }

    def queryDispatches(seqDescripts: Array[DispatchDescriptor]): Array[XDispatch] = {
        val nCount = seqDescripts.length
        var lDispatcher: ListBuffer[XDispatch] = ListBuffer[XDispatch]()

        for(i <- 0 to nCount)
            lDispatcher += queryDispatch(seqDescripts(i).FeatureURL,
                                         seqDescripts(i).FrameName,
                                         seqDescripts(i).SearchFlags)
        lDispatcher.toArray
    }

    // XDispatch
    def dispatch(aURL: com.sun.star.util.URL,
                 aArguments: Array[com.sun.star.beans.PropertyValue]): Unit = {
        Log("SampleAddon -> dispatch -> protocol = " + aURL.Protocol)

        SUrl(aURL) match {
            case SUrl("sample.oo.ProtocolHandler:", "Function1")  =>
                    showMessageBox("SDK DevGuide Add-On",  _currentDoc.getURL)
            case SUrl("sample.oo.ProtocolHandler:", "Function2")  =>
                    showMessageBox("SDK DevGuide Add-On", "Function 2")
            case SUrl("sample.oo.ProtocolHandler:", "Help")  =>
                    showMessageBox("About SDK DevGuide Add-On ", "This is the SDK Add-On example")
            case _ => None
        }
    }

    def addStatusListener(xControl: XStatusListener,
                          aURL: com.sun.star.util.URL): Unit = None

    def removeStatusListener(xControl: XStatusListener,
                            aURL: com.sun.star.util.URL): Unit = None

    def showMessageBox(sTitle: String, sMessage: String): Unit = try {
        Log("SampleAddon -> showMessageBox xframe = " + _xFrame)
        Log("SampleAddon -> showMessageBox xToolkit = " + _xToolkit)

        if (null != _xFrame && null != _xToolkit ) {
                    // describe window properties.
            val aDescriptor: WindowDescriptor = new WindowDescriptor
            aDescriptor.Type              = WindowClass.MODALTOP
            aDescriptor.WindowServiceName = "infobox"
            aDescriptor.ParentIndex       = -1
            aDescriptor.Parent            = UnoHelper.queryInterface[XWindowPeer](_xFrame.getContainerWindow)
            aDescriptor.Bounds            = new Rectangle(0, 0, 300, 200)
            aDescriptor.WindowAttributes  = WindowAttribute.BORDER |
                        WindowAttribute.MOVEABLE |
                        WindowAttribute.CLOSEABLE

            val xPeer: XWindowPeer  = _xToolkit.createWindow(aDescriptor)
            if ( null != xPeer ) {
                var xMsgBox: XMessageBox = UnoHelper.queryInterface[XMessageBox](xPeer)
                if (null != xMsgBox) {
                    xMsgBox.setCaptionText(sTitle)
                    xMsgBox.setMessageText(sMessage)
                    xMsgBox.execute
                }
            }
        }
    }
    catch {
        case e: Exception => Log(e.getMessage)
    }

    private def _currentDoc = {
        val desktop = ServiceHelper.service("com.sun.star.frame.Desktop", _xCmpCtx)
        val xDesktop = UnoHelper.queryInterface[XDesktop](desktop)
        val document = xDesktop.getCurrentComponent

        UnoHelper.queryInterface[XModel](document)
    }
}
