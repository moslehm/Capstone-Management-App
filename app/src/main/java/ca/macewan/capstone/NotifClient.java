package ca.macewan.capstone;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NotifClient {

    public JSONObject payloadConstructor(String title, String body, String token) {
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("body", body);
            json.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject payloadConstructor(String title, String body, String topicB, String topicC) {
        //TopicB and TopicC are interchangeable.
        //One must be a related project or other identifier.
        //The other must be a related
        JSONObject json = new JSONObject();
        try {
            json.put("title", title);
            json.put("body", body);
            json.put("topicA", "notifsEnabled");
            if (topicB != null) {
                json.put("topicB", topicB);
            }

            if (topicC != null) {
                json.put("topicC", topicC);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public void tokenThread(final JSONObject payload) {
        Thread t = new Thread(() -> {
            try {
                sendToken(payload);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    private void sendToken(JSONObject payload) throws IOException, JSONException {
        Socket socket = null;
        OutputStreamWriter output = null;
        socket = new Socket("34.168.78.99", 10000);
        System.out.println("Connected");
        output = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        output.write(payload.toString());
        output.close();
        socket.close();
    }
}
