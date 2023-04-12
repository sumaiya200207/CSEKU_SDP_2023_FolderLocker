import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Tray {
    public String title;
    private String iconPath;
    private String message;

    public Tray(String title, String iconPath, String message) {
        this.title = title;
        this.iconPath = iconPath;
        this.message = message;
    }

    public void showTray() {
        TrayIcon t = new TrayIcon(Toolkit.getDefaultToolkit().getImage(this.iconPath));
        t.setToolTip(this.title);

        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(null, message);
            }
        });

        try {
            SystemTray.getSystemTray().add(t);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
