package org.example.types.utils;

import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WXAccessTokenUtils {

    private static final String APPID = "wx5a228ff69e28a91f";
    private static final String SECRET = "0bea03aa1310bac050aae79dd8703928";
    private static final String GRANT_TYPE = "client_credential";
    private static final String URL_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/token?grant_type=%s&appid=%s&secret=%s";

    /**
     * 使用默认配置获取 access_token。
     *
     * @return access_token
     */
    public static String getAccessToken() {
        return getAccessToken(APPID, SECRET);
    }

    /**
     * 使用指定 appid/secret 获取 access_token。
     *
     * @param APPID  公众号 appid
     * @param SECRET 公众号 secret
     * @return access_token
     */
    public static String getAccessToken(String APPID, String SECRET) {
        try {
            String urlString = String.format(URL_TEMPLATE, GRANT_TYPE, APPID, SECRET);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Print the response
                System.out.println("Response: " + response.toString());

                Token token = JSON.parseObject(response.toString(), Token.class);

                return token.getAccess_token();
            } else {
                System.out.println("GET request failed");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Token {
        private String access_token;
        private Integer expires_in;

        /**
         * 获取 access_token。
         *
         * @return access_token
         */
        public String getAccess_token() {
            return access_token;
        }

        /**
         * 设置 access_token。
         *
         * @param access_token access_token
         */
        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        /**
         * 获取过期时间（秒）。
         *
         * @return 过期时间
         */
        public Integer getExpires_in() {
            return expires_in;
        }

        /**
         * 设置过期时间（秒）。
         *
         * @param expires_in 过期时间
         */
        public void setExpires_in(Integer expires_in) {
            this.expires_in = expires_in;
        }
    }


}

