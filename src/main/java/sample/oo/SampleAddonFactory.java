package sample.oo;

import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;

public class SampleAddonFactory {
    public static XSingleComponentFactory __getComponentFactory(String implementationName) {
        Log.apply("__getComponentFactory implementationName = " + implementationName);
        Log.apply("__getComponentFactory name = " + SampleAddonImpl.class.getName());
        Log.apply("__getComponentFactory equals = " + implementationName.equals(SampleAddonImpl.class.getName()));

        XSingleComponentFactory xFactory = null;

        if (implementationName.equals(SampleAddonImpl.class.getName()))
            xFactory = Factory.createComponentFactory(SampleAddonImpl.class,
                                                      SampleAddon$.MODULE$.serviceNames());

        return xFactory;
    }

    /** Writes the service information into the given registry key.
     * This method is called by the <code>JavaLoader</code>.
     * @return returns true if the operation succeeded
     * @see com.sun.star.comp.loader.JavaLoader#
     * @see com.sun.star.lib.uno.helper.Factory#
     * @param xregistrykey Makes structural information (except regarding tree
     * structures) of a single
     * registry key accessible.
     */
    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
        Log.apply("__writeRegistryServiceInfo");

        return Factory.writeRegistryServiceInfo(
            SampleAddonImpl.class.getName(),
            SampleAddon$.MODULE$.serviceNames(),
            xRegistryKey);
    }
}