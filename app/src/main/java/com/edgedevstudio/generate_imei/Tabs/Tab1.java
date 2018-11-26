package com.edgedevstudio.generate_imei.Tabs;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class Tab1 extends Fragment {

    private static final String TAG = "Tab1";
    private final String TAB_ONE_LIST = "tab.one.list";
    private final String TAB1_QUANTITY_SPINNER_INDEX_KEY = "tab1.index";
    private final String TAB1_PHONE_SPINNER_INDEX_KEY = "tab1.phone.index";
    private Spinner quantitySpinner;
    private ProcessNumbers theNumbersBB;
    private int spinnerPosition, phoneSpinnerPosition;
    private String dataFromArray = null;
    private String qty2Generate;
    private TinyDB mTab_1_ListPref, qty2GenSpinnerIndex, phoneSpinnerIndex;
    private RecyclerViewAdapter recyclerViewAdapter;
    private Spinner phoneSpinner;
    private Callback mCallback;
    private ProgressBar mProgressBar;
    private Button generateButton;
    private RecyclerView mRecyclerView;
    private int indexOfNumOfImeiToGenerate;
    private int indexOfPhoneSelected;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        theNumbersBB = new ProcessNumbers();
        mTab_1_ListPref = new TinyDB(context);
        qty2GenSpinnerIndex = new TinyDB(context);
        phoneSpinnerIndex = new TinyDB(context);
        spinnerPosition = qty2GenSpinnerIndex.getInt(TAB1_QUANTITY_SPINNER_INDEX_KEY);
        phoneSpinnerPosition = phoneSpinnerIndex.getInt(TAB1_PHONE_SPINNER_INDEX_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab1, container, false);
        phoneSpinner = view.findViewById(R.id.phoneSpinner);
        quantitySpinner = view.findViewById(R.id.spinnerTab1);
        mProgressBar = view.findViewById(R.id.progress_bar_tab1);
        generateButton = view.findViewById(R.id.generateButtonTab1);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, Singleton.getInstance().getPhoneImeiList());
        quantitySpinner.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, Singleton.getInstance().getQtyToGenerateValues()));
        phoneSpinner.setAdapter(spinnerAdapter);

        phoneSpinner.setSelection(phoneSpinnerPosition);
        quantitySpinner.setSelection(spinnerPosition);

        mRecyclerView = view.findViewById(R.id.recyclerViewTab1);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerViewAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(recyclerViewAdapter);


        if (mTab_1_ListPref.getListString(TAB_ONE_LIST).size() != 0) {
            List<String> list = new ArrayList<>();
            String data_stored = "" + mTab_1_ListPref.getListString(TAB_ONE_LIST);
            String step1 = "" + data_stored.replace("]", "");
            String step2 = "" + step1.replace("[", "");
            String step3 = "" + step2.replace(",", "");
            String[] step4 = step3.split(" ");

            Collections.addAll(list, step4);
            recyclerViewAdapter.setIMEIList(list);
            String imeiData = step3.replace(" ", "\n");
            qty2Generate = "" + quantitySpinner.getSelectedItem();
            dataFromArray = list.size() + " " + getString(R.string.generated_imei) + "\n\n" + imeiData + getString(R.string.these_imeis_were);
        }

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallback.clickedGenerate();
                if (dataFromArray != null) dataFromArray = null;

                String imeiNumber = Singleton.getInstance().getPhoneIMEI(phoneSpinner.getSelectedItem().toString());
                indexOfNumOfImeiToGenerate = quantitySpinner.getSelectedItemPosition();
                indexOfPhoneSelected = phoneSpinner.getSelectedItemPosition();
                qty2Generate = quantitySpinner.getSelectedItem().toString();
                new AsyncGenerator().execute(imeiNumber, qty2Generate);
            }
        });
        return view;
    }

    public String copyAll() {
        if (dataFromArray == null)
            return null;
        if (recyclerViewAdapter.mIMEIList.size() < 5000)
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
        recyclerViewAdapter.mIMEIList.clear();
        recyclerViewAdapter.notifyDataSetChanged();
        mTab_1_ListPref.putListString(TAB_ONE_LIST, new ArrayList<String>());
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

        void vibrateCallback();
    }

    private final static class IMEI_Holder extends RecyclerView.ViewHolder {
        private TextView imei_txt_view;
        private ImageButton copy_btn;

        public IMEI_Holder(View view, final OnButtonClickedListener listener) {
            super(view);
            imei_txt_view = view.findViewById(R.id.imei_text_view);
            copy_btn = view.findViewById(R.id.imei_copy_btn);
            copy_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCopyBtnClicked(getAdapterPosition());
                }
            });
        }

        private interface OnButtonClickedListener {
            void onCopyBtnClicked(int position);
        }
    }

    private class AsyncGenerator extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            generateButton.setEnabled(false);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected List<String> doInBackground(String... params) {

            List<String> list = new ArrayList<>();
            //code
            return list;
        }

        @Override
        protected void onPostExecute(List<String> s) {
            super.onPostExecute(s);
            //if (!isAdded()) return;
            try {
                generateButton.setEnabled(true);
            }
            catch (Exception e){
                // Implement Crashlytics
                return;
            }
            mProgressBar.setVisibility(View.GONE);
            if (s == null) return;
            Log.d(TAG, "onPostExecute: Size of List = " + s.size());
            recyclerViewAdapter.setIMEIList(s);
            mRecyclerView.setVisibility(View.VISIBLE);
            mCallback.vibrateCallback();
            if (s.size() < 5000) {
                mTab_1_ListPref.putListString(TAB_ONE_LIST, s);
                qty2GenSpinnerIndex.putInt(TAB1_QUANTITY_SPINNER_INDEX_KEY, indexOfNumOfImeiToGenerate);
                phoneSpinnerIndex.putInt(TAB1_PHONE_SPINNER_INDEX_KEY, indexOfPhoneSelected);
            }
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<IMEI_Holder> implements IMEI_Holder.OnButtonClickedListener {
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
            notifyDataSetChanged();
        }

        @Override
        public void onCopyBtnClicked(int position) {
            if (mIMEIList != null) mCallback.clickedCopyBtn(mIMEIList.get(position));
        }
    }

    private class AsyncSaver extends AsyncTask<Void, Void, Boolean> {
        private String filepath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            filepath = quantitySpinner.getSelectedItem().toString().replace(",", "") + " " + phoneSpinner.getSelectedItem().toString() + " " + getString(R.string.generate_imei_txt);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // code
                return false;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            if (isAdded()) mProgressBar.setVisibility(View.GONE);
            if (isSuccess) {
                mCallback.vibrateCallback();
                mCallback.onSaveFinished("\"" + filepath + "\" " + getString(R.string.saved_in_sd_card));
            } else
                mCallback.onSaveFinished(getString(R.string.cross));

        }
    }
}