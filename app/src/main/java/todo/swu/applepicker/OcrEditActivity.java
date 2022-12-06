package todo.swu.applepicker;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.tasks.Task;
import com.google.common.primitives.UnsignedInteger;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

// 파일 입출력 관련


public class OcrEditActivity extends AppCompatActivity {
    //Access a Firestore
    FirebaseFirestore db;

    private int WRITE_REQUEST_CODE = 43;
    private ParcelFileDescriptor pfd;
    private FileOutputStream fileOutputStream;
    private String resultData;
    private String jsonResponse;

    Map<String, Object> dateTodayMap;
    String currentDate;
    private FirebaseFirestore test;
    private int lastIndex;

    public void setLastIndex(int lastIdx)
    {
        this.lastIndex = lastIdx;
    }

    public int getLastIndex()
    {
        return this.lastIndex;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_edit);
        TextView saved_memo = (TextView)findViewById(R.id.saved_memo);
        saved_memo.setText("");
        db = FirebaseFirestore.getInstance();

        Intent receive_intent = getIntent();

        jsonResponse = receive_intent.getStringExtra("jsonResponse");
        String imagePath = receive_intent.getStringExtra("imagePath");
        List<String> memoList = new ArrayList<String>();

        Log.e("test", "json 파싱 실행 전");


        // json 데이터 구조 확인
        //StartRecord();

        // OCR 글자들 모음 List
        memoList = jsonParsing(jsonResponse);
        resultData = memoList.toString();

        // 대괄호 없애기
        resultData = removeChar(resultData, 0);
        resultData = removeChar(resultData, resultData.length()-1);

        // 쉼표 없애기
//        resultData= resultData.replace(",", "");


        Log.e("test", "json 파싱 실행 후");
        //Log.e(memoList.toString(), "memoList");
        //Log.e(imagePath.toString(), "imagePath");

        //StartRecord();

        Map<String, Object> memoMap = new HashMap<>();

        Date now = new Date();
        String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        currentDate = dateToday;


        //Create field
        dateTodayMap = new HashMap<>();
        dateTodayMap.put("date", dateToday);

        List<String> finalMemoList = memoList;


        //이미 있던 데이터 몇개인지 세고 업데이트
        DocumentReference docRef = db.collection("daily/" + currentDate + "/memoItem").document("size");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // size 문서가 있는 경우 size+=size
                        Log.e(TAG, "DocumentSnapshot data: " + document.getData());

                        String memoSizeStr = document.getData().toString();
                        // 중괄호 없애기
                        memoSizeStr = memoSizeStr.replace("{", "");
                        memoSizeStr = memoSizeStr.replace("}", "");
                        memoSizeStr = memoSizeStr.replace("size=", "");

                        // memoSizeStr -> 원래 DB에 있던 size
                        // finalMemoList.size() ocr받은 리스트 size

                        memoMap.clear();
                        memoMap.put("size", Integer.valueOf(memoSizeStr) + finalMemoList.size());
                        memoMap.put("newImageSize", finalMemoList.size());
                        db.collection("/daily/" + dateToday + "/memoItem")
                                .document("size")
                                .set(memoMap);
                        memoMap.clear();

                        //memoList
                        // 10이 010으로 저장되고 있음

                        Log.e("finalMemoList.size(): ", Integer.toString(finalMemoList.size()));
                        Log.e("memoSizeStr: ", memoSizeStr);


