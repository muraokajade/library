package com.example.library.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class ExtractJWT {

    /*
     * JWTトークンのPayload部分から、指定したキー（例："email"）の値を抽出する
     *
     * @param token "Bearer ..." 形式のJWTトークン
     * @param key 取得したいPayloadのキー名（例："email", "user_id" など）
     * @return 指定キーの値。なければnull
     */
    public static String payloadJWTExtraction(String token, String key) {
        try {
            // "Bearer除去
            String pureToken = token.replace("Bearer ", "");

            //<ヘッダー>.<ペイロード>.<署名> JWTは3部構成。2番目がPayload（Base64URLエンコードされてる）
            String[] chunks = pureToken.split("\\.");
            if(chunks.length < 2) {
                return null;
            }
            // PayloadをBase64URLデコードしてJSON文字列を取得、バイト列を UTF-8文字列として変換
            String payloadJson = new String(Base64.getUrlDecoder().decode(chunks[1]));

            // デコード後の JSON 文字列
            // {"email":"test@example.com","user_id":"123"}

            //JSON文字列 → Mapに変換、payloadMap.get("email")のようにアクセスできる様にする
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> payloadMap = mapper.readValue(payloadJson, Map.class);


            if(payloadMap.containsKey(key)) {
                return payloadMap.get(key).toString();
            }


        } catch (Exception e) {
            System.out.println("JWT解析エラー" + e.getMessage());
        }
        return null;
    }
}
