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

### 分享工具

1. fb分享
加入依赖
```groovy
    //fb分享
    implementation 'com.facebook.android:facebook-share:13.0.0'
```
```kotlin
    fun fbShareLink(
       activity: Activity,
       url: String?,
       quote: String? = null,
       callback: ShareCallBack?
    )
    fun fbShareMedia(
       activity: Activity,
       images: List<String?>?,
       videos: List<String?>?,
       bitmap: List<Bitmap?>?,
       callback: ShareCallBack?
    )
    fun fbShare(activity: Activity, content: ShareContent<*, *>, callback: ShareCallBack?) 
```
2. twitter分享
加入依赖
```groovy
    //tw分享
    implementation 'com.twitter.sdk.android:twitter:3.3.0'
```
```kotlin
    //https://github.com/twitter-archive/twitter-kit-android/wiki/Compose-Tweets#launching-twitter-composer
    fun twShare(
        activity: Activity,
        content: String?,
        imageUri: String?,
        url: String?,
        bitmap: Bitmap?,
        callback: ShareCallBack?
    )
```
3. line分享
```kotlin
    /**
     * lin分享必须是file:/// 这种path路径，
     * 不能是url，必须先下载到本地 getHttpImageToLocal
     * 不能是content，必须找到原本的path  getRealFilePath
     *
     */
    fun lineShareImage(activity: Activity, imageUri: String?)
    fun lineShareImage(activity: Activity, bitmap: Bitmap?)
    fun lineShareString(activity: Activity, content: String?) 
```
4. 封装了不具名分享
```kotlin
    fun justSendShare(
        activity: Activity,
        title: String?,
        text: String?,
        uris: List<String?>?,
        bitmaps: List<Bitmap?>?
    )
```