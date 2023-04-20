
import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setSize(900, 600);
        MainForm mainForm = new MainForm();
        frame.add(mainForm.getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!Files.exists(Paths.get("info/Log.txt"))) {
                        Files.createFile(Paths.get("info/Log.txt"));
                    }
                    Files.writeString(Paths.get("info/Log.txt"), mainForm.getLog(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
