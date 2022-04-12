# ICProject-HTTPImpl
Term project for Internet Computing (2022, Spring), aiming to implement HTTP client and server with Java socket API.

# 1 主题
基于Java Socket API搭建简单的HTTP客户端和服务器端程序

## 1.1 说明：
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

# 2. 注意事项

## 2.1 配置与启动

- 首次打开或`git pull`后打开项目时，务必 reload maven project
- Lombok 需要启动 IntelliJ IDEA 的 Annotation Processor 才可使用， 
初次 build project 时留意 IDEA右下角的提示弹窗
- IDE 出现异常时记得 Invalidate Cache

## 2.2 项目编写

- 所有的Debug信息都请使用 util.Log.debug() 输出，切勿直接使用任何stdio输出