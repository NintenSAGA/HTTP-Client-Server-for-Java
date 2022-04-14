# ICProject-HTTPImpl
Term project for Internet Computing (2022, Spring), aiming to implement HTTP client and server with Java socket API.

## 成员

- 谭子悦
- 徐浩钦
- 陈骏
- 邱兴驰
- 李佳骏

# 1 概览
基于Java Socket API搭建简单的HTTP客户端和服务器端程序

## 1.1 说明
1. 不允许基于netty等框架，完全基于Java Socket API进行编写

2. 不分区使用的IO模型，BIO、NIO和AIO都可以

3. 实现基础的HTTP请求、响应功能，具体要求如下：

    3.1 HTTP客户端可以发送请求报文、呈现响应报文（命令行和GUI都可以）
    
    3.2 HTTP客户端对301、302、304的状态码做相应的处理
    
    3.3 HTTP服务器端支持GET和POST请求
    
    3.4 HTTP服务器端支持200、301、302、304、404、405、500的状态码
    
    3.5 HTTP服务器端实现长连接
    
    3.6 MIME至少支持三种类型，包含一种非文本类型
    
4. 基于以上的要求，实现注册，登录功能(数据无需持久化，存在内存中即可，只需要实现注册和登录的接口，可以使用postman等方法模拟请求发送，无需客户端)。

## 1.2 任务

- [ ] Message Parser: `util.MessageHelper.MessageParser`. 李佳骏 邱兴驰

### 1.2.1 Http Server

- [x] Main loop: `HttpServer.launch` 谭子悦
- [x] Socket Handler: `HttpServer.handleSocket` 谭子悦
  - [x] Short connection
  - [x] Long connection
- [ ] Http Request Message Parser: `HttpServer.ParseRequestMessage. 李佳骏 邱兴驰
- [ ] MIME types: 谭子悦
  - [x] Type 1: text/plain
  - [ ] Type 2: text/html
  - [ ] Type 3: TBD
- [x] Target Handler

### 1.2.2 Http Client

- [ ] Request Methods    徐浩钦
  - [ ] Custom request (debug only): `HttpClientrequest(HttpRequestMessage request)`
  - [ ] GET request
  - [ ] POST request
- [ ] Http Response Message Parser: `HttpClient.ParseResponseMessage` 李佳骏 邱兴驰
- [ ] Display
  - [ ] GUI
  - [ ] CLI
- [ ] Status Handler     陈骏
  - [ ] 301 Moved Permanently
  - [ ] 302 Found
  - [ ] 304 Not Modified

### 1.2.3 Login System (Server only) 谭子悦

- [x] Global Header Map
- [x] Verification Code Generator
- [x] Temporary User Map

### 1.2.4 Test Cases

- [ ] Server Tests
  - [ ] Target Handler Test
  - [ ] MIME Test
  - [ ] Parser Test 李佳骏 邱兴驰
  - [ ] Long Connection Test
- [ ] Client Tests
  - [ ] Parser Test 李佳骏 邱兴驰
  - [ ] Status Handler Test 陈骏
- [ ] Login System Tests
  - [ ] TBD

### 1.2.x Suggestions

- [ ] Response 和 Request Message Parser所需要进行的操作其实大致相同（只有第一行不一样），也许可以抽象出一个独立的静态方法，再让这两个方法作为该方法的 Decorator

## 1.3 代码结构图

Updated on Apr 12, 2022

```Java
./java   // ./src/main/java
├── client                           // Client Module
│   ├── HttpClient.java              // HttpClient 主程序
│   ├── HttpRequestMessage.java      // Http Request Message 类, 继承HttpMessage
│   └── StatusHandler.java           // Status Code Handler, Singleton
├── server                           // Server Module
│   ├── HttpResponseMessage.java     // Http Response Message 类, 继承HttpMessage       
│   ├── HttpServer.java              // HttpServer 主程序
│   ├── Mapping.java                 // Mapping Annotation
│   ├── ResponseMessageFactory.java  // Response Message Factory, Singleton
│   ├── TargetHandler.java           // Target Handler, Singleton
│   └── TargetSet.java               // Target Set, Singleton
└── util                              // Utility Module
    ├── HttpMessage.java              // Http Message 抽象类
    ├── Log.java                      // 日志工具方法类，仅在 -ea (Enable Assertion) 时启用
    ├── MessageHelper.java            // Message Helper 助手方法类
    └── WebMethods.java               // 常量存储类，包含 WebMethods.GET, WebMethods.POST
```

```Java
test				// ./src/test 测试用例
└── java
    ├── CustomTests.java	// 自定义测试类，可自行编写测试
  	......  // More Tests TBD
```



# 2. 注意事项

## 2.1 配置与启动

- 首次打开或`git pull`后打开项目时，务必 reload maven project
- Lombok 需要启动 IntelliJ IDEA 的 Annotation Processor 才可使用， 初次 build project 时留意 IDEA 右下角的提示弹窗
- IDE 出现异常时记得 Invalidate Cache

## 2.2 项目编写

- 所有的Debug信息都请使用 util.Log.debug() 输出，切勿直接使用任何stdio输出
  - 多写Debug信息，便于自己调试也便于他人阅读
  - 格式化输出请使用 `“Various texts...”.formatted()`
- 如需修改 [Public | Protected | Default] [methods | fields]，请先在工作群中汇报
  - Private [methods | fields] 可按需增加
- JVM默认参数不启用assertion，因此推荐在 ./test/java/CustomTests.java 中编写自己的测试程序
  - 不要随意修改别人的用例
- 编写不确定或逻辑复杂的部分时，请多写 [Assertions](https://www.geeksforgeeks.org/assertions-in-java/)

- 所有的命名请遵守 Java 规范
- 请不要从任何地方 (StackOverflow, GitHub, etc.) 直接复制代码到本项目中

- 填写复用纯文本 (如 `GET`, `HTTP/1.1`) 前，先检查项目中是否已定义对应常量，若无，可考虑增加该常量类

- 配置参数可以存为json文件放在`main/resources`下，将文件路径作为常量存在`util.Config`中，并用`util.Config`中的方法读取