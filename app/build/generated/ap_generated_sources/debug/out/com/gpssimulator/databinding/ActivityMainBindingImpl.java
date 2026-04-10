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
        sViewsWithIds.put(R.id.controlPanel, 2);
        sViewsWithIds.put(R.id.distanceGroup, 3);
        sViewsWithIds.put(R.id.distance5km, 4);
        sViewsWithIds.put(R.id.distance10km, 5);
        sViewsWithIds.put(R.id.distanceCustom, 6);
        sViewsWithIds.put(R.id.paceSlider, 7);
        sViewsWithIds.put(R.id.paceText, 8);
        sViewsWithIds.put(R.id.routeTypeGroup, 9);
        sViewsWithIds.put(R.id.routeRandom, 10);
        sViewsWithIds.put(R.id.routePins, 11);
        sViewsWithIds.put(R.id.routeDraw, 12);
        sViewsWithIds.put(R.id.progressBar, 13);
        sViewsWithIds.put(R.id.progressText, 14);
        sViewsWithIds.put(R.id.startStopButton, 15);
        sViewsWithIds.put(R.id.pauseResumeButton, 16);
        sViewsWithIds.put(R.id.emergencyStopButton, 17);
        sViewsWithIds.put(R.id.currentLocationButton, 18);
        sViewsWithIds.put(R.id.historyButton, 19);
        sViewsWithIds.put(R.id.settingsButton, 20);
    }
    // views
    @NonNull
    private final androidx.constraintlayout.widget.ConstraintLayout mboundView0;
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityMainBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 21, sIncludes, sViewsWithIds));
    }
    private ActivityMainBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (androidx.cardview.widget.CardView) bindings[2]
            , (android.widget.ImageButton) bindings[18]
            , (android.widget.RadioButton) bindings[5]
            , (android.widget.RadioButton) bindings[4]
            , (android.widget.RadioButton) bindings[6]
            , (android.widget.RadioGroup) bindings[3]
            , (android.widget.Button) bindings[17]
            , (android.widget.ImageButton) bindings[19]
            , (org.osmdroid.views.MapView) bindings[1]
            , (com.google.android.material.slider.Slider) bindings[7]
            , (android.widget.TextView) bindings[8]
            , (android.widget.Button) bindings[16]
            , (android.widget.ProgressBar) bindings[13]
            , (android.widget.TextView) bindings[14]
            , (android.widget.RadioButton) bindings[12]
            , (android.widget.RadioButton) bindings[11]
            , (android.widget.RadioButton) bindings[10]
            , (android.widget.RadioGroup) bindings[9]
            , (android.widget.ImageButton) bindings[20]
            , (android.widget.Button) bindings[15]
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