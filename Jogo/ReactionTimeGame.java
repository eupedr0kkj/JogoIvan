import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class ReactionTimeGame extends JFrame {
    private GamePanel gamePanel;
    
    public ReactionTimeGame() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Teste de Tempo de Reação");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
    }
}

class GamePanel extends JPanel {
    private JLabel instructionLabel;
    private JLabel targetKeyLabel;
    private JLabel timerLabel;
    private JLabel resultLabel;
    private JLabel rankingLabel;
    private JButton startButton;
    
    private String targetKey;
    private long startTime;
    private boolean gameRunning;
    private boolean waitingForStart;
    private javax.swing.Timer gameTimer;
    private Random random;
    
    // Sistema de ranking
    private ArrayList<PlayerScore> ranking;
    private final int MAX_RANKING_ENTRIES = 5;
    
    // Array de teclas disponíveis para o jogo
    private final String[] availableKeys = {
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };
    
    // Classe interna para armazenar os scores dos jogadores
    class PlayerScore implements Comparable<PlayerScore> {
        String playerId;
        long reactionTime;
        
        PlayerScore(String playerId, long reactionTime) {
            this.playerId = playerId;
            this.reactionTime = reactionTime;
        }
        
        @Override
        public int compareTo(PlayerScore other) {
            return Long.compare(this.reactionTime, other.reactionTime);
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d ms", playerId, reactionTime);
        }
    }
    
    public GamePanel() {
        ranking = new ArrayList<>();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        resetGame();
        updateRankingDisplay();
    }
    
    private void initializeComponents() {
        setPreferredSize(new Dimension(500, 400));
        setBackground(Color.WHITE);
        
        instructionLabel = new JLabel("Pressione START para começar");
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        targetKeyLabel = new JLabel("");
        targetKeyLabel.setFont(new Font("Arial", Font.BOLD, 48));
        targetKeyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        targetKeyLabel.setForeground(Color.BLUE);
        
        timerLabel = new JLabel("00:000");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setForeground(Color.RED);
        
        resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        rankingLabel = new JLabel("");
        rankingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rankingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        startButton = new JButton("START");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        
        random = new Random();
        
        // Configurar o timer para atualizar o cronômetro (a cada 10ms)
        gameTimer = new javax.swing.Timer(10, e -> updateTimer());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(instructionLabel, BorderLayout.CENTER);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(targetKeyLabel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        bottomPanel.add(timerLabel);
        bottomPanel.add(resultLabel);
        bottomPanel.add(rankingLabel);
        bottomPanel.add(startButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }
    
    private void setupEventListeners() {
        startButton.addActionListener(e -> startGame());
        
        // Adicionar KeyListener para capturar as teclas pressionadas
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameRunning && !waitingForStart) {
                    String pressedKey = KeyEvent.getKeyText(e.getKeyCode());
                    checkKeyPress(pressedKey);
                }
            }
        });
    }
    
    private void startGame() {
        resetGame();
        
        instructionLabel.setText("Prepare-se...");
        resultLabel.setText("");
        targetKeyLabel.setText("");
        startButton.setEnabled(false);
        
        // Esperar 1-3 segundos aleatórios antes de mostrar a tecla
        int randomDelay = 1000 + random.nextInt(2000); // 1-3 segundos
        
        javax.swing.Timer delayTimer = new javax.swing.Timer(randomDelay, e -> {
            // Mostrar a tecla alvo e começar a contar o tempo
            targetKey = availableKeys[random.nextInt(availableKeys.length)];
            targetKeyLabel.setText(targetKey);
            targetKeyLabel.setForeground(Color.BLUE);
            
            instructionLabel.setText("Pressione a tecla: " + targetKey);
            
            startTime = System.currentTimeMillis();
            gameRunning = true;
            waitingForStart = false;
            gameTimer.start();
            
            requestFocusInWindow();
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    private void resetGame() {
        // Parar todos os timers
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        
        // Resetar todas as variáveis
        gameRunning = false;
        waitingForStart = true;
        startTime = 0L;
        targetKey = "";
        
        // Resetar a interface
        timerLabel.setText("00:000");
        timerLabel.setForeground(Color.RED);
        targetKeyLabel.setText("");
        resultLabel.setText("");
    }
    
    private void updateTimer() {
        if (gameRunning && !waitingForStart) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            
            // Garantir que o tempo não seja negativo
            if (elapsedTime < 0) {
                elapsedTime = 0;
            }
            
            long seconds = elapsedTime / 1000;
            long milliseconds = elapsedTime % 1000;
            
            timerLabel.setText(String.format("%02d:%03d", seconds, milliseconds));
        }
    }
    
    private void checkKeyPress(String pressedKey) {
        if (gameRunning && !waitingForStart && pressedKey.equalsIgnoreCase(targetKey)) {
            // Parar o jogo
            gameRunning = false;
            gameTimer.stop();
            
            // Calcular o tempo de reação
            long endTime = System.currentTimeMillis();
            long reactionTime = endTime - startTime;
            
            // Garantir que o tempo seja válido
            if (reactionTime < 0) {
                reactionTime = 0;
            }
            
            // Mostrar resultado
            String result = String.format("Tempo de reação: %d ms", reactionTime);
            resultLabel.setText(result);
            
            // Adicionar ao ranking
            addToRanking(reactionTime);
            
            instructionLabel.setText("Pressione START para jogar novamente");
            startButton.setEnabled(true);
            targetKeyLabel.setText("✓");
            targetKeyLabel.setForeground(Color.GREEN);
            timerLabel.setForeground(Color.GREEN);
        }
    }
    
    private void addToRanking(long reactionTime) {
        // Pedir os 4 primeiros dígitos do nome do jogador
        String playerId = JOptionPane.showInputDialog(
            this, 
            "Digite os 4 primeiros dígitos do seu nome:",
            "Registrar Score",
            JOptionPane.QUESTION_MESSAGE
        );
        
        // Validar entrada
        if (playerId != null && !playerId.trim().isEmpty()) {
            // Pegar apenas os primeiros 4 caracteres
            if (playerId.length() > 4) {
                playerId = playerId.substring(0, 4);
            }
            
            // Adicionar ao ranking
            ranking.add(new PlayerScore(playerId.toUpperCase(), reactionTime));
            
            // Ordenar o ranking
            Collections.sort(ranking);
            
            // Manter apenas os melhores scores
            if (ranking.size() > MAX_RANKING_ENTRIES) {
                ranking = new ArrayList<>(ranking.subList(0, MAX_RANKING_ENTRIES));
            }
            
            // Atualizar display do ranking
            updateRankingDisplay();
        }
    }
    
    private void updateRankingDisplay() {
        if (ranking.isEmpty()) {
            rankingLabel.setText("Ranking: (vazio)");
            return;
        }
        
        StringBuilder rankingText = new StringBuilder("<html>Ranking:<br>");
        for (int i = 0; i < ranking.size(); i++) {
            PlayerScore score = ranking.get(i);
            rankingText.append(String.format("%dº - %s<br>", i + 1, score.toString()));
        }
        rankingText.append("</html>");
        rankingLabel.setText(rankingText.toString());
    }
    
    // Método para garantir que o painel possa receber foco
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
}