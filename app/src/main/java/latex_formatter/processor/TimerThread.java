package latex_formatter.processor;

import latex_formatter.Main;
import latex_formatter.TimeHelper;
import latex_formatter.config.ConfigManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TimerThread extends Thread {
    private static TimerThread instance = null;
    private final double startedTime;

    private TimerThread() {
        super("process-timer");
        this.startedTime = TimeHelper.now();
    }

    @Override
    public void run() {
        super.run();
        while (TimeHelper.now() - this.startedTime < ConfigManager.getInstance().getConfig().timeoutSeconds) {
        }
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
        jFrame.setVisible(true);
        JOptionPane.showMessageDialog(jFrame, "The format processing timed out");
        System.exit(0);
    }

    public static TimerThread getInstance() {
        if (instance == null) {
            instance = new TimerThread();
        }
        return instance;
    }
}
