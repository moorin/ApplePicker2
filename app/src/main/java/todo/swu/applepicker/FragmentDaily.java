package todo.swu.applepicker;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FragmentDaily extends Fragment {
    ImageButton iButton_calendar;
    TextView tv_date;
    /*
    EditText edit_dDay;
    EditText edit_comment;
    EditText edit_total_time;
    EditText edit_subject;
    EditText edit_part;
     */
    EditText edit_memo;

    //ImageButton iButton_task_add;

    ImageButton iButton_memo_add;

    FirebaseFirestore db;
    Map<String, Object> dailyMap;
    static String currentDate;

    RecyclerView memoRecyclerView;
    //데베 종속 버전
    ArrayList<MemoItem> memoItemList;
    //임시
    //ArrayList<String> memoItemList;
    //ListView memolistview;
    //TaskAdapter taskAdapter;
    MemoAdapter memoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.fragment_daily, container, false);

        //이 버튼을 클릭하면 달력이 호출됨.
        iButton_calendar = (ImageButton) myView.findViewById(R.id.iButton_calendar);
        edit_memo = (EditText) myView.findViewById(R.id.edit_memo);
        tv_date = (TextView) myView.findViewById(R.id.tv_date);

        iButton_memo_add = (ImageButton) myView.findViewById(R.id.iButton_memo_add);

        //Access a Firestore
        db = FirebaseFirestore.getInstance();

//        db.collection("isDataPassing")
//                .document("isDataPassing") //선택한 날짜에 해당하는 데이터 유무 확인
//                .get()
//                .addOnSuccessListener(snapShotData -> {
//                    if (snapShotData.exists()) {//선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.
//                        //Log.e("선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.", dateToday);
//
//                        String flag = (String) snapShotData.getData().get("flag");
//                        Log.e("flag 값: ", flag);
//
//                        if(flag.equals("True")) {
//
//
//                            Log.e("데일리 프레그먼트", "if문 안");
//                        }
//                    }
//                }).addOnFailureListener(e -> e.printStackTrace());


        //Task RecyclerView에 표시할 데이터 리스트 생성.
        /*
        taskItemList = new ArrayList<TaskItem>();
        //RecyclerView에 LinearLayoutManager 객체 지정, 어댑터 연결
        taskRecyclerView = (RecyclerView) myView.findViewById(R.id.recyclerView_task);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        taskAdapter = new TaskAdapter(taskItemList);
        taskRecyclerView.setAdapter(taskAdapter);
        */

        //Memo RecyclerView에 표시할 데이터 리스트 생성.
        memoItemList = new ArrayList<MemoItem>();
        //RecyclerView에 LinearLayoutManager 객체 지정, 어댑터 연결.
        RecyclerView memoRecyclerView = (RecyclerView) myView.findViewById(R.id.recyclerView_memo);
        memoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        memoAdapter = new MemoAdapter(memoItemList);
        memoRecyclerView.setAdapter(memoAdapter);

        //오늘날짜에 해당하는 데이터 불러와 화면에 보여줌.
        initFragment();

        iButton_calendar.setOnClickListener(v -> {
            DialogFragment dateFragment = new DatePickerFragment();
            dateFragment.show(getActivity().getSupportFragmentManager(), "dateFragment");
        });

        /*
        edit_dDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String field = "dDay";
                updateDB(field, edit_dDay);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                edit_dDay.setTextColor(getActivity().getResources().getColor(R.color.green_text));
                edit_dDay.setBackground(null);
            }
        });

         */

        /*
        edit_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String field = "comment";
                updateDB(field, edit_comment);
            }
            @Override
            public void afterTextChanged(Editable editable) {
                edit_comment.setTextColor(getActivity().getResources().getColor(R.color.green_text));
                edit_comment.setBackground(null);
            }
        });

         */

        /*
        edit_total_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String field = "totalTime";
                updateDB(field, edit_total_time);
            }
            @SuppressLint("LongLogTag")
            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("edit_total_time의 afterTextChanged 호출됨.", currentDate);
                edit_total_time.setTextColor(getActivity().getResources().getColor(R.color.green_text));
                edit_total_time.setBackground(null);
            }
        });

         */

        /*
        edit_subject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                edit_subject.setBackground(null);
            }
        });

         */

        /*
        edit_part.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                edit_part.setBackground(null);
            }
        });

         */

