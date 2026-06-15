package com.example.weatherforecast;

import android.content.Context;
import android.util.Base64;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 按和风官方文档生成 Ed25519 JWT。
 * @see <a href="https://dev.qweather.com/docs/configuration/authentication/">身份认证</a>
 */
public final class QWeatherJwt {
    private static final String ASSET_PRIVATE_KEY = "qweather_private.pem";

    private QWeatherJwt() {
    }

    public static boolean hasPrivateKeyAsset(Context context) {
        try {
            InputStream in = context.getAssets().open(ASSET_PRIVATE_KEY);
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String createToken(Context context) throws Exception {
        PrivateKey privateKey = loadPrivateKey(readAssetPem(context));
        String headerJson = "{\"alg\":\"EdDSA\",\"kid\":\"" + BuildConfig.QWEATHER_CREDENTIAL_ID + "\"}";
        long iat = System.currentTimeMillis() / 1000L - 30;
        long exp = iat + 900;
        String payloadJson = "{\"sub\":\"" + BuildConfig.QWEATHER_PROJECT_ID + "\",\"iat\":"
                + iat + ",\"exp\":" + exp + "}";
        String data = base64Url(headerJson) + "." + base64Url(payloadJson);

        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
        Signature signer = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
        signer.initSign(privateKey);
        signer.update(data.getBytes(StandardCharsets.UTF_8));
        return data + "." + base64Url(signer.sign());
    }

    private static String readAssetPem(Context context) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = context.getAssets().open(ASSET_PRIVATE_KEY);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static PrivateKey loadPrivateKey(String pem) throws Exception {
        String stripped = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.decode(stripped, Base64.DEFAULT);
        return new EdDSAPrivateKey(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static String base64Url(String text) {
        return base64Url(text.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Url(byte[] data) {
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
