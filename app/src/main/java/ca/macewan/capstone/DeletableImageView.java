package ca.macewan.capstone;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class DeletableImageView extends LinearLayout {
    private ImageView imageView;
    private ImageButton mButton;
    private LinearLayout linearLayout;
    private int index;
    private Uri uri;
    private ImageButton buttonAttachImage;
    private HorizontalScrollView scrollViewImages;
    private int clearButtonDistance;

    public DeletableImageView(Context context, int i, ImageButton buttonAttachImage, HorizontalScrollView scrollViewImages, int clearButtonDistance) {
        super(context);
        index = i;
        this.buttonAttachImage = buttonAttachImage;
        this.scrollViewImages = scrollViewImages;
        this.clearButtonDistance = clearButtonDistance;
        init(context);
    }

    public DeletableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DeletableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.deletable_image, this);
        initViews();
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.image);

        View cardViewDelete = findViewById(R.id.cardViewDelete);
        ViewGroup.MarginLayoutParams p = (ConstraintLayout.MarginLayoutParams) cardViewDelete.getLayoutParams();
        p.setMarginEnd(clearButtonDistance);
        cardViewDelete.requestLayout();

        mButton = (ImageButton) findViewById(R.id.delete_button);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.removeView(DeletableImageView.this);
                int size = linearLayout.getChildCount() - 1;
                if (size == 0) {
                    buttonAttachImage.setVisibility(View.VISIBLE);
                    scrollViewImages.setVisibility(View.GONE);
                } else {
                    buttonAttachImage.setVisibility(View.GONE);
                    scrollViewImages.setVisibility(View.VISIBLE);
                    for (int i = index; i < size; i++) {
                        DeletableImageView child = (DeletableImageView) linearLayout.getChildAt(i);
                        child.setIndex(i);
                        if (i == 0) {
                            child.setLayoutParams(DeletableImageView.this.getLayoutParams());
                        }
                    }
                }
            }
        });
    }

    public void setImage(String filePath) {
        uri = Uri.parse(filePath);
        imageView.setImageURI(uri);
    }

    public Uri getUri() {
        return uri;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setLinearLayout(LinearLayout linearLayoutImages) {
        linearLayout = linearLayoutImages;
    }
}