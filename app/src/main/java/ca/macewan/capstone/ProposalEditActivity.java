package ca.macewan.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProposalEditActivity extends AppCompatActivity {
    FirebaseFirestore db;
    EditText editTextTitle, editTextDescription;
    private EditText editTextKeyword;
    List<String> tags;
    private HorizontalScrollView scrollViewTags;
    private ChipGroup chipGroup;
    private ImageButton buttonAttachImage;
    HorizontalScrollView scrollViewImages;
    LinearLayout linearLayoutImages;
    private int downX;
    Button buttonSubmit;
    private String projectID;
    private String email;
    private DocumentReference projectRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_edit);

        // Get action bar and show back button
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Edit Project");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        db = FirebaseFirestore.getInstance();
        projectID = getIntent().getExtras().getString("projectID");
        email = getIntent().getExtras().getString("email");
        projectRef = db.collection("Projects").document(projectID);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);

        setupTagChips();
        setupImageAttachment();
        setupSubmitButton();
        fillExistingInfo();
    }

    private void setupSubmitButton() {
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

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                projectRef.update("name", editTextTitle.getText().toString());
                projectRef.update("description", editTextDescription.getText().toString());
                projectRef.update("tags", tags);
                projectRef.update("lastModified", Timestamp.now());

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
//                                List<String> imagePaths = new ArrayList<String>();
                                for (Object object : objects) {
                                    Uri uri = (Uri) object;
//                                    imagePaths.add(uri.toString());
                                    projectRef.update("imagePaths", FieldValue.arrayUnion(uri.toString()));
                                }
                                finish();
                            }
                        });
                    }
                });
                NotifClient notifier = new NotifClient();
                notifier.payloadThread(notifier.payloadConstructor(
                        String.format("%s has received changes!", editTextTitle.getText().toString()),
                        "Check them out now!", "projectChange", projectID));
                finish();
            }
        });
    }

    private void checkRequiredFields() {
        boolean titleEmpty = editTextTitle.getText().toString().isEmpty();
        boolean descriptionEmpty = editTextDescription.getText().toString().isEmpty();
        buttonSubmit.setEnabled(!titleEmpty && !descriptionEmpty);
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
                String keyword = editTextKeyword.getText().toString();
                addNewChip(keyword);
                return true;
            }
            return false;
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = editTextKeyword.getText().toString();
                addNewChip(keyword);
            }
        });
    }

    private void addNewChip(String keyword) {
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
                ImagePicker.create(ProposalEditActivity.this).start();
            }
        });
        ImageButton buttonAttachImage2 = (ImageButton) findViewById(R.id.buttonAttachImage2);
        buttonAttachImage2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImagePicker.create(ProposalEditActivity.this).start();
            }
        });

        linearLayoutImages = (LinearLayout) findViewById(R.id.linearLayoutImages);
        scrollViewImages = (HorizontalScrollView) findViewById(R.id.scrollViewImages);
    }

    private void fillExistingInfo() {
        projectRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        editTextTitle.setText(snapshot.getString("name"));
                        editTextDescription.setText(snapshot.getString("description"));
                        Object tagsObject = snapshot.get("tags");
                        if (tagsObject == null) {
                            return;
                        }
                        ArrayList<String> tagsList = (ArrayList<String>) tagsObject;
                        for (String tag : tagsList) {
                            addNewChip(tag);
                        }
                    }
                }
            });
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
                int clearButtonDistance = (int) (imageViewWidth * 0.25);
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
                    imageView = new DeletableImageView(this, i, buttonAttachImage, scrollViewImages, clearButtonDistance);
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
                    imageView = new DeletableImageView(this, i, buttonAttachImage, scrollViewImages, clearButtonDistance);
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