package com.nibm.techfix.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.nibm.techfix.R;
import com.nibm.techfix.activities.AccountActivity;
import com.nibm.techfix.activities.ActivitiesActivity;
import com.nibm.techfix.activities.HomeActivity;
import com.nibm.techfix.activities.NotificationsActivity;
import com.nibm.techfix.database.NotificationDao;

/**
 * Wires up the shared bottom_nav.xml include: highlights the active tab,
 * sets click navigation between the 4 top-level screens, and shows the
 * unread notification badge. Call from onCreate/onResume of each of the
 * 4 top-level activities (Home, Activities, Notifications, Account).
 */
public class BottomNavHelper {

    public enum Tab { HOME, ACTIVITIES, NOTIFICATIONS, ACCOUNT }

    public static void setup(Activity activity, int userId, boolean isAdmin, Tab activeTab) {
        int active = ContextCompat.getColor(activity, R.color.primary);
        int inactive = ContextCompat.getColor(activity, R.color.text_secondary);

        TextView homeLabel = activity.findViewById(R.id.navHomeLabel);
        TextView activitiesLabel = activity.findViewById(R.id.navActivitiesLabel);
        TextView notificationsLabel = activity.findViewById(R.id.navNotificationsLabel);
        TextView accountLabel = activity.findViewById(R.id.navAccountLabel);
        android.widget.ImageView homeIcon = activity.findViewById(R.id.navHomeIcon);
        android.widget.ImageView activitiesIcon = activity.findViewById(R.id.navActivitiesIcon);
        android.widget.ImageView notificationsIcon = activity.findViewById(R.id.navNotificationsIcon);
        android.widget.ImageView accountIcon = activity.findViewById(R.id.navAccountIcon);

        homeLabel.setTextColor(activeTab == Tab.HOME ? active : inactive);
        homeIcon.setColorFilter(activeTab == Tab.HOME ? active : inactive);
        activitiesLabel.setTextColor(activeTab == Tab.ACTIVITIES ? active : inactive);
        activitiesIcon.setColorFilter(activeTab == Tab.ACTIVITIES ? active : inactive);
        notificationsLabel.setTextColor(activeTab == Tab.NOTIFICATIONS ? active : inactive);
        notificationsIcon.setColorFilter(activeTab == Tab.NOTIFICATIONS ? active : inactive);
        accountLabel.setTextColor(activeTab == Tab.ACCOUNT ? active : inactive);
        accountIcon.setColorFilter(activeTab == Tab.ACCOUNT ? active : inactive);

        activity.findViewById(R.id.navHome).setOnClickListener(v -> {
            if (activeTab != Tab.HOME) navigate(activity, HomeActivity.class, userId, isAdmin);
        });
        activity.findViewById(R.id.navActivities).setOnClickListener(v -> {
            if (activeTab != Tab.ACTIVITIES) navigate(activity, ActivitiesActivity.class, userId, isAdmin);
        });
        activity.findViewById(R.id.navNotifications).setOnClickListener(v -> {
            if (activeTab != Tab.NOTIFICATIONS) navigate(activity, NotificationsActivity.class, userId, isAdmin);
        });
        activity.findViewById(R.id.navAccount).setOnClickListener(v -> {
            if (activeTab != Tab.ACCOUNT) navigate(activity, AccountActivity.class, userId, isAdmin);
        });

        // Unread notification badge
        TextView badge = activity.findViewById(R.id.navNotificationsBadge);
        int unread = new NotificationDao(activity).getUnreadNotificationCount(userId);
        if (unread > 0) {
            badge.setVisibility(android.view.View.VISIBLE);
            badge.setText(unread > 9 ? "9+" : String.valueOf(unread));
        } else {
            badge.setVisibility(android.view.View.GONE);
        }
    }

    private static void navigate(Activity from, Class<?> target, int userId, boolean isAdmin) {
        Intent intent = new Intent(from, target);
        intent.putExtra("userId", userId);
        intent.putExtra("isAdmin", isAdmin);
        from.startActivity(intent);
        from.finish();
    }
}
