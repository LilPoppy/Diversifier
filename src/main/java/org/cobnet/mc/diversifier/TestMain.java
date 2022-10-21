package org.cobnet.mc.diversifier;

import org.cobnet.mc.diversifier.plugin.annotation.Signal;
import org.cobnet.mc.diversifier.utils.ProxyUtils;

import java.io.IOException;

public class TestMain {

    public static void main(String[] args) throws IOException, NoSuchMethodException {
        //Fun.toMethod(TestMain::main);
        long ms = System.currentTimeMillis();
        TestMain.class.getMethod("main", String[].class);
        //Fun.toMethod(TestMain::main);
        System.out.println("Took " + (System.currentTimeMillis() - ms) + "ms");

        System.out.println((ProxyUtils.createProxy(Signal.class) instanceof Signal));
//        JarFileScanner scanner = new JarFileScanner("/Users/lilpoppy/IdeaProjects/mc/Diversifier/target/Diversifier-1.0-SNAPSHOT.jar");
//        long ms = System.currentTimeMillis();
//        for(int i = 0; i < 1; i++) {
//            System.out.println(scanner.scan());
//        }
//        System.out.println("Time: " + (System.currentTimeMillis() - ms) + "ms");
//
//
//        HashMap<String, String> map = new HashMap<>();
//        for(int i = 0; i < 1000; i++) {
//            map.put("test" + i, "test");
//        }
//        ms = System.nanoTime();
//        System.out.println(map.get("test" + 40));
//        System.out.println("Time: " + (System.nanoTime() - ms) + "ms");
//
//        ArrayList<String> list = new ArrayList<>();
//        for(int i = 0; i < 1000; i++) {
//            list.add("test" + i);
//        }
//        ms = System.nanoTime();
//        for(int i = 0; i < 1000; i++) {
//            if(list.get(i).equals("test" + 40)) {
//                System.out.println("Found");
//                break;
//            }
//        }
//        System.out.println("Time: " + (System.nanoTime() - ms) + "ms");
    }
    //1967ms, 1982ms, 1969ms (average: 1972ms)
    //1914ms, 1916ms, 1925ms (average: 1918ms)
}
