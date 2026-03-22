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
 * 五子棋小游戏（玩家 vs AI 大模型）
 * 功能说明：
 *  1. 9x9 棋盘，玩家执黑，AI 执白
 *  2. 玩家点击棋盘落子，AI 自动响应
 *  3. AI 可通过 TestConnect 调用大模型获取落子决策
 *  4. 支持“加速思考”开关，关闭时调用 AI 决策，否则直接随机落子
 *  5. 游戏结束时弹窗提示胜负
 */
public class RobotChessGame {

    /** 根布局 */
    private BorderPane root;

    /** 棋盘网格布局 */
    private GridPane boardPane;

    /** 游戏状态标签 */
    private Label statusLabel;

    /** 重新开始按钮 */
    private Button restartButton;

    /** AI 加速思考按钮 */
    private Button acclerateButton;

    /** 棋盘尺寸 */
    private static final int BOARD_SIZE = 9;

    /** 每个格子的像素大小 */
    private static final int CELL_SIZE = 40;

    /** 棋盘数组，0=空 1=玩家 2=AI */
    private int[][] board;

    /** 是否轮到玩家落子 */
    private boolean isPlayerTurn = true;

    /** 游戏是否结束 */
    private boolean gameOver = false;

    /** AI 连接对象，用于获取 AI 决策 */
    private TestConnect aiConnect;

    /** 是否开启加速思考（true=跳过 AI 逻辑，直接随机落子） */
    private boolean accelerate;

    /** 构造函数，初始化棋盘数组和 AI 连接，并初始化 UI */
    public RobotChessGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        aiConnect = new TestConnect(0); // 初始化 AI 连接
        initUI();
    }

    /** 初始化界面布局和控件 */
    private void initUI() {
        root = new BorderPane();
        // 设置渐变背景
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f4e7d7, #d4c4a8);");

        // 状态标签
        statusLabel = new Label("你的回合 (黑棋)");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // 重新开始按钮
        restartButton = new Button("重新开始");
        restartButton.setOnAction(e -> restart());
        restartButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-padding: 5 15;");

        // AI 加速思考按钮
        acclerateButton = new Button("加速思考: 关");
        acclerateButton.setOnAction(e -> {
            accelerate = !accelerate;
            acclerateButton.setText("加速思考: " + (accelerate ? "开" : "关"));
        });

        // 顶部按钮栏布局
        HBox topBar = new HBox(20, statusLabel, restartButton, acclerateButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));
        root.setTop(topBar);

        // 创建棋盘 GridPane
        boardPane = createBoard();
        root.setCenter(boardPane);
    }

    /** 创建棋盘网格 */
    private GridPane createBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #daa520;"); // 棋盘背景

        // 遍历每个格子，生成可点击 Pane
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Pane cell = createCell(row, col);
                grid.add(cell, col, row);
            }
        }
        return grid;
    }

    /** 创建单个格子 Pane，并添加点击落子事件 */
    private Pane createCell(int row, int col) {
        Pane pane = new Pane();
        pane.setPrefSize(CELL_SIZE, CELL_SIZE);
        pane.setStyle("-fx-border-color: black; -fx-border-width: 0.5;");

        // 玩家点击落子事件
        pane.setOnMouseClicked(e -> {
            if (!gameOver && isPlayerTurn && board[row][col] == 0) {
                placePiece(row, col, 1, pane); // 玩家落子
                if (!gameOver) {
                    isPlayerTurn = false;
                    statusLabel.setText("机器人思考中...");
                    // 异步调用 AI 落子，延迟 500ms 模拟思考
                    new Thread(() -> {
                        try { Thread.sleep(500); } catch (Exception ignored) {}
                        javafx.application.Platform.runLater(this::aiMove);
                    }).start();
                }
            }
        });

        return pane;
    }

    /** 在棋盘指定位置放置棋子，并检查胜利条件 */
    private void placePiece(int row, int col, int player, Pane pane) {
        board[row][col] = player;

        // 创建圆形棋子
        Circle piece = new Circle(CELL_SIZE / 2.5);
        piece.setFill(player == 1 ? Color.BLACK : Color.WHITE);
        piece.setStroke(Color.GRAY);
        piece.setCenterX(CELL_SIZE / 2.0);
        piece.setCenterY(CELL_SIZE / 2.0);
        pane.getChildren().add(piece);

        // 检查胜利
        if (checkWin(row, col, player)) {
            gameOver = true;
            String winner = player == 1 ? "你赢了！" : "机器人赢了！";
            statusLabel.setText(winner);
            showAlert(winner);
        }
    }

    /** AI 落子逻辑 */
    private void aiMove() {
        if (gameOver) return;

        if (!accelerate) {
            try {
                // 生成棋盘状态字符串
                String boardState = generateBoardState();
                System.out.println("发送给AI的棋盘状态:\n" + boardState);

                // 调用 AI 获取决策
                String aiResponse = aiConnect.chessout(boardState);

                // 解析 AI 返回的落子坐标
                int[] move = parseAIResponse(aiResponse);

                if (move != null && move[0] >= 0 && move[0] < BOARD_SIZE &&
                    move[1] >= 0 && move[1] < BOARD_SIZE && board[move[0]][move[1]] == 0) {
                    Pane cell = (Pane) boardPane.getChildren().get(move[0] * BOARD_SIZE + move[1]);
                    placePiece(move[0], move[1], 2, cell);
                    isPlayerTurn = true;
                    if (!gameOver) statusLabel.setText("你的回合 (黑棋)");
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
                if (!gameOver) statusLabel.setText("你的回合 (黑棋)");
                return;
            }
        }
    }

    /** 检查是否五子连线获胜 */
    private boolean checkWin(int row, int col, int player) {
        return checkLine(row, col, player, 0, 1) ||  // 水平
               checkLine(row, col, player, 1, 0) ||  // 垂直
               checkLine(row, col, player, 1, 1) ||  // 主对角线
               checkLine(row, col, player, 1, -1);   // 副对角线
    }

    /** 检查某方向是否连续 5 子 */
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

    /** 重新开始游戏 */
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

    /** 弹出提示框显示游戏结果 */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏结束");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** 生成棋局状态字符串，供 AI 分析 */
    private String generateBoardState() {
        StringBuilder sb = new StringBuilder();
        sb.append("当前棋盘状态 (9x9五子棋，0=空 1=黑棋/玩家 2=白棋/AI):\n");
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                sb.append(board[row][col]).append(" ");
            }
            sb.append("\n");
        }
        sb.append("\n请分析棋局并返回你的落子位置，格式: row:数字 col:数字 (例: row:4 col:5)");
        return sb.toString();
    }

    /** 解析 AI 返回坐标，支持多种格式 */
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

    /** 获取根布局，用于嵌入其他窗口 */
    public Pane getRoot() {
        return root;
    }
}
