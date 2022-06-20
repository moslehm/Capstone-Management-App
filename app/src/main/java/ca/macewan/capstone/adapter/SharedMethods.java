package ca.macewan.capstone.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.macewan.capstone.EventCompleteListener;
import ca.macewan.capstone.R;
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
        ArrayList<String> imagePaths = (ArrayList<String>) object;
        if (imagePaths.size() == 0) {
            return;
        }
        LinearLayout linearLayoutImages = (LinearLayout) projectView.findViewById(R.id.linearLayoutImages);
        linearLayoutImages.removeAllViews();
        linearLayoutImages.setVisibility(View.VISIBLE);
        projectView.findViewById(R.id.textViewImages).setVisibility(View.VISIBLE);

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
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
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
    }

    public static void displaySupervisors(Object object, MaterialTextView textView) {
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

    public static void displayMembers(Object object, MaterialTextView textView) {
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

    public static void quitProject(DocumentReference userRef, DocumentReference projectRef, Context context, EventCompleteListener eventCompleteListener) {
        new AlertDialog.Builder(context)
                .setMessage("Quit this project?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        projectRef.update("members", FieldValue.arrayRemove(userRef));
                        userRef.update("projects", FieldValue.arrayRemove(projectRef.getId()));
                        eventCompleteListener.onComplete();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    public static void joinProject(DocumentReference userRef, DocumentReference projectRef, Context context, EventCompleteListener eventCompleteListener) {
        new AlertDialog.Builder(context)
                .setMessage("Join this project?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        projectRef.update("members", FieldValue.arrayUnion(userRef));
                        userRef.update("projects", FieldValue.arrayUnion(projectRef.getId()));
                        eventCompleteListener.onComplete();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }


    public static void deleteProject(DocumentReference userRef, DocumentReference projectRef, Context context, EventCompleteListener eventCompleteListener) {
        new AlertDialog.Builder(context)
                .setMessage("Delete this project?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    String projectId = projectRef.getId();
                                    userRef.update("projects", FieldValue.arrayRemove(projectId));
                                    removeFromFields(snapshot.get("members"), projectId, "projects");
                                    removeFromFields(snapshot.get("supervisors"), projectId, "projects");
                                    removeFromFields(snapshot.get("supervisorsPending"), projectId, "invited");
                                    Object imagesObject = snapshot.get("imagePaths");
                                    if (imagesObject != null) {
                                        List<String> imagesList = (ArrayList<String>) imagesObject;
                                        for (String imagePath : imagesList) {
                                            StorageReference photoRef =  FirebaseStorage.getInstance().getReferenceFromUrl(imagePath);
                                            photoRef.delete();
                                        }
                                    }
                                    projectRef.delete();
                                    eventCompleteListener.onComplete();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

    // Removes itemToRemove from fieldString of each item in listObject
    // Ex. Remove a project's DocumentReference from "projects" field for each member in the list
    // provided
    private static void removeFromFields(Object listObject, String itemToRemove, String fieldString) {
        if (listObject == null) {
            return;
        }
        List<DocumentReference> itemList = (ArrayList<DocumentReference>) listObject;
        for (DocumentReference item : itemList) {
            item.update(fieldString, FieldValue.arrayRemove(itemToRemove));
        }
    }

    public static <T> boolean listIsEmpty(List<T> list) {
        return list == null || list.size() == 0;
    }

    public static void createFragment(FragmentManager supportFragmentManager, int id, Fragment fragment, String tag){
        supportFragmentManager.beginTransaction()
                .add(id, fragment, tag)
                .hide(fragment)
                .commit();
    }
    public static void showFragment(FragmentManager supportFragmentManager, Fragment fragment){
        supportFragmentManager.beginTransaction()
                .show(fragment)
                .commit();
    }
    public static void hideFragment(FragmentManager supportFragmentManager, Fragment fragment){
        supportFragmentManager.beginTransaction()
                .hide(fragment)
                .commit();
    }
}
