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

   - [x] 已实现

2. 不分区使用的IO模型，BIO、NIO和AIO都可以

   - [x] 已基于NIO.2(AIO)实现

3. 实现基础的HTTP请求、响应功能，具体要求如下：

    3.1 HTTP客户端可以发送请求报文、呈现响应报文（命令行和GUI都可以）

    - [ ] TBD

    3.2 HTTP客户端对301、302、304的状态码做相应的处理

    - [ ] 未完全完成，未测试

    3.3 HTTP服务器端支持GET和POST请求

    - [x] 通过测试

    3.4 HTTP服务器端支持200、301、302、304、404、405、500的状态码

    - [x] 通过测试

    3.5 HTTP服务器端实现长连接

    - [x] 通过测试

    3.6 MIME至少支持三种类型，包含一种非文本类型

    - [x] 超额完成

4. 基于以上的要求，实现注册，登录功能(数据无需持久化，存在内存中即可，只需要实现注册和登录的接口，可以使用postman等方法模拟请求发送，无需客户端)。

    - [x] 测试通过

### 实现情况

## 1.2 任务

- [x] Message Parser: `util.MessageHelper.MessageParser`.
    - [x] Framework
    - [x] Transfer-Encoding
      - [x] Content-Length
      - [x] Chunked

    - [x] Content-Encoding
      - [x] Gzip

- [x] Message Packer

    - [x] Framework
    - [x] Transfer-Encoding
      - [x] Content-Length
      - [x] Chunked

    - [x] Content-Encoding
      - [x] Gzip


### 1.2.1 Http Server

- [x] Main loop: `HttpServer.launch`

- [x] Socket Handler: `HttpServer.handleSocket`
  - [x] Short connection
  - [x] Long connection
  
- [x] MIME types
  
- [x] Target Handler

### 1.2.2 Http Client

- [x] Request Methods
  - [x] Custom request (debug only): `HttpClientrequest(HttpRequestMessage request)`
  - [x] GET request
  - [x] POST request
- [ ] Display
  - [ ] CLI
- [ ] Status Handler
  - [x] 301 Moved Permanently
  - [x] 302 Found
  - [ ] 304 Not Modified

### 1.2.3 Login System (Server only)

- [x] Global Header Map
- [x] Verification Code Generator
- [x] Temporary User Map

### 1.2.4 Test Cases

- [x] Server Tests
  - [x] Main Loop
  - [x] Long Connection Test
  - [x] Manual Target Handler Test
  - [x] Manual MIME Test
- [ ] Client Tests
  - [x] Main Loop
  - [ ] Status Handler Test
  - [x] Request Test
- [x] Parser and Packer Test
  - [x] Content-Length Test
  - [x] Chunked Test
  - [x] File Tests
- [ ] Login System Tests
  - [x] Register
  - [ ] Login
  - [ ] Status

### 1.2.x Suggestions


## 1.3 代码结构图

Updated on Apr 16, 2022

```
.
        ├── main
        │   ├── java
        │   │   ├── client
        │   │   │   ├── HttpClient.java
        │   │   │   ├── HttpRequestMessage.java
        │   │   │   └── StatusHandler.java
        │   │   ├── exception
        │   │   │   └── InvalidMessageException.java
        │   │   ├── server
        │   │   │   ├── HttpResponseMessage.java
        │   │   │   ├── HttpServer.java
        │   │   │   ├── Mapping.java
        │   │   │   ├── ResponseMessageFactory.java
        │   │   │   ├── TargetHandler.java
        │   │   │   └── target
        │   │   │       ├── Common.java
        │   │   │       ├── Html.java
        │   │   │       ├── LoginSystem.java
        │   │   │       └── TargetSet.java
        │   │   └── util
        │   │       ├── Config.java
        │   │       ├── HttpMessage.java
        │   │       ├── Log.java
        │   │       ├── MessageHelper.java
        │   │       ├── consts
        │   │       │   ├── TransferEncoding.java
        │   │       │   └── WebMethods.java
        │   │       └── parser
        │   │           ├── CustomizedReader.java
        │   │           ├── MessageParser.java
        │   │           └── transdecode
        │   │               ├── ChunkedStrategy.java
        │   │               ├── ContentLengthStrategy.java
        │   │               ├── DeflateStrategy.java
        │   │               ├── GzipStrategy.java
        │   │               └── TransDecodeStrategy.java
        │   └── resources
        │       ├── default_status_text.json
        │       ├── global_headers.json
        │       ├── mime.json
        │       └── static_html
        └── test
            └── java
                ├── CustomTests.java
                ├── PackerAndParserTest.java
                ├── clienttests
                │   ├── RequestTest.java
                │   └── StatusHandlerTest.java
                ├── loginsystemtests
                │   └── LoginSystemTests.java
                └── servertests
                ├── LongConnectionTest.java
                ├── MIMETest.java
                ├── ServerTests.java
                └── TargetHandlerTest.java

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

- 由于HTTP Message是 case insensitive 的，所以header, target等的解析和获取都要转换成全小写

# 3. Notes

- 不采用GZIP压缩传输时，传输稳定性会出现问题
  - 已解决，问题并不在压缩传输或传输稳定性，在于并发问题。synchronized method只能防止同一object里的方法被并发调用，这时候不同object就可以同时访问他们共有的static member fields