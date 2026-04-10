package com.gpssimulator;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.gpssimulator.databinding.ActivityHistoryBindingImpl;
import com.gpssimulator.databinding.ActivityMainBindingImpl;
import com.gpssimulator.databinding.ActivitySettingsBindingImpl;
import com.gpssimulator.databinding.DialogCustomDistanceBindingImpl;
import com.gpssimulator.databinding.ItemRouteHistoryBindingImpl;
import com.gpssimulator.databinding.NotificationLayoutBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_ACTIVITYHISTORY = 1;

  private static final int LAYOUT_ACTIVITYMAIN = 2;

  private static final int LAYOUT_ACTIVITYSETTINGS = 3;

  private static final int LAYOUT_DIALOGCUSTOMDISTANCE = 4;

  private static final int LAYOUT_ITEMROUTEHISTORY = 5;

  private static final int LAYOUT_NOTIFICATIONLAYOUT = 6;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(6);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.activity_history, LAYOUT_ACTIVITYHISTORY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.activity_main, LAYOUT_ACTIVITYMAIN);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.activity_settings, LAYOUT_ACTIVITYSETTINGS);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.dialog_custom_distance, LAYOUT_DIALOGCUSTOMDISTANCE);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.item_route_history, LAYOUT_ITEMROUTEHISTORY);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.gpssimulator.R.layout.notification_layout, LAYOUT_NOTIFICATIONLAYOUT);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_ACTIVITYHISTORY: {
          if ("layout/activity_history_0".equals(tag)) {
            return new ActivityHistoryBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_history is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYMAIN: {
          if ("layout/activity_main_0".equals(tag)) {
            return new ActivityMainBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_main is invalid. Received: " + tag);
        }
        case  LAYOUT_ACTIVITYSETTINGS: {
          if ("layout/activity_settings_0".equals(tag)) {
            return new ActivitySettingsBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for activity_settings is invalid. Received: " + tag);
        }
        case  LAYOUT_DIALOGCUSTOMDISTANCE: {
          if ("layout/dialog_custom_distance_0".equals(tag)) {
            return new DialogCustomDistanceBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for dialog_custom_distance is invalid. Received: " + tag);
        }
        case  LAYOUT_ITEMROUTEHISTORY: {
          if ("layout/item_route_history_0".equals(tag)) {
            return new ItemRouteHistoryBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for item_route_history is invalid. Received: " + tag);
        }
        case  LAYOUT_NOTIFICATIONLAYOUT: {
          if ("layout/notification_layout_0".equals(tag)) {
            return new NotificationLayoutBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for notification_layout is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(1);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(6);

    static {
      sKeys.put("layout/activity_history_0", com.gpssimulator.R.layout.activity_history);
      sKeys.put("layout/activity_main_0", com.gpssimulator.R.layout.activity_main);
      sKeys.put("layout/activity_settings_0", com.gpssimulator.R.layout.activity_settings);
      sKeys.put("layout/dialog_custom_distance_0", com.gpssimulator.R.layout.dialog_custom_distance);
      sKeys.put("layout/item_route_history_0", com.gpssimulator.R.layout.item_route_history);
      sKeys.put("layout/notification_layout_0", com.gpssimulator.R.layout.notification_layout);
    }
  }
}
