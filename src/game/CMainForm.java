package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;

public class CMainForm extends JFrame implements Runnable {
    private JPanel mainPanel;
    private JPanel jPanel1;
    private JTextField ipTextField;
    private JButton STARTButton;
    private JButton restartButton;

    private JGamePanel gamePanel;

    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private Thread thread;

    private void createUIComponents() {
        jPanel1 = new JGamePanel();
        gamePanel = (JGamePanel) jPanel1;
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    public CMainForm(String title) throws HeadlessException {
        super(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        STARTButton.addActionListener(e -> startGame());
        restartButton.addActionListener(e -> restartGame());
    }

    @Override
    public void run() {
        while (CConfig.threadRunning) {
            if (CConfig.errors >= 10) {
                CConfig.comError = true;
            }

            if (!CConfig.yourTurn && !CConfig.comError) {
                try {
                    int space = dis.readInt();

                    if (space >= 1 && space < 9) {
                        CConfig.board[space] = CConfig.circle ? "X" : "O";
                        CConfig.yourTurn = true;

                        if (checkWin(false) || checkTie()) {
                            restartButton.setEnabled(true);
                            CConfig.yourTurn = false;
                        }
                    } else if (space == 999) {
                        CConfig.reset();
                        CConfig.yourTurn = true;
                        gamePanel.repaint();
                    }
                } catch (IOException ex) {
                    CConfig.errors++;
                }
            }

            gamePanel.repaint();

            if (!CConfig.circle && !CConfig.accepted) {
                waitForClient();
            }
        }
    }

    private void startGame() {
        CConfig.ip = ipTextField.getText().trim();

        if (!connectToServer()) {
            setupServer();
        }

        thread = new Thread(this, "game-thread");
        CConfig.threadRunning = true;
        thread.start();
        STARTButton.setEnabled(false);

        setTitle("Kółko - Krzyżyk: [" + (CConfig.circle ? "O" : "X") + "]");
    }

    private void restartGame() {
        CConfig.reset();
        Toolkit.getDefaultToolkit().sync();

        try {
            dos.writeInt(999);
            dos.flush();
        } catch (IOException ex) {
            CConfig.errors++;
        }

        CConfig.yourTurn = false;
        gamePanel.repaint();
    }

    private void handleMouseClick(MouseEvent e) {
        if (CConfig.accepted && CConfig.yourTurn &&
                !CConfig.comError && !CConfig.won && !CConfig.enemyWon) {

            int x = 3 * e.getX() / CConfig.WIDTH;
            int y = 3 * e.getY() / CConfig.HEIGHT;
            int position = x + 3 * y;

            if (CConfig.board[position] == null) {
                CConfig.board[position] = CConfig.circle ? "O" : "X";
                CConfig.yourTurn = false;
                Toolkit.getDefaultToolkit().sync();

                try {
                    dos.writeInt(position);
                    dos.flush();
                } catch (IOException ex) {
                    CConfig.errors++;
                }

                if (checkWin(true) || checkTie()) {
                    restartButton.setEnabled(true);
                }

                gamePanel.repaint();
            }
        }
    }

    private void setupServer() {
        try {
            serverSocket = new ServerSocket(CConfig.port, 8, InetAddress.getByName(CConfig.ip));
            CConfig.yourTurn = true;
            CConfig.circle = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void waitForClient() {
        try {
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            CConfig.accepted = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean connectToServer() {
        try {
            socket = new Socket(CConfig.ip, CConfig.port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            CConfig.accepted = true;
            gamePanel.repaint();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    private boolean checkTie() {
        for (String s : CConfig.board) {
            if (s == null) return false;
        }

        CConfig.tie = true;
        return true;
    }

    private boolean checkWin(boolean isMyTurn) {
        String symbol = isMyTurn
                ? (CConfig.circle ? "O" : "X")
                : (CConfig.circle ? "X" : "O");

        for (int[] combo : CConfig.wins) {
            String a = CConfig.board[combo[0]];
            String b = CConfig.board[combo[1]];
            String c = CConfig.board[combo[2]];

            if (a != null && a.equals(b) && b.equals(c) && a.equals(symbol)) {
                CConfig.line[0] = combo[0];
                CConfig.line[1] = combo[2];

                if (isMyTurn) CConfig.won = true;
                else CConfig.enemyWon = true;

                return true;
            }
        }

        return false;
    }
}

