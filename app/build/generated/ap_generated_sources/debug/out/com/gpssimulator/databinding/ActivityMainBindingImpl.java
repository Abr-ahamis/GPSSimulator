package com.gpssimulator.databinding;
import com.gpssimulator.R;
import com.gpssimulator.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityMainBindingImpl extends ActivityMainBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.mapView, 1);
        sViewsWithIds.put(R.id.topActions, 2);
        sViewsWithIds.put(R.id.currentLocationButton, 3);
        sViewsWithIds.put(R.id.historyButton, 4);
        sViewsWithIds.put(R.id.settingsButton, 5);
        sViewsWithIds.put(R.id.mapModePanel, 6);
        sViewsWithIds.put(R.id.mapModeTitle, 7);
        sViewsWithIds.put(R.id.mapGuideText, 8);
        sViewsWithIds.put(R.id.clearMapPointsButton, 9);
        sViewsWithIds.put(R.id.mapModeDoneButton, 10);
        sViewsWithIds.put(R.id.controlPanel, 11);
        sViewsWithIds.put(R.id.panelTitle, 12);
        sViewsWithIds.put(R.id.panelSubtitle, 13);
        sViewsWithIds.put(R.id.distanceSection, 14);
        sViewsWithIds.put(R.id.distanceGroup, 15);
        sViewsWithIds.put(R.id.distance5km, 16);
        sViewsWithIds.put(R.id.distance10km, 17);
        sViewsWithIds.put(R.id.distanceCustom, 18);
        sViewsWithIds.put(R.id.distanceNextButton, 19);
        sViewsWithIds.put(R.id.paceSection, 20);
        sViewsWithIds.put(R.id.paceSlider, 21);
        sViewsWithIds.put(R.id.paceText, 22);
        sViewsWithIds.put(R.id.paceHint, 23);
        sViewsWithIds.put(R.id.paceNextButton, 24);
        sViewsWithIds.put(R.id.routeSection, 25);
        sViewsWithIds.put(R.id.routeTypeGroup, 26);
        sViewsWithIds.put(R.id.routeRandom, 27);
        sViewsWithIds.put(R.id.routePins, 28);
        sViewsWithIds.put(R.id.routeDraw, 29);
        sViewsWithIds.put(R.id.routeNextButton, 30);
        sViewsWithIds.put(R.id.summaryPanel, 31);
        sViewsWithIds.put(R.id.summaryGoalValue, 32);
        sViewsWithIds.put(R.id.summaryTargetValue, 33);
        sViewsWithIds.put(R.id.summaryVarianceValue, 34);
        sViewsWithIds.put(R.id.summaryPathValue, 35);
        sViewsWithIds.put(R.id.progressContainer, 36);
        sViewsWithIds.put(R.id.progressBar, 37);
        sViewsWithIds.put(R.id.progressText, 38);
        sViewsWithIds.put(R.id.startButton, 39);
        sViewsWithIds.put(R.id.activeButtonRow, 40);
        sViewsWithIds.put(R.id.pauseButton, 41);
        sViewsWithIds.put(R.id.stopButton, 42);
        sViewsWithIds.put(R.id.pauseButtonLayout, 43);
        sViewsWithIds.put(R.id.continueButton, 44);
        sViewsWithIds.put(R.id.pausedStopButton, 45);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 46, sIncludes, sViewsWithIds));
    }
    private ActivityMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.widget.LinearLayout) bindings[40]
            , (android.widget.Button) bindings[9]
            , (android.widget.Button) bindings[44]
            , (androidx.cardview.widget.CardView) bindings[11]
            , (android.widget.ImageButton) bindings[3]
            , (android.widget.RadioButton) bindings[17]
            , (android.widget.RadioButton) bindings[16]
            , (android.widget.RadioButton) bindings[18]
            , (android.widget.RadioGroup) bindings[15]
            , (android.widget.Button) bindings[19]
            , (android.widget.LinearLayout) bindings[14]
            , (android.widget.ImageButton) bindings[4]
            , (android.widget.TextView) bindings[8]
            , (android.widget.Button) bindings[10]
            , (android.widget.LinearLayout) bindings[6]
            , (android.widget.TextView) bindings[7]
            , (org.osmdroid.views.MapView) bindings[1]
            , (android.widget.TextView) bindings[23]
            , (android.widget.Button) bindings[24]
            , (android.widget.LinearLayout) bindings[20]
            , (com.google.android.material.slider.Slider) bindings[21]
            , (android.widget.TextView) bindings[22]
            , (android.widget.TextView) bindings[13]
            , (android.widget.TextView) bindings[12]
            , (android.widget.Button) bindings[41]
            , (android.widget.LinearLayout) bindings[43]
            , (android.widget.Button) bindings[45]
            , (android.widget.ProgressBar) bindings[37]
            , (android.widget.LinearLayout) bindings[36]
            , (android.widget.TextView) bindings[38]
            , (android.widget.RadioButton) bindings[29]
            , (android.widget.Button) bindings[30]
            , (android.widget.RadioButton) bindings[28]
            , (android.widget.RadioButton) bindings[27]
            , (android.widget.LinearLayout) bindings[25]
            , (android.widget.RadioGroup) bindings[26]
            , (android.widget.ImageButton) bindings[5]
            , (android.widget.Button) bindings[39]
            , (android.widget.Button) bindings[42]
            , (android.widget.TextView) bindings[32]
            , (android.widget.LinearLayout) bindings[31]
            , (android.widget.TextView) bindings[35]
            , (android.widget.TextView) bindings[33]
            , (android.widget.TextView) bindings[34]
            , (android.widget.LinearLayout) bindings[2]
            );
        this.mboundView0 = (androidx.constraintlayout.widget.ConstraintLayout) bindings[0];
        this.mboundView0.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}