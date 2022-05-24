# HTTP Client/Server for Java

![JDK](https://img.shields.io/badge/-JDK_17-FC801D?style=flat&logo=java&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-macOS_|_Windows_|_Linux-white)

由 Java Socket API 实现的简单 HTTP Client 与 Http Server

## TODO

- [x] 语言
- [x] 程序运行方式
- [x] 功能点
  - [x] 说明、运行截图
- [ ] 关键数据结构、类说明
  - [ ] 名字与对应功能

# 1. 概览

## 1.1 环境

本项目基于 JDK 17.0 编写，由 Maven 构造。

## 1.2 依赖

| Scope | GroupId             | ArtifactId             | Version    |
| ----- | ------------------- | ---------------------- | ---------- |
| Build | `org.projectlombok` | `lombok`               | `1.18.22`  |
| Build | `org.json`          | `json`                 | `20220320` |
| Test  | `org.junit.jupiter` | `junit-jupiter-engine` | `5.8.2`    |

## 1.3 运行方式

最终产品为 jar 文件，通过 `java -jar [NAME].jar` 运行。

# 2. 使用

## 2.1 外部文件

[Data](./Data/) 及其子目录会在 HttpClient 或 HttpServer 启动时被自动创建于 jar 文件所在目录，结构如下：

```
Data
├── Client
│   └── Cache   // Client的缓存目录
└── Server
    ├── Cache   // Server的缓存目录（未使用）
    └── Static  // Server的静态文件存放目录
```

## 2.2 HttpServer

## 2.2.1 执行指令

`java -jar /path/to/HttpServer.jar [OPTIONS]...`

## 2.2.2 具体语法

```
SYNOPSIS
    ~   [-p <PORT>] [--keep-alive]
        [-t <TIMEOUT>]

OPTIONS
    -p <PORT>       Set up the server with the specified port number.
                    The default value is 8080

    --keep-alive    Enable keep-alive.

    -t <TIMEOUT>    Socket timeout.
                    The default value is 10000
```

## 2.2.3 启动信息

若运行正常，则会显示启动信息，包含：

- Server 运行日志
- 预设 URL Mapping 信息
- 静态文件读取目录

如下所示：

```
SERVER: Preset mappings:
/register, methods: [POST]
/status, methods: [GET]
/login, methods: [GET]
/logout, methods: [GET]
/test, methods: [GET]
/missing, methods: [GET,POST]
/moved, methods: [GET]
SERVER: Reading static files from: [file:///.../Data/Server/Static]
SERVER: The server is now running
```

## 2.3 HttpClient

## 2.3.1 执行指令

`java -jar /path/to/HttpClient.jar <URL> [OPTIONS]...`

## 2.3.2 具体语法

```
SYNOPSIS
    ~   <URL>
        [-m <METHOD>] [--keep-alive] [-b <text>]
        [-h <headers>...]

URL
    Using the generic URI syntax of:
    http://<HOSTNAME>[:PORT][/PATH][?QUERY]

    The default value of the port number is 80.
    Only support HTTP protocol (not HTTPS).

OPTIONS
    -m <METHOD>     Send with the specified web method.
                    Only supports GET and POST.
                    The default value is GET.

    --keep-alive    Enable keep-alive.ß

    -b <text>       Plain text body.

    -h <header>...  Send with the specified headers.
                    Syntax: <key>:<value>
                    e.g.: User-Agent:AbaAba/0.1
```

## 2.3.3 执行信息

若执行正常，则会显示

- Client 执行日志
- HTTP Request Message 原信息
- HTTP Response Message 原信息

其中原信息包含：

- Startline
- Headers
- Body
  - 若为 `text/plain` 则直接显示
  - 否则存储在[缓存目录](./Data/Client/Cache/)中，显示文件路径

如下所示：

```
/* 直接显示 Body 内容 */

CLIENT: Client has connect to the host
SOCKET[localhost127.0.0.1:8080]: Message sent 0.097656 KB

>> ==================== HTTP Request Message ==================== <<
>> GET /test HTTP/1.1
>> Accept: */*
>> User-Agent: Wget/1.21.3
>> Host: localhost
>> Accept-Encoding: gzip
>>
CLIENT: Status code received: 200
CLIENT: Handle returned directly...
CLIENT: Request complete

<< ==================== HTTP Response Message ==================== >>
<< HTTP/1.1 200 OK
<< date: Tue, 24 May 2022 03:54:16 GMT
<< server: nju_ic
<< content-encoding: gzip
<< connection: keep-alive
<< content-type: text/plain; charset=UTF-8
<<
<< You got the place!!!
```

```
/* 显示 Body 存储路径 */

>> ==================== HTTP Request Message ==================== <<
>> GET /OS/2022/ HTTP/1.1
>> Accept: */*
>> User-Agent: Wget/1.21.3
>> If-Modified-Since: Sun, 22 May 2022 11:13:51 GMT
>> Host: jyywiki.cn
>> Accept-Encoding: gzip
>>
CLIENT: Status code received: 200
CLIENT: Handle returned directly...
CLIENT: Request complete

<< ==================== HTTP Response Message ==================== >>
<< HTTP/1.1 200 OK
<< date: Tue, 24 May 2022 03:56:37 GMT
<< server: nginx/1.18.0 (Ubuntu)
<< content-encoding: gzip
<< content-type: text/html; charset=utf-8
<< connection: keep-alive
<< Content-Length: 16876
<<
<< Body saved at:
<< file:///.../Data/Client/Cache/jyywiki.cn/OS/2022/index/cache.html
```

# 3. 功能

HttpServer 与 HttpClient 均采用 Java NIO.2 模型进行开发，使用了 `java.nio.channels.AsynchronousServerSocketChannel`、`java.nio.channels.AsynchronousSocketChannel` 等类。

## 3.1 HttpServer


### 3.1.1 支持 `GET` 和 `POST` 请求

相关测试: [LoginSystemTests](./src/test/java/loginsystemtests/LoginSystemTests.java)

<img src="./docs/image/feature_get.png" alt="GET" width="300"> <img src="./docs/image/feature_post.png" alt="POST" width="300">

### 3.1.2 支持以下状态码：

- 200 OK<br/>
<img src="./docs/image/feature_status_200.png" alt="200" width="300">
  
- 301 Moved Permanently<br/>
<img src="./docs/image/feature_status_301.png" alt="301" width="300">

- 302 Found<br/>
<img src="./docs/image/feature_status_302.png" alt="302" width="300">

- 304 Not Modified<br/>
<img src="./docs/image/feature_status_304.png" alt="304" width="300">

- 404 Not Found<br/>
<img src="./docs/image/feature_status_404.png" alt="404" width="300">

- 405 Method Not Allowed<br/>
<img src="./docs/image/feature_status_405.png" alt="405" width="300">

- 500 Internal Server Error<br/>
<img src="./docs/image/feature_status_500.png" alt="500" width="300">

- 505 HTTP Version Not Supported<br/>
<img src="./docs/image/feature_status_505.png" alt="505" width="300">

### 3.1.3 支持长连接 (Keep-Alive) 

相关测试：[KeepAliveTest](./src/test/java/servertests/KeepAliveTest.java)

<img src="./docs/image/feature_keep_alive.png" alt="Keep-Alive">
### 3.1.4 支持MIME类型：

已测试过的全部类型: [mime.json](./src/main/resources/mime.json)

理论支持任意文件传输

- HTML: `text/html`<br/>
<img src="./docs/image/feature_mime_html.png" alt="HTML" width="500">

- png: `image/png`<br/>
<img src="./docs/image/feature_mime_png.png" alt="PNG" width="500">
  
- mp4: `video/mp4`<br/>
<img src="./docs/image/feature_mime_mp4.png" alt="MP4" width="500">

- svg: `image/svg+xml`<br/>
<img src="./docs/image/feature_mime_svg.png" alt="SVG" width="500">

### 3.1.5 支持注册、登录、状态保持系统

相关测试: [LoginSystemTests](./src/test/java/loginsystemtests/LoginSystemTests.java)

1. 用户注册<br/>
<img src="./docs/image/feature_loginsys_1.png" alt="loginsys_1" width="500">

2. 检查登录状态<br/>
<img src="./docs/image/feature_loginsys_2.png" alt="loginsys_2" width="500">

3. 登出<br/>
<img src="./docs/image/feature_loginsys_3.png" alt="loginsys_3" width="500">

4. 检查登录状态<br/>
<img src="./docs/image/feature_loginsys_4.png" alt="loginsys_4" width="500">

5. 登录<br/>
<img src="./docs/image/feature_loginsys_5.png" alt="loginsys_5" width="500">

## 3.2 HttpClient

### 3.2.1 在CLI界面显示 Http Request&Response Message

相关测试：[WANTest](./src/test/java/clienttests/WANTest.java)

<img src="./docs/image/feature_client_cli.png" alt="client_cli">

### 3.2.2 对 Status code 301, 302, 304 的处理

相关测试：[RequestTest](./src/test/java/clienttests/RequestTest.java)

1. 301 Moved<br/>
<img src="./docs/image/feature_client_301.png" alt="301">

2. 302 Found<br/>
<img src="./docs/image/feature_client_302.png" alt="302">

3. 304 Not Modified<br/>
<img src="./docs/image/feature_client_304.png" alt="304">


### 3.3 其他

- 仅支持 HTTP/1.1
- 收/发支持的 Content-Encoding: gzip
- 收/发支持的 Transfer-Encoding: default(Content-Length), chunked
- HttpServer 可以自动根据负载大小调整选用的 Content-Encoding 与 Transfer-Encoding
  - [MessagePacker.java](./src/main/java/edu/nju/http/message/packer/MessagePacker.java)
- HttpServer 可以用类似 SpringBoot 的方式编写 URL Mapping
  - [Target Handler](./src/main/java/edu/nju/http/server/TargetHandler.java)
  - [Mapping Annotation](./src/main/java/edu/nju/http/server/target/Mapping.java)
  - [target_path.json](./src/main/resources/target_path.json)
  - [Target Set](./src/main/java/edu/nju/http/server/target/TargetSet.java)


# 4. 源码结构