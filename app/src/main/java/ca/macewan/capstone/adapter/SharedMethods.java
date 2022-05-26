package ca.macewan.capstone.adapter;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class SharedMethods {
    public static void displayItems (List<DocumentReference> itemList, TextView textView) {
        textView.setText("");
        for (DocumentReference documentReference : itemList) {
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    String info = task.getResult().get("name").toString()
                            + " <" + task.getResult().get("email").toString() + ">";
                    String temp = textView.getText().toString();
                    if (temp.equals("")) {
                        textView.append(info);
                        System.out.println("no newline");
                    }
                    else {
                        textView.append("\n" + info);
                        System.out.println("has newline");
                    }
                }
            });
        }
//        if (itemList.size() > 0) {
//            for (int i = 0; i < itemList.size(); i++) {
//                if (i == 0) {
//                    itemList.get(i).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            String info = task.getResult().get("name").toString()
//                                    + " <" + task.getResult().get("email").toString() + ">";
//                            textView.append(info);
//                        }
//                    });
//                }
//                else {
//                    itemList.get(i).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            String info = "\n" + task.getResult().get("name").toString()
//                                    + " <" + task.getResult().get("email").toString() + ">";
//                            textView.append(info);
//                        }
//                    });
//                }
//            }
//        }
    }
}
