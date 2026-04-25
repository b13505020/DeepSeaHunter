import javax.swing.JFrame;

public class GameFrame extends JFrame {

    public GameFrame() {
        this.setTitle("Deep Sea Hunter");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        GamePanel panel = new GamePanel();
        this.add(panel);
        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}