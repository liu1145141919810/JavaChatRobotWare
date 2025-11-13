# JAVA

## VERSION NUMBER: 0.1

The Repository used for Java Homework

## 更新0.1

使用下面的命令可以打开界面

即1.跳转 2.编译 3.执行
命令需要使用本地配置，同时setting.json和launch.json文件也需要本地管理，这个需要自己配置自己的环境

我的（刘栋旭本地）操作如下
cd E:\Code\Gitbox\JAVA\Robot
& 'E:\JAVA_HOME\jdk-21_windows-x64_bin\jdk-21.0.8\bin\javac.exe' --module-path "D:\JAVALib\Lib\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib" --add-modules javafx.controls -d ../out MainApp.java
Get-ChildItem -Path ..\out -Recurse
& 'E:\JAVA_HOME\jdk-21_windows-x64_bin\jdk-21.0.8\bin\java.exe' --module-path "D:\JAVALib\Lib\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib" --add-modules javafx.controls -cp ../out MainApp

这将创建out/xx.class文件并执行

本处使用Robot/MainApp.java打开了一个窗口

![示例1][Picture/p1.png]