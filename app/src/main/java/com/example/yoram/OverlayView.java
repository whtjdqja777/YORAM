package com.example.yoram;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ShareActionProvider;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class OverlayView extends View {

    private Drawable overlayDrawable;
    private String overlayText;
    private Paint textPaint;
    private Handler handler;
    public static int count = 15;
    public String[] yoga_array = {"전사자세", "다리당기기", "코브라자세"};
    private HashMap<String, String> yogamap;
    public ArrayList<Integer> yoga_id_array = new ArrayList<>(Arrays.asList(
            R.drawable.warrior,
            R.drawable.for_back_pose,
            R.drawable.cobra_pose
    ));
    private Integer yoga_image;
    private Integer tmp_yoga_image;
    private YogaViewModel yogaViewModel;
    public int yoga_count = 0;
    private SharedPreferences prefs;
    private Set<String> PoseName;
    private final ArrayList<String> PosNameArray = new ArrayList<>();
    public OverlayView(Context context) {
        super(context);
        init();
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        yogaViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(YogaViewModel.class);

        // 기본 이미지 리소스 ID와 텍스트를 초기화
        prefs = getContext().getSharedPreferences("yoga", MODE_PRIVATE);
        PoseName = prefs.getStringSet("pose", new LinkedHashSet<>());
//        yoga_count = PoseName.size();
        Log.d("PoseName", String.valueOf(PoseName));
        yogamap = new HashMap<>();
        String pose_name;
        ArrayList<Integer> tmp_yoga_id_array = new ArrayList<>(yoga_id_array);
        for(Integer yoga_pose_overlay_id : tmp_yoga_id_array){
            pose_name = getResources().getResourceEntryName(yoga_pose_overlay_id);
            if (PoseName.contains(pose_name)){
                PosNameArray.add(pose_name);
            }else{
                yoga_id_array.remove(yoga_pose_overlay_id);
            }
        }
        Log.d("yoga_id_array", String.valueOf(yoga_id_array));
        updateTextPeriodically();
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
        if (!tmp_yoga_image.equals(yoga_image)){// yoga_count가 증가해서 이미지가 바뀌면 target이미지를 바꾸기 위한 코드
            //

        }
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
            int imageWidth = (int) (getWidth() * 0.8);
            int imageHeight = (int) (getHeight() * 0.8);

            // 이미지를 가운데에 위치시키기 위한 x, y 시작 좌표를 계산
            int x = (getWidth() - imageWidth) / 2;
            int y = (getHeight() - imageHeight) / 2;

            overlayDrawable.setBounds(x, y, x + imageWidth, y + imageHeight);
            overlayDrawable.draw(canvas);
        }


        // 텍스트를 그립니다.
        if (overlayText != null) {
            if (textPaint == null) {
                textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(50); // 텍스트 크기 설정
            }
            if (yoga_count < yoga_array.length) {
                canvas.drawText(overlayText, 60, 50, textPaint); // 텍스트 위치 설정
                canvas.drawText("카메라에 보이시는 동작을 따라하세요.", 30, 110, textPaint);
                canvas.drawText("정확한 동작을 하셔야 카운트가 줄어듭니다.", 30, 170, textPaint);
                canvas.drawText("동작 이름 : " + yoga_array[yoga_count], 30, 230, textPaint);
            }
        }
    }

    public String getCurrenPose(){
        return getResources().getResourceEntryName(yoga_image);
    }
}