/*
        iButton_task_add.setOnClickListener(v -> {
            Timestamp timestamp = Timestamp.now();
            taskItemList.add(new TaskItem(timestamp, edit_subject.getText().toString(),
                edit_part.getText().toString(), false));

            addTaskDB(timestamp);
            EventChangeListener();
            Log.e(TAG, "DB에 Task 저장됨 => ");

            edit_subject.setText(null);
            edit_part.setText(null);
        });

 */

        iButton_memo_add.setOnClickListener(v -> {
            // 리사이클러뷰에 SimpleTextAdapter 객체 지정.

            Timestamp timestamp = Timestamp.now();
            memoItemList.add(new MemoItem(edit_memo.getText().toString()));

            //addMemoDB(timestamp);
            //EventChangeListener();
            memoAdapter.notifyDataSetChanged();
            /*
            memoAdapter = new MemoAdapter(memoItemList);
            memoRecyclerView.setAdapter(memoAdapter);

             */
            Log.e(TAG, "DB에 Task 저장됨 => ");

            edit_memo.setText(null);

        });

        // Inflate the layout for this fragment
        return myView;
    } //onCreateView End.


    //오늘날짜에 해당하는 데이터 불러와 화면에 보여줌.
    @SuppressLint("LongLogTag")
    public void initFragment() {
        //오늘 날짜 얻고 date 표시함.
        Log.e("이게 2번 실행되나", "환장");
        tv_date.setText(getCurrentDate());
        Date now = new Date();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        currentDate = dateToday;
        Log.e(" onCreateView 안의 currentDate 변수", currentDate);

        //Create field
        dailyMap = new HashMap<>();
        dailyMap.put("date", dateToday);


        //오늘 날짜에 저장된 데이터 불러와서 EditText에 setText()하기
        db.collection("daily")
            .document(dateToday)//선택한 날짜에 해당하는 데이터 유무 확인
            .get()
            .addOnSuccessListener(snapShotData -> {
                if (snapShotData.exists()) {//선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.
                    Log.e("선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.", dateToday);

                    String memoItem = (String) snapShotData.getData().get("memoItem");
                    
                    // 메모 에디터 텍스트
                    edit_memo.setText(dateToday);

                } else {//선택한 날짜에 해당하는 데이터가 없는 경우
                    //새로 만들어서 DB에 추가함
                    Log.e("선택한 날짜에 해당하는 데이터가 없는 경우", dateToday);
                    db.collection("daily").document(dateToday)
                        .set(dailyMap)
                        .addOnSuccessListener(documentReference -> {
                            Log.e(TAG, "DocumentSnapshot added with ID: ");
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding document", e);
                        });
                }
            }).addOnFailureListener(e -> e.printStackTrace());

        /*
        //오늘 날짜에 저장된 데이터 불러와 RecyclerView 2개에 setText()하기
        Query query = db.collection("daily").document(dateToday)
            .collection("task").orderBy("createdAt");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        TaskItem obj = doc.toObject(TaskItem.class);
                        taskItemList.add(obj);
                    }
                    taskAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Recyclerview 초기화 else 부분");
                }
            }
        });

         */
    }



    @SuppressLint("LongLogTag")
    public void processDatePickerResult(String year, String month, String day, String day_of_week, String datePicked) {
        tv_date.setText(month + "/" + day + "(" + day_of_week + ")");
        currentDate = datePicked;

        //Create field
        dailyMap = new HashMap<>();
        dailyMap.put("date", datePicked);

        //Edittext 3개 표시
        db.collection("daily")
            .document(datePicked) //선택한 날짜에 해당하는 데이터 유무 확인
            .get()
            .addOnSuccessListener(snapShotData -> {
                if (snapShotData.exists()) {//선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.
                    Log.e("선택한 날짜에 저장된 데이터가 있는 경우 해당 data 갖고와서 화면에 뿌려줌.", currentDate);
                    String dDay = snapShotData.getString("dDay");
                    // String comment = snapShotData.getString("comment");
                    // String totalTime = snapShotData.getString("totalTime");
                    //

                    //edit_dDay.setText(dDay);
                    //edit_comment.setText(comment);
                    //edit_total_time.setText(totalTime)

                } else {//선택한 날짜에 해당하는 데이터가 없는 경우
                    //새로 만들어서 DB에 데이터 추가함
                    Log.e("선택한 날짜에 해당하는 데이터가 없는 경우", currentDate);
                    db.collection("daily").document(datePicked)
                        .set(dailyMap)
                        .addOnSuccessListener(documentReference -> {
                            Log.e(TAG, "DocumentSnapshot added with ID: ");
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding document", e);
                        });
                }
            }).addOnFailureListener(e -> e.printStackTrace());

        /*
        //Task
        taskItemList = new ArrayList<TaskItem>();
        db.collection("daily").document(datePicked)
            .collection("task").orderBy("createdAt")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore Error", error.getMessage());
                    }
                    for (DocumentSnapshot dc : value.getDocuments()) {
                        TaskItem obj = dc.toObject(TaskItem.class);
                        taskItemList.add(obj);
                    }
                    //taskAdapter.notifyDataSetChanged();
                    taskAdapter = new TaskAdapter(taskItemList);
                    taskRecyclerView.setAdapter(taskAdapter);
                }
            });

         */

    }

    //EditText 3개에 변경사항 생길때마다 DB 업데이트함.
    @SuppressLint("LongLogTag")
    public void updateDB(String field, EditText editText) {
        //field, editText로 3개 필드 구분해서 업데이트.
        db.collection("daily").document(currentDate)
            .update(field, editText.getText().toString())
            .addOnSuccessListener(documentReference -> {
                Log.e(TAG, "DocumentSnapshot updated with ID: ");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error updating document", e);
            });
    }

    /*
    //RecyclerView Task에 입력&변경사항 생길때마다 DB 업데이트함.
    @SuppressLint("LongLogTag")
    public void addTaskDB(Timestamp timestamp) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("createdAt", timestamp);
        /*
        taskMap.put("subject", edit_subject.getText().toString());
        taskMap.put("part", edit_part.getText().toString());
        taskMap.put("achievement", false);

        */
