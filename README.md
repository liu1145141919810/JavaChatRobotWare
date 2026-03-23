# JAVA

## VERSION NUMBER: 0.5

开发的聊天机器人主要包括“数据库管理”，“UI界面涉及”，“AI回答云端调用”，“内置智能小游戏”五大板块。用户进入系统后从选项栏选择一个合适的机器人，进入聊天页面（按照会话自动区分管理）。而后以聊天界面的详细发送内容并获得机器人的回复。同时可以通过点击机器人头像选择小游戏进行游玩。

## 技术栈

JavaFX（GUI）+ 字节跳动 Ark 大模型（豆包）API+ PostgreSQL（数据库）+ 多线程并发 + 多媒体处理

## 项目结构

```

JavaChatRobotWare/
│
├── 📁 only-java-sources-with-resources/  【核心源代码与资源目录】
│   │
│   ├── 📁 com/
│   │   ├── 📁 ark/example/
│   │   │   └── TestConnect.java 【大模型API接口】
│   │   │       └── 功能：火山引擎Ark SDK集成，调用大模型API，支持普通聊天和象棋对话
│   │   │
│   │   └── 📁 desktoprobot/util/
│   │       └── DatabaseConnection.java 【数据库连接工具】
│   │           └── 功能：PostgreSQL JDBC连接池，配置文件管理，会话和消息持久化
│   │
│   ├── 📁 Public/  【公共提示词目录】
│   │   ├── public.txt 【通用Prompt】
│   │   │   └── 功能：统一约束模型行为，减少响应延迟
│   │   │
│   │   └── chess.txt 【象棋专用Prompt】
│   │       └── 功能：五子棋棋局分析和落子决策
│   │
│   ├── 📁 Robot1/  【机器人1配置】
│   │   └── Robot1.txt 【角色Prompt】
│   │       └── 功能：定义为篮球明星Kobe的性格和对话风格
│   │
│   ├── 📁 Robot2/  【机器人2配置】
│   │   └── Robot2.txt 【角色Prompt】
│   │       └── 功能：定义为《孤独摇滚》角色喜多川郁代的性格
│   │
│   ├── MainApp.java 【启动窗口 v1.0】
│   │   └── 功能：
│   │       • 机器人选择界面（ChoiceBox）
│   │       • 动态切换机器人头像
│   │       • 进入聊天窗口的入口
│   │
│   ├── MainApp2.java 【启动窗口 v2.0 (美化版)】
│   │   └── 功能：
│   │       • 开场视频动画（原神启动画面）
│   │       • UI界面美化（渐变背景、边框效果）
│   │       • 团队信息展示
│   │
│   ├── RobotChatFrame.java 【聊天窗口基类】
│   │   └── 功能：
│   │       • 消息发送/接收展示
│   │       • 数据库会话管理
│   │       • 历史消息加载
│   │       • JavaFX UI布局
│   │
│   ├── RobotChatFrame2.java 【聊天窗口 v2.0】
│   │   └── 功能：(继承自RobotChatFrame)
│   │       • 集成TestConnect实现AI智能回复
│   │       • 添加重启按钮清空聊天历史
│   │       • 增强的顶部控制栏
│   │
│   ├── RobotChatFrame3.java 【聊天窗口 v3.0 (最终版)】
│   │   └── 功能：(继承自RobotChatFrame2)
│   │       • UI美化（紫色渐变背景、发光边框）
│   │       • 头像右键菜单（快速访问小游戏）
│   │       • 聊天气泡增强样式
│   │       • 快速访问入口：象棋游戏、骰子游戏
│   │
│   ├── RobotChessGame.java 【五子棋小游戏】
│   │   └── 功能：
│   │       • 9x9棋盘，玩家执黑，AI执白
│   │       • 点击棋盘落子
│   │       • AI通过TestConnect调用大模型获取决策
│   │       • 加速思考开关（跳过AI，直接随机落子）
│   │       • 五子连线胜负判定
│   │
│   ├── DiceRollingGame.java 【扔骰子小游戏】
│   │   └── 功能：
│   │       • 玩家vs机器人骰子对战
│   │       • 动画骰子滚动效果
│   │       • 点数对比与比分累计
│   │       • 实时UI更新
│   │
│   └── database.properties 【数据库配置文件】
│       └── 功能：
│           • PostgreSQL连接参数
│           • 用户名、密码管理
│           • 数据库名称和端口配置
│
├── 📄 pom.xml 【Maven项目配置】
│   └── 依赖管理：
│       • JavaFX (GUI框架)
│       • PostgreSQL JDBC (数据库驱动)
│       • Ark SDK (火山引擎大模型API)
│
├── 📄 dependency-reduced-pom.xml 【简化的依赖配置 (Maven生成)】
│
└── 📄 README.md 【项目说明文档】

```

## 核心功能

## 演示效果

## 更新日志

### 更新0.1

使用下面的命令可以打开界面

即1.跳转 2.编译 3.执行
命令需要使用本地配置，同时setting.json和launch.json文件也需要本地管理，这个需要自己配置自己的环境

我的（刘栋旭本地）操作如下


这将创建out/xx.class文件并执行

本处使用Robot/MainApp.java打开了一个窗口

![示例1][Picture/p1.png]

### 更新0.2

实现了初始登入选择界面，创建了两个机器人的用例参数，并且能够进行选择和确定触发进入某个聊天

### 更新0.3

聊天框架搭建完毕，用户可以输入内容

### 更新0.4

聊天的数据库内容存取搭建完毕

### 更新0.5

嵌入豆包的Doubao seed 1.6 250615

