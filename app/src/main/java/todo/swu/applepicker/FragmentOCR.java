package todo.swu.applepicker;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;


public class FragmentOCR extends Fragment {
    Button galleryBtn;
    Button cameraBtn;
    ImageView ocrImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_ocr, container, false);
        galleryBtn = (Button) myView.findViewById(R.id.galleryBtn);
        cameraBtn = (Button) myView.findViewById(R.id.cameraBtn);
        ocrImageView = (ImageView) myView.findViewById(R.id.ocrImageView);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //앨범에서 이미지 가져오기
                startActivityResult.launch("image/*");
            }
        });

        cameraBtn.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            activityResultPicture.launch(cameraIntent);

        });

        return myView; // Inflate the layout for this fragment
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "title", null);
        return Uri.parse(path);
    }

    Bitmap bitmap;

    ActivityResultLauncher<Intent> activityResultPicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Uri cameraUri;
                    String cameraImagePath="";
                    if(result != null) {
                        Bundle extras = result.getData().getExtras();
                        bitmap = (Bitmap) extras.get("data");
                        ocrImageView.setImageBitmap(bitmap);
                        Log.e(getImageUri(getContext(), bitmap).toString(), "URI 가져옴");
                        cameraUri = getImageUri(getContext(), bitmap);
                        cameraImagePath = getPathFromUri(getActivity(), cameraUri);
                        NetworkTask networkTask = new NetworkTask(cameraImagePath);
                        networkTask.execute();
                    }

                }
            }
    );


    ActivityResultLauncher<String> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        //ocrImageView.setImageURI(result);

                        String imagePath = getPathFromUri(getActivity(), result);
                        Log.e(imagePath, "URI에서 변환한 이미지의 절대경로");
                        Log.e(result.toString(), "선택한 이미지 파일의 URI 출력");
                        NetworkTask networkTask = new NetworkTask(imagePath);
                        networkTask.execute();

                    }
                    if (result == null) {
                        Log.d(this.getClass().getName(), "사진의 URI값이 null입니다.");
                    }
                }
            }
    );

    public class NetworkTask extends AsyncTask<Void, Void, String> {
        String imagePath;

        NetworkTask(String path) {
            imagePath = path;
        }

        @Override
        protected String doInBackground(Void... params) {
            RequestHttpConnection requestHttpURLConnection = new RequestHttpConnection();
            String response = requestHttpURLConnection.SendImage(imagePath);
            return response;
        }

        @Override
        //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어온다.
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            Intent intent = new Intent(getActivity().getApplicationContext(), OcrEditActivity.class);

            // OcrEditActivity로 json 응답 데이터 전달하기
            intent.putExtra("jsonResponse", response);
            intent.putExtra("imagePath", imagePath);

            startActivity(intent);
        }
    }

    //URI값을 절대 경로(Real path)로 바꿔주는 함수
    public static String getPathFromUri(Activity ctx, Uri fileUri) {
        String path = null;
        final String column = "_data";
        Cursor cursor = ctx.getContentResolver().query(fileUri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            if (document_id == null) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    if (column.equalsIgnoreCase(cursor.getColumnName(i))) {
                        path = cursor.getString(i);
                        break;
                    }
                }
            } else {
                document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
                cursor.close();

                final String[] projection = {column};
                try {
                    cursor = ctx.getContentResolver().query(
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            projection, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        path = cursor.getString(cursor.getColumnIndexOrThrow(column));
                    }
                } finally {
                    if (cursor != null)
                    {
                        cursor.moveToFirst();
                        cursor.close();
                    }
                }
            }
        }

        return path;
    }
}