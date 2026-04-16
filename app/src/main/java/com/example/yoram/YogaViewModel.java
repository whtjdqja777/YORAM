package com.example.yoram;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class YogaViewModel extends ViewModel {
    private final MutableLiveData<String> selectPose = new MutableLiveData<>();

    public LiveData<String> getSelectedPose(){
        return selectPose;
    }
    public void setSelectPose(String pose){
        selectPose.setValue(pose);
    }
}
