package todo.swu.applepicker;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.FirestoreClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class FragmentDaily extends Fragment {
    ImageButton iButton_calendar;
    TextView tv_date;
    EditText edit_memo;
    Button refreshBtn;
    ImageButton iButton_memo_add;

    FirebaseFirestore db;
    Map<String, Object> dailyMap;

    static String currentDate;

    RecyclerView memoRecyclerView;

    ArrayList<MemoItem> memoItemList;

    MemoAdapter memoAdapter;


    private int lastIndex;
    private int newSize;

    public void setLastIndexSize(int lastIdx)
    {
        this.lastIndex = lastIdx;
        this.newSize = newSize;
    }
    public void setNewImageSize(int newSize)
    {
        this.lastIndex = lastIndex;
        this.newSize = newSize;
    }

    public int getLastIndex()
    {
        return this.lastIndex;
    }
    public int getNewImageSize()
    {
        return this.newSize;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_daily, container, false);

        //??? ????????? ???????????? ????????? ?????????.
        iButton_calendar = (ImageButton) myView.findViewById(R.id.iButton_calendar);
        edit_memo = (EditText) myView.findViewById(R.id.edit_memo);
        tv_date = (TextView) myView.findViewById(R.id.tv_date);

        refreshBtn = (Button) myView.findViewById(R.id.refreshBtn);

        iButton_memo_add = (ImageButton) myView.findViewById(R.id.iButton_memo_add);

        //Access a Firestore
        db = FirebaseFirestore.getInstance();

        //Memo RecyclerView??? ????????? ????????? ????????? ??????.
        memoItemList = new ArrayList<MemoItem>();

        //RecyclerView??? LinearLayoutManager ?????? ??????, ????????? ??????.
        RecyclerView memoRecyclerView = (RecyclerView) myView.findViewById(R.id.recyclerView_memo);
        memoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        memoAdapter = new MemoAdapter(memoItemList);
        memoRecyclerView.setAdapter(memoAdapter);

        //??????????????? ???????????? ????????? ????????? ????????? ?????????.
        initFragment();

        //??????????????? ???????????? OCR ?????? ????????? ????????? ?????????.
        db.collection("daily/"+currentDate+"/memoItem")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                String resultStr = "";
                                Log.e(TAG, document.getId() + " => " + document.getData());
                                // get data ??????
                                edit_memo.setText(null);

                                resultStr = document.getData().toString();
                                resultStr = resultStr.replace("{memo=","");
                                resultStr = resultStr.replace("}","");
                                //Log.e("resultStr[0]", resultStr.indexOf());

                                memoItemList.add(new MemoItem(resultStr));


                            }
                            if(!memoItemList.isEmpty())
                                memoItemList.remove(memoItemList.size()-1);
                            memoAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });

        iButton_calendar.setOnClickListener(v -> {
            DialogFragment dateFragment = new DatePickerFragment();
            dateFragment.show(getActivity().getSupportFragmentManager(), "dateFragment");


        });

        refreshBtn.setOnClickListener(v -> {
            for(int i=0;i<memoItemList.size();i++)
                memoItemList.removeAll(memoItemList);

            db.collection("daily/"+currentDate+"/memoItem")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                    String resultStr = "";
                                    Log.e(TAG, document.getId() + " => " + document.getData());
                                    // get data ??????
                                    edit_memo.setText(null);

                                    resultStr = document.getData().toString();
                                    resultStr = resultStr.replace("{memo=","");
                                    resultStr = resultStr.replace("}","");
                                    //Log.e("resultStr[0]", resultStr.indexOf());

                                    memoItemList.add(new MemoItem(resultStr));


                                }
                                if(!memoItemList.isEmpty())
                                    memoItemList.remove(memoItemList.size()-1);
                                memoAdapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }
                    });
        });
