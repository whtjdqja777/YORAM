package com.example.yoram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class YogaSellectActivity extends AppCompatActivity {

    private LinearLayout list1;
    private LinearLayout list2;
    private LinearLayout list3;
    private LinearLayout list4;
    private final Set<String> selectedpose = new LinkedHashSet<>();
    private  SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga_sellect); // Replace with your layout file name

        // Find your LinearLayouts by their IDs
        list1 = findViewById(R.id.list1);
        list2 = findViewById(R.id.list2);
        list3 = findViewById(R.id.list3);
        list4 = findViewById(R.id.list4);

        // Find your buttons by their IDs
        Button neckButton = findViewById(R.id.neck_button);
        Button waistButton = findViewById(R.id.waist_button);
        Button hipJointButton = findViewById(R.id.hip_joint_button);
        Button legButton = findViewById(R.id.leg_button);

        prefs = getSharedPreferences("yoga", MODE_PRIVATE);
        // Set click listeners to your buttons
        neckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.neck_button) {
                    showLayout(list1);
                } else if (v.getId() == R.id.waist_button) {
                    showLayout(list2);
                } else if (v.getId() == R.id.hip_joint_button) {
                    showLayout(list3);
                } else if (v.getId() == R.id.leg_button) {
                    showLayout(list4);
                }
            }
        });

        waistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.neck_button) {
                    showLayout(list1);
                } else if (v.getId() == R.id.waist_button) {
                    showLayout(list2);
                } else if (v.getId() == R.id.hip_joint_button) {
                    showLayout(list3);
                } else if (v.getId() == R.id.leg_button) {
                    showLayout(list4);
                }
            }
        });
        hipJointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.neck_button) {
                    showLayout(list1);
                } else if (v.getId() == R.id.waist_button) {
                    showLayout(list2);
                } else if (v.getId() == R.id.hip_joint_button) {
                    showLayout(list3);
                } else if (v.getId() == R.id.leg_button) {
                    showLayout(list4);
                }
            }
        });
        legButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.neck_button) {
                    showLayout(list1);
                } else if (v.getId() == R.id.waist_button) {
                    showLayout(list2);
                } else if (v.getId() == R.id.hip_joint_button) {
                    showLayout(list3);
                } else if (v.getId() == R.id.leg_button) {
                    showLayout(list4);
                }
            }
        });

        // For neck actions
        setClickListener((ImageButton) findViewById(R.id.cat_pose), (ImageView) findViewById(R.id.cat_pose), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.for_back_pose), (ImageView) findViewById(R.id.for_back_pose), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.neckact3), (ImageView) findViewById(R.id.neckact3), new boolean[]{false});

// For waist actions
        setClickListener((ImageButton) findViewById(R.id.waistact1), (ImageView) findViewById(R.id.waistact1), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.waistact2), (ImageView) findViewById(R.id.waistact2), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.cobra_pose), (ImageView) findViewById(R.id.cobra_pose), new boolean[]{false});

// For hip joint actions
        setClickListener((ImageButton) findViewById(R.id.hipjointact1), (ImageView) findViewById(R.id.hipjointact1), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.hipjointact2), (ImageView) findViewById(R.id.hipjointact2), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.hipjointact3), (ImageView) findViewById(R.id.hipjointact3), new boolean[]{false});

// For leg actions
        setClickListener((ImageButton) findViewById(R.id.warrior), (ImageView) findViewById(R.id.warrior), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.legact2), (ImageView) findViewById(R.id.legact2), new boolean[]{false});
        setClickListener((ImageButton) findViewById(R.id.legact3), (ImageView) findViewById(R.id.legact3), new boolean[]{false});
        ImageButton backButton = findViewById(R.id.back_button);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedpose.clear();
                finish();
            }
        });
    }

    private void setClickListener(ImageButton button, ImageView image, final boolean[] isChecked) {
        // Initially setting the image as empty

        image.setImageDrawable(null);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked[0] = !isChecked[0]; // Toggle the state // 위에 세팅 된거 보면 무조건 false임 그래서 true로 만들어준 듯

                if (isChecked[0]) {
                    image.setImageResource(R.drawable.clickicon); // Set the check image
                    selectedpose.add(getResources().getResourceEntryName(button.getId()));
                    prefs.edit().clear();
                    prefs.edit().putStringSet("pose",selectedpose).apply();
                    Log.d("자세 리스트", String.valueOf(selectedpose));
                    Log.d("선택된 자세", String.valueOf(prefs.getStringSet("pose", new LinkedHashSet<>())));
                } else {
                    image.setImageDrawable(null); // Remove the image
                    selectedpose.remove(getResources().getResourceEntryName(button.getId()));

                    prefs.edit().clear();
                    prefs.edit().putStringSet("pose", selectedpose).apply();
                    Log.d("자세 리스트", String.valueOf(selectedpose));
                    Log.d("선택된 자세", String.valueOf(prefs.getStringSet("pose", new LinkedHashSet<>())));
                }
            }
        });
    }

    public void onClick(View v) {
        // Check which button was clicked
        if (v.getId() == R.id.neck_button) {
            showLayout(list1);
        } else if (v.getId() == R.id.waist_button) {
            showLayout(list2);
        } else if (v.getId() == R.id.hip_joint_button) {
            showLayout(list3);
        } else if (v.getId() == R.id.leg_button) {
            showLayout(list4);
        }
    }

    // Function to show the selected layout and hide the others
    private void showLayout(LinearLayout layoutToShow) {
        list1.setVisibility(View.GONE);
        list2.setVisibility(View.GONE);
        list3.setVisibility(View.GONE);
        list4.setVisibility(View.GONE);

        layoutToShow.setVisibility(View.VISIBLE);
    }
    private interface PoseinfoCallback{
        void onCallback(String PoseName);
    }
}