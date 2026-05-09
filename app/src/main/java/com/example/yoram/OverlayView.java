package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.mediapipe.tasks.components.containers.Connection;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OverlayView extends View {

    private Drawable overlayDrawable;
    private String overlayText;
    private Paint textPaint;
    private Handler handler;
    public static int count = 15;
    public String[] yoga_array = {"전사자세", "다리당기기", "코브라자세"};
    private HashMap<String, String> yogamap;
    public ArrayList<Integer> yoga_id_array;
    private Integer yoga_image;
    private Integer tmp_yoga_image;
    private YogaViewModel yogaViewModel;
    public int yoga_count = 0;
    private SharedPreferences prefs;
    private JSONArray PoseName;
    private final ArrayList<String> PosNameArray = new ArrayList<>();
    private final HashMap<Integer,String > PoseNameMap = new HashMap<>();

    // 스켈레톤 관련
    private PoseLandmarkerResult results = null;
    private final Paint LinePaint = new Paint();
    private final Paint PointPaint = new Paint();


    private float ScaleFactor = 1f;
    private int ImageWidth = 1;
    private int ImageHeight = 1;
    private final float LANDMARK_STROKE_WIDTH = 12F;
    private int imageRotation = 0;

    private Set<Integer> Correct_landmark_index = new HashSet<>();
    private Set<Integer> Incorrect_landmark_index = new HashSet<>();

    private boolean IsFront = false;
    public OverlayView(Context context) {
        super(context);
        clear();
        init();
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        clear();
        init();
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        clear();
        init();
    }

    private void init() { // receive에서 알람 받으면 prefs.edit().clear() 하고 prefs.edit().putStringSet("pose", pose1).apply();
        // 해서 overView의 init()이 실행됬을때 해당 pose들을 가져오게 한다. 근데 overView가 알람 울릴때 마다 init()을 하는지 봐야됨
//        yogaViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(YogaViewModel.class);


        yoga_id_array = new ArrayList<>(Arrays.asList(
                R.drawable.warrior1,
                R.drawable.for_back_pose,
                R.drawable.cobra_pose
//                R.drawable.cat_pose
        ));
        PoseNameMap.put(R.drawable.warrior1, "전사자세");
        PoseNameMap.put(R.drawable.for_back_pose, "다리잡아 당기기");
        PoseNameMap.put(R.drawable.cobra_pose, "코브라 자세");
        // 기본 이미지 리소스 ID와 텍스트를 초기화
        prefs = getContext().getSharedPreferences("yoga", MODE_PRIVATE);


        // 스켈레톤 관련 초기화
        LinePaint.setColor(Color.WHITE);
        LinePaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        LinePaint.setStyle(Paint.Style.STROKE);

        PointPaint.setColor(Color.BLACK);
        PointPaint.setStrokeWidth(LANDMARK_STROKE_WIDTH);
        PointPaint.setStyle(Paint.Style.FILL);


        invalidate();
    }

    // 이미지 리소스 ID를 설정하는 메서드
    public void setOverlayImage(Integer resourceId) {
        try {
            overlayDrawable = ContextCompat.getDrawable(getContext(), resourceId);
            invalidate(); // 뷰를 다시 그리도록 요청
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 텍스트를 주기적으로 업데이트하는 메서드
    // 클래스 멤버 변수로 Handler 선언

    public void updateTextPeriodically() {
        if (yoga_count == yoga_id_array.size()) {// 이거 array 다 돌았으면 yoga_count 초기화 하는거
            yoga_count = 0;
        }
        // 동작선택 화면에서 자세를 선택하고 버튼을 클릭하면 callback으로 OverlayView에 선택된 자세정보와 갯수 정보 넘기면됨
        yoga_image = yoga_id_array.get(yoga_count);
        tmp_yoga_image = new Integer(yoga_image);
        Log.d("upyogaImage: ", String.valueOf(yoga_image));
        setOverlayImage(yoga_image);//yoga_count 기본값이 0이라 일단 출력이 되긴함
        if (count > 0) {
            setOverlayText("남은 시간 : " + count--);
        } else {
            setOverlayText("다음 동작으로 넘어갑니다.");
            count = 15; // 카운트 재설정
            yoga_count++;
        }
    }


    // 텍스트를 설정하는 메서드
    public void setOverlayText(String text) {
        overlayText = text;
        invalidate(); // 뷰를 다시 그리도록 요청
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 카메라 영상 위에 이미지를 그립니다.

        if (overlayDrawable != null) {
            int imageWidth2 = (int) (getWidth() * 0.8);
            int imageHeight2 = (int) (getHeight() * 0.8);

            // 이미지를 가운데에 위치시키기 위한 x, y 시작 좌표를 계산
            int x = (getWidth() - imageWidth2) / 2;
            int y = (getHeight() - imageHeight2) / 2;

            overlayDrawable.setBounds(x, y, x + imageWidth2, y + imageHeight2);
            overlayDrawable.draw(canvas);
        }


        // 텍스트를 그립니다.
        if (overlayText != null) {
            if (textPaint == null) {
                textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(50); // 텍스트 크기 설정
            }
            if (yoga_count < yoga_id_array.size()) {
                canvas.drawText(overlayText, 60, 50, textPaint); // 텍스트 위치 설정
                canvas.drawText("카메라에 보이시는 동작을 따라하세요.", 30, 110, textPaint);
                canvas.drawText("정확한 동작을 하셔야 카운트가 줄어듭니다.", 30, 170, textPaint);
                canvas.drawText("동작 이름 : " + PoseNameMap.get(yoga_id_array.get(yoga_count)), 30, 230, textPaint);
            }
        }


        if (results != null && results.landmarks() != null) {
            float xOffset = (getWidth() - ImageWidth * ScaleFactor) / 2f;
            float yOffset = (getHeight() - ImageHeight * ScaleFactor) / 2f;

            for (List<NormalizedLandmark> landmarkList : results.landmarks()) {
                for (int i = 0; i < landmarkList.size(); i++) {
                    float correctedX, correctedY;


                    switch (imageRotation) {
                        case 90:
                            correctedX = 1f - landmarkList.get(i).y();
                            correctedY = landmarkList.get(i).x();
                            break;
                        case 270:
                            correctedX = landmarkList.get(i).y();
                            correctedY = 1f - landmarkList.get(i).x();
                            break;
                        case 180:
                            correctedX = 1f - landmarkList.get(i).x();
                            correctedY = 1f - landmarkList.get(i).y();
                            break;
                        default:
                            correctedX = landmarkList.get(i).x();
                            correctedY = landmarkList.get(i).y();
                    }
                    if (IsFront) {
                        correctedX = 1.0f - correctedX;
                    }
                    if (Incorrect_landmark_index.contains(i)){
                        PointPaint.setColor(Color.RED);
                    } else if (Correct_landmark_index.contains(i)) {
                        PointPaint.setColor(Color.GREEN);
                    }else{
                        PointPaint.setColor(Color.WHITE);
                    }
                    canvas.drawPoint(correctedX * ImageWidth * ScaleFactor+xOffset,
                            correctedY * ImageHeight * ScaleFactor+yOffset,
                            PointPaint);
                }


                for (Connection connection : PoseLandmarker.POSE_LANDMARKS) {
                    int startlanmark = connection.start();
                    int endlanmark = connection.end();

                    NormalizedLandmark start = landmarkList.get(connection.start());
                    NormalizedLandmark end = landmarkList.get(connection.end());
                    float startX, startY, endX, endY;
                    if (imageRotation == 90) {
                        startX = 1f - start.y(); startY = start.x();
                        endX = 1f - end.y(); endY = end.x();
                    } else if (imageRotation == 270) {
                        startX = start.y(); startY = 1f - start.x();
                        endX = end.y(); endY = 1f - end.x();
                    } else {
                        startX = start.x(); startY = start.y();
                        endX = end.x(); endY = end.y();
                    }
                    if (Incorrect_landmark_index.contains(startlanmark) || Incorrect_landmark_index.contains(endlanmark)){
                        LinePaint.setColor(Color.RED);
                    } else if (Correct_landmark_index.contains(startlanmark) || Correct_landmark_index.contains(endlanmark)) {
                        LinePaint.setColor(Color.GREEN);
                    }else{
                        LinePaint.setColor(Color.WHITE);
                    }
                    canvas.drawLine(startX * ImageWidth * ScaleFactor+xOffset,
                            startY  * ImageHeight * ScaleFactor+yOffset,
                            endX * ImageWidth * ScaleFactor+xOffset,
                            endY * ImageHeight * ScaleFactor+yOffset,
                            LinePaint);
                }
            }
        }
    }

    public String getCurrenPose(){
        return getResources().getResourceEntryName(yoga_image);
    }
    public void setPoses(JSONArray poses) throws JSONException {
        this.PoseName = poses;
        ArrayList<String> PoseName_Array = new ArrayList<>();
        for (int i = 0; i < PoseName.length(); i++){
            PoseName_Array.add(PoseName.getString(i));

        }

        String pose_name;
        ArrayList<Integer> tmp_yoga_id_array = new ArrayList<>(yoga_id_array);
        for(Integer yoga_pose_overlay_id : tmp_yoga_id_array){
            pose_name = getResources().getResourceEntryName(yoga_pose_overlay_id);
            Log.d("PoseName_Array : pose_name 비교 ", "PoseName_Array:" +  PoseName_Array + "pose_name:" + pose_name);
            if (PoseName_Array.contains(pose_name)){
                Log.d("yoga_id_poseNameTrue", getResources().getResourceEntryName(yoga_pose_overlay_id));
                Log.d("yoga_id_pose", yoga_pose_overlay_id.toString());
            }else{
                Log.d("yoga_id_poseName", getResources().getResourceEntryName(yoga_pose_overlay_id));
                yoga_id_array.remove(yoga_pose_overlay_id);
                PoseNameMap.remove(yoga_pose_overlay_id);
            }
        }
        updateTextPeriodically();
        Log.d("yoga_id_array", String.valueOf(yoga_id_array));

    }

    private void clear(){
        results = null;
        PointPaint.reset();
        LinePaint.reset();
        invalidate();
        init();
    }

    public void setResults(PoseLandmarkerResult result, int imageWidth, int imageHeight, int rotation, Set<Integer> Correct_landmarker_index, Set<Integer> Incorrect_landmarker_index, RunningMode runningMode){

        this.results = result;
        this.ImageWidth = imageWidth;
        this.ImageHeight = imageHeight;
        this.imageRotation = rotation;
        this.Correct_landmark_index = Correct_landmarker_index;
        this.Incorrect_landmark_index = Incorrect_landmarker_index;

        switch (runningMode){
            case VIDEO:
                this.ScaleFactor = Math.min(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
                break;
            case IMAGE:
                break;
            case LIVE_STREAM:
                this.ScaleFactor = Math.max(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
                break;

        }
        invalidate();

    }

}
