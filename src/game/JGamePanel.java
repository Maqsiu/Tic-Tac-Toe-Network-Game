package game;

import javax.swing.*;
import java.awt.*;

public class JGamePanel extends JPanel {

    private static final Font GAME_FONT = new Font("Verdana", Font.BOLD, 28);

    public JGamePanel() {
        super(true);
        setFocusable(true);
        requestFocusInWindow();
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderGame(g);
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, CConfig.WIDTH, CConfig.HEIGHT);

        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));

        int thirdWidth = CConfig.WIDTH / 3;
        int thirdHeight = CConfig.HEIGHT / 3;

        // Horizontal lines
        g.drawLine(0, thirdHeight, CConfig.WIDTH, thirdHeight);
        g.drawLine(0, 2 * thirdHeight, CConfig.WIDTH, 2 * thirdHeight);

        // Vertical lines
        g.drawLine(thirdWidth, 0, thirdWidth, CConfig.HEIGHT);
        g.drawLine(2 * thirdWidth, 0, 2 * thirdWidth, CConfig.HEIGHT);
    }

    private void drawSymbol(Graphics g, char symbol, int x, int y, Color color) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));
        g2.setColor(color);

        int size = CConfig.WIDTH / 3 - 20;
        int offset = size / 2;

        switch (symbol) {
            case 'O' -> g2.drawOval(x - offset, y - offset, size, size);
            case 'X' -> {
                g2.drawLine(x - offset, y - offset, x + offset, y + offset);
                g2.drawLine(x + offset, y - offset, x - offset, y + offset);
            }
        }
    }

    private void renderGame(Graphics g) {
        drawGrid(g);
        g.setFont(GAME_FONT);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (CConfig.comError) {
            drawCenteredText(g2, CConfig.comErrorString, Color.RED);
            return;
        }

        if (!CConfig.accepted) {
            if (CConfig.threadRunning) {
                drawCenteredText(g2, CConfig.waitingString, Color.RED);
            }
            return;
        }

        drawBoardState(g);

        if (CConfig.won || CConfig.enemyWon) {
            drawWinningLine(g2);
            drawCenteredText(g2, CConfig.won ? CConfig.wonString : CConfig.enemyWonString, Color.RED);
        } else if (CConfig.tie) {
            drawCenteredText(g2, CConfig.tieString, Color.BLACK);
        } else if (!CConfig.yourTurn) {
            drawCenteredText(g2, CConfig.waitingOpString, Color.RED);
        }
    }

    private void drawBoardState(Graphics g) {
        for (int i = 0; i < CConfig.board.length; i++) {
            String cell = CConfig.board[i];
            if (cell == null) continue;

            char symbol = cell.charAt(0);
            boolean isCircle = symbol == 'O';
            Color color = (isCircle == CConfig.circle) ? Color.BLUE : Color.RED;

            int x = ((i % 3) * 2 + 1) * CConfig.WIDTH / 6;
            int y = ((i / 3) * 2 + 1) * CConfig.HEIGHT / 6;
            drawSymbol(g, symbol, x, y, color);
        }
    }

    private void drawWinningLine(Graphics2D g2) {
        g2.setStroke(new BasicStroke(10));
        g2.setColor(Color.GREEN);

        int startX = ((CConfig.line[0] % 3) * 2 + 1) * CConfig.WIDTH / 6;
        int startY = ((CConfig.line[0] / 3) * 2 + 1) * CConfig.HEIGHT / 6;
        int endX = ((CConfig.line[1] % 3) * 2 + 1) * CConfig.WIDTH / 6;
        int endY = ((CConfig.line[1] / 3) * 2 + 1) * CConfig.HEIGHT / 6;

        g2.drawLine(startX, startY, endX, endY);
    }

    private void drawCenteredText(Graphics2D g2, String text, Color color) {
        g2.setColor(color);
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (CConfig.WIDTH - textWidth) / 2, CConfig.HEIGHT / 2);
    }
}
