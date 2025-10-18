package latex_formatter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        OutputStreamWriter stream = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        JFrame jFrame = new JFrame("Latex Formatter");
        try {
            String path = "/lf_icon.png";
            InputStream imgStream = Main.class.getResourceAsStream(path);
            assert imgStream != null;
            BufferedImage myImg = ImageIO.read(imgStream);
            assert myImg != null;
            ImageIcon imageIcon = new ImageIcon(myImg.getScaledInstance(64, 64, Image.SCALE_DEFAULT));
            jFrame.setIconImage(imageIcon.getImage());
            if (Taskbar.isTaskbarSupported()) {
                Taskbar tb = Taskbar.getTaskbar();
                if (tb.isSupported(Taskbar.Feature.ICON_IMAGE)) tb.setIconImage(imageIcon.getImage());
                if (tb.isSupported(Taskbar.Feature.ICON_BADGE_TEXT))
                    Taskbar.getTaskbar().setIconBadge("Latex Formatter");
            }
        } catch (NullPointerException | IOException ex) {
            // DO NOTHING
        }
        try {
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            while (scanner.hasNext()) {
                stream.write(scanner.nextLine() + (scanner.hasNext() ? "\n" : ""));
            }
            stream.write("プギャー");
            scanner.close();
            stream.close();
        } catch (IOException e) {
            String s = "";
            for(StackTraceElement element : e.getStackTrace()) {
                s += element.toString() + "\n";
            }
            jFrame.setVisible(true);
            JOptionPane.showMessageDialog(jFrame, s);
            System.exit(0);
        }
    }
}
