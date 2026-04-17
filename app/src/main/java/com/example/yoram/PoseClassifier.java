package com.example.yoram;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PoseClassifier implements MideaPipePosepredict.PoseLandmarkerListener{
    MideaPipePosepredict mideaPipePosepredict;

    public static LinkedHashMap<Integer, String> mappingindex = new LinkedHashMap<>();
    static{
        mappingindex.put(11, "왼쪽 어께");
        mappingindex.put(12, "오른쪽 어께");
        mappingindex.put(13, "왼쪽 팔꿈치");
        mappingindex.put(14, "오른쪽 팔꿈치");
        mappingindex.put(15, "왼쪽 손목");
        mappingindex.put(16, "오른쪽 손목");

        mappingindex.put(23, "왼쪽 골반");
        mappingindex.put(24, "오른쪽 골반");
        mappingindex.put(25, "왼쪽 무릎");
        mappingindex.put(26, "오른쪽 무릎");
        mappingindex.put(27, "왼쪽 발목");
        mappingindex.put(28, "오른쪽 발목");
    }

    List<Integer> test_image_list = new ArrayList<>(Arrays.asList(
            R.drawable.warrior2,
            R.drawable.warrior3,
            R.drawable.warrior4,
            R.drawable.warrior5,
            R.drawable.warrior6,
            R.drawable.warrior7,
            R.drawable.warrior8,
            R.drawable.warrior9,
            R.drawable.warrior10));

    double upper_left_shoulder_to_wrist_angle;
    double upper_right_shoulder_to_wrist_angle;
    double lower_left_hip_to_ankle_angle;
    double lower_right_hip_to_ankle_angle;
    LinkedHashMap<String, ArrayList<Double>> AllAngles = new LinkedHashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, Double>>> Criterion = new HashMap<>();
    private final Context context;
    public PoseClassifier(Context context){
        this.context = context;
        mideaPipePosepredict = new MideaPipePosepredict(context,this,1);
        AllAngles.put("상체 각도", new ArrayList<>());
        AllAngles.put("하체 각도", new ArrayList<>());

        Criterion.put("warrior", new HashMap<>());
        Criterion.put("for_back", new HashMap<>());
        Criterion.put("cobra", new HashMap<>());

        Criterion.get("warrior").put("상체", new HashMap<>());
        Criterion.get("warrior").put("하체", new HashMap<>());

        Criterion.get("warrior").get("하체").put("큰 각도 최대", 138.465);
        Criterion.get("warrior").get("하체").put("큰 각도 최소", 94.673);
        Criterion.get("warrior").get("하체").put("작은 각도 최대", 180.0);
        Criterion.get("warrior").get("하체").put("작은 각도 최소", 142.280);
        Criterion.get("warrior").get("상체").put("최대", 180.0);
        Criterion.get("warrior").get("상체").put("최소", 160.716);


    }
    public void run(){

        for (Integer test_image : test_image_list){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), test_image);
            mideaPipePosepredict.detectLiveStream(bitmap);
        }
    }
    @Override
    public void onResult(PoseLandmarkerResult result, int imageWidth, int imageHeight) {
        Log.d("결과 수신: ", String.valueOf(result));
        Log.d("이미지 가로: ", String.valueOf(imageWidth));
        Log.d("이미지 세로: ", String.valueOf(imageHeight));


//        List<NormalizedLandmark> upper_body = result.landmarks().get(0).subList(11,17);
//        List<NormalizedLandmark> lower_body = result.landmarks().get(0).subList(23, 28);

        for (Integer tmp : mappingindex.keySet()){
            Log.d(mappingindex.get(tmp) + String.valueOf(tmp), String.valueOf(result.landmarks().get(0).get(tmp)));

        }

        WarriorAllAngleCalculate(result);

        for(String key : AllAngles.keySet()){
            Log.d(key + " ", String.valueOf(AllAngles.get(key)));

        }
        if (AllAngles.get("하체 각도").size() >= 10){
            ArrayList<Double> bigAngleside = new ArrayList<>();
            ArrayList<Double> smallAngleside = new ArrayList<>();

            for (double d : AllAngles.get("하체 각도")){
                if (d <= 130) bigAngleside.add(d);
                if (d >= 160) smallAngleside.add(d);
            }
            Log.d("bigAngleside_length", String.valueOf(bigAngleside.size()));
            Log.d("smallAnglesidelength", String.valueOf(smallAngleside.size()));

            Log.d("bigAngle_Max", String.valueOf(Collections.max(bigAngleside)));
            Log.d("smallAngle_Max", String.valueOf(Collections.max(smallAngleside)));
            Log.d("bigAngle_Min", String.valueOf(Collections.min(bigAngleside)));
            Log.d("smallAngle_Min", String.valueOf(Collections.min(smallAngleside)));

            Log.d("상체 최소", String.valueOf(Collections.min(AllAngles.get("상체 각도"))));
            Log.d("상체 최대", String.valueOf(Collections.max(AllAngles.get("상체 각도"))));

        }



//        Log.d("상체 왼쪽 어께, 팔꿈치, 손목 각도", String.valueOf(upper_left_shoulder_to_wrist_angle));

    }

    @Override
    public void onError(String error) {
        Log.d("결과 수신 실패: ", error);
    }
    public double CalculateAngle(float x1, float y1, float x2, float y2, float x3, float y3){
        double BAX = x1 - x2;
        double BAY = y1 - y2;
        double BCX = x3 - x2;
        double BCY = y3 - y2;

        double DotProuct = BAX * BCX + BAY * BCY;
        double MagnitudeBA = Math.sqrt(BAX * BAX + BAY * BAY);
        double MagnitudeBC = Math.sqrt(BCX * BCX + BCY * BCY);

        if (MagnitudeBA == 0 || MagnitudeBC == 0){
            return 0.0;
        }

        double COSAngle = DotProuct / (MagnitudeBA *MagnitudeBC);

        COSAngle = Math.max(-1.0, Math.min(1.0, COSAngle));

        double AngleRad = Math.acos(COSAngle);

        return Math.toDegrees(AngleRad);
    }

    public Boolean WarriorAllAngleCalculate(PoseLandmarkerResult result){
        upper_left_shoulder_to_wrist_angle = CalculateAngle(
                result.landmarks().get(0).get(11).x(),
                result.landmarks().get(0).get(11).y(),
                result.landmarks().get(0).get(13).x(),
                result.landmarks().get(0).get(13).y(),
                result.landmarks().get(0).get(15).x(),
                result.landmarks().get(0).get(15).y());


        upper_right_shoulder_to_wrist_angle = CalculateAngle(
                result.landmarks().get(0).get(12).x(),
                result.landmarks().get(0).get(12).y(),
                result.landmarks().get(0).get(14).x(),
                result.landmarks().get(0).get(14).y(),
                result.landmarks().get(0).get(16).x(),
                result.landmarks().get(0).get(16).y());

        lower_left_hip_to_ankle_angle = CalculateAngle(
                result.landmarks().get(0).get(23).x(),
                result.landmarks().get(0).get(23).y(),
                result.landmarks().get(0).get(25).x(),
                result.landmarks().get(0).get(25).y(),
                result.landmarks().get(0).get(27).x(),
                result.landmarks().get(0).get(27).y());

        lower_right_hip_to_ankle_angle = CalculateAngle(
                result.landmarks().get(0).get(24).x(),
                result.landmarks().get(0).get(24).y(),
                result.landmarks().get(0).get(26).x(),
                result.landmarks().get(0).get(26).y(),
                result.landmarks().get(0).get(28).x(),
                result.landmarks().get(0).get(28).y());

        AllAngles.get("상체 각도").add(upper_left_shoulder_to_wrist_angle);
        AllAngles.get("상체 각도").add(upper_right_shoulder_to_wrist_angle);
        AllAngles.get("하체 각도").add(lower_left_hip_to_ankle_angle);
        AllAngles.get("하체 각도").add(lower_right_hip_to_ankle_angle);
        double small_leg_angle = Math.max(lower_left_hip_to_ankle_angle, lower_right_hip_to_ankle_angle);
        double big_leg_angle = Math.min(lower_left_hip_to_ankle_angle, lower_right_hip_to_ankle_angle);
        boolean upperresult;
        boolean lowerresult;
        
        if (Criterion.get("warrior").get("상체").get("최소") <= upper_left_shoulder_to_wrist_angle &&
                upper_left_shoulder_to_wrist_angle <= Criterion.get("warrior").get("상체").get("최대")){
            upperresult = true;
            Log.d("상체 통과", "왼쪽 팔 " + String.valueOf(upper_left_shoulder_to_wrist_angle)
            + "오른쪽 팔" + String.valueOf(upper_right_shoulder_to_wrist_angle));
        }else upperresult = false;

        if(Criterion.get("warrior").get("하체").get("큰 각도 최소") <= big_leg_angle
        && big_leg_angle <= Criterion.get("warrior").get("하체").get("큰 각도 최대")
        && Criterion.get("warrior").get("하체").get("작은 각도 최소") <= small_leg_angle
        && small_leg_angle <= Criterion.get("warrior").get("하체").get("작은 각도 최대")){

            Log.d("하체 통과", "큰쪽 다리 " + String.valueOf(big_leg_angle)
                    + "작은쪽 다리" + String.valueOf(small_leg_angle));
            lowerresult = true;
        }else {Log.d("하체 불통", "큰쪽 다리 " + String.valueOf(big_leg_angle) + "작은 쪽 다리" +
                String.valueOf(small_leg_angle));
                lowerresult = false;}
        
        if (upperresult && lowerresult){
            return true;
        }else return false;


    }
    private LinkedHashMap<String, Double> For_Back_Pose_AllAngleCalculate(PoseLandmarkerResult result){

        return new LinkedHashMap<>();
    }
}
