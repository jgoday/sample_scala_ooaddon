package sample.oo

import com.sun.star.uno.{UnoRuntime, XComponentContext}

object UnoHelper {
    def queryInterface[T](obj: Object)(implicit a: Manifest[T]) =
        UnoRuntime.queryInterface(a.erasure, obj).asInstanceOf[T]
}


object ServiceHelper {
    def service(name: String, ctx: XComponentContext) =
        ctx.getServiceManager.createInstanceWithContext(name, ctx)
}
