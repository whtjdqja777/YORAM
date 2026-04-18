package com.example.yoram;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.Arrays;
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
    List<Integer> for_back_test_image_list = new ArrayList<>(Arrays.asList(
            R.drawable.for_back1,
            R.drawable.for_back2,
            R.drawable.for_back3,
            R.drawable.for_back4,
            R.drawable.for_back5,
            R.drawable.for_back6,
            R.drawable.for_back7
            ));


    double upper_left_shoulder_to_wrist_angle;
    double upper_right_shoulder_to_wrist_angle;
    double lower_left_hip_to_ankle_angle;
    double lower_right_hip_to_ankle_angle;
    LinkedHashMap<String, ArrayList<Double>> AllAngles = new LinkedHashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, Double>>> WarriorCriterion = new HashMap<>();
    private HashMap<String, HashMap<String, Float>> For_Back_Criterion = new HashMap<>();
    private final Context context;

    private HashMap<String, ArrayList<Float>> hip_knee = new HashMap<>();

    private ArrayList<Float> for_back_angle_list = new ArrayList<>();
    public PoseClassifier(Context context){
        this.context = context;
        mideaPipePosepredict = new MideaPipePosepredict(context,this,1);
        AllAngles.put("상체 각도", new ArrayList<>());
        AllAngles.put("하체 각도", new ArrayList<>());

        WarriorCriterion.put("warrior", new HashMap<>());
        WarriorCriterion.put("for_back", new HashMap<>());
        WarriorCriterion.put("cobra", new HashMap<>());

        WarriorCriterion.get("warrior").put("상체", new HashMap<>());
        WarriorCriterion.get("warrior").put("하체", new HashMap<>());
        WarriorCriterion.get("warrior").put("상하체 비율", new HashMap<>());

        WarriorCriterion.get("warrior").get("하체").put("큰 각도 최대", 138.465);
        WarriorCriterion.get("warrior").get("하체").put("큰 각도 최소", 94.673);
        WarriorCriterion.get("warrior").get("하체").put("작은 각도 최대", 180.0);
        WarriorCriterion.get("warrior").get("하체").put("작은 각도 최소", 142.280);
        WarriorCriterion.get("warrior").get("상체").put("최대", 180.0);
        WarriorCriterion.get("warrior").get("상체").put("최소", 160.716);

        WarriorCriterion.get("warrior").get("상하체 비율").put("최대", 0.4); // 이미지 보니까 이정도는 펼쳐야될거 같아서 해당 이미지의 상하체 비율을 기준으로 잡음
        WarriorCriterion.get("warrior").get("상하체 비율").put("최소", 0.20);// -> 많이 펼처진거기 때문에 더 낮게 잡음

        For_Back_Criterion.put("angle", new HashMap<>());
        For_Back_Criterion.put("ratio", new HashMap<>());

        For_Back_Criterion.get("angle").put("max_range", 65F);
        For_Back_Criterion.get("angle").put("min_range", 30F);
        For_Back_Criterion.get("ratio").put("max_range", 10.5F);
        For_Back_Criterion.get("ratio").put("min_range", 2.5F);



        hip_knee.put("골반", new ArrayList<>());
        hip_knee.put("무릎", new ArrayList<>());
        hip_knee.put("발목", new ArrayList<>());
        hip_knee.put("비율", new ArrayList<>());
        
        
    }
    public void run(){

        for (Integer test_image : for_back_test_image_list){
            Log.d("test_image_name", context.getResources().getResourceEntryName(test_image));
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), test_image);
            mideaPipePosepredict.detectLiveStream(bitmap);
        }
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.worrior_pose);
//        mideaPipePosepredict.detectLiveStream(bitmap);
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

        CheckFor_Back(result);
//        CheckWarrior(result);
        Log.d("hip_knee_x", String.valueOf(hip_knee));


        for(String key : AllAngles.keySet()){
            Log.d(key + " ", String.valueOf(AllAngles.get(key)));

        }



