import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class MissionHudOverlay extends JComponent {

    private Timer repaintTimer;

    public MissionHudOverlay() {
        setOpaque(false);

        repaintTimer = new Timer(300, e -> repaint());
        repaintTimer.start();
    }

    // 很重要：讓這層 HUD 不會擋住滑鼠點擊下面的按鈕
    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawMissionHud(g2);

        g2.dispose();
    }

    private void drawMissionHud(Graphics2D g2) {
        List<MissionBoardView.Mission> missions = MissionBoardView.getAcceptedMissions();

        int panelX = 1125;
        int panelY = 25;
        int panelW = 430;
        int panelH = 165;

        drawPanel(g2, panelX, panelY, panelW, panelH);

        g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 20));
        g2.setColor(new Color(255, 220, 130));
        g2.drawString("目前任務", panelX + 22, panelY + 35);

        g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
        g2.setColor(new Color(160, 230, 255));
        g2.drawString("Accepted Missions", panelX + 22, panelY + 58);

        if (missions == null || missions.isEmpty()) {
            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 17));
            g2.setColor(new Color(220, 235, 235));
            g2.drawString("尚未接取任務", panelX + 22, panelY + 95);
            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
            g2.setColor(new Color(150, 210, 225));
            g2.drawString("可到 Headquarters 找任務官接取。", panelX + 22, panelY + 120);
            return;
        }

        int start = Math.max(0, missions.size() - 4);
        int y = panelY + 86;

        for (int i = start; i < missions.size(); i++) {
            MissionBoardView.Mission m = missions.get(i);

            String status;
            if (m.completed) {
                status = "完成";
            } else {
                status = m.currentAmount + "/" + m.targetAmount;
            }

            Color statusColor = m.completed
                ? new Color(150, 255, 155)
                : new Color(105, 230, 255);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 15));
            g2.setColor(new Color(245, 245, 235));
            g2.drawString((i + 1) + ". " + m.title, panelX + 22, y);

            g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
            g2.setColor(statusColor);
            g2.drawString("[" + status + "]", panelX + 310, y);

            y += 24;
        }

        if (missions.size() > 4) {
            g2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
            g2.setColor(new Color(180, 210, 220));
            g2.drawString("僅顯示最近 4 個任務", panelX + 22, panelY + panelH - 14);
        }
    }

    private void drawPanel(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 135));
        g2.fillRoundRect(x + 6, y + 6, w, h, 22, 22);

        GradientPaint gp = new GradientPaint(
            x,
            y,
            new Color(10, 42, 58, 225),
            x,
            y + h,
            new Color(4, 18, 30, 225)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, 22, 22);

        g2.setColor(new Color(210, 145, 58));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 22, 22);

        g2.setColor(new Color(105, 225, 255, 45));
        g2.fillRoundRect(x + 10, y + 10, w - 20, h - 20, 16, 16);
    }
}
