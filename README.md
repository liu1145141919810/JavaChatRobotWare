# JAVA

## VERSION NUMBER: 0.3

The Repository used for Java Homework

## 更新0.1

使用下面的命令可以打开界面

即1.跳转 2.编译 3.执行
命令需要使用本地配置，同时setting.json和launch.json文件也需要本地管理，这个需要自己配置自己的环境

我的（刘栋旭本地）操作如下


这将创建out/xx.class文件并执行

本处使用Robot/MainApp.java打开了一个窗口

![示例1][Picture/p1.png]

## 更新0.2

实现了初始登入选择界面，创建了两个机器人的用例参数，并且能够进行选择和确定触发进入某个聊天

## 更新0.3

聊天框架搭建完毕，用户可以输入内容

## 更新0.4

聊天的数据库内容存取搭建完毕

## 更新0.5

嵌入豆包的Doubao seed 1.6 250615

# 学习内容记录

## 路径调试法
robotIcon = new ImageIcon("Robot_App/Robot"+id.get()+"/static/figure.jpg");
        String filePath = "Robot_App/Robot" + id.get() + "/static/figure.jpg";
        File file = new File(filePath);
        System.out.println("Absolute path: " + file.getAbsolutePath());
