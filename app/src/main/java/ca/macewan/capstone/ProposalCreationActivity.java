package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observer;

import me.srodrigo.androidhintspinner.HintAdapter;
import me.srodrigo.androidhintspinner.HintSpinner;


public class ProposalCreationActivity extends AppCompatActivity {
    FirebaseFirestore db;
    EditText editTextTitle, editTextDescription, editTextYear;
    String selectedSemester;
    ArrayList<User> arrayListSupervisors;
    boolean[] selectedSupervisors;
    ArrayList<Integer> supervisorList = new ArrayList<>();
    private EditText editTextSupervisors, editTextKeyword;
    public AlertDialog alertDialogSupervisor;
    List<String> tags;
    private HorizontalScrollView scrollViewTags;
    private ChipGroup chipGroup;
    private ImageButton buttonAttachImage;
    HorizontalScrollView scrollViewImages;
    LinearLayout linearLayoutImages;
    private int downX;
    Button buttonSubmit;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_creation);

        // Get action bar and show back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Create Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        String[] semesters = {"Fall", "Winter", "Spring", "Summer"};
        Spinner semestersSpinner = (Spinner) findViewById(R.id.spinnerSemester);
        addSpinner(semestersSpinner, semesters, "Semester");

        setupSupervisors();
        setupTagChips();
        setupImageAttachment();
        setupSubmitButton();
    }

    private void setupSubmitButton() {
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextTitle.addTextChangedListener (new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                checkRequiredFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        editTextDescription.addTextChangedListener (new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                checkRequiredFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editTextYear = (EditText) findViewById(R.id.editTextYear);
        editTextYear.addTextChangedListener (new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                checkRequiredFields();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setup Creator, Title, Description, semester, and supervisor values
                DocumentReference creator =  db.collection("Users").document(userEmail);
                String title = editTextTitle.getText().toString();
                String description = editTextDescription.getText().toString();
                String year = editTextYear.getText().toString();
                Project project = new Project(creator, title, description, selectedSemester, year);

                db.collection("Projects")
                    .add(project)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                DocumentReference projectRef = task.getResult();
                                inviteSupervisors(projectRef);
                                creator.update("projects", FieldValue.arrayUnion(projectRef));
                                projectRef.update("tags", tags);
                                if (linearLayoutImages.getChildCount() - 1 == 0) {
                                    finish();
                                }
                                uploadImages(projectRef);
                            }
                        }

                        private void uploadImages(DocumentReference projectRef) {
                            String projectID = projectRef.getId();
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference imageRef = null;
                            List<UploadTask> myTasks = new ArrayList<>();
                            for (int i = 0; i < linearLayoutImages.getChildCount() - 1; i++) {
                                DeletableImageView image = (DeletableImageView) linearLayoutImages.getChildAt(i);
                                imageRef = storageRef.child("project_images/" + projectID + "/" + image.getUri().getLastPathSegment());
                                InputStream stream = null;
                                try {
                                    stream = new FileInputStream(image.getUri().getPath());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                myTasks.add(imageRef.putStream(stream));
                            }
                            Tasks.whenAllSuccess(myTasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                @Override
                                public void onSuccess(List<Object> objects) {
                                    List<Task<Uri>> tasks = new ArrayList<Task<Uri>>();
                                    for (Object object : objects) {
                                        UploadTask.TaskSnapshot snapshot = (UploadTask.TaskSnapshot) object;
                                        tasks.add(snapshot.getMetadata().getReference().getDownloadUrl());
                                    }
                                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                        @Override
                                        public void onSuccess(List<Object> objects) {
                                            List<String> imagePaths = new ArrayList<String>();
                                            for (Object object : objects) {
                                                Uri uri = (Uri) object;
                                                imagePaths.add(uri.toString());
                                                finish();
                                            }
                                            projectRef.update("imagePaths", imagePaths);
                                        }
                                    });
                                }
                            });
                        }
                    });
            }
        });
    }

    private void inviteSupervisors(DocumentReference projectRef) {
       for (int i = 0; i < arrayListSupervisors.size(); i++) {
            if (selectedSupervisors[i]) {
                String supervisorEmail = arrayListSupervisors.get(i).email;
                DocumentReference supervisor = db.collection("Users").document(supervisorEmail);
                if (supervisorEmail.equals(userEmail)) {
                    supervisor.update("projects", FieldValue.arrayUnion(projectRef));
                    projectRef.update("supervisors", FieldValue.arrayUnion(supervisor));
                    continue;
                }
                supervisor.update("invited", FieldValue.arrayUnion(projectRef));
                projectRef.update("supervisorsPending", FieldValue.arrayUnion(supervisor));
            }
        }
    }

    private void checkRequiredFields() {
        boolean titleEmpty = editTextTitle.getText().toString().isEmpty();
        boolean descriptionEmpty = editTextDescription.getText().toString().isEmpty();
        boolean semesterChosen = selectedSemester != null;
        boolean yearEmpty = editTextYear.getText().toString().isEmpty();
        boolean supervisorsChosen = anySupervisorsSelected();
        buttonSubmit.setEnabled(!titleEmpty && !descriptionEmpty && semesterChosen && !yearEmpty && supervisorsChosen);
    }

    public boolean anySupervisorsSelected() {
        for (boolean val : selectedSupervisors) {
            if (val)
                return true;
        }
        return false;
    }

    private void setupSupervisors() {
        arrayListSupervisors = new ArrayList<User>();
        db.collection("Users")
                .whereEqualTo("role", "professor")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                arrayListSupervisors.add(document.toObject(User.class));
                            }
                            populateAlertDialog();
                        }
                    }
                });
    }

    private void populateAlertDialog() {
        String[] supervisors = new String[arrayListSupervisors.size()];
        for (int i = 0; i < arrayListSupervisors.size(); i++){
            supervisors[i] = arrayListSupervisors.get(i).name;
        }
        editTextSupervisors = findViewById(R.id.editTextSupervisors);
        selectedSupervisors = new boolean[supervisors.length];

        editTextSupervisors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alertDialogSupervisor != null && alertDialogSupervisor.isShowing()) return;

                // Close keyboard if open
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(ProposalCreationActivity.this);
                builder.setTitle("Supervisor(s)");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(supervisors, selectedSupervisors, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean boxChecked) {
                        if (boxChecked) {
                            supervisorList.add(i);
                            Collections.sort(supervisorList);
                        } else {
                            supervisorList.remove(Integer.valueOf(i));
                        }
                        checkRequiredFields();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Put supervisor names together for the EditText
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < supervisorList.size(); j++) {
                            stringBuilder.append(supervisors[supervisorList.get(j)]);
                            if (j != supervisorList.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }
                        editTextSupervisors.setText(stringBuilder.toString());
                    }
                });
                alertDialogSupervisor = builder.show();
            }
        });
    }


    private void addSpinner(Spinner spinner, String[] stringArray, String hint) {
        HintSpinner<String> hintSpinner = new HintSpinner<String>(
                spinner,
                new HintAdapter<String>(this, hint, Arrays.asList(stringArray)),
                new HintSpinner.Callback<String>() {
                    @Override
                    public void onItemSelected(int position, String itemAtPosition) {
                        // Semester selected here
                        selectedSemester = itemAtPosition;
                        checkRequiredFields();
                    }
                });
        View spinnerOverlay = findViewById(R.id.spinner_overlay);
        spinnerOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.performClick();
            }
        });
        hintSpinner.init();
    }

    private void setupTagChips() {
        tags = new ArrayList<String>();
        scrollViewTags = (HorizontalScrollView) findViewById(R.id.scrollViewTags);
        editTextKeyword = (EditText) findViewById(R.id.editTextTags);
        chipGroup = (ChipGroup) findViewById(R.id.chipGroup);
        ImageButton buttonAdd = (ImageButton) findViewById(R.id.buttonAddTag);

        editTextKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    buttonAdd.setVisibility(View.GONE);
                } else {
                    buttonAdd.setVisibility(View.VISIBLE);
                    final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollViewMain);
                    scrollView.scrollTo(0, scrollView.getBottom());
                }

            }
        });
        editTextKeyword.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNewChip();
                return true;
            }
            return false;
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewChip();
            }
        });
    }

    private void addNewChip() {
        String keyword = this.editTextKeyword.getText().toString();
        if (keyword.isEmpty()) {
            return;
        }
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            // Create a Chip from Layout.
            Chip newChip = (Chip) inflater.inflate(R.layout.layout_entry_chip, this.chipGroup, false);
            newChip.setText(keyword);
            tags.add(keyword);
            this.chipGroup.addView(newChip);
            newChip.setClickable(false);
            newChip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleChipCloseIconClicked((Chip) v);
                }
            });
            this.editTextKeyword.setText("");
            scrollViewTags.post(new Runnable() {
                @Override
                public void run() {
                    scrollViewTags.fullScroll(View.FOCUS_RIGHT);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleChipCloseIconClicked(Chip chip) {
        chipGroup.removeView(chip);
        tags.remove((String) chip.getText());
    }

    private void setupImageAttachment() {
        buttonAttachImage = (ImageButton) findViewById(R.id.buttonAttachImage);
        buttonAttachImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImagePicker.create(ProposalCreationActivity.this).start();
            }
        });
        ImageButton buttonAttachImage2 = (ImageButton) findViewById(R.id.buttonAttachImage2);
        buttonAttachImage2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImagePicker.create(ProposalCreationActivity.this).start();
            }
        });

        linearLayoutImages = (LinearLayout) findViewById(R.id.linearLayoutImages);
        scrollViewImages = (HorizontalScrollView) findViewById(R.id.scrollViewImages);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            List<Image> images = ImagePicker.getImages(data);
            ImageButton buttonFound = linearLayoutImages.findViewById(R.id.buttonAttachImage2);
            if (buttonFound == null) {
                // Button should always be found
                try {
                    throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            linearLayoutImages.removeView(buttonFound);
            int currentSize = linearLayoutImages.getChildCount();
            int newSize = currentSize + images.size();
            if (newSize > 0) {
                buttonAttachImage.setVisibility(View.GONE);
                scrollViewImages.setVisibility(View.VISIBLE);
                int width = this.getResources().getDisplayMetrics().widthPixels;
                int edgeOfScreenMargin = (int) dpToPx(16);
                int inbetweenImagesMargin = (int) dpToPx(6);
                // Not sure why we need to add 50 but it gives the images the perfect width
                int imageViewWidth = (width/2) - edgeOfScreenMargin - edgeOfScreenMargin - inbetweenImagesMargin + 50;
                LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(imageViewWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                String filePath;
                DeletableImageView imageView;
                int i = currentSize;
                int index = 0;
                // Layout Params for first image if no images are already attached
                // This allows us to have the larger margin on the left
                if (linearLayoutImages.getChildCount() == 0) {
                    filePath = images.get(0).getPath();
                    parms.rightMargin = inbetweenImagesMargin;
                    parms.leftMargin = edgeOfScreenMargin;
                    imageView = new DeletableImageView(this, i, buttonAttachImage, scrollViewImages);
                    imageView.setImage(filePath);
                    imageView.setLinearLayout(linearLayoutImages);
                    imageView.setLayoutParams(parms);
                    linearLayoutImages.addView(imageView);
                    i++;
                    index++;
                }
                // Layout Params for the rest of the images
                parms = new LinearLayout.LayoutParams(imageViewWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                parms.rightMargin = inbetweenImagesMargin;
                for (; i < newSize; i++) {
                    filePath = images.get(index).getPath();
                    imageView = new DeletableImageView(this, i, buttonAttachImage, scrollViewImages);
                    imageView.setImage(filePath);
                    imageView.setLinearLayout(linearLayoutImages);
                    imageView.setLayoutParams(parms);
                    linearLayoutImages.addView(imageView);
                    index++;
                }
                // Layout Params for the button at the end
                parms = new LinearLayout.LayoutParams(imageViewWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                parms.rightMargin = edgeOfScreenMargin;
                buttonFound.setLayoutParams(parms);
                linearLayoutImages.addView(buttonFound);
            } else {
                buttonAttachImage.setVisibility(View.VISIBLE);
                scrollViewImages.setVisibility(View.GONE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public float dpToPx(final float dp) {
        return dp * this.getResources().getDisplayMetrics().density;
    }

    // Makes EditTexts lose focus when appropriate
    // Reference: https://stackoverflow.com/a/61290481
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = (int) event.getRawX();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                // Was it a scroll - If skip all
                if (Math.abs(downX - x) > 5) {
                    return super.dispatchTouchEvent(event);
                }
                final int reducePx = 25;
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                // Bounding box is to big, reduce it just a little bit
                outRect.inset(reducePx, reducePx);
                if (!outRect.contains(x, y)) {
                    v.clearFocus();
                    boolean touchTargetIsEditText = false;
                    // Check if another editText has been touched
                    for (View vi : v.getRootView().getTouchables()) {
                        if (vi instanceof EditText) {
                            Rect clickedViewRect = new Rect();
                            vi.getGlobalVisibleRect(clickedViewRect);
                            // Bounding box is to big, reduce it just a little bit
                            clickedViewRect.inset(reducePx, reducePx);
                            if (clickedViewRect.contains(x, y)) {
                                touchTargetIsEditText = true;
                                break;
                            }
                        }
                    }
                    if (!touchTargetIsEditText) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}