import com.ark.example.TestConnect;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * 五子棋小游戏（玩家 vs AI大模型）
 */
public class RobotChessGame {
    private BorderPane root;
    private GridPane boardPane;
    private Label statusLabel;
    private Button restartButton;
    private Button acclerateButton;
    
    private static final int BOARD_SIZE = 9;
    private static final int CELL_SIZE = 40;
    private int[][] board;
    private boolean isPlayerTurn = true;
    private boolean gameOver = false;
    private TestConnect aiConnect;
    private boolean accelerate;

    public RobotChessGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        aiConnect = new TestConnect(0); // 初始化 AI 连接
        initUI();
    }

    private void initUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f4e7d7, #d4c4a8);");
        
        statusLabel = new Label("你的回合 (黑棋)");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        restartButton = new Button("重新开始");
        restartButton.setOnAction(e -> restart());
        restartButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-padding: 5 15;");
        
        acclerateButton = new Button("加速思考: 关");
        acclerateButton.setOnAction(e -> {
            accelerate = !accelerate;
            acclerateButton.setText("加速思考: " + (accelerate ? "开" : "关"));
        });
        
        HBox topBar = new HBox(20, statusLabel, restartButton, acclerateButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        root.setTop(topBar);
        
        boardPane = createBoard();
        root.setCenter(boardPane);
    }

    private GridPane createBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #daa520;");
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Pane cell = createCell(row, col);
                grid.add(cell, col, row);
            }
        }
        return grid;
    }

    private Pane createCell(int row, int col) {
        Pane pane = new Pane();
        pane.setPrefSize(CELL_SIZE, CELL_SIZE);
        pane.setStyle("-fx-border-color: black; -fx-border-width: 0.5;");
        
        pane.setOnMouseClicked(e -> {
            if (!gameOver && isPlayerTurn && board[row][col] == 0) {
                placePiece(row, col, 1, pane);
                if (!gameOver) {
                    isPlayerTurn = false;
                    statusLabel.setText("机器人思考中...");
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (Exception ignored) {}
                        javafx.application.Platform.runLater(this::aiMove);
                    }).start();
                }
            }
        });
        
        return pane;
    }

    private void placePiece(int row, int col, int player, Pane pane) {
        board[row][col] = player;
        
        Circle piece = new Circle(CELL_SIZE / 2.5);
        piece.setFill(player == 1 ? Color.BLACK : Color.WHITE);
        piece.setStroke(Color.GRAY);
        piece.setCenterX(CELL_SIZE / 2.0);
        piece.setCenterY(CELL_SIZE / 2.0);
        pane.getChildren().add(piece);
        
        if (checkWin(row, col, player)) {
            gameOver = true;
            String winner = player == 1 ? "你赢了！" : "机器人赢了！";
            statusLabel.setText(winner);
            showAlert(winner);
        }
    }

    private void aiMove() {
        if (gameOver) return;
        if( !accelerate ) {
            try {
                // 生成棋局信息
                String boardState = generateBoardState();
                System.out.println("发送给AI的棋盘状态:\n" + boardState);
                
                // 调用 AI 获取决策
                String aiResponse = aiConnect.chessout(boardState);
                
                // 解析 AI 返回的坐标 (格式: "行,列" 或 "row:X col:Y")
                int[] move = parseAIResponse(aiResponse);
                
                if (move != null && move[0] >= 0 && move[0] < BOARD_SIZE && 
                    move[1] >= 0 && move[1] < BOARD_SIZE && board[move[0]][move[1]] == 0) {
                    Pane cell = (Pane) boardPane.getChildren().get(move[0] * BOARD_SIZE + move[1]);
                    placePiece(move[0], move[1], 2, cell);
                    isPlayerTurn = true;
                    if (!gameOver) {
                        statusLabel.setText("你的回合 (黑棋Caillo)");
                    }
                    return;
                }
            } catch (Exception e) {
                System.err.println("AI 决策失败，使用随机落子: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 备用：随机落子
        for (int attempt = 0; attempt < 100; attempt++) {
            int row = (int) (Math.random() * BOARD_SIZE);
            int col = (int) (Math.random() * BOARD_SIZE);
            if (board[row][col] == 0) {
                Pane cell = (Pane) boardPane.getChildren().get(row * BOARD_SIZE + col);
                placePiece(row, col, 2, cell);
                isPlayerTurn = true;
                if (!gameOver) {
                    statusLabel.setText("你的回合 (黑棋)");
                }
                return;
            }
        }
    }

    private boolean checkWin(int row, int col, int player) {
        return checkLine(row, col, player, 0, 1) || 
               checkLine(row, col, player, 1, 0) || 
               checkLine(row, col, player, 1, 1) || 
               checkLine(row, col, player, 1, -1);
    }

    private boolean checkLine(int row, int col, int player, int dx, int dy) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            int r = row + i * dx, c = col + i * dy;
            if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE || board[r][c] != player) break;
            count++;
        }
        for (int i = 1; i < 5; i++) {
            int r = row - i * dx, c = col - i * dy;
            if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE || board[r][c] != player) break;
            count++;
        }
        return count >= 5;
    }

    private void restart() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isPlayerTurn = true;
        gameOver = false;
        statusLabel.setText("你的回合 (黑棋)");
        boardPane.getChildren().clear();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boardPane.add(createCell(row, col), col, row);
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏结束");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 生成棋局状态字符串，供 AI 分析
     * 格式：每行表示棋盘一行，0=空 1=黑棋(玩家) 2=白棋(AI)
     */
    private String generateBoardState() {
        StringBuilder sb = new StringBuilder();
        sb.append("当前棋盘状态 (9x9五子棋，0=空 1=黑棋/玩家 2=白棋/你):\n");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(board[row][col]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("\n请分析棋局并返回你的落子位置，格式: row:数字 col:数字 (例: row:4 col:5)");
        return sb.toString();
    }

    /**
     * 解析 AI 返回的坐标
     * 支持格式: "row:4 col:5" 或 "4,5" 或 "(4,5)"
     */
    private int[] parseAIResponse(String response) {
        try {
            response = response.toLowerCase().replaceAll("[^0-9,:rowcol\\s]", "");
            
            // 尝试 "row:X col:Y" 格式
            if (response.contains("row") && response.contains("col")) {
                String[] parts = response.split("col");
                int row = Integer.parseInt(parts[0].replaceAll("[^0-9]", ""));
                int col = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                return new int[]{row, col};
            }
            
            // 尝试 "X,Y" 格式
            if (response.contains(",")) {
                String[] parts = response.split(",");
                int row = Integer.parseInt(parts[0].trim().replaceAll("[^0-9]", ""));
                int col = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                return new int[]{row, col};
            }
            
            // 提取前两个数字
            String[] nums = response.split("\\s+");
            if (nums.length >= 2) {
                int row = Integer.parseInt(nums[0].replaceAll("[^0-9]", ""));
                int col = Integer.parseInt(nums[1].replaceAll("[^0-9]", ""));
                return new int[]{row, col};
            }
        } catch (Exception e) {
            System.err.println("解析 AI 响应失败: " + response);
        }
        return null;
    }

    public Pane getRoot() {
        return root;
    }
}
