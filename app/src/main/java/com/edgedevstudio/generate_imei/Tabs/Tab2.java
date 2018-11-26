package com.edgedevstudio.generate_imei.Tabs;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.edgedevstudio.generate_imei.ProcessNumbers;
import com.edgedevstudio.generate_imei.R;
import com.edgedevstudio.generate_imei.Singleton;
import com.edgedevstudio.generate_imei.TinyDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;


public class Tab2 extends Fragment {

    private static final String TAG = "Tab2";
    private final String TAB_TWO_LIST = "tab.two.list";
    private final String TAB2_EDIT_TEXT_KEY = "tab2.edit.text.key";
    private final String TAB2_SPINNER_INDEX_KEY = "tab2.spinner.index.key";
    int indexOfNumOfImeiToGenerate;
    private Spinner qty2GenSpinner_Tab2;
    private TextInputEditText editTextTab2;
    private TextInputLayout editTextTextInputLayout;
    private Button generateButton;
    private String finalIMEI, ourEditTextString, qtyToGenString;
    private ProcessNumbers theNumbersBulk;
    private String dataFromArray = null;
    private TinyDB sharedPrefsStoreIMEI_List, sharedPrefs_Edit_Text_string, spinnerIndex;
    private int qtyToGenPosition;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private Callback mCallback;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        theNumbersBulk = new ProcessNumbers();
        sharedPrefsStoreIMEI_List = new TinyDB(getContext());
        sharedPrefs_Edit_Text_string = new TinyDB(getContext());
        spinnerIndex = new TinyDB(getContext());
        qtyToGenPosition = spinnerIndex.getInt(TAB2_SPINNER_INDEX_KEY);
        ourEditTextString = sharedPrefs_Edit_Text_string.getString(TAB2_EDIT_TEXT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tab2, container, false);

