package com.ebadalrahman.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuranActivity extends Activity {

    private Spinner reciterSpinner;
    private Spinner surahSpinner;
    private Spinner fromSpinner;
    private Spinner toSpinner;

    private Spinner speedSpinner;
    private Spinner ayahRepeatSpinner;
    private Spinner rangeRepeatSpinner;
    private Spinner waitSpinner;

    private Switch basmalaSwitch;

    private TextView currentStatus;
    private TextView progressText;

    private Button playButton;
    private Button stopButton;
    private Button downloadButton;

    private MediaPlayer mediaPlayer;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private int currentSurah = 1;
    private int startAyah = 1;
    private int endAyah = 1;
    private int currentAyah = 1;

    private int currentAyahRepeat = 0;
    private int currentRangeRepeat = 0;

    private boolean playing = false;
    private boolean playingBasmala = false;

    private boolean stoppedByUser = false;

    private double currentSpeed = 1.0;
    private double waitMultiplier = 0.0;

    private int ayahRepeatCount = 1;
    private int rangeRepeatCount = 1;

    private double lastAyahDuration = 0;

    private String selectedReciter = "إبراهيم";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildInterface();
    }

    // =========================================================
    // الواجهة
    // =========================================================

    private void buildInterface() {

        ScrollView scrollView = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25, 30, 25, 40);

        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(7, 25, 43),
                                Color.rgb(12, 53, 65),
                                Color.rgb(18, 80, 67)
                        }
                );

        root.setBackground(background);

        TextView title = new TextView(this);
        title.setText("📖 قسم التحفيظ");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "اختر إعدادات الحفظ ثم ابدأ رحلتك"
        );
        subtitle.setTextColor(Color.rgb(210, 235, 225));
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 25);

        root.addView(subtitle);

        // -----------------------------------------------------
        // الشيخ
        // -----------------------------------------------------

        root.addView(sectionTitle("الشيخ"));

        reciterSpinner = createSpinner(
                new String[]{
                        "إبراهيم",
                        "زياد",
                        "أسامة"
                }
        );

        root.addView(reciterSpinner);

        // -----------------------------------------------------
        // السورة
        // -----------------------------------------------------

        root.addView(sectionTitle("السورة"));

        surahSpinner = createSpinner(
                QuranData.SURAHS
        );

        root.addView(surahSpinner);

        // -----------------------------------------------------
        // من آية إلى آية
        // -----------------------------------------------------

        LinearLayout rangeRow =
                new LinearLayout(this);

        rangeRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        rangeRow.setGravity(Gravity.CENTER);

        LinearLayout fromBox =
                createHalfBox("من آية");

        fromSpinner =
                createNumberSpinner(1);

        fromBox.addView(fromSpinner);

        LinearLayout toBox =
                createHalfBox("إلى آية");

        toSpinner =
                createNumberSpinner(1);

        toBox.addView(toSpinner);

        rangeRow.addView(
                fromBox,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        rangeRow.addView(
                toBox,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        root.addView(rangeRow);

        // -----------------------------------------------------
        // السرعة
        // -----------------------------------------------------

        root.addView(sectionTitle("سرعة التلاوة"));

        speedSpinner = createSpinner(
                new String[]{
                        "0.75x",
                        "1x",
                        "1.25x",
                        "1.5x",
                        "1.75x",
                        "2x"
                }
        );

        speedSpinner.setSelection(1);

        root.addView(speedSpinner);

        // -----------------------------------------------------
        // تكرار الآية
        // -----------------------------------------------------

        root.addView(sectionTitle("تكرار كل آية"));

        ayahRepeatSpinner = createSpinner(
                new String[]{
                        "مرة واحدة",
                        "مرتان",
                        "3 مرات",
                        "5 مرات",
                        "10 مرات",
                        "∞ بلا توقف"
                }
        );

        root.addView(ayahRepeatSpinner);

        // -----------------------------------------------------
        // تكرار المقطع
        // -----------------------------------------------------

        root.addView(sectionTitle("تكرار المقطع كاملًا"));

        rangeRepeatSpinner = createSpinner(
                new String[]{
                        "مرة واحدة",
                        "مرتان",
                        "3 مرات",
                        "5 مرات",
                        "10 مرات",
                        "∞ بلا توقف"
                }
        );

        root.addView(rangeRepeatSpinner);

        // -----------------------------------------------------
        // الانتظار
        // -----------------------------------------------------

        root.addView(sectionTitle(
                "الانتظار بعد كل آية"
        ));

        waitSpinner = createSpinner(
                new String[]{
                        "بدون انتظار",
                        "نصف مدة الآية",
                        "مدة الآية كاملة",
                        "1.5 × مدة الآية",
                        "ضعف مدة الآية"
                }
        );

        root.addView(waitSpinner);

        // -----------------------------------------------------
        // البسملة
        // -----------------------------------------------------

        basmalaSwitch = new Switch(this);

        basmalaSwitch.setText(
                "تشغيل البسملة قبل بداية السورة"
        );

        basmalaSwitch.setTextColor(Color.WHITE);
        basmalaSwitch.setTextSize(17);
        basmalaSwitch.setPadding(10, 20, 10, 20);

        root.addView(basmalaSwitch);

        // -----------------------------------------------------
        // الحالة
        // -----------------------------------------------------

        LinearLayout statusCard =
                createCard();

        currentStatus =
                new TextView(this);

        currentStatus.setText(
                "جاهز لبدء الحفظ"
        );

        currentStatus.setTextColor(Color.WHITE);
        currentStatus.setTextSize(19);
        currentStatus.setGravity(Gravity.CENTER);

        progressText =
                new TextView(this);

        progressText.setText(
                "لم يبدأ التشغيل بعد"
        );

        progressText.setTextColor(
                Color.rgb(200, 230, 220)
        );

        progressText.setTextSize(15);
        progressText.setGravity(Gravity.CENTER);

        statusCard.addView(currentStatus);
        statusCard.addView(progressText);

        root.addView(statusCard);

        // -----------------------------------------------------
        // الأزرار
        // -----------------------------------------------------

        playButton =
                createButton(
                        "▶ تشغيل مباشر",
                        Color.rgb(40, 145, 90)
                );

        stopButton =
                createButton(
                        "■ إيقاف",
                        Color.rgb(145, 55, 55)
                );

        downloadButton =
                createButton(
                        "↓ تنزيل المقطع",
                        Color.rgb(35, 100, 135)
                );

        root.addView(playButton);

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout.LayoutParams half =
                new LinearLayout.LayoutParams(
                        0,
                        125,
                        1
                );

        half.setMargins(5, 15, 5, 0);

        buttons.addView(
                stopButton,
                half
        );

        buttons.addView(
                downloadButton,
                half
        );

        root.addView(buttons);

        // -----------------------------------------------------
        // الأحداث
        // -----------------------------------------------------

        setupListeners();

        updateAyahSpinners(0);

        scrollView.addView(root);

        setContentView(scrollView);
    }

    // =========================================================
    // الأحداث
    // =========================================================

    private void setupListeners() {

        surahSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        currentSurah =
                                position + 1;

                        updateAyahSpinners(position);
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        reciterSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        selectedReciter =
                                position == 0
                                        ? "إبراهيم"
                                        : position == 1
                                        ? "زياد"
                                        : "أسامة";
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );

        speedSpinner.setOnItemSelectedListener(
                new SimpleSelection() {

                    @Override
                    public void selected(
                            int position
                    ) {

                        double[] values = {
                                0.75,
                                1.0,
                                1.25,
                                1.5,
                                1.75,
                                2.0
                        };

                        currentSpeed =
                                values[position];

                        if (mediaPlayer != null) {
                            applySpeed();
                        }
                    }
                }
        );

        ayahRepeatSpinner.setOnItemSelectedListener(
                new SimpleSelection() {

                    @Override
                    public void selected(
                            int position
                    ) {

                        int[] values = {
                                1,
                                2,
                                3,
                                5,
                                10,
                                -1
                        };

                        ayahRepeatCount =
                                values[position];
                    }
                }
        );

        rangeRepeatSpinner.setOnItemSelectedListener(
                new SimpleSelection() {

                    @Override
                    public void selected(
                            int position
                    ) {

                        int[] values = {
                                1,
                                2,
                                3,
                                5,
                                10,
                                -1
                        };

                        rangeRepeatCount =
                                values[position];
                    }
                }
        );

        waitSpinner.setOnItemSelectedListener(
                new SimpleSelection() {

                    @Override
                    public void selected(
                            int position
                    ) {

                        double[] values = {
                                0.0,
                                0.5,
                                1.0,
                                1.5,
                                2.0
                        };

                        waitMultiplier =
                                values[position];
                    }
                }
        );

        playButton.setOnClickListener(
                v -> startPlayback()
        );

        stopButton.setOnClickListener(
                v -> stopPlayback()
        );

        downloadButton.setOnClickListener(
                v -> downloadRange()
        );
    }

    // =========================================================
    // تشغيل
    // =========================================================

    private void startPlayback() {

        stopPlayback();

        startAyah =
                fromSpinner.getSelectedItemPosition() + 1;

        endAyah =
                toSpinner.getSelectedItemPosition() + 1;

        if (startAyah > endAyah) {

            Toast.makeText(
                    this,
                    "الآية الأولى يجب أن تكون قبل الآية الأخيرة",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        currentAyah = startAyah;

        currentAyahRepeat = 0;
        currentRangeRepeat = 0;

        stoppedByUser = false;

        playing = true;

        if (
                basmalaSwitch.isChecked()
                        &&
                QuranData.hasBasmala(currentSurah)
        ) {

            playingBasmala = true;

            playBasmala();

        } else {

            playingBasmala = false;

            playCurrentAyah();
        }
    }

    // =========================================================
    // البسملة
    // =========================================================

    private void playBasmala() {

        File local =
                AudioConfig.getLocalBasmalaFile(
                        this,
                        selectedReciter
                );

        if (local.exists()) {

            playFile(local);

        } else {

            playUrl(
                    AudioConfig.getBasmalaUrl(
                            selectedReciter
                    )
            );
        }
    }

    // =========================================================
    // تشغيل الآية الحالية
    // =========================================================

    private void playCurrentAyah() {

        if (stoppedByUser) {
            return;
        }

        if (currentAyah > endAyah) {

            finishCurrentRange();

            return;
        }

        currentStatus.setText(
                "الآية " + currentAyah
        );

        progressText.setText(
                "سورة "
                        + QuranData.SURAHS[currentSurah - 1]
                        + "  •  "
                        + currentAyah
                        + " / "
                        + endAyah
        );

        File local =
                AudioConfig.getLocalAyahFile(
                        this,
                        selectedReciter,
                        currentSurah,
                        currentAyah
                );

        if (local.exists()) {

            playFile(local);

        } else {

            playUrl(
                    AudioConfig.getAyahUrl(
                            selectedReciter,
                            currentSurah,
                            currentAyah
                    )
            );
        }
    }

    // =========================================================
    // تشغيل ملف محلي
    // =========================================================

    private void playFile(File file) {

        try {

            releasePlayer();

            mediaPlayer =
                    new MediaPlayer();

            mediaPlayer.setDataSource(
                    file.getAbsolutePath()
            );

            preparePlayer();

        } catch (Exception e) {

            showError(
                    "تعذر تشغيل الملف المحلي"
            );
        }
    }

    // =========================================================
    // تشغيل مباشر
    // =========================================================

    private void playUrl(String url) {

        try {

            releasePlayer();

            mediaPlayer =
                    new MediaPlayer();

            mediaPlayer.setDataSource(
                    this,
                    Uri.parse(url)
            );

            preparePlayer();

        } catch (Exception e) {

            showError(
                    "تعذر فتح مصدر الصوت"
            );
        }
    }

    // =========================================================
    // تجهيز MediaPlayer
    // =========================================================

    private void preparePlayer() {

        mediaPlayer.setOnPreparedListener(
                mp -> {

                    applySpeed();

                    int duration =
                            mp.getDuration();

                    lastAyahDuration =
                            duration / currentSpeed;

                    mp.start();

                    playing = true;

                    if (playingBasmala) {

                        currentStatus.setText(
                                "البسملة"
                        );

                    } else {

                        currentStatus.setText(
                                "▶ الآية "
                                        + currentAyah
                        );
                    }
                }
        );

        mediaPlayer.setOnCompletionListener(
                mp -> {

                    if (playingBasmala) {

                        playingBasmala = false;

                        scheduleNext(
                                0
                        );

                        return;
                    }

                    handleAyahCompletion();
                }
        );

        mediaPlayer.setOnErrorListener(
                (mp, what, extra) -> {

                    showError(
                            "تعذر تشغيل التسجيل"
                    );

                    return true;
                }
        );

        mediaPlayer.prepareAsync();
    }

    // =========================================================
    // نهاية الآية
    // =========================================================

    private void handleAyahCompletion() {

        currentAyahRepeat++;

        boolean repeatSameAyah;

        if (ayahRepeatCount == -1) {

            repeatSameAyah = true;

        } else {

            repeatSameAyah =
                    currentAyahRepeat
                            < ayahRepeatCount;
        }

        if (repeatSameAyah) {

            scheduleNext(
                    calculateWait()
            );

            return;
        }

        currentAyahRepeat = 0;

        if (currentAyah < endAyah) {

            currentAyah++;

            scheduleNext(
                    calculateWait()
            );

        } else {

            finishCurrentRange();
        }
    }

    // =========================================================
    // نهاية المقطع
    // =========================================================

    private void finishCurrentRange() {

        currentRangeRepeat++;

        boolean repeatRange;

        if (rangeRepeatCount == -1) {

            repeatRange = true;

        } else {

            repeatRange =
                    currentRangeRepeat
                            < rangeRepeatCount;
        }

        if (repeatRange) {

            currentAyah = startAyah;
            currentAyahRepeat = 0;

            scheduleNext(
                    calculateWait()
            );

        } else {

            playing = false;

            currentStatus.setText(
                    "✓ انتهى المقطع"
            );

            progressText.setText(
                    "أحسنت، انتهى نطاق الحفظ"
            );
        }
    }

    // =========================================================
    // حساب الانتظار من مدة الآية
    // =========================================================

    private long calculateWait() {

        if (waitMultiplier <= 0) {
            return 0;
        }

        return (long)
                (lastAyahDuration
                        * waitMultiplier);
    }

    // =========================================================
    // الانتظار
    // =========================================================

    private void scheduleNext(long milliseconds) {

        if (stoppedByUser) {
            return;
        }

        if (milliseconds <= 0) {

            playCurrentAyah();

            return;
        }

        currentStatus.setText(
                "انتظار..."
        );

        handler.postDelayed(
                () -> {

                    if (!stoppedByUser) {
                        playCurrentAyah();
                    }

                },
                milliseconds
        );
    }

    // =========================================================
    // السرعة
    // =========================================================

    private void applySpeed() {

        if (mediaPlayer == null) {
            return;
        }

        try {

            if (
                    android.os.Build.VERSION.SDK_INT
                            >= 23
            ) {

                PlaybackParams params =
                        mediaPlayer
                                .getPlaybackParams();

                params.setSpeed(
                        (float) currentSpeed
                );

                mediaPlayer.setPlaybackParams(
                        params
                );
            }

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // إيقاف
    // =========================================================

    private void stopPlayback() {

        stoppedByUser = true;

        playing = false;
        playingBasmala = false;

        handler.removeCallbacksAndMessages(
                null
        );

        releasePlayer();

        currentStatus.setText(
                "متوقف"
        );

        progressText.setText(
                "يمكنك بدء التشغيل من جديد"
        );
    }

    private void releasePlayer() {

        if (mediaPlayer != null) {

            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }

            try {
                mediaPlayer.release();
            } catch (Exception ignored) {
            }

            mediaPlayer = null;
        }
    }

    // =========================================================
    // تنزيل المقطع
    // =========================================================

    private void downloadRange() {

        final int surah =
                surahSpinner.getSelectedItemPosition() + 1;

        final int from =
                fromSpinner.getSelectedItemPosition() + 1;

        final int to =
                toSpinner.getSelectedItemPosition() + 1;

        if (from > to) {

            Toast.makeText(
                    this,
                    "النطاق غير صحيح",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        downloadButton.setEnabled(false);

        currentStatus.setText(
                "جاري تنزيل المقطع..."
        );

        executor.execute(() -> {

            int total =
                    to - from + 1;

            int completed = 0;

            try {

                for (
                        int ayah = from;
                        ayah <= to;
                        ayah++
                ) {

                    File target =
                            AudioConfig.getLocalAyahFile(
                                    this,
                                    selectedReciter,
                                    surah,
                                    ayah
                            );

                    if (!target.exists()) {

                        downloadFile(
                                AudioConfig.getAyahUrl(
                                        selectedReciter,
                                        surah,
                                        ayah
                                ),
                                target
                        );
                    }

                    completed++;

                    int finalCompleted =
                            completed;

                    runOnUiThread(() ->
                            progressText.setText(
                                    "تم تنزيل "
                                            + finalCompleted
                                            + " / "
                                            + total
                                            + " آية"
                            )
                    );
                }

                runOnUiThread(() -> {

                    downloadButton.setEnabled(
                            true
                    );

                    currentStatus.setText(
                            "✓ اكتمل التنزيل"
                    );

                    progressText.setText(
                            "يمكن تشغيل هذا المقطع بدون إنترنت"
                    );

                    Toast.makeText(
                            this,
                            "تم حفظ التسجيلات على الجهاز",
                            Toast.LENGTH_LONG
                    ).show();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    downloadButton.setEnabled(
                            true
                    );

                    currentStatus.setText(
                            "فشل التنزيل"
                    );

                    Toast.makeText(
                            this,
                            "تعذر تنزيل التسجيل",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void downloadFile(
            String urlString,
            File target
    ) throws Exception {

        URL url =
                new URL(urlString);

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setConnectTimeout(
                15000
        );

        connection.setReadTimeout(
                30000
        );

        connection.connect();

        if (
                connection.getResponseCode()
                        < 200
                        ||
                connection.getResponseCode()
                        >= 300
        ) {

            throw new Exception(
                    "HTTP "
                            + connection.getResponseCode()
            );
        }

        InputStream input =
                connection.getInputStream();

        FileOutputStream output =
                new FileOutputStream(target);

        byte[] buffer =
                new byte[8192];

        int length;

        while (
                (length =
                        input.read(buffer))
                        != -1
        ) {

            output.write(
                    buffer,
                    0,
                    length
            );
        }

        output.flush();

        output.close();
        input.close();

        connection.disconnect();
    }

    // =========================================================
    // تحديث الآيات
    // =========================================================

    private void updateAyahSpinners(
            int surahIndex
    ) {

        int count =
                QuranData.getAyahCount(
                        surahIndex
                );

        String[] numbers =
                new String[count];

        for (int i = 0; i < count; i++) {

            numbers[i] =
                    String.valueOf(i + 1);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        numbers
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        fromSpinner.setSelection(0);
        toSpinner.setSelection(
                count - 1
        );
    }

    // =========================================================
    // عناصر الواجهة
    // =========================================================

    private TextView sectionTitle(
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(18);
        view.setTypeface(
                null,
                Typeface.BOLD
        );

        view.setPadding(
                8,
                22,
                8,
                8
        );

        return view;
    }

    private Spinner createSpinner(
            String[] items
    ) {

        Spinner spinner =
                new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        items
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);

        return spinner;
    }

    private Spinner createNumberSpinner(
            int max
    ) {

        String[] values =
                new String[max];

        for (int i = 0; i < max; i++) {
            values[i] =
                    String.valueOf(i + 1);
        }

        return createSpinner(values);
    }

    private LinearLayout createHalfBox(
            String title
    ) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                8,
                5,
                8,
                5
        );

        TextView label =
                sectionTitle(title);

        box.addView(label);

        return box;
    }

    private LinearLayout createCard() {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setPadding(
                20,
                20,
                20,
                20
        );

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(
                Color.argb(
                        80,
                        255,
                        255,
                        255
                )
        );

        drawable.setCornerRadius(30);

        drawable.setStroke(
                2,
                Color.argb(
                        100,
                        255,
                        255,
                        255
                )
        );

        card.setBackground(drawable);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                25,
                0,
                20
        );

        card.setLayoutParams(params);

        return card;
    }

    private Button createButton(
            String text,
            int color
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(18);
        button.setTypeface(
                null,
                Typeface.BOLD
        );

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(30);

        button.setBackground(drawable);

        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        130
                );

        params.setMargins(
                0,
                15,
                0,
                0
        );

        button.setLayoutParams(params);

        return button;
    }

    // =========================================================
    // مساعد Spinner
    // =========================================================

    private abstract class SimpleSelection
            implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(
                AdapterView<?> parent,
                View view,
                int position,
                long id
        ) {

            selected(position);
        }

        @Override
        public void onNothingSelected(
                AdapterView<?> parent
        ) {
        }

        public abstract void selected(
                int position
        );
    }

    // =========================================================
    // الخطأ
    // =========================================================

    private void showError(
            String message
    ) {

        runOnUiThread(() -> {

            playing = false;

            currentStatus.setText(
                    "تعذر التشغيل"
            );

            progressText.setText(
                    message
            );

            Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
            ).show();
        });
    }

    @Override
    protected void onDestroy() {

        stopPlayback();

        executor.shutdownNow();

        super.onDestroy();
    }
}
