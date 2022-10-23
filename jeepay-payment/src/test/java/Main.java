import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import com.wechat.pay.contrib.apache.httpclient.exception.NotFoundException;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.stream.Collectors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

public class Main {

  public static void main(String[] args)
      throws GeneralSecurityException, IOException, HttpCodeException, NotFoundException {

    String mchId = "131226658";

    PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey("-----BEGIN PRIVATE KEY-----\n"
        + "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCyitCuyvkspWGl\n"
        + "6Q5BNlVkgYOSN7+7wE/Ws9iW1pjw8ksG+e9v4ZVbUKCMw0GbvZe4B7dF8pR1srfQ\n"
        + "JawRLRbGoQBqISMRfYrknH67ld9chdMoIm8avaijl3AM+1E8YeP+XrZrjhT1HzPe\n"
        + "tIoJfDMCUhj7jpyD2ll4yoDPZcmtdwZa2Sn9DiyZTR/pN++LKtVlXraP426AU8xU\n"
        + "7mRvcoGTPK+Jfoj65Efv2eTfU/9dVZpx3FicJUnXbhXTBdvvQQeK3InkLSH+E7Cm\n"
        + "ViuoAmOlodVHmQnAtIy809VdafFMELa/oSNS2zabV+h0VE2/aGjJk/uuIsfs9MNE\n"
        + "kdfyZ1qRAgMBAAECggEALx3hCm+4KCcQyObjAySijl57aUfbCmIq758QhmXlgJiA\n"
        + "YjvtCxp9/tYdOGiC1OXItyEaJZRpx1G9nmmCqhm7C/oLEPbjuUbnM4N1Em3vvwv1\n"
        + "+H510ZnBekKJqHNeMdAnicZW37VkJHfyiVAdqXkkQPWHcENnXIhv2ieerJzRBzyT\n"
        + "pnFQ7uTNgt4rIn97Jo2Ic8ih71lEjHDTj9xBnm/sCLXV444vvZ4gqetEf4a+kpXQ\n"
        + "vSrv8jRDSdb4O5EgbxzjFRuggS+X48iFeBW1GYFwfCmkOZn+ck0RYotZXCUY3bhv\n"
        + "8sYTymccJFbb1X3TQQ9aE+8tzL6PrkJTtj6RVk95HQKBgQDpIdY+gRJ/kC+0xv/m\n"
        + "F4iciVFogtlMhiu+qRQ3Oq/IrKTLhiXAETglhmCaATXaaCFheoc1wJYWQrfYhYrU\n"
        + "77hNgflOainwNDiEdgvxPb4o2vsd/10VYgv4e+HJFihdmXTnD1Xhajn8BjhekrB2\n"
        + "UW7RJhRHLVDt1or8gNd30C+o3wKBgQDEDizNydhpYXUxsTJfeu+MKQzwYmZGx3gO\n"
        + "2GTg8256MLVtMSDQb2Fo8VPHMUG923UnaUhrajAizwrn18y0XrxPBD59/9m0Fapt\n"
        + "DoJeiyYE2/Aki/Mv+RCkPtzDn3vhuLek1XYZzZaMbkSdbfuEb8lhWwoXtQVbQI3k\n"
        + "xkUTfaq6jwKBgQCeMebGfSFHTFkC67JDuR5rUkbai0fWxIi0lNjx7THRE020CXBe\n"
        + "V5Do7blpZ37amKRmGBXd5LQzoMxN8bhWa+B9P8AjdKjGpTjgsZXC+mmqIYYbQegx\n"
        + "FVoYIEADCrUTZolMLTzSfBmiqbngetuhPfrbk2JZNiffbCDd3LPZSny8swKBgGCU\n"
        + "CwYk1HK1C6I7A/F9AdwtrK4OCwv63u2RM8CMqnOO4xCzDtMeywGDQB3z6cq4/PE5\n"
        + "L++aE7RSAbFL4Q9JS8Iw+QfgrUHFGns2loo0SbpDDf0y481SCBygyuei/Meo0u5k\n"
        + "KRV07SCogSx8LVaEKtPGoSYkikr1dfCoqwZ69UuJAoGATWDBW+158JejA0ggwT/q\n"
        + "WuKe9GoUasX4KHiO2bLq8TJxBq3ZZsUBXcxHuVuE6lBunA/wUD5x1wgj/izEgVrQ\n"
        + "G4ZafON7IrcVgtRLsAJ2ml2Ea6RG2VN4HTx9Dtqzk9138gB9goCEsCfsRXsHyO2o\n"
        + "jSbk+66yvxrddh+/2Hk2oTY=\n"
        + "-----END PRIVATE KEY-----\n");
    CertificatesManager certificatesManager = CertificatesManager.getInstance();

    String apiV3Key = "6B75X82A2BKSJHP9FR0Z0DSJVEIT8MMT";

    String merchantSerialNumber = "64F442842DAA110270FB2AED2F7FFD2FA5264D8E";

    certificatesManager.putMerchant(mchId,
        new WechatPay2Credentials(mchId,
            new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
        apiV3Key.getBytes(StandardCharsets.UTF_8));
    Verifier verifier = certificatesManager.getVerifier(mchId);
    WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
        .withMerchant(mchId, merchantSerialNumber, merchantPrivateKey)
        .withValidator(new WechatPay2Validator(verifier));
    CloseableHttpClient httpClient = builder.build();

    String unifiedOrderBody = "{\"sp_appid\":\"wx8c942eb5eb3f0856\",\"sp_mchid\":\"131226658\",\"sub_mchid\":\"468031172\",\"out_trade_no\":\"YX201710140020Z\",\"merchant_category_code\":\"4111\",\"notify_url\":\"https://wxpay.wxutil.com/pub_v2/pay/notify.v2.php\",\"trade_type\":\"NATIVE\",\"amount\":{\"total\":10,\"currency\":\"SGD\"},\"attach\":\"QR code test\",\"description\":\"QR code test\",\"goods_tag\":\"001\",\"detail\":{\"cost_price\":10000,\"receipt_id\":\"1234\",\"goods_detail\":[{\"goods_id\":\"iphone6s_16G\",\"wxpay_goods_id\":\"1001\",\"goods_name\":\"iPhone6s 16G\",\"quantity\":1,\"price\":10}]},\"scene_info\":{\"payer_client_ip\":\"14.23.150.211\",\"device_ip\":\"59.37.125.32\",\"device_id\":\"013467007045764\",\"operator_id\":\"P001\",\"store_info\":{\"id\":\"SZTX001\"}}}";

    HttpPost httpPost = new HttpPost(
        "https://apihk.mch.weixin.qq.com/v3/global/transactions/native");
    httpPost.addHeader("Accept", "application/json");
    httpPost.addHeader("Content-type", "application/json; charset=utf-8");
    httpPost.setEntity(new StringEntity(unifiedOrderBody));
    CloseableHttpResponse response = httpClient.execute(httpPost);

    System.out.println(response.getStatusLine().getStatusCode());

    String result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
        .lines().parallel().collect(Collectors.joining(System.lineSeparator()));

    System.out.println(result);


  }

}
