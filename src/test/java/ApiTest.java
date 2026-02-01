

import org.example.types.utils.BearerTokenUtils;

public class ApiTest {

    public static void main(String[] args) {

        String apiKeySecret = "febc2789803a43b48ae4d02f7b8342d8.CTTTXZRtWv9QLoLo";

        String token = BearerTokenUtils.getToken(apiKeySecret);
        System.out.println("token" + token);
    }
}