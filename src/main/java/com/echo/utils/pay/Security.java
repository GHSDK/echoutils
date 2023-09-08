package com.echo.utils.pay;

import android.text.TextUtils;
import android.util.Base64;

import com.echo.utils.EchoLog;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/9/7
 * change   :
 * describe :
 */
public class Security {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";

    public static boolean verifyPurchase(String base64PublicKey,
                                         String signedData,
                                         String signature,
                                         String algorithm) {
        if (TextUtils.isEmpty(signedData)
                || TextUtils.isEmpty(base64PublicKey)
                || TextUtils.isEmpty(signature)) {
            EchoLog.INSTANCE.log("Purchase verification failed: missing data.");
            return false;
        }
        PublicKey key = generatePublicKey(base64PublicKey);
        return verify(key, signedData, signature, algorithm);
    }

    private static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));

        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("RSA not available", e);

        } catch (InvalidKeySpecException e) {
            EchoLog.INSTANCE.log("Invalid key specification.");
            throw new IllegalArgumentException(e);
        }
    }

    private static boolean verify(PublicKey publicKey,
                                  String signedData,
                                  String signature,
                                  String algorithm) {
        byte[] signatureBytes;
        try {
            signatureBytes = Base64.decode(signature, Base64.DEFAULT);

        } catch (IllegalArgumentException e) {
            EchoLog.INSTANCE.log("Base64 decoding failed.");
            return false;
        }
        try {
            Signature signatureAlgorithm = Signature.getInstance(algorithm);
            signatureAlgorithm.initVerify(publicKey);
            signatureAlgorithm.update(signedData.getBytes());
            if (!signatureAlgorithm.verify(signatureBytes)) {
                EchoLog.INSTANCE.log("Signature verification failed.");
                return false;
            }
            return true;

        } catch (NoSuchAlgorithmException e) {
            EchoLog.INSTANCE.log("NoSuchAlgorithmException.");

        } catch (InvalidKeyException e) {
            EchoLog.INSTANCE.log("Invalid key specification.");

        } catch (SignatureException e) {
            EchoLog.INSTANCE.log("Signature exception.");
        }
        return false;
    }
}

