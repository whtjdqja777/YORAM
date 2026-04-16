package com.example.yoram;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.HashMap;
import java.util.Map;


public class MideaPipePosepredict {
    private final Context context;
    private final PoseLandmarkerListener listener;
    private final int model_type;

    private PoseLandmarker poseLandmarker;

    private static final int MODEL_LITE = 0;
    private static final int MODEL_FULL = 1;
    private static final int MODEL_HEAVY = 0;

    private static final Map<Integer, String> MODEL_NAMES = new HashMap<Integer, String>();
    static {
        MODEL_NAMES.put(MODEL_LITE, "pose_landmarker_lite.task");
        MODEL_NAMES.put(MODEL_FULL, "pose_landmarker_full.task");
        MODEL_NAMES.put(MODEL_HEAVY, "pose_landmarker_heavy.task");
    }

    public interface PoseLandmarkerListener {
        void onResult(PoseLandmarkerResult result, int imageWidth, int imageHeight);
        void onError(String error);
    }

    public MideaPipePosepredict(Context context, PoseLandmarkerListener listener){

        this.context = context;
        this.listener = listener;
        this.model_type = MODEL_FULL;// FULL 모델로 고정


    }
    public MideaPipePosepredict(Context context, PoseLandmarkerListener listener, int model_select){

        this.context = context;
        this.listener = listener;
        this.model_type = model_select;// 선택한 모델로 사용
        setupPoseLandMaker();
    }

    private void setupPoseLandMaker(){
        String modelname = MODEL_NAMES.containsKey(model_type)?MODEL_NAMES.get(model_type):MODEL_NAMES.get(MODEL_FULL);

        BaseOptions baseOptions = BaseOptions.builder()// 모델 기본 설정
                .setModelAssetPath(modelname) // 모델 경로
                .setDelegate(Delegate.CPU) // CPU, GPU 추론 설정
                .build();

        PoseLandmarker.PoseLandmarkerOptions options= PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)// LIVE_STREAM, IMAGE, VIDEO 등 추론할 대상 설정
                .setNumPoses(1) //탐지할 객체 수
                .setMinPoseDetectionConfidence(0.5f)// 이 사람이 사람 자세로 검출 됬다는 최소 신뢰도
                .setMinPosePresenceConfidence(0.5f)// 검출된 자세가 실제로 존재한다는 최소 신뢰도
//                .setMinTrackingConfidence(0.5f) // 이전 프레임들 참고한 tracking 정보를 활용한 신뢰도
//                .setResultListener((result, inputImage) -> {
//                    listener.onResult(result, inputImage.getWidth(), inputImage.getHeight());
//                })
//                .setErrorListener(error -> {
//                    listener.onError(error.getMessage() != null ? error.getMessage():"알 수 없는 에러");
//                })
                .build();

        try{
            poseLandmarker = PoseLandmarker.createFromOptions(context, options);
            Log.d("초기화 성공", modelname);
        } catch (Exception e){
            listener.onError("초기화 실패" + e.getMessage());
            Log.e("MideaPipPosepredict", e.getMessage());
        }



    }
    public void detectLiveStream(Bitmap bitmap){
        if (poseLandmarker == null){
            listener.onError("poseLandmarker 객체 없음");
            return;
        }
        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
//        long frametime = SystemClock.uptimeMillis();
//        poseLandmarker.detectAsync(mpImage, frametime);// 스트림 데이터 탐지
        try{
            PoseLandmarkerResult result = poseLandmarker.detect(mpImage);
            listener.onResult(result, mpImage.getWidth(), mpImage.getHeight());
        } catch (Exception e) {
            listener.onError("추론실패" + e.getMessage());
        }


    }

    public void close(){
        if (poseLandmarker != null){
            poseLandmarker.close();
            poseLandmarker = null;
        }
    }
}
