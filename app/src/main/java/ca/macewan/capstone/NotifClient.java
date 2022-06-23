package ca.macewan.capstone;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

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

    public void joinThread(final DocumentReference userRef, final DocumentReference projectRef) {
        Thread t = new Thread(() -> {
            try {
                String[] info = retrieveInfo(userRef, projectRef);
                payloadThread(payloadConstructor(
                        String.format("%s has a new member!", info[1]),
                        String.format("%s has joined the project.", info[2]),
                        "projectJoin",
                        info[3]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void quitThread(final DocumentReference userRef, final DocumentReference projectRef) {
        Thread t = new Thread(() -> {
            try {
                String[] info = retrieveInfo(userRef, projectRef);
                payloadThread(payloadConstructor(
                        String.format("%s has lost a member!", info[1]),
                        String.format("%s has left the project.", info[2]),
                        "projectJoin",
                        info[3]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();
    }
    public String[] retrieveInfo(DocumentReference userRef, DocumentReference projectRef) {
        //Returns an array of 3 strings
        //info[0] is username
        //info[1] is project name
        //info[2] is project id

        String[] info = new String[3];
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    info[0] = task.getResult().getString("name");
                }
            }
        });
        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    info[1] = task.getResult().getString("name");
                    info[2] = task.getResult().getId();
                }
            }
        });
        return info;
    }

    public void payloadThread(final JSONObject payload) {
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
