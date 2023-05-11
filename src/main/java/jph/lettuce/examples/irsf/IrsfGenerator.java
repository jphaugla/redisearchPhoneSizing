package jph.lettuce.examples.irsf;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class IrsfGenerator {
    private static String redis_host="redis-12077.c263.us-east-1-2.ec2.cloud.redislabs.com";
    private static String redis_port="12077";
    private static String redis_pw="tiIPBVc282vsTLpnfhsVCwoEwQPaXdgi";
    private static int num_elements=500000;

    // redis-18345.c17524.eu-west-1-mz.ec2.cloud.rlrcp.com:18345
    // rdRmtLYEaicqCH0b4lmrJWKTUBh5eYYJ

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
        lettuceGen();
        //jedisGen();
    }

    private static void lettuceGen() {

        RedisURI redisURI = create(redis_host,
                                  redis_port,
                                   null,
                                   redis_pw,
                                   false,
                                   null);
        RedisClient redisClient = RedisClient.create(redisURI);


        Random random = new Random();

        RedisCommands<String, String> redisCommands = redisClient.connect()
                                                                 .sync();

        IntStream.range(0, num_elements)
                 .parallel()
                 .forEach(i -> {
                     /*
                     String phoneNumber = String.valueOf(random.nextInt(1_000_000_000)               // Last 9 digits
                                                                 + (random.nextInt(90) + 10) * 1_000_000_000L); // First 2 digits
                     new_phone.put("n", phoneNumber);
                     new_phone.put("n5", phoneNumber.substring(0,5));
                     new_phone.put("n6", phoneNumber.substring(0,6));
                     new_phone.put("n7", phoneNumber.substring(0,7));
                     new_phone.put("n8", phoneNumber.substring(0,8));
                     redisCommands.hmset("n:" + phoneNumber, new_phone);
                     */
                             Map<String, String> new_phone = new HashMap<String, String>();
                             String lastNine = String.format("%09d",random.nextInt(1_000_000_000));// Last 9 digits
                             String firstTwo = String.valueOf((random.nextInt(90) + 10)); // First 2 digits
                             String all11 = firstTwo + lastNine;
                             String key = "n:" + all11;


                             new_phone.put("f2", firstTwo);
                             new_phone.put("f2H", firstTwo.substring(1) + "0");
                             new_phone.put("l9", lastNine);
                             new_phone.put("n", all11);
                             //  this column is next 6 so does not include the first 2 digits
                             new_phone.put("n6", lastNine.substring(0,6));
                             new_phone.put("n6H", lastNine.substring(0,3) + "000");
                             new_phone.put("l3", lastNine.substring(6));

                             redisCommands.hmset(key, new_phone);
                 }
                 );
    }

    public static RedisURI create(String hostname,
                                  String port,
                                  String username,
                                  String password,
                                  boolean isSslEnabled,
                                  String clientName) {
        RedisURI.Builder uriBuilder = RedisURI.builder()
                                              .withSsl(isSslEnabled)
                                              .withHost(hostname)
                                              .withPort(Integer.parseInt(port));

        if (nonNull(username) && nonNull(password)) {
            uriBuilder = uriBuilder.withAuthentication(username, password.toCharArray());
        } else if (nonNull(password)) {
            uriBuilder = uriBuilder.withPassword(password.toCharArray());
        }

        if (clientName != null) {
            uriBuilder = uriBuilder.withClientName(clientName);
        }
        uriBuilder.withVerifyPeer(false);

        return uriBuilder.build();
    }

}
