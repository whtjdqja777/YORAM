package com.example.yoram;//import static androidx.appcompat.graphics.drawable.DrawableContainerCompat.Api21Impl.getResources;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.yoram.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class YogaClassifier {
    Context context;
    private int imageSize = 224;
    public YogaClassifier(Context context){
        this.context = context;
    }
    YogaActivity yogaActivity;
    public Bitmap TestClassifier(){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.warrior10);
        Bitmap resultbitmap = runInference(bitmap);
        return resultbitmap;
    }
    public Bitmap runInference(Bitmap imageData) {

        try {
            ModelUnquant model = ModelUnquant.newInstance(context.getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageData, 224, 224, true);
            int[] intValues = new int[imageSize * imageSize];
            imageData.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

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
//            checkAndCompleteYoga();//모든 요가 자세가 완료되었는지 확인
            Log.d("warrior_ result", result);
            return resizedBitmap;
            // find the index of the class with the biggest confidence.
            // Releases model resources if no longer used.

        } catch (IOException e) {
            // TODO Handle the exception
            Log.d("warrior_ result", "stand");
            return null;
        }
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
        Log.d("predicted_Pose", result);
        // 정확한 동작을 할수있게 확률이 85퍼센트 이상이면 실행할수있게 만든다.
        if (max_val < 0.8) { // 가장 확률이 높은 클래스의 확률이 적어도 0.8 이상은 되야 해당 자세명을 리턴하고
            result = "stand";//해당 자세 클래스의 확률이 0.8 미만이면 그냥 stand를 반환
        }
        Log.d("max_val", String.valueOf(max_val));
//        Log.i("yoga", result);

        return result;
    }
}