//        if (AllAngles.get("하체 각도").size() >= 10){ //각도 분석을 위해서 썻던 코드
//            ArrayList<Double> bigAngleside = new ArrayList<>();
//            ArrayList<Double> smallAngleside = new ArrayList<>();
//
//            for (double d : AllAngles.get("하체 각도")){
//                if (d <= 130) bigAngleside.add(d);
//                if (d >= 160) smallAngleside.add(d);
//            }
//            Log.d("bigAngleside_length", String.valueOf(bigAngleside.size()));
//            Log.d("smallAnglesidelength", String.valueOf(smallAngleside.size()));
//
//            Log.d("bigAngle_Max", String.valueOf(Collections.max(bigAngleside)));
//            Log.d("smallAngle_Max", String.valueOf(Collections.max(smallAngleside)));
//            Log.d("bigAngle_Min", String.valueOf(Collections.min(bigAngleside)));
//            Log.d("smallAngle_Min", String.valueOf(Collections.min(smallAngleside)));
//
//            Log.d("상체 최소", String.valueOf(Collections.min(AllAngles.get("상체 각도"))));
//            Log.d("상체 최대", String.valueOf(Collections.max(AllAngles.get("상체 각도"))));
//
//        }



//        Log.d("상체 왼쪽 어께, 팔꿈치, 손목 각도", String.valueOf(upper_left_shoulder_to_wrist_angle));

    }

    @Override
    public void onError(String error) {
        Log.d("결과 수신 실패: ", error);
    }
    public float CalculateAngle(float x1, float y1, float x2, float y2, float x3, float y3){
        double BAX = x1 - x2;
        double BAY = y1 - y2;
        double BCX = x3 - x2;
        double BCY = y3 - y2;

        double DotProuct = BAX * BCX + BAY * BCY;
        double MagnitudeBA = Math.sqrt(BAX * BAX + BAY * BAY);
        double MagnitudeBC = Math.sqrt(BCX * BCX + BCY * BCY);

        if (MagnitudeBA == 0 || MagnitudeBC == 0){
            return 0.0F;
        }

        double COSAngle = DotProuct / (MagnitudeBA *MagnitudeBC);

        COSAngle = Math.max(-1.0, Math.min(1.0, COSAngle));

        double AngleRad = Math.acos(COSAngle);

        return (float) Math.toDegrees(AngleRad);
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
        
        if (WarriorCriterion.get("warrior").get("상체").get("최소") <= upper_left_shoulder_to_wrist_angle &&
                upper_left_shoulder_to_wrist_angle <= WarriorCriterion.get("warrior").get("상체").get("최대")){
            upperresult = true;
            Log.d("상체 통과", "왼쪽 팔 " + String.valueOf(upper_left_shoulder_to_wrist_angle)
            + "오른쪽 팔" + String.valueOf(upper_right_shoulder_to_wrist_angle));
        }else upperresult = false;

        if(WarriorCriterion.get("warrior").get("하체").get("큰 각도 최소") <= big_leg_angle
        && big_leg_angle <= WarriorCriterion.get("warrior").get("하체").get("큰 각도 최대")
        && WarriorCriterion.get("warrior").get("하체").get("작은 각도 최소") <= small_leg_angle
        && small_leg_angle <= WarriorCriterion.get("warrior").get("하체").get("작은 각도 최대")){

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

    private Boolean Warrior_Upper_Lower_Ratio(PoseLandmarkerResult result){
        //11, 12 -> 어께
        //27, 28 -> 발목
        double upper = Math.abs(result.landmarks().get(0).get(12).x() - result.landmarks().get(0).get(11).x());
        double lower = Math.abs(result.landmarks().get(0).get(28).x() - result.landmarks().get(0).get(27).x());

        Log.d("하체 대비 상체 비율", String.valueOf(upper/lower));
        double upper_lower_ratio = upper/lower;
        if (WarriorCriterion.get("warrior").get("상하체 비율").get("최소") <= upper_lower_ratio
                && upper_lower_ratio <= WarriorCriterion.get("warrior").get("상하체 비율").get("최대")){
            return true;

        }else return false;
    }


    private LinkedHashMap<String, Double> For_Back_Pose_AllAngleCalculate(PoseLandmarkerResult result){

        return new LinkedHashMap<>();
    }

    private void CheckWarrior(PoseLandmarkerResult result){
        boolean AngleResult = WarriorAllAngleCalculate(result);
        boolean RatioResult = Warrior_Upper_Lower_Ratio(result);
        if(AngleResult && RatioResult){
            Log.d("통과", "각도 비율 정상");
        } else if (!AngleResult) {
            Log.d("각도: ", "비정상");
        }else{
            Log.d("비율: ", "비정상");
        }
    }
    private Boolean CheckFor_Back(PoseLandmarkerResult result){
        // 각도 계산
        // 필요 랜드마크: 23,24,25,26,27,28
        // 무릎 골반 위치 계산
        // 필요 랜드마크: 23,24,25,26
        // 지세 방향 별 랜드마크 필터링 (left or right)
        Log.d("worldlandmakers", String.valueOf(result.worldLandmarks().get(0).get(23)));
        double left_visibility = result.landmarks().get(0).get(23).visibility().get()
                + result.landmarks().get(0).get(25).visibility().get()
                +result.landmarks().get(0).get(27).visibility().get();
        double right_visibility = result.landmarks().get(0).get(24).visibility().get()
                + result.landmarks().get(0).get(26).visibility().get()
                +result.landmarks().get(0).get(28).visibility().get();
        float for_back_angle_result;
        float ratio_result;
        Log.d("left_right_visibility_hip", String.valueOf(left_visibility/3) + " " + String.valueOf(right_visibility/3));
        if (left_visibility > right_visibility){ // 왼쪽 오른쪽 방향 판별

            float left_hip_x = result.landmarks().get(0).get(23).x();
            float left_hip_y = result.landmarks().get(0).get(23).y();
            float left_knee_x = result.landmarks().get(0).get(25).x();
            float left_knee_y = result.landmarks().get(0).get(25).y();
            float left_ankle_x = result.landmarks().get(0).get(27).x();
            float left_ankle_y = result.landmarks().get(0).get(27).y();

            //각도 계산
            for_back_angle_result = CalculateAngle(left_hip_x, left_hip_y, left_knee_x, left_knee_y,left_ankle_x,left_ankle_y);
//            Log.d("for_back_angle", String.valueOf(for_back_angle));

            // (무릎 - 골반) / (발목 - 골반) 비율
            ratio_result = Math.abs(left_knee_x - left_hip_x) / Math.abs(left_ankle_x - left_hip_x);
//            hip_knee.get("골반").add(left_hip_x);
//            hip_knee.get("무릎").add(left_knee_x);
//            hip_knee.get("발목").add(left_ankle_x);
//            hip_knee.get("비율").add(ratio_result);
//            for_back_angle_list.add(for_back_angle);



        }else{
            float right_hip_x = result.landmarks().get(0).get(24).x();
            float right_hip_y = result.landmarks().get(0).get(24).y();
            float right_knee_x = result.landmarks().get(0).get(26).x();
            float right_knee_y = result.landmarks().get(0).get(26).y();
            float right_ankle_x = result.landmarks().get(0).get(28).x();
            float right_ankle_y = result.landmarks().get(0).get(28).y();
            for_back_angle_result = CalculateAngle(right_hip_x, right_hip_y, right_knee_x, right_knee_y,right_ankle_x,right_ankle_y);
            // 골반-무릅 위치
            ratio_result = Math.abs(right_knee_x - right_hip_x) / Math.abs(right_ankle_x - right_hip_x);
//            hip_knee.get("골반").add(right_hip_x);
//            hip_knee.get("무릎").add(right_knee_x);
//            hip_knee.get("발목").add(right_ankle_x);
//            hip_knee.get("비율").add(ratio_result);
//            for_back_angle_list.add(for_back_angle);


        }
        if (For_Back_Criterion.get("angle").get("min_range") <= for_back_angle_result &&
                 for_back_angle_result<= For_Back_Criterion.get("angle").get("max_range") &&
        For_Back_Criterion.get("ratio").get("min_range") <= ratio_result &&
        ratio_result <= For_Back_Criterion.get("ratio").get("max_range")){
            Log.d("통과", "각도 비율 통과");
            return true;
        }else if(for_back_angle_result < For_Back_Criterion.get("angle").get("min_range") ||
        for_back_angle_result > For_Back_Criterion.get("angle").get("max_range")) {
            Log.d("불통", "각도 불통");
            return false;
        }else {
            Log.d("불통", "비율 불통");
            return false;
        }



    }


}