                        for (int i = 0; i < finalMemoList.size(); i++) {
                            Log.e("새로 선택한 사진의 size", Integer.toString(finalMemoList.size()));
                            Log.e("i + memoSizeStr +1:  ", Integer.toString(i + Integer.valueOf(memoSizeStr) +1));

                            if (i + Integer.valueOf(memoSizeStr) +1 <= 9) {
                                memoMap.put("memo", finalMemoList.get(i));
                                db.collection("/daily/" + dateToday + "/memoItem")
                                        .document("0" + Integer.toString(i + Integer.valueOf(memoSizeStr) + 1))
                                        .set(memoMap);
                                memoMap.clear();
                                Log.e("first", "if");
                            } else if(i + Integer.valueOf(memoSizeStr) +1 == 9) {
                                memoMap.put("memo", finalMemoList.get(i));
                                db.collection("/daily/" + dateToday + "/memoItem")
                                        .document(Integer.toString(10))
                                        .set(memoMap);
                                memoMap.clear();
                                Log.e("second", "if");
                            } else {
                                memoMap.put("memo", finalMemoList.get(i));
                                db.collection("/daily/" + dateToday + "/memoItem")
                                        .document(Integer.toString(i + Integer.valueOf(memoSizeStr) + 1))
                                        .set(memoMap);
                                memoMap.clear();
                                Log.e("second", "if");
                            }

                        }

                } else {
                        Log.e(TAG, "No such document");
                        // DB에 size라는 문서가 없을 경우 새로 추가
                        memoMap.clear();
                        memoMap.put("size", finalMemoList.size());
                        db.collection("/daily/" + dateToday + "/memoItem")
                                .document("size")
                                .set(memoMap);
                        memoMap.clear();

                        //memoList
                        for (int i = 0; i < finalMemoList.size(); i++) {
                            Log.e("새로 선택한 사진의 size", Integer.toString(finalMemoList.size()));

                            if (i <= 9) {
                                memoMap.put("memo", finalMemoList.get(i));
                                db.collection("/daily/" + dateToday + "/memoItem")
                                        .document("0" + Integer.toString(i + lastIndex + 1))
                                        .set(memoMap);
                                memoMap.clear();
                                Log.e("first", "if");
                            } else {
                                memoMap.put("memo", finalMemoList.get(i));
                                db.collection("/daily/" + dateToday + "/memoItem")
                                        .document(Integer.toString(i + lastIndex + 1))
                                        .set(memoMap);
                                memoMap.clear();
                                Log.e("second", "if");
                            }
                        }
                }
            } else

            {
                Log.d(TAG, "get failed with ", task.getException());
            }
        }
    }).addOnFailureListener(e -> e.printStackTrace());
        saved_memo.setText("메모 저장 완료!");
    }




    public String removeChar(String str, int n) {
        String front = str.substring(0, n);
        String back = str.substring(n+1, str.length());
        return front + back;
    }

//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    public void StartRecord(){
//        try {

//            long now = System.currentTimeMillis();
//            Date date = new Date(now);
//            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdfNow
//                    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String formatDate = sdfNow.format(date);
//
//            /**
//             * SAF 파일 편집
//             * */
//            String fileName = formatDate+".txt";
//
//            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("text/plain" );
//            intent.putExtra(Intent.EXTRA_TITLE,fileName);
//
//            startActivityForResult(intent, WRITE_REQUEST_CODE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Uri uri = data.getData();
//            try {
//                addText(uri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void addText(Uri uri) throws IOException {
//        try {
//            String inAddTextStr = "TEST";
//
//            pfd = this.getContentResolver().openFileDescriptor(uri, "w");
//            fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
//            putString(jsonResponse);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void writeFile(String fileName, String msg) {
//        try {
//            OutputStreamWriter oStreamWriter = new OutputStreamWriter(openFileOutput(fileName,
//                    Context.MODE_PRIVATE));
//            oStreamWriter.write(msg);
//            oStreamWriter.close();
//        } catch(FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 이 메서드를 통해 기록
//     */
//    public void putString(String st) throws IOException {
//        if(fileOutputStream!=null) fileOutputStream.write(st.getBytes());
//    }
//
//    public void FinishRecord() throws IOException {
//        Toast.makeText(getApplicationContext(), "내용 기록완료.", Toast.LENGTH_LONG).show();
//        fileOutputStream.close();
//        pfd.close();
//
//    }

    private List jsonParsing(String json)
    {
        List<String> memoList = new ArrayList<String>();
        Log.e("in jsonParsing Test", json);
        try{
            JSONObject imagesJsonObject = new JSONObject(json);
            JSONArray imagesArray = imagesJsonObject.getJSONArray("images");

            String imagesJson = imagesArray.toString();
            imagesJson = removeChar(imagesJson, 0);
            int len = imagesJson.length()-1;
            imagesJson = removeChar(imagesJson, len);

            JSONObject fieldsJsonObject = new JSONObject(imagesJson);
            JSONArray fieldsArray = fieldsJsonObject.getJSONArray("fields");

            String memo = "";

            for(int i=0; i<fieldsArray.length(); i++)
            {
                JSONObject fieldsObject = fieldsArray.getJSONObject(i);
                String inferText = fieldsObject.getString("inferText");
                String lineBreak = fieldsObject.getString("lineBreak");
                //Log.e(inferText, "inferText");
                //Log.e(lineBreak, "lineBreak");

                if(lineBreak == "true")
                {
                    memo+=inferText;
                    memoList.add(memo);
                    memo="";
                }
                else
                {
                    memo+=inferText;
                    memo+=" ";
                    //Log.e(memo, "memo");
                }


            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return memoList;
    }


}