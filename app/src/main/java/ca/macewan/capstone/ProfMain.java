package ca.macewan.capstone;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ca.macewan.capstone.adapter.ViewPagerAdapter;

public class ProfMain extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_prof);
//
//        TabLayout tabLayout = findViewById(R.id.tl_main);
//        ViewPager2 viewPager2 = findViewById(R.id.vp2_main);
//
//        ViewPagerAdapter adapter = new ViewPagerAdapter(this, email);
//        viewPager2.setAdapter(adapter);
//
//
//        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
//            @Override
//            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
//                switch (position) {
//                    case 0:
//                        tab.setText("All");
//                        break;
//                    case 1:
//                        tab.setText("Invited");
//                        break;
//                }
//            }
//        }).attach();
//    }
}
