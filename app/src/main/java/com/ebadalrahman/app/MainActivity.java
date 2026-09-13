package com.ebadalrahman.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(35, 50, 35, 50);

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 25, 45),
                        Color.rgb(15, 55, 75),
                        Color.rgb(20, 80, 75)
                }
        );
        root.setBackground(background);

        TextView title = new TextView(this);
        title.setText("عباد الرحمن");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("منصة القرآن والتحفيظ");
        subtitle.setTextColor(Color.rgb(220, 240, 235));
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 10, 0, 45);

        root.addView(title);
        root.addView(subtitle);

        TextView websiteButton = createButton(
                "🕌  موقع المسابقة",
                Color.rgb(30, 110, 120)
        );

        TextView quranButton = createButton(
                "📖  قسم التحفيظ",
                Color.rgb(45, 125, 80)
        );

        root.addView(
                websiteButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        150
                )
        );

        LinearLayout.LayoutParams quranParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        150
                );

        quranParams.topMargin = 25;

        root.addView(quranButton, quranParams);

        websiteButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    WebsiteActivity.class
            );
            startActivity(intent);
        });

        quranButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    QuranActivity.class
            );
            startActivity(intent);
        });

        setContentView(root);
    }

    private TextView createButton(String text, int color) {

        TextView button = new TextView(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(22);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(null, android.graphics.Typeface.BOLD);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(35);
        drawable.setStroke(2, Color.argb(100, 255, 255, 255));

        button.setBackground(drawable);
        button.setElevation(10);

        return button;
    }
} 
