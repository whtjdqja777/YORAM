package com.example.yoram;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.ArrayList;
import java.util.Arrays;
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
    List<Integer> for_back_test_image_list = new ArrayList<>(Arrays.asList(
            R.drawable.for_back1,
            R.drawable.for_back2,
            R.drawable.for_back3,
            R.drawable.for_back4,
            R.drawable.for_back5,
            R.drawable.for_back6,
            R.drawable.for_back7
            ));
    List<Integer> cobra_test_image_list = new ArrayList<>(Arrays.asList(
            R.drawable.cobra1,
            R.drawable.cobra2,
            R.drawable.cobra3,
            R.drawable.cobra4,
            R.drawable.cobra5,
            R.drawable.cobra6,
            R.drawable.cobra7,
            R.drawable.cobra8,
            R.drawable.cobra9,
            R.drawable.cobra10
    ));


    double upper_left_shoulder_to_wrist_angle;
    double upper_right_shoulder_to_wrist_angle;
    double lower_left_hip_to_ankle_angle;
    double lower_right_hip_to_ankle_angle;
    LinkedHashMap<String, ArrayList<Double>> AllAngles = new LinkedHashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, Double>>> WarriorCriterion = new HashMap<>();
    private HashMap<String, HashMap<String, Float>> For_Back_Criterion = new HashMap<>();
    private HashMap<String, HashMap<String, HashMap<String, Float>>> Cobra_Criterion = new HashMap<>();

    private final Context context;

    private HashMap<String, ArrayList<Float>> hip_knee = new HashMap<>();

    private ArrayList<Float> for_back_angle_list = new ArrayList<>();
    private List<NormalizedLandmark> landmarks;
    private HashMap<String, ArrayList<Float>> Cobra_Angle_HashMap = new HashMap<>();
    public int success = 0;
    public int fail = 1;
    ImageProcessingOptions options;
    public PoseClassifier(Context context){
        this.context = context;
        mideaPipePosepredict = new MideaPipePosepredict(context,this,2);
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

        Cobra_Criterion.put("angle", new HashMap<>());
        Cobra_Criterion.put("ratio", new HashMap<>());

        Cobra_Criterion.get("angle").put("Whole_body_Angle", new HashMap<>());
        Cobra_Criterion.get("angle").get("Whole_body_Angle").put("max_range", 145F);
        Cobra_Criterion.get("angle").get("Whole_body_Angle").put("min_range", 108F);

        Cobra_Criterion.get("angle").put("Arm_Angle", new HashMap<>());
        Cobra_Criterion.get("angle").get("Arm_Angle").put("max_range", 180F);
        Cobra_Criterion.get("angle").get("Arm_Angle").put("min_range", 148F);

        Cobra_Criterion.get("angle").put("Lower_Angle", new HashMap<>());
        Cobra_Criterion.get("angle").get("Lower_Angle").put("max_range", 180F);
        Cobra_Criterion.get("angle").get("Lower_Angle").put("min_range", 143F);

        Cobra_Criterion.get("ratio").put("hip_knee_distance_ratio", new HashMap<>());
        Cobra_Criterion.get("ratio").get("hip_knee_distance_ratio").put("max_range", 0.42F);
        Cobra_Criterion.get("ratio").get("hip_knee_distance_ratio").put("min_range", 0.1F);



        hip_knee.put("골반", new ArrayList<>());
        hip_knee.put("무릎", new ArrayList<>());
        hip_knee.put("발목", new ArrayList<>());
        hip_knee.put("비율", new ArrayList<>());

        Cobra_Angle_HashMap.put("Whole_body_Angle", new ArrayList<>());
        Cobra_Angle_HashMap.put("Arm_Angle", new ArrayList<>());
        Cobra_Angle_HashMap.put("Lower_Angle", new ArrayList<>());
        Cobra_Angle_HashMap.put("ratio", new ArrayList<>());


    }
    public void run(MPImage mpimage, ImageProcessingOptions options){

//        for (Integer test_image : cobra_test_image_list){
//            Log.d("test_image_name", context.getResources().getResourceEntryName(test_image));
//            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), test_image);
//            mideaPipePosepredict.detectLiveStream(bitmap);
//        }

        mideaPipePosepredict.detectLiveStream(mpimage, options);
//        Log.d("Cobra_whole_body_Angle_MAX",String.valueOf(Collections.max(Cobra_Angle_HashMap.get("Whole_body_Angle"))));
//        Log.d("Cobra_whole_body_Angle_MIN",String.valueOf(Collections.min(Cobra_Angle_HashMap.get("Whole_body_Angle"))));
//
//        Log.d("Cobra_Arm_Angle_MAX",String.valueOf(Collections.max(Cobra_Angle_HashMap.get("Arm_Angle"))));
//        Log.d("Cobra_Arm_Angle_MIN",String.valueOf(Collections.min(Cobra_Angle_HashMap.get("Arm_Angle"))));
//
//        Log.d("Cobra_Lower_Angle_MAX",String.valueOf(Collections.max(Cobra_Angle_HashMap.get("Lower_Angle"))));
//        Log.d("Cobra_Lower_Angle_MIN",String.valueOf(Collections.min(Cobra_Angle_HashMap.get("Lower_Angle"))));
//        for (int i = 0; i < Cobra_Angle_HashMap.get("ratio").size(); i++){
//            Log.d("Cobra_ratio", String.valueOf(Cobra_Angle_HashMap.get("ratio").get(i)));
//        }
//        Log.d("Max_ratio", String.valueOf(Collections.max(Cobra_Angle_HashMap.get("ratio"))));
//        Log.d("Min_ratio", String.valueOf(Collections.min(Cobra_Angle_HashMap.get("ratio"))));

//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.worrior_pose);
//        mideaPipePosepredict.detectLiveStream(bitmap);
        // return
    }
    @Override
    public void onResult(PoseLandmarkerResult result, int imageWidth, int imageHeight) {
        Log.d("결과 수신: ", String.valueOf(result));
        Log.d("이미지 가로: ", String.valueOf(imageWidth));
        Log.d("이미지 세로: ", String.valueOf(imageHeight));
        Boolean Calculate_result = false;
        if (result.landmarks().size() > 0){
            landmarks = result.landmarks().get(0);

//        List<NormalizedLandmark> upper_body = result.landmarks().get(0).subList(11,17);
//        List<NormalizedLandmark> lower_body = result.landmarks().get(0).subList(23, 28);

//            for (Integer tmp : mappingindex.keySet()) {
//                Log.d(mappingindex.get(tmp) + String.valueOf(tmp), String.valueOf(result.landmarks().get(0).get(tmp)));
//                //        CheckFor_Back(landmarks);
//            }
            Calculate_result = CheckWarrior(landmarks);
//            Boolean Calculate_result = Check_Cobra(landmarks);
            if (Calculate_result){
                Log.d("Cobra_통과", "각도 비율 통과");
                success +=1;
            }else{
                fail +=1;
                Log.d("Cobras_불통", "불통");
            }
            Log.d("hip_knee_x", String.valueOf(hip_knee));




//            for(String key : AllAngles.keySet()){
//                Log.d(key + " ", String.valueOf(AllAngles.get(key)));
//
//            }
        }else{
            Log.d("검출 실패", "검출 실패");
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

    public Boolean WarriorAllAngleCalculate(List<NormalizedLandmark> landmarks){
        upper_left_shoulder_to_wrist_angle = CalculateAngle(
                landmarks.get(11).x(),
                landmarks.get(11).y(),
                landmarks.get(13).x(),
                landmarks.get(13).y(),
                landmarks.get(15).x(),
                landmarks.get(15).y());


        upper_right_shoulder_to_wrist_angle = CalculateAngle(
                landmarks.get(12).x(),
                landmarks.get(12).y(),
                landmarks.get(14).x(),
                landmarks.get(14).y(),
                landmarks.get(16).x(),
                landmarks.get(16).y());

        lower_left_hip_to_ankle_angle = CalculateAngle(
                landmarks.get(23).x(),
                landmarks.get(23).y(),
                landmarks.get(25).x(),
                landmarks.get(25).y(),
                landmarks.get(27).x(),
                landmarks.get(27).y());

        lower_right_hip_to_ankle_angle = CalculateAngle(
                landmarks.get(24).x(),
                landmarks.get(24).y(),
                landmarks.get(26).x(),
                landmarks.get(26).y(),
                landmarks.get(28).x(),
                landmarks.get(28).y());

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

    private Boolean Warrior_Upper_Lower_Ratio(List<NormalizedLandmark> landmarks){
        //11, 12 -> 어께
        //27, 28 -> 발목
        double upper = Math.abs(landmarks.get(12).x() - landmarks.get(11).x());
        double lower = Math.abs(landmarks.get(28).x() - landmarks.get(27).x());

        Log.d("하체 대비 상체 비율", String.valueOf(upper/lower));
        double upper_lower_ratio = upper/lower;
        if (WarriorCriterion.get("warrior").get("상하체 비율").get("최소") <= upper_lower_ratio
                && upper_lower_ratio <= WarriorCriterion.get("warrior").get("상하체 비율").get("최대")){
            return true;

        }else return false;
    }



    private Boolean CheckWarrior(List<NormalizedLandmark> landmarks){
        boolean AngleResult = WarriorAllAngleCalculate(landmarks);
        boolean RatioResult = Warrior_Upper_Lower_Ratio(landmarks);
        if(AngleResult && RatioResult){
            Log.d("통과", "각도 비율 정상");
            return true;
        } else if (!AngleResult) {
            Log.d("각도: ", "비정상");
            return false;
        }else{
            Log.d("비율: ", "비정상");
            return false;
        }
    }
    private Boolean CheckFor_Back(List<NormalizedLandmark> landmarks){
        // 각도 계산
        // 필요 랜드마크: 23,24,25,26,27,28
        // 무릎 골반 위치 계산
        // 필요 랜드마크: 23,24,25,26
        // 지세 방향 별 랜드마크 필터링 (left or right)

        ArrayList<Integer> LeftPosenum = new ArrayList<>(Arrays.asList(23, 25, 27));
        ArrayList<Integer> RightPosenum = new ArrayList<>(Arrays.asList(24, 26, 28));
        Boolean Selectpoint = getSelectPoint(LeftPosenum, RightPosenum);


        float for_back_angle_result;
        float ratio_result;

        if (Selectpoint){ // 왼쪽 오른쪽 방향 판별

            float left_hip_x = landmarks.get(23).x();
            float left_hip_y = landmarks.get(23).y();
            float left_knee_x = landmarks.get(25).x();
            float left_knee_y = landmarks.get(25).y();
            float left_ankle_x = landmarks.get(27).x();
            float left_ankle_y = landmarks.get(27).y();

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
            float right_hip_x =landmarks.get(24).x();
            float right_hip_y = landmarks.get(24).y();
            float right_knee_x = landmarks.get(26).x();
            float right_knee_y = landmarks.get(26).y();
            float right_ankle_x = landmarks.get(28).x();
            float right_ankle_y = landmarks.get(28).y();
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
    private Boolean Check_Cobra(List<NormalizedLandmark> landmarks){
        //자세 방향 선택
        ArrayList<Integer> LeftPosenum = new ArrayList<>(Arrays.asList(11, 13, 15, 23, 25, 27));
        ArrayList<Integer> RightPosenum = new ArrayList<>(Arrays.asList(12, 14, 16, 24, 26, 28));
        Boolean Selectpoint = getSelectPoint(LeftPosenum, RightPosenum);

        if (Cobra_AllAngleCalculation(landmarks, Selectpoint) && Cobra_hip_ratio(landmarks, Selectpoint)){
            return true;
        }else return false;

    }

    private Boolean Cobra_AllAngleCalculation(List<NormalizedLandmark> landmarks, Boolean Selectpoint){
        float Whole_body_Angle;
        float Arm_Angle;
        float Lower_Angle;
        if (Selectpoint){
            Whole_body_Angle = CalculateAngle(landmarks.get(11).x(), landmarks.get(11).y(),
                    landmarks.get(23).x(), landmarks.get(23).y(),
                    landmarks.get(25).x(), landmarks.get(25).y());
            Arm_Angle = CalculateAngle(landmarks.get(11).x(), landmarks.get(11).y(),
                    landmarks.get(13).x(), landmarks.get(13).y(),
                    landmarks.get(15).x(), landmarks.get(15).y());
            Lower_Angle = CalculateAngle(landmarks.get(23).x(), landmarks.get(23).y(),
                    landmarks.get(25).x(), landmarks.get(25).y(),
                    landmarks.get(27).x(), landmarks.get(27).y());


        }else {
            Whole_body_Angle = CalculateAngle(landmarks.get(12).x(), landmarks.get(12).y(),
                    landmarks.get(24).x(), landmarks.get(24).y(),
                    landmarks.get(26).x(), landmarks.get(26).y());
            Arm_Angle = CalculateAngle(landmarks.get(12).x(), landmarks.get(12).y(),
                    landmarks.get(14).x(), landmarks.get(14).y(),
                    landmarks.get(16).x(), landmarks.get(16).y());
            Lower_Angle = CalculateAngle(landmarks.get(24).x(), landmarks.get(24).y(),
                    landmarks.get(26).x(), landmarks.get(26).y(),
                    landmarks.get(28).x(), landmarks.get(28).y());
        }
        Log.d("Whole_body_Angle", String.valueOf(Whole_body_Angle));
        Log.d("Arm_Angle", String.valueOf(Arm_Angle));
        Log.d("Lower_Angle", String.valueOf(Lower_Angle));

        Cobra_Angle_HashMap.get("Whole_body_Angle").add(Whole_body_Angle);
        Cobra_Angle_HashMap.get("Arm_Angle").add(Arm_Angle);
        Cobra_Angle_HashMap.get("Lower_Angle").add(Lower_Angle);

        if (Cobra_Criterion.get("angle").get("Whole_body_Angle").get("min_range") <= Whole_body_Angle &&
        Whole_body_Angle <= Cobra_Criterion.get("angle").get("Whole_body_Angle").get("max_range")&&
        Cobra_Criterion.get("angle").get("Arm_Angle").get("min_range") <= Arm_Angle &&
        Arm_Angle <= Cobra_Criterion.get("angle").get("Arm_Angle").get("max_range")&&
        Cobra_Criterion.get("angle").get("Lower_Angle").get("min_range") <= Lower_Angle &&
        Lower_Angle <= Cobra_Criterion.get("angle").get("Lower_Angle").get("max_range")){
            Log.d("몸 전체, 팔, 하체 각도 통과", "통과");
            return true;
        }else {
            Log.d("불통", "불통");
            return false;
        }

    }

    private Boolean Cobra_hip_ratio(List<NormalizedLandmark> landmarks, Boolean Selectpoint){
        //hip-knee y값 차이 / 유클리드 거리
        Log.d("Covra_hip_ratio","Covra_hip_ratio");
        float hip_knee_differnce;
        float Eucidean_distance;
        float Cobra_ratio;
        if (Selectpoint){
            hip_knee_differnce = Math.abs(landmarks.get(23).y() - landmarks.get(25).y());
            Eucidean_distance = (float) Math.sqrt(Math.pow(landmarks.get(23).x() - landmarks.get(25).x(),2) +
                    Math.pow(landmarks.get(23).y() - landmarks.get(25).y(),2));
            Cobra_Angle_HashMap.get("ratio").add(hip_knee_differnce/Eucidean_distance);


        }else{
            hip_knee_differnce = Math.abs(landmarks.get(24).y() - landmarks.get(26).y());
            Eucidean_distance = (float) Math.sqrt(Math.pow(landmarks.get(24).x() - landmarks.get(26).x(),2) +
                    Math.pow(landmarks.get(24).y() - landmarks.get(26).y(),2));
            Cobra_Angle_HashMap.get("ratio").add(hip_knee_differnce/Eucidean_distance);
        }
        Cobra_ratio = hip_knee_differnce/Eucidean_distance;

        if(Cobra_Criterion.get("ratio").get("hip_knee_distance_ratio").get("min_range") <= Cobra_ratio &&
        Cobra_ratio <= Cobra_Criterion.get("ratio").get("hip_knee_distance_ratio").get("max_range")){
            Log.d("hip_knee_ratio 통과", String.valueOf(Cobra_ratio));
            return true;
        }else {
            Log.d("hip_knee_ratio 불통", String.valueOf(Cobra_ratio));
            return false;
        }
    }

    private Boolean getSelectPoint(ArrayList<Integer> LeftPosenum, ArrayList<Integer> RightPosenum){
        float Left_Visibility = 0;
        float Right_Visibility = 0;
        Boolean Selectpoint = false;
        for (int l : LeftPosenum){
            Left_Visibility += landmarks.get(l).visibility().get();
        }

        for (int r : RightPosenum){
            Right_Visibility += landmarks.get(r).visibility().get();
        }

        if (Left_Visibility > Right_Visibility){
            Selectpoint = true;
        }
        return Selectpoint;
    }

}
