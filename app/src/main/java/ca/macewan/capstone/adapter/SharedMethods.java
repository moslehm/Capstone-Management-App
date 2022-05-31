package ca.macewan.capstone.adapter;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import uk.co.onemandan.materialtextview.MaterialTextView;

public class SharedMethods {
    public static void displayItems (List<DocumentReference> itemList, MaterialTextView textView) {
        textView.setContentText("", null);
        for (DocumentReference documentReference : itemList) {
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    String info = task.getResult().get("name").toString()
                            + " <" + task.getResult().get("email").toString() + ">";
                    String temp = (String) textView.getContentText();
                    if (temp.equals("")) {
                        textView.setContentText(info, null);
                    }
                    else {
                        temp += ("\n" + info);
                        textView.setContentText(temp, null);
                    }
                }
            });
        }
    }
}