/*
        //task 업데이트.
        db.collection("daily").document(currentDate)
            .collection("task")
            .document(timestamp.toString()).set(taskMap)
            .addOnSuccessListener(documentReference -> {
                Log.e(TAG, "DocumentSnapshot added with ID: ");
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error adding document", e);
            });
    }

 */


    //오늘 날짜 얻기.
    public String getCurrentDate() {
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int dayNum = now.get(Calendar.DAY_OF_WEEK);
        //요일 구함
        String day_of_week = "";
        switch (dayNum) {
            case 1:
                day_of_week = "일";
                break;
            case 2:
                day_of_week = "월";
                break;
            case 3:
                day_of_week = "화";
                break;
            case 4:
                day_of_week = "수";
                break;
            case 5:
                day_of_week = "목";
                break;
            case 6:
                day_of_week = "금";
                break;
            case 7:
                day_of_week = "토";
                break;
        }
        return month + "/" + day + "(" + day_of_week + ")";
    }

/*
    public void EventChangeListener() {
        taskItemList = new ArrayList<TaskItem>();
        db.collection("daily").document(currentDate)
            .collection("task").orderBy("createdAt")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore Error", error.getMessage());
                    }
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            TaskItem obj = dc.getDocument().toObject(TaskItem.class);
                            taskItemList.add(obj);
                        }
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            });
    }

 */


}