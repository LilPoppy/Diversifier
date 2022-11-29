package org.cobnet.mc.diversifier;

import org.cobnet.mc.diversifier.external.JarFileScanner;
import org.cobnet.mc.diversifier.reference.GlobalPluginConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class TestMain {

    public static void main(String[] args) throws IOException, IllegalAccessException {
        boolean PREVIOUS = true, NEXT = false;

        System.out.println(false ^ PREVIOUS);
        System.out.println();
        GlobalPluginConfiguration.writeYAML(new File("C:\\Users\\LilPoppy\\Downloads\\MCSERVER\\plugins\\Diversifier\\config.yml"));
        JarFileScanner scanner = new JarFileScanner("C:\\Users\\LilPoppy\\Downloads\\MCSERVER\\plugins\\Diversifier-1.0-SNAPSHOT.jar", List.of(".class"), List.of("org.yaml.*"));
        long ms = System.currentTimeMillis();
        for(int i = 0; i < 1; i++) {
            System.out.println(scanner.scan().keySet());
        }
        System.out.println("Time: " + (System.currentTimeMillis() - ms) + "ms");

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
