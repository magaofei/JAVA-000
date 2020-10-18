package loader;

import com.oracle.tools.packager.IOUtils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * 自定义 ClassLoader
 */
public class HelloClassLoader extends ClassLoader {

    private byte[] f;
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, IOException {

        HelloClassLoader helloClassLoader = new HelloClassLoader();

        System.out.println(Paths.get("", "Week_01", "loader", "Hello.xlass").toAbsolutePath());
        helloClassLoader.f = IOUtils.readFully(Paths.get("", "Week_01", "loader", "Hello.xlass").toFile());
        helloClassLoader.findClass("Hello").newInstance();

    }

    @Override
    protected Class<?> findClass(String name) {
        System.out.println("find class name = " + name);
        for (int i = 0; i < f.length; i++) {
            f[i] = (byte) (255 - f[i]);
        }
        return defineClass(name, f, 0, f.length);
    }
}