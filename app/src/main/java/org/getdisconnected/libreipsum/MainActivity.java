package org.getdisconnected.libreipsum;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.shawnlin.numberpicker.NumberPicker;
import com.thedeanda.lorem.LoremIpsum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;

import java.lang.reflect.TypeVariable;

public class MainActivity extends AppCompatActivity {

    private NumberPicker numberPicker;
    private MenuItem shareMenuItem;
    private MenuItem copyMenuItem;
    private TextView textView;

    private MenuItem currentMenuScope;

    private final Handler updateHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberPicker = findViewById(R.id.number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateText();
            }
        });

        textView = findViewById(R.id.the_text);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                updateScope(menuItem);
                return true;
            }
        });
        navView.setSelectedItemId(R.id.navigation_paragraphs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        copyMenuItem = menu.findItem(R.id.copy);
        shareMenuItem = menu.findItem(R.id.action_share);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String label = getLoremLabel();
        switch (item.getItemId()) {
            case R.id.copy:
                Toast.makeText(this, String.format(getString(R.string.copy_message), label), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(label, textView.getText());
                clipboard.setPrimaryClip(clip);
                return true;
            case R.id.action_share:
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                share.putExtra(Intent.EXTRA_SUBJECT, label);
                share.putExtra(Intent.EXTRA_TEXT, textView.getText().toString());
                startActivity(Intent.createChooser(share, String.format(getString(R.string.share_message), label)));
                return true;
            case R.id.action_about:
                String msg = getString(R.string.about_message);
                String versionInfo = BuildConfig.VERSION_NAME;
                if (!"release".equals(BuildConfig.BUILD_TYPE)) {
                    versionInfo += " (" + BuildConfig.BUILD_TYPE + ")";
                }
                msg = msg.replace("{versionInfo}", versionInfo);
                Spanned spannedMsg = Build.VERSION.SDK_INT >= 24 ? Html.fromHtml(msg, Html.FROM_HTML_MODE_COMPACT) : Html.fromHtml(msg);
                TextView tvAboutContent = new TextView(this);
                int dp25 = Math.round(25f*(getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
                int dp10 = Math.round(10f*(getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
                tvAboutContent.setPadding(dp25, dp10, dp25, dp10);
                tvAboutContent.setText(spannedMsg);
                tvAboutContent.setMovementMethod(LinkMovementMethod.getInstance());
                new AlertDialog.Builder(this).setTitle(R.string.about_title).setView(tvAboutContent).setNegativeButton(R.string.about_close, new AlertDialog.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    }).create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getLoremLabel() {
        int n = numberPicker.getValue();

        String unit = (String) currentMenuScope.getTitle();

        if ( n == 1 ) {
            switch (currentMenuScope.getItemId()) {
                case R.id.navigation_paragraphs:
                    unit = getString(R.string.title_paragraph);
                    break;
                case R.id.navigation_sentences:
                    unit = getString(R.string.title_sentence);
                    break;
                case R.id.navigation_words:
                    unit = getString(R.string.title_word);
                    break;
            }
        }

        return String.format(getString(R.string.label), n, unit);
    }

    private void updateScope(final MenuItem newScopeItem) {
        currentMenuScope = newScopeItem;
        updateText();
    }

    private void updateText() {
        updateHandler.removeCallbacksAndMessages(null);
        textView.animate().alpha(0F).setDuration(100).start();
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (currentMenuScope.getItemId()) {
                    case R.id.navigation_words:
                        numberPicker.setMaxValue(5000);
                        textView.setText(LoremIpsum.getInstance().getWords(numberPicker.getValue()));
                        textView.animate().alpha(1F).setDuration(300).start();
                        return;
                    case R.id.navigation_sentences:
                        numberPicker.setMaxValue(500);
                        int i = numberPicker.getValue();
                        StringBuilder sb = new StringBuilder();
                        while (i > 0) {
                            sb.append(LoremIpsum.getInstance().getWords(4, 15));
                            sb.append(". ");
                            i--;
                        }
                        textView.setText(sb.toString());
                        textView.animate().alpha(1F).setDuration(300).start();
                        return;
                    case R.id.navigation_paragraphs:
                        numberPicker.setMaxValue(100);
                        textView.setText(LoremIpsum.getInstance().getParagraphs(numberPicker.getValue(), numberPicker.getValue()).replaceAll("\n", "\n\n"));
                        textView.animate().alpha(1F).setDuration(300).start();
                        return;
                }
            }
        }, 200);
    }
}
