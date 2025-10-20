package latex_formatter;

import latex_formatter.processor.Processor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
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
            List<String> lines = new ArrayList<>();
            OutputStreamWriter stream = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
            while (scanner.hasNext()) {
                lines.add(scanner.nextLine());
            }
            List<String> outputs = Processor.process(lines);
            for (int i = 0;i < outputs.size(); i++) {
                stream.write(outputs.get(i));
            }
            scanner.close();
            stream.close();
        } catch (IOException e) {
            String s = "";
            for(StackTraceElement element : e.getStackTrace()) {
                s += element.toString() + "\n";
            }
            jFrame.setVisible(true);
            JOptionPane.showMessageDialog(jFrame, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