        mProgressBar = view.findViewById(R.id.progress_bar_tab2);
        mRecyclerView = view.findViewById(R.id.recyclerViewTab2);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mRecyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        qty2GenSpinner_Tab2 = view.findViewById(R.id.spinnerTab2);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, Singleton.getInstance().getQtyToGenerateValues());
        qty2GenSpinner_Tab2.setAdapter(spinnerAdapter);
        qty2GenSpinner_Tab2.setSelection(qtyToGenPosition);

        editTextTab2 = view.findViewById(R.id.editTextTab2);
        editTextTextInputLayout = view.findViewById(R.id.editTextTextInputLayoutTab2);
        editTextTab2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editTextTextInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        generateButton = view.findViewById(R.id.generateButtonTab2);

        if (sharedPrefsStoreIMEI_List.getListString(TAB_TWO_LIST).size() != 0) {
            List<String> list = new ArrayList<>();
            String data_stored = "" + sharedPrefsStoreIMEI_List.getListString(TAB_TWO_LIST);
            String step1 = "" + data_stored.replace("]", "");
            String step2 = "" + step1.replace("[", "");
            String step3 = "" + step2.replace(",", "");
            String[] step4 = step3.split(" ");

            Collections.addAll(list, step4);
            mRecyclerViewAdapter.setIMEIList(list);

            String imeiData = step3.replace(" ", "\n");
            qtyToGenString = "" + qty2GenSpinner_Tab2.getItemAtPosition(qtyToGenPosition);
            dataFromArray = list.size() + " " + getString(R.string.generated_imei) + "\n\n" + imeiData + getString(R.string.these_imeis_were);
        }

        editTextTab2.setText(ourEditTextString);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.clickedGenerate();
                ourEditTextString = editTextTab2.getText().toString();
                if (ourEditTextString.length() < 8) {
                    editTextTab2.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
                    editTextTextInputLayout.setError(getString(R.string.btw_8_12_digits));
                    mCallback.vibrate();
                } else {
                    if (dataFromArray != null) dataFromArray = null;
                    indexOfNumOfImeiToGenerate = qty2GenSpinner_Tab2.getSelectedItemPosition();
                    qtyToGenString = qty2GenSpinner_Tab2.getSelectedItem().toString();
                    new AsyncGenerator().execute(ourEditTextString, qtyToGenString);
                }
            }
        });
    }


    public String copyAll() {
        if (dataFromArray == null)
            return null;
        if (mRecyclerViewAdapter.mIMEIList.size() < 5000)
            return dataFromArray;
        return "a";
    }

    public boolean save() {
        if (dataFromArray != null) {
            new AsyncSaver().execute();
            return true;
        }
        return false;
    }


    public void clearAll() {
        mRecyclerViewAdapter.mIMEIList.clear();
        mRecyclerViewAdapter.notifyDataSetChanged();
        sharedPrefsStoreIMEI_List.putListString(TAB_TWO_LIST, new ArrayList<String>());
        dataFromArray = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    private DecimalFormat formatIt() {
        return new DecimalFormat("#,###");
    }

    public interface Callback {
        void clickedCopyBtn(String imeiCopied);

        void clickedGenerate();

        void onSaveFinished(String message);

        void vibrate();
    }

    private static class IMEI_Holder extends RecyclerView.ViewHolder {
        private TextView imei_txt_view;
        private ImageButton copy_btn;

        public IMEI_Holder(View view, final OnBtnClickLister listener) {
            super(view);
            imei_txt_view = view.findViewById(R.id.imei_text_view);
            copy_btn = view.findViewById(R.id.imei_copy_btn);
            copy_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCopyButtonClick(getAdapterPosition());
                }
            });
        }

        private interface OnBtnClickLister {
            void onCopyButtonClick(int position);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<IMEI_Holder> implements IMEI_Holder.OnBtnClickLister {
        private List<String> mIMEIList = new ArrayList<>();

        @Override
        public IMEI_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.single_imei, parent, false);
            return new IMEI_Holder(view, this);
        }


        @Override
        public void onBindViewHolder(IMEI_Holder holder, int position) {
            holder.imei_txt_view.setText(mIMEIList.get(position));
        }

        @Override
        public int getItemCount() {
            return mIMEIList.size();
        }

        public void setIMEIList(List<String> IMEIList) {
            mIMEIList = IMEIList;
            mRecyclerView.getRecycledViewPool().clear();
            notifyDataSetChanged();
        }

        @Override
        public void onCopyButtonClick(int position) {
            if (mIMEIList != null) mCallback.clickedCopyBtn(mIMEIList.get(position));
        }
    }

    private class AsyncGenerator extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            generateButton.setEnabled(false);
            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> list = new ArrayList<>();
            //
            return list;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);
            //if (!isAdded()) return;
            try {
                generateButton.setEnabled(true);
            } catch (Exception e) {
                // Implement Crashlytics
                return;
            }
            mProgressBar.setVisibility(View.GONE);
            if (s == null) return;
            Log.d(TAG, "onPostExecute: Size of List = " + s.size());
            mRecyclerViewAdapter.setIMEIList(s);
            mRecyclerView.setVisibility(View.VISIBLE);
            mCallback.vibrate();
            if (s.size() < 5000) {
                sharedPrefsStoreIMEI_List.putListString(TAB_TWO_LIST, s);
                spinnerIndex.putInt(TAB2_SPINNER_INDEX_KEY, indexOfNumOfImeiToGenerate);
                sharedPrefs_Edit_Text_string.putString(TAB2_EDIT_TEXT_KEY, ourEditTextString);
            }
        }
    }

    private class AsyncSaver extends AsyncTask<Void, Void, Boolean> {
        private String filepath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            filepath = qty2GenSpinner_Tab2.getSelectedItem().toString().replace(",", "") + " " + getString(R.string.imei_gen) + " " + getString(R.string.generate_imei_txt);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
           //code
                return false;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (isAdded()) mProgressBar.setVisibility(View.GONE);
            if (isSuccess) {
                mCallback.vibrate();
                mCallback.onSaveFinished("\"" + filepath + "\" " + getString(R.string.saved_in_sd_card));
            } else
                mCallback.onSaveFinished(getString(R.string.cross));
        }
    }

}