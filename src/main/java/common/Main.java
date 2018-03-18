package common;

/**
 * Created by evgeniyh on 1/18/18.
 */

public class Main {
    public static void main(String[] args) {
//        String json = System.getenv("M");
//        System.out.println(json);
//        MessageHubCredentials credentials = (new Gson()).fromJson(json, MessageHubCredentials.class);
//        System.out.println(credentials.getRestUrl());
//        System.out.println(credentials.getInstanceId());
//        System.out.println(Arrays.toString(credentials.getBrokers()));
//        try (InputStream inputStream = MessageHubHealthCheck.class.getClassLoader().getResourceAsStream("consumer.properties")) {
//
//            Properties prop = new Properties();
//            prop.load(inputStream);
//            System.out.println(prop);
//        } catch (Throwable e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//        }

//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        JsonObject e = (JsonObject) new JsonParser().parse("{\n" +
//                "        \"CATALOG-SEARCH-SERVICE\": 0,\n" +
//                "        \"FRONTEND-SERVICE\": 1,\n" +
//                "        \"BUSINESS-PROCESS-SERVICE\": 1,\n" +
//                "        \"CATALOGUE-SERVICE-SRDC\": 1,\n" +
//                "        \"IDENTITY-SERVICE\": 1,\n" +
//                "        \"GATEWAY-PROXY\": 1\n" +
//                "      }");
//        String json = gson.toJson(e);
//        System.out.println(json);
//        System.out.println(e.get("CATALOGUE-SERVICE-SRDC").getAsBoolean());

//        try {
//            ObjectStoreHealthChecker h = new ObjectStoreHealthChecker(new ObjectStoreConfig("asd", "OBJECT_STORE_CREDENTIALS", "test", "health-test-file"));
//            h.init();
//            h.runCheck();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

//        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
//
//            HttpResponse resp1 = Common.executeHttpGet("http://9.148.10.164:998/health-check");
//
//            String res1 = Common.inputStreamToString(resp1.getEntity().getContent());
//            System.out.println("res = " + res1 );
//
//            HttpGet getHealth = new HttpGet("http://9.148.10.164:998/health-check");
//
//            CloseableHttpResponse response = httpclient.execute(getHealth);
//
//            String res = Common.inputStreamToString(response.getEntity().getContent());
//            System.out.println("res = " + res );
//
//        } catch (Throwable t) {
//            System.out.println("FAILED");
//
//        }
//    }

////        Keycloak kc = KeycloakBuilder.builder() //
////                .serverUrl("http://nimble-platform.salzburgresearch.at:8080/auth") //
////                .realm("master")//
////                .username("admin") //
////                .password("_Nimble_2016_") //
////                .grantType(OAuth2Constants.PASSWORD) //
////                .clientId("security-admin-console") //
////                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()) //
////                .build();
////        kc.realm("master").users().list().forEach(System.out::println);
//        Keycloak keycloak = KeycloakBuilder.builder()
//                .serverUrl("http://nimble-platform.salzburgresearch.at:8080/auth")
//                .realm("master")
//                .grantType(OAuth2Constants.PASSWORD)
//                .username("admin")
//                .password("_Nimble_2016_")
//                .clientId("admin-cli")
//                .clientSecret("1bd6e868-171d-4df1-b654-8ba7b2d2a7d7")
//                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
//                .build();
//        keycloak.realm("master").users().list().forEach(System.out::println);
//
//
//        Keycloak kc = Keycloak.getInstance("http://nimble-platform.salzburgresearch.at:8080/auth", "master", "admin", "_Nimble_2016_", "security-admin-console", "1d3722e3-9b5c-4505-ae07-978c2355072b");
////        Keycloak kc = Keycloak.getInstance("http://nimble-platform.salzburgresearch.at:8080/auth/", "master", "security-admin-console","eyJhbGciOiJSUzI1NiIsImtpZCIgOiAienBadi1ZTU5SX0VEWnhCQzdLUU5sNFRFanpLaXFCZzcyYWNCR2Y5Qm1RVSJ9.eyJqdGkiOiIyYTMwZGZjMy04MTA1LTQ2Y2EtOGJhMi0wYWY3NDBlNzIwM2UiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTE2MjY2NjgxLCJpc3MiOiJodHRwczovL25pbWJsZS1wbGF0Zm9ybS5zYWx6YnVyZ3Jlc2VhcmNoLmF0OjgwODAvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoiaHR0cHM6Ly9uaW1ibGUtcGxhdGZvcm0uc2FsemJ1cmdyZXNlYXJjaC5hdDo4MDgwL2F1dGgvcmVhbG1zL21hc3RlciIsInR5cCI6IlJlZ2lzdHJhdGlvbkFjY2Vzc1Rva2VuIiwicmVnaXN0cmF0aW9uX2F1dGgiOiJhdXRoZW50aWNhdGVkIn0.Uzhnt8NGqfcz_t0gDUHOfCKZDKm7JrByTE6lkBH40T43xg9LJYrZ5_urmX64skw8m8mFFrfX1Qwee1Cj_-8biiYhKLT6APJBuT56NJIhhd__9o4OtzshdsbhJnpkaTEaTSiIJLsJO5rqABHDbFeQXmq4c7VnTAukN14Wt8Yrx8sxZAmYlzx5bkwunZyXVTKEw13PNRsMsDVFYL3w4RVl-D4OI-EBsdMQ6tL97pznzUYMTaAWYZbhFLajZYogElYhUxCL8bpa13fITtIwj_dC23QlzLpzKjUUn89U_zPRbhHGWYUYUVb4XoVZuxf6bgCt04Tz8MoQVt9JDEFvtN-g5Q");
//        kc.realm("master").users().list().forEach(System.out::println);
//
//        kc.realms().findAll().forEach(System.out::println);
//        System.out.println(kc);
//        try {
//            HttpResponse res = Common.executeHttpGet("https://nimble-platform.salzburgresearch.at:8080/auth/");
//
//            System.out.println();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
