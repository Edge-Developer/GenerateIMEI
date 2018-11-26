package com.edgedevstudio.generate_imei;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageActivity extends AppCompatActivity {

    public static final String LANG_NAME_KEY = "lang.name.key";
    public static final String LANG_SHORTCODE_KEY = "lang.shortcode.key";
    public static final String LANG_COUNTRY_CODE_KEY = "lang.country.code.key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        RecyclerView recyclerView = findViewById(R.id.lang_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        LanguageAdapter adapter = new LanguageAdapter();
        recyclerView.setAdapter(adapter);
        Locale locale = Locale.getDefault();
        getSupportActionBar().setSubtitle(locale.getDisplayLanguage());

        List<LanguageModel> langauges = new ArrayList<>();
        langauges.add(getLanguageModel("sq"));
        langauges.add(getLanguageModel("am"));
        langauges.add(getLanguageModel("ar"));
        langauges.add(getLanguageModel("az"));
        langauges.add(getLanguageModel("be"));
        langauges.add(getLanguageModel("bn"));
        langauges.add(getLanguageModel("bs"));
        langauges.add(getLanguageModel("bg"));
        langauges.add(getLanguageModel("my"));
        langauges.add(getLanguageModel("zh", "CN"));
        langauges.add(getLanguageModel("zh", "TW"));
        langauges.add(getLanguageModel("hr"));
        langauges.add(getLanguageModel("cs"));
        langauges.add(getLanguageModel("da"));
        langauges.add(getLanguageModel("nl"));
        langauges.add(getLanguageModel("en"));
        langauges.add(getLanguageModel("et"));
        langauges.add(getLanguageModel("fil"));
        langauges.add(getLanguageModel("fi"));
        langauges.add(getLanguageModel("fr"));
        langauges.add(getLanguageModel("ka"));
        langauges.add(getLanguageModel("de"));
        langauges.add(getLanguageModel("el"));
        langauges.add(getLanguageModel("iw"));
        langauges.add(getLanguageModel("hi"));
        langauges.add(getLanguageModel("hu"));
        langauges.add(getLanguageModel("is"));
        langauges.add(getLanguageModel("in"));
        langauges.add(getLanguageModel("it"));
        langauges.add(getLanguageModel("ja"));
        langauges.add(getLanguageModel("kk"));
        langauges.add(getLanguageModel("km"));
        langauges.add(getLanguageModel("ko"));
        langauges.add(getLanguageModel("lt"));
        langauges.add(getLanguageModel("lb"));
        langauges.add(getLanguageModel("mk"));
        langauges.add(getLanguageModel("mg"));
        langauges.add(getLanguageModel("ms"));
        langauges.add(getLanguageModel("mn"));
        langauges.add(getLanguageModel("mn"));
        langauges.add(getLanguageModel("no"));
        langauges.add(getLanguageModel("pl"));
        langauges.add(getLanguageModel("pt"));
        langauges.add(getLanguageModel("ro"));
        langauges.add(getLanguageModel("ru"));
        langauges.add(getLanguageModel("sk"));
        langauges.add(getLanguageModel("sl"));
        langauges.add(getLanguageModel("es"));
        langauges.add(getLanguageModel("sv"));
        langauges.add(getLanguageModel("th"));
        langauges.add(getLanguageModel("tr"));
        langauges.add(getLanguageModel("uk"));
        langauges.add(getLanguageModel("ur"));
        langauges.add(getLanguageModel("vi"));
        langauges.add(getLanguageModel("cy"));

        adapter.setLangauges(langauges);
    }

    private LanguageModel getLanguageModel(String langShortCode) {
        Locale locale = new Locale(langShortCode);
        return new LanguageModel(locale.getDisplayName(), langShortCode, "");
    }

    private LanguageModel getLanguageModel(String langShortCode, String country) {
        Locale locale = new Locale(langShortCode, country);
        return new LanguageModel(locale.getDisplayName(), langShortCode, country);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String subject = "";
        switch (item.getItemId()) {
            case R.id.wants_to_translate:
                subject = "I want to Translate this App to my Language for IMEI Generator LITE\"";
                break;
            case R.id.mistake_report:
                subject = "I want to Translate Correct some Errors in Translation for IMEI Generator LITE";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", getString(R.string.dev_email), null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.hello));
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (Exception e) {
            //Implement Crashlytics
        }
        return true;
    }

    private static class LanguageHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public LanguageHolder(View itemView, final OnClickModel model) {
            super(itemView);
            mTextView = (TextView) itemView;
            mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.clickedLanguage(getAdapterPosition());
                }
            });
        }

        public interface OnClickModel {
            void clickedLanguage(int index);
        }
    }

    private class LanguageAdapter extends RecyclerView.Adapter<LanguageHolder> implements LanguageHolder.OnClickModel {
        private List<LanguageModel> mLangauges = new ArrayList<>();

        public void setLangauges(List<LanguageModel> langauges) {
            mLangauges = langauges;
            notifyDataSetChanged();
        }

        @Override
        public LanguageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.language_layout, parent, false);
            return new LanguageHolder(view, this);
        }

        @Override
        public void onBindViewHolder(LanguageHolder holder, int position) {
            holder.mTextView.setText(mLangauges.get(position).getLanguageName());
        }

        @Override
        public int getItemCount() {
            return mLangauges.size();
        }


        @Override
        public void clickedLanguage(int index) {
            LanguageModel model = mLangauges.get(index);
            Intent intent = new Intent();
            intent.putExtra(LANG_NAME_KEY, model.getLanguageName());
            intent.putExtra(LANG_SHORTCODE_KEY, model.getLangShortCode());
            intent.putExtra(LANG_COUNTRY_CODE_KEY, model.getLangCountryCode());
            //setResult(Companion.getCHANGE_LANG_RC(), intent);
            finish();
        }
    }
}
