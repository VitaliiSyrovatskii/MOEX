import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MOEXConnection {

    private static HttpClient client;
    private static HttpRequest request;

    private static void getClient(){
        if (client == null){
            client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }
    }

    private static void setRequest (String uri){
        request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .build();
    }

    public static String getResponse (String uri){
        getClient();
        setRequest(uri);
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            e.printStackTrace();
            getResponse(uri);
        }
        return new String();
    }
}
