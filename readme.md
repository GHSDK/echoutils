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