//
//            // ???????????? ????????????
//            db.collection("daily/" + currentDate + "/memoItem")
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isSuccessful()) {
//                                int newImageSize = getNewImageSize();
//                                int lastIndex = getLastIndex();
//
//                                int i = 0;
//                                int flag = 0;
//
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    String resultStr = "";
//                                    Log.e(TAG, document.getId() + " => " + document.getData());
//                                    // get data ??????
//                                    edit_memo.setText(null);
//
//                                    resultStr = document.getData().toString();
//                                    resultStr = resultStr.replace("{memo=", "");
//                                    resultStr = resultStr.replace("}", "");
//                                    Log.e("i", Integer.toString(i));
//                                    Log.e("lastIndex-newImageSize", Integer.toString(lastIndex-newImageSize));
//                                    if (lastIndex -  newImageSize< i) {
//                                        memoItemList.add(new MemoItem(resultStr));
//                                        flag = 1;
//                                    }
//
//                                    i++;
//                                }
//                                if (flag == 1) {
//                                    memoItemList.remove(memoItemList.size() - 1);
//                                    memoAdapter.notifyDataSetChanged();
//                                }
//
//                            } else {
//                                Log.d(TAG, "Error getting documents: ", task.getException());
//                            }
//
//                        }
//                    });
//
//        });

        DatePickerDialog.OnDateSetListener dateSetListener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yy, int mm, int dd) {
                        Log.e("date picker ??????: ", String.format("%d-%d-%d", yy,mm+1,dd));
                    }
                };

        edit_memo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                edit_memo.setBackground(null);
            }
        });

        iButton_memo_add.setOnClickListener(v -> {

            memoItemList.add(new MemoItem(""));
            memoAdapter.notifyDataSetChanged();

            edit_memo.setText(null);
        });

        return myView;

    } //onCreateView End.

    public String removeChar(String str, Integer n) {
        String front = str.substring(0, n);
        String back = str.substring(n+1, str.length());
        return front + back;
    }

    //??????????????? ???????????? ????????? ????????? ????????? ?????????.
    @SuppressLint("LongLogTag")
    public void initFragment() {
        //?????? ?????? ?????? date ?????????.

        tv_date.setText(getCurrentDate());
        Date now = new Date();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        currentDate = dateToday;
        Log.e(" onCreateView ?????? currentDate ??????", currentDate);

        //Create field
        dailyMap = new HashMap<>();
        dailyMap.put("date", dateToday);


        db.collection("daily")
                .document(dateToday)//????????? ????????? ???????????? ????????? ?????? ??????
                .get()
                .addOnSuccessListener(snapShotData -> {

                    Log.e("????????? ????????? ???????????? ???????????? ?????? ??????", dateToday);
                    db.collection("daily").document(dateToday)
                            .set(dailyMap)
                            .addOnSuccessListener(documentReference -> {
                                Log.e(TAG, "DocumentSnapshot added with ID: ");
                            }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding document", e);
                    });

                }).addOnFailureListener(e -> e.printStackTrace());
    }




    @SuppressLint("LongLogTag")
    public void processDatePickerResult(String year, String month, String day, String day_of_week, String datePicked) {
        tv_date.setText(month + "/" + day + "(" + day_of_week + ")");
        currentDate = datePicked;

        //Create field
        dailyMap = new HashMap<>();
        dailyMap.put("date", datePicked);

        Log.e("daily date ??????", datePicked);
        
        // ?????? ?????? ??????????????? ??? ????????? ?????? ?????? ?????? ????????????

        memoItemList.removeAll(memoItemList);
        memoAdapter.notifyDataSetChanged();

        db.collection("daily")
                .document(currentDate)//????????? ????????? ???????????? ????????? ?????? ??????
                .get()
                .addOnSuccessListener(snapShotData -> {

                    //Log.e("????????? ????????? ???????????? ???????????? ?????? ??????", currentDate);
                    db.collection("daily").document(currentDate)
                            .set(dailyMap)
                            .addOnSuccessListener(documentReference -> {
                                Log.e(TAG, "DocumentSnapshot added with ID: ");
                            }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding document", e);
                    });

                }).addOnFailureListener(e -> e.printStackTrace());

        //??????????????? ???????????? OCR ?????? ????????? ????????? ?????????.
        db.collection("daily/"+currentDate+"/memoItem")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                String resultStr = "";
                                Log.e(TAG, document.getId() + " => " + document.getData());
                                // get data ??????
                                edit_memo.setText(null);

                                resultStr = document.getData().toString();
                                resultStr = resultStr.replace("{memo=","");
                                resultStr = resultStr.replace("}","");
                                Log.e("resultStr", resultStr);

                                memoItemList.add(new MemoItem(resultStr));
                                memoItemList.remove(memoItemList.size()-1);
                                memoAdapter.notifyDataSetChanged();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
        
    }


    //?????? ?????? ??????.
    public String getCurrentDate() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int dayNum = now.get(Calendar.DAY_OF_WEEK);
        //?????? ??????
        String day_of_week = "";
        switch (dayNum) {
            case 1:
                day_of_week = "???";
                break;
            case 2:
                day_of_week = "???";
                break;
            case 3:
                day_of_week = "???";
                break;
            case 4:
                day_of_week = "???";
                break;
            case 5:
                day_of_week = "???";
                break;
            case 6:
                day_of_week = "???";
                break;
            case 7:
                day_of_week = "???";
                break;
        }
        return month + "/" + day + "(" + day_of_week + ")";
    }
}
