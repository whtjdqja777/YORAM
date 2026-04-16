package com.example.yoram;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.yoram.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;

public class YogaActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private PreviewView viewFinder;
    private ProcessCameraProvider cameraProvider;
    private OverlayView overlayView;
    private long lastAnalyzedTimestamp = 0;
    private int imageSize = 224;
    private String targetPoseName = "stand"; // 목표 요가 자세 이름
    YogaViewModel yogaViewModel;
    int current_yoga_id = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoga);
        viewFinder = findViewById(R.id.viewFinder);
        overlayView = findViewById(R.id.overlayView);
        targetPoseName = overlayView.getCurrenPose();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private MappedByteBuffer loadModelFile(String modelFileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(getAssets().openFd(modelFileName).getFileDescriptor())) {
            FileChannel fileChannel = fis.getChannel();
            long startOffset = getAssets().openFd(modelFileName).getStartOffset();
            long declaredLength = getAssets().openFd(modelFileName).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(this).get();
                bindPreview();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), image -> {
            if (System.currentTimeMillis() - lastAnalyzedTimestamp >= 1000) {// 프레임 마다 분석 시도하지만 조건상 1초마다 실행됨
                Bitmap imageData = convertImageToByteArray(image);// 여기서 image가 1초마다 들어오는 프레임
                String poseName = runInference(imageData);// runInference가 자세 인식하는거고 여기서 인식 후 posName이 현재 동작해야될 포즈인
                                                        //targetPoseName과 같아야 OverlayView의 남은 초 수를 업데이트 해줌
                //모델 추론과정이 있기 때문에 비동기로 callback 써서 수정해야할 듯
                // 현재 targePoseName이 "stand"이고

                if (poseName.equals(targetPoseName)) {// 모델이 출력한 poseName과 targetPoseName이 같아야 실행됨
                    //위에 조건문이 카운트까지 담당하고 있음
                    overlayView.updateTextPeriodically(); // 여기서
                    String tmpnewtarget = overlayView.getCurrenPose();
                    if (!targetPoseName.equals(tmpnewtarget)){// update중 yoga_count가 증가하면 검사하는 요가 자세가 바뀜
                        targetPoseName = tmpnewtarget;//그래서 현재의 타겟 포즈와 새로운 포즈가 다르면 다음 동작으로 넘어간거고 바뀐 포즈로 바꿔줌
                    }
                }
                lastAnalyzedTimestamp = System.currentTimeMillis();
            }
            image.close();
        });

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
    }


    public String runInference(Bitmap imageData) {

        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            imageData.getPixels(intValues, 0, imageData.getWidth(), 0, 0, imageData.getWidth(), imageData.getHeight());

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            int pixel = 0;
            for (int i = 0; i < 224; i++) {
                for (int j = 0; j < 224; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();//촬영된 자세와 학습된 자세의 일치 신뢰도(확률)
            String result = interpretOutput(confidences);// 신뢰도에 따라 다른 결과를 출력
            //기본적으로 인식률 자체가 낮으면 "stand"만 리턴받음 그런데 현재 엑티비티에서
            // targetPoseName이 "stand"로 고정되어 있어서 인식이 안되도 카운트가 되고 있었던거
            //
            checkAndCompleteYoga();//모든 요가 자세가 완료되었는지 확인
            return result;
            // find the index of the class with the biggest confidence.
            // Releases model resources if no longer used.
        } catch (IOException e) {
            // TODO Handle the exception
            return "stand";
        }
    }

    private Bitmap convertImageToByteArray(ImageProxy image) {
        // YUV 데이터를 가져오기 위한 준비
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        // YUV -> NV21 포맷으로 변환
        byte[] nv21 = new byte[ySize + uSize + vSize];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        // NV21 -> Bitmap 변환
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();

        // Bitmap 객체 생성
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        // Bitmap 크기 조정 (224x224)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 224, 224, true);
        return resizedBitmap;
    }
    private void checkAndCompleteYoga() {
        // 모든 요가 동작이 완료되었는지 확인
        if(overlayView.yoga_count >= overlayView.yoga_id_array.size()){
            Intent intent = new Intent(YogaActivity.this, YogaEndActivity.class);
            startActivity(intent);
            finish();
        }

        // 모든 요가 동작이 완료되었다면 요가 끝 페이지로 이동
; // 현재 액티비티 종료
    }

    private String interpretOutput(float[] outputData) {
        // TODO: 모델의 출력 데이터를 해석하여 자세 이름을 반환하는 로직 구현
        String result = "stand";
        int max_index = 0;
        float max_val = 0;
        Log.i("ARR", Arrays.toString(outputData));

        for (int i = 0; i < 6; i++) {       // 모델 출력인 각 클래스에 대한 확률 배열 중 가장 큰 인덱스를 찾음
            if (max_val < outputData[i]) {
                max_val = outputData[i];    // 확률이 가장 큰 인덱스의 확률 값을 찾음
                max_index = i;
            }
        }
        //번호마다 요가 이름 부여
        if (max_index == 0) { // 자세 확률이 가장 큰 클래스에 해당하는 결과를 반환 result의 기반값은 "stand"임
        } else if (max_index == 1) {
            result = "bow";
        } else if (max_index == 2) {
            result = "warrior";
        } else if (max_index == 3) {
            result = "cobra";
        } else if (max_index == 4) {
            result = "cat_pose_pose";
        } else if (max_index == 5) {
            result = "for_back_pose";
        }

        // 정확한 동작을 할수있게 확률이 85퍼센트 이상이면 실행할수있게 만든다.
        if (max_val < 0.8) { // 가장 확률이 높은 클래스의 확률이 적어도 0.8 이상은 되야 해당 자세명을 리턴하고
            result = "stand";//해당 자세 클래스의 확률이 0.8 미만이면 그냥 stand를 반환
        }
        Log.i("yoga", result);

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                // 권한 거부 처리
            }
        }
    }
}
