## 工具类

为了避免多个工程用到相同代码。这里将项目中的用到了公用工具打包

### 第三方库本地化处理

1. secure-preferences
   [原工程地址](https://github.com/scottyab/secure-preferences#secure-preferences---deprecated)

```
    //替代 implementation 'com.scottyab:secure-preferences-lib:0.1.7'
    implementation 'androidx.security:security-crypto-ktx:1.1.0-alpha03'
```

由于目前项目最低api为21.但是Androidx这个要求的是23.所以暂时仍然使用scottyab的解决方案

### LOG工具

使用方法

```kotlin
     EchoLog.log(objects)
```

默认的tag为：**EchoTag**
可通过设置echo_log_tag字符来设置，或者setLogTag来修改

```groovy
   resValue("string", "echo_log_tag", "WGSDK")
```

eg：顶部标出打log的线程名称。内容部分默认打印2个调用栈(可通过 EchoLog.setTraceCount(3)修改)

```
    ╔═══main:1════════════════════════════
    ║➨➨at com.gamehours.japansdk.pay.GooglePayChannel.<init>(GooglePayChannel.kt:430)
    ║➨➨➨at com.gamehours.japansdk.pay.PayManager.init(PayManager.kt:28)
    ╟───────────────────────────────────
    ║Creating Google Billing Client.___
    ╚═════════════════════════════════
```

忽略无意义的封装类中的栈

```kotlin
    EchoLog.addIgnore(CommonUtils.class. getName ());
```