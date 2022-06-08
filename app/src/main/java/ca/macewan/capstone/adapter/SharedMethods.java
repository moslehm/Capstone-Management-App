package ca.macewan.capstone.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.macewan.capstone.R;
import ca.macewan.capstone.User;
import uk.co.onemandan.materialtextview.MaterialTextView;

public class SharedMethods {
    public static void displayItems(ArrayList<DocumentReference> itemList, MaterialTextView textView) {
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

    public static void displayStrings(Object object, MaterialTextView textView) {
        textView.setContentText("", null);
        if (object == null) {
            return;
        }
        ArrayList<String> itemList = (ArrayList<String>) object;

        StringBuilder finalString = new StringBuilder();
        for (String item : itemList) {
                finalString.append(item).append("\n");
        }
        textView.setContentText(finalString.toString(), null);
    }

    public static void displayImages(Object object, View projectView, FragmentActivity activity) {
        if (object == null) {
            return;
        }
        LinearLayout linearLayoutImages = (LinearLayout) projectView.findViewById(R.id.linearLayoutImages);
        linearLayoutImages.removeAllViews();
        linearLayoutImages.setVisibility(View.VISIBLE);
        projectView.findViewById(R.id.textViewImages).setVisibility(View.VISIBLE);
        ArrayList<String> imagePaths = (ArrayList<String>) object;

        for (String imagePath : imagePaths) {
            ImageView newImage = new ImageView(activity);
            linearLayoutImages.addView(newImage);
            Picasso.get().load(imagePath).into(newImage);
        }
    }

    public static void setupProjectView(View projectView, DocumentReference documentReference, String email, FragmentActivity activity) {
        MaterialTextView textViewTitle = (MaterialTextView) projectView.findViewById(R.id.textViewTitle);
        MaterialTextView textViewCreator = (MaterialTextView) projectView.findViewById(R.id.textViewCreator);
        MaterialTextView textViewSemesterAndYear = (MaterialTextView) projectView.findViewById(R.id.textViewSemesterAndYear);
        MaterialTextView textViewSupervisors = (MaterialTextView) projectView.findViewById(R.id.textViewSupervisors);
        MaterialTextView textViewMembers = (MaterialTextView) projectView.findViewById(R.id.textViewMembers);
        MaterialTextView textViewDescription = (MaterialTextView) projectView.findViewById(R.id.textViewDescription);
        MaterialTextView textViewTags = (MaterialTextView) projectView.findViewById(R.id.textViewTags);
        CheckBox checkBoxStatus = (CheckBox) projectView.findViewById(R.id.checkBox_StatusProf);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    textViewTitle.setContentText(documentSnapshot.getString("name"), null);

                    DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
                    creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                String creatorName = task.getResult().getString("name");
                                String creatorEmail = task.getResult().getString("email");
                                String creatorInfo = creatorName + " <" + creatorEmail + ">";
                                if (Objects.equals(email, creatorEmail)) {
                                    checkBoxStatus.setEnabled(true);
                                }
                                textViewCreator.setContentText(creatorInfo, null);
                            }
                        }
                    });

                    String semesterAndYear = documentSnapshot.getString("semester") + " " + documentSnapshot.getString("year");
                    textViewSemesterAndYear.setContentText(semesterAndYear, null);
                    SharedMethods.displaySupervisors(documentSnapshot.get("supervisors"), textViewSupervisors);
                    SharedMethods.displayMembers(documentSnapshot.get("members"), textViewMembers);
                    textViewDescription.setContentText(documentSnapshot.getString("description"), null);
                    SharedMethods.displayStrings(documentSnapshot.get("tags"), textViewTags);
                    SharedMethods.displayImages(documentSnapshot.get("imagePaths"), projectView, activity);

                    boolean status = documentSnapshot.getBoolean("status");
                    if (status) {
                        checkBoxStatus.setChecked(true);
                    }
                    else {
                        checkBoxStatus.setChecked(false);
                    }
                    checkBoxStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (checkBoxStatus.isChecked()) {
                                documentReference.update("status", true);
                            }
                            else {
                                documentReference.update("status", false);
                            }
                        }
                    });
                }
            }
        });
//        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                DocumentSnapshot documentSnapshot = task.getResult();
//                if (documentSnapshot.exists()) {
//                    textViewTitle.setContentText(documentSnapshot.getString("name"), null);
//
//                    DocumentReference creatorRef = documentSnapshot.getDocumentReference("creator");
//                    creatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if (task.isSuccessful()) {
//                                String creatorName = task.getResult().getString("name");
//                                String creatorEmail = task.getResult().getString("email");
//                                String creatorInfo = creatorName + " <" + creatorEmail + ">";
//                                if (Objects.equals(email, creatorEmail)) {
//                                    checkBoxStatus.setEnabled(true);
//                                }
//                                textViewCreator.setContentText(creatorInfo, null);
//                            }
//                        }
//                    });
//
//                    String semesterAndYear = documentSnapshot.getString("semester") + " " + documentSnapshot.getString("year");
//                    textViewSemesterAndYear.setContentText(semesterAndYear, null);
//                    SharedMethods.displayItems(documentSnapshot.get("supervisors"), textViewSupervisors);
//                    SharedMethods.displayItems(documentSnapshot.get("members"), textViewMembers);
//                    textViewDescription.setContentText(documentSnapshot.getString("description"), null);
//                    SharedMethods.displayStrings(documentSnapshot.get("tags"), textViewTags);
//                    SharedMethods.displayImages(documentSnapshot.get("imagePaths"), projectView, activity);
//
//                    boolean status = documentSnapshot.getBoolean("status");
//                    if (status) {
//                        checkBoxStatus.setChecked(true);
//                    }
//                    else {
//                        checkBoxStatus.setChecked(false);
//                    }
//                    checkBoxStatus.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (checkBoxStatus.isChecked()) {
//                                documentReference.update("status", true);
//                            }
//                            else {
//                                documentReference.update("status", false);
//                            }
//                        }
//                    });
//                }
//            }
//        });
    }

    private static void displaySupervisors(Object object, MaterialTextView textView) {
        if (object == null) {
            textView.setContentText("Pending", null);
            return;
        }
        ArrayList<DocumentReference> members = (ArrayList<DocumentReference>) object;
        if (members.size() == 0) {
            textView.setContentText("Pending", null);
            return;
        }
        displayItems(members, textView);
    }

    private static void displayMembers(Object object, MaterialTextView textView) {
        // TODO: check project's "invitedSupervisors" field and display "None" if there are none there
        if (object == null) {
            textView.setContentText("None", null);
            return;
        }
        ArrayList<DocumentReference> supervisors = (ArrayList<DocumentReference>) object;
        if (supervisors.size() == 0) {
            textView.setContentText("None", null);
            return;
        }
        displayItems(supervisors, textView);
    }
